using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using Ototake.Edcw.Lexicon;

namespace Ototake.Edcw.OpenNlp
{
    /// <summary>
    /// 文を表す．
    /// </summary>
    public class Sentence : IEnumerable<Word>
    {
        #region プロパティ
        /// <summary>
        /// テキスト本文
        /// </summary>
        public string Text { get; private set; }

        /// <summary>
        /// 文に含まれるチャンク集合
        /// </summary>
        public Chunk[] Chunks { get; private set; }

        /// <summary>
        /// 文に含まれる単語集合
        /// </summary>
        public Word[] Words { get; private set; }

        /// <summary>
        /// インデックス番目の単語を取得
        /// </summary>
        /// <param name="i"></param>
        /// <returns></returns>
        public Word this[int i]
        {
            get { return Words[i]; }
        }
        #endregion

        #region コンストラクタ
        internal Sentence(Chunk[] c, Word[] w, string text)
        {
            Text = text;
            Chunks = c;
            Words = w;

            for (int i = 0; i < c.Length; i++)
            {
                Chunks[i].ParentSentence = this;
            }
        }
        #endregion

        #region メソッド
        /// <summary>
        /// KJCorpusと同じフォーマットでチャンクを文字列表示する．
        /// </summary>
        /// <returns></returns>
        public override string ToString()
        {
            return string.Join(" ", Chunks.Select(x => x.ToString()));
        }

        /// <summary>
        /// KJCorpusの解析フォーマットからインスタンスを生成．
        /// </summary>
        /// <param name="lex">Lexicon</param>
        /// <param name="str">POS付属文字列</param>
        /// <param name="text">原文</param>
        /// <returns></returns>
        public static Sentence FromString(ILexicon lex, string str, string text)
        {
            int i = 0;
            var matches = Regex.Matches(str.ReplaceNestedBracket(), @"(\[(?<chunk>[^\]]+)\]|(?<word>[^\s]+/[^\s]+))");
            var chunks = new List<Chunk>(matches.Count);
            foreach (Match m in matches)
            {
                if (!m.Groups["chunk"].Success && !m.Groups["word"].Success)
                    throw new Exception("正規表現の失敗: " + str);

                bool isChunk = m.Groups["chunk"].Success;
                var chunk = (isChunk) ?
                    Chunk.FromString(lex, m.Groups["chunk"].Value, i, isChunk) :
                    Chunk.FromString(lex, m.Groups["word"].Value, i, isChunk);
                i += chunk.Count;
                chunks.Add(chunk);
            }

            // Chunk連結関係
            for (int j = 0; j < chunks.Count; j++)
            {
                if (j > 0) chunks[j].Prev = chunks[j - 1];
                if (j < chunks.Count - 1) chunks[j].Next = chunks[j + 1];
            }

            return new Sentence(chunks.ToArray(), chunks.SelectMany(x => x).ToArray(), text);
        }

        /// <summary>
        /// 指定した単語の連続が含まれている文中の開始インデックスを取得
        /// </summary>
        /// <param name="words"></param>
        /// <returns></returns>
        public int IndexOf(params Tuple<string, string>[] wordAndPos)
        {
            for (int i = 0; i < Words.Length; i++)
            {
                if (Words[i].Text == wordAndPos[0].Item1 && Words[i].Tag == wordAndPos[0].Item2)
                {
                    bool flag = true;
                    for (int j = 1; j < wordAndPos.Length; j++)
                    {
                        if (i + j >= Words.Length ||
                            (Words[i + j].Text != wordAndPos[j].Item1 && Words[i + j].Tag == wordAndPos[j].Item2))
                        {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) return i;
                }
            }
            return -1;
        }

        /// <summary>
        /// 指定した単語の連続が含まれている文中の開始インデックスを取得
        /// </summary>
        /// <param name="words"></param>
        /// <returns></returns>
        public int IndexOf(params string[] words)
        {
            for (int i = 0; i < Words.Length; i++)
            {
                if (Words[i].Text == words[0])
                {
                    bool flag = true;
                    for (int j = 1; j < words.Length; j++)
                    {
                        if (i + j >= Words.Length ||
                            (Words[i + j].Text != words[j]))
                        {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) return i;
                }
            }
            return -1;
        }
        #endregion

        public IEnumerator<Word> GetEnumerator()
        {
            return Words.AsEnumerable().GetEnumerator();
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return Words.GetEnumerator();
        }
    }
}
