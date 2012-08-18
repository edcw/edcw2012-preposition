using System;
using System.Collections.Generic;
using System.Linq;
using Ototake.Edcw.Lexicon;

namespace Ototake.Edcw.OpenNlp
{
    /// <summary>
    /// チャンク
    /// </summary>
    public sealed class Chunk : IEnumerable<Word>
    {
        #region フィールド
        private Word[] words;
        #endregion

        #region プロパティ
        /// <summary>
        /// チャンクタグ
        /// </summary>
        public string Tag { get; private set; }

        /// <summary>
        /// チャンクに含まれる単語を取得する．
        /// </summary>
        /// <param name="index"></param>
        /// <returns></returns>
        public Word this[int index]
        {
            get { return words[index]; }
        }

        /// <summary>
        /// チャンクに含まれる単語数．
        /// </summary>
        public int Count
        {
            get { return words.Length; }
        }

        /// <summary>
        /// チャンクの先頭の単語を取得．
        /// </summary>
        public Word Head
        {
            get { return words[0]; }
        }

        /// <summary>
        /// チャンクの末尾の単語を取得．
        /// </summary>
        public Word Tail
        {
            get { return words[words.Length - 1]; }
        }

        /// <summary>
        /// 前のチャンク
        /// </summary>
        public Chunk Prev { get; internal set; }

        /// <summary>
        /// 後ろのチャンク
        /// </summary>
        public Chunk Next { get; internal set; }

        /// <summary>
        /// 親となる文
        /// </summary>
        public Sentence ParentSentence { get; internal set; }
        #endregion

        #region コンストラクタ
        internal Chunk(string tag)
        {
            this.Tag = tag;
        }

        internal static Chunk[] CreateChunks(string[] tags, Word[] words)
        {
            if (tags.Length != words.Length)
                throw new ArgumentException("tags と wordsの配列サイズが一致していない．");

            int beginITag = tags[0][0] == 'I' ? 1 : 0;
            var ret = new Chunk[tags.Count(s => s[0] != 'I') + beginITag];
            List<Word> wordList = new List<Word>();
            int j = 0;
            for (int i = 0; i < words.Length; i++)
            {
                // I- タグだった場合は今のチャンクに追加
                if (tags[i][0] == 'I')
                {
                    // しょっぱなからIタグの場合は特別扱い
                    if (i == 0)
                        ret[j] = new Chunk(tags[i].Substring(2));
                    wordList.Add(words[i]);
                }

                // B- タグだった場合は新規チャンク
                else
                {
                    // 古いチャンクを追加しておく
                    if (wordList.Count > 0)
                    {
                        ret[j].words = wordList.ToArray();
                        j++;
                        wordList.Clear();
                    }

                    // 新規チャンク
                    if (tags[i][0] == 'B')
                        ret[j] = new Chunk(tags[i].Substring(2));
                    else
                        ret[j] = new Chunk(tags[i]);
                    wordList.Add(words[i]);
                }
            }

            if (wordList.Count > 0)
            {
                ret[j].words = wordList.ToArray();
                j++;
                wordList.Clear();
            }

            // 連結関係を記述
            for (int i = 0; i < ret.Length; i++)
            {
                if (i > 0) ret[i].Prev = ret[i - 1];
                if (i < ret.Length - 1) ret[i].Next = ret[i + 1];

                for (int k = 0; k < ret[i].Count; k++)
                {
                    ret[i][k].Parent = ret[i];
                    ret[i][k].IndexInChunk = k;
                }
            }

            return ret;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// KJCorpusと同じフォーマットでチャンクを文字列表示する．
        /// </summary>
        /// <returns></returns>
        public override string ToString()
        {
            if (Tag == "O")
                return string.Join(" ", words.Select(x => x.ToString()));
            return string.Format("[{0} {1} ]", Tag, string.Join(" ", words.Select(x => x.ToString())));
        }

        /// <summary>
        /// KJCorpusの解析フォーマットからインスタンスを生成．
        /// </summary>
        /// <param name="str">文字列</param>
        /// <param name="beginIndex">チャンクの単語インデックス開始番号</param>
        /// <param name="isChunk">チャンクなのか，単一単語なのか</param>
        /// <param name="lex">レキシコン</param>
        /// <returns></returns>
        public static Chunk FromString(ILexicon lex, string _str, int beginIndex, bool isChunk)
        {
            Chunk ret;
            string str = _str.RestoreNestedBracket();

            // ./. のような形式
            if (!isChunk)
            {
                ret = new Chunk("O");
                ret.words = new[] { Word.FromString(lex, str, beginIndex) };
            }
            // NP tennis/NN player/NN のような形式
            else
            {
                var tmp = str.Split(default(char[]), StringSplitOptions.RemoveEmptyEntries);
                if (tmp.Length < 2) throw new Exception("不正な形式: " + str);

                ret = new Chunk(tmp[0]);
                ret.words = new Word[tmp.Length - 1];
                for (int i = 0; i < ret.words.Length; i++)
                {
                    ret.words[i] = Word.FromString(lex, tmp[i + 1], beginIndex + i);
                }
            }

            for (int i = 0; i < ret.words.Length; i++)
            {
                ret.words[i].Parent = ret;
                ret.words[i].IndexInChunk = i;
            }

            return ret;
        }

        /// <summary>
        /// ベースSentenceにおけるChunkの開始位置と終了位置を取得する．
        /// </summary>
        /// <returns></returns>
        public Tuple<int, int, CharBasedPosition, CharBasedPosition> GetBeginEndPosition()
        {
            // 開始位置
            var startWord = words.First();
            char c = startWord.Text.First();
            int n = ParentSentence.Take(startWord.IndexInSentence)
                                  .SelectMany(x => x.Text)
                                  .Count(x => x == c);
            var startPosition = new CharBasedPosition { Letter = c, Times = n + 1 };

            // 終了位置
            var endWord = words.Last();
            c = endWord.Text.Last();
            n = ParentSentence.Take(endWord.IndexInSentence + 1)
                              .SelectMany(x => x.Text)
                              .Count(x => x == c);
            var endPosition = new CharBasedPosition { Letter = c, Times = n };

            // 真の位置
            int startIndex = startPosition.GetIndex(ParentSentence.Text);
            int endIndex = endPosition.GetIndex(ParentSentence.Text) + 1;

            return Tuple.Create(startIndex, endIndex, startPosition, endPosition);
        }
        #endregion

        #region IEnumerable

        public IEnumerator<Word> GetEnumerator()
        {
            return words.AsEnumerable().GetEnumerator();
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return words.GetEnumerator();
        }

        #endregion
    }
}
