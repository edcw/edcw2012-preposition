using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Ototake.Edcw.Data;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Detect
{
    /// <summary>
    /// v_agr誤り検出器
    /// </summary>
    public sealed class VAgrDetector
    {
        #region プロパティ
        public DocumentAnalyzer Analyzer { get; private set; }
        #endregion

        #region コンストラクタ
        public VAgrDetector(DocumentAnalyzer analyzer)
        {
            Analyzer = analyzer;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// v_agrエラーを検出し，タグ付けしたものを返す．
        /// </summary>
        /// <param name="src"></param>
        /// <returns></returns>
        public string DetectErrors(KJCorpusData src)
        {
            var ret = new StringBuilder();
            var edcLines = src.Edc.SplitLine();
            var posLines = src.Pos.SplitLine();
            for (int i = 0; i < edcLines.Length; i++)
            {
                ret.AppendLine(DetectLine(edcLines[i], posLines[i]));
            }
            return ret.ToString();
        }
        #endregion

        #region プライベートメソッド
        private string DetectLine(string edcLine, string posLine)
        {
            var s = Sentence.FromString(Analyzer.Lexicon, posLine, edcLine);
            var tstr = new TagInsertableString(edcLine);

            // VP を抽出する
            var vpArray = s.Chunks.Where(x => x.Tag == "VP").ToArray();

            for (int i = 0; i < vpArray.Length; i++)
            {
                // 主語を見つける
                var subWord = FindSubject(vpArray[i]);

                // 動詞を見つける
                var verb = Common.FindVerb(vpArray[i]);

                // 主語と動詞どちらか片方がない場合はとばす
                if (subWord == null || verb == null)
                    continue;

                // 主語と動詞の対応をチェック
                // 不適切な組み合わせの場合は誤りと判断
                if (!CheckSubjectAndVerb(subWord, verb))
                {
                    // タグはVPをくくる形でつける
                    tstr.TagSpans.Add(new TagSpan(vpArray[i].GetBeginEndPosition(), "v_agr"));
                }
            }

            return tstr;
        }

        /// <summary>
        /// 主語を見つける
        /// </summary>
        /// <param name="vp"></param>
        /// <returns></returns>
        private Word FindSubject(Chunk vp)
        {
            Word ret = null;

            // 前の句
            var prevNp = vp.Prev;

            // 前の句がNPだったらその線で考える
            if (prevNp != null && prevNp.Tag == "NP")
            {
                var prevSubj = FindHead(prevNp);

                // 主名詞が関係代名詞な場合は，もう一つ前を見る
                if (prevSubj.Tag == "WDT")
                    ret = FindHead(prevNp.Prev);
                else
                    ret = prevSubj;
            }

            // その結果，thereが候補として出てきたときは，後ろを見る
            if (ret != null && vp.Next != null && ret.Text.ToLower() == "there")
            {
                // 一つ後ろがNPなら
                if (vp.Next.Tag == "NP")
                    ret = FindHead(vp.Next);
                else
                    ret = FindHead(vp.Next.Next);
            }

            return ret;
        }

        /// <summary>
        /// 主名詞を見つける
        /// </summary>
        /// <param name="np"></param>
        /// <returns></returns>
        private Word FindHead(Chunk np)
        {
            if (np == null) return null;

            // そもそも1個しかない場合はそれ
            if (np.Count == 1) return np[0];

            // 2個以上の時
            // ing形の動名詞を含む場合はそれ
            var ing = np.Where(x => x.Tag == "VBG").LastOrDefault();
            if (ing != default(Word)) return ing;

            // 基本は末尾の単語
            return np.Last();
        }

        /// <summary>
        /// 主語と動詞の対応をチェックする．
        /// </summary>
        /// <param name="subj"></param>
        /// <param name="verb"></param>
        /// <returns></returns>
        private bool CheckSubjectAndVerb(Word subj, Word verb)
        {
            if (subj == null || verb == null) return true;

            P p = GetSubjPerson(subj);

            // SubjのNPにAndを含む場合は複数形
            if (subj.Parent.Any(x => x.Text.ToLower() == "and"))
                p = P.Three | P.Plural;

            // be動詞の場合は特別処理
            if (Common.beVerb.Contains(verb.Text.ToLower()))
                return CheckSubjectAndBeVerb(subj, verb, p);

            // 現在形だった場合
            if (verb.Tag == "VBP" &&
                p == (P.Three | P.Single))
                return false;

            // 3人称単数形だった場合
            if (verb.Tag == "VBZ" &&
                p != (P.Three | P.Single))
                return false;

            return true;
        }

        /// <summary>
        /// 主語とbe動詞の対応をチェックする．
        /// </summary>
        /// <param name="subj"></param>
        /// <param name="verb"></param>
        /// <returns></returns>
        private bool CheckSubjectAndBeVerb(Word subj, Word verb, P p)
        {
            switch (verb.Text.ToLower())
            {
                case "am":
                    return p == (P.One | P.Single);
                case "are":
                case "were":
                    return p.HasFlag(P.Plural);
                case "is":
                    return p == (P.Three | P.Single);
                case "was":
                    return p.HasFlag(P.Single);
            }
            return true;
        }
        #endregion

        #region 動詞ー名詞対応表
        /// <summary>
        /// 主語の人称を取得
        /// </summary>
        /// <param name="subj"></param>
        /// <returns></returns>
        private P GetSubjPerson(Word subj)
        {
            // 単語ベース
            switch (subj.Text.ToLower())
            {
                case "i":
                    return P.One | P.Single;
                case "we":
                    return P.One | P.Plural;
                case "you":
                    return P.Two | P.Single | P.Plural;
                case "he":
                case "she":
                case "it":
                    return P.Three | P.Single;
                case "they":
                    return P.Three | P.Plural;
            }

            // POSベース
            switch (subj.Tag)
            {
                case "NN":
                case "NNP":
                    return P.Three | P.Single;
                case "NNS":
                case "NNPS":
                    return P.Three | P.Plural;
            }

            return P.Three | P.Single;
        }
        #endregion

        #region 人称列挙型
        [Flags]
        public enum P
        {
            Unknown = 0,
            One = 0x01,
            Two = 0x02,
            Three = 0x04,
            Single = 0x08,
            Plural = 0x10
        }
        #endregion
    }
}
