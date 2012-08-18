using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using Ototake.Edcw.Data;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Detect
{
    /// <summary>
    /// ME版prp誤り検出器
    /// </summary>
    public sealed class PrpDetectorME
    {
        #region 静的変数
        private static HashSet<string> targetPrps = new HashSet<string>
        {
            "of", 
            "in",
            "on", 
            "at",
            "for",
            "by",
            "to",
            "from",
            //"with",
            "about"
        };

        public static IEnumerable<string> TargetPrps
        {
            get { return targetPrps; }
        }
        #endregion

        #region プロパティ
        /// <summary>
        /// 解析器
        /// </summary>
        public DocumentAnalyzer Analyzer { get; set; }

        /// <summary>
        /// 名前解析器
        /// </summary>
        public NameAnalyzer NAnalyzer { get; set; }

        /// <summary>
        /// 分類器ユーティリティ
        /// </summary>
        public ClassiasTagUtil Cls { get; set; }

        /// <summary>
        /// 分類モデルがあるディレクトリ
        /// </summary>
        public string ModelDir { get; set; }
        #endregion

        #region メソッド
        /// <summary>
        /// prpエラーを検出し，タグ付けしたものを返す．
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

            // 前置詞を抜き出してみる
            var ppArray = s.Chunks.Where(x => x.Tag == "PP").ToArray();

            Array.ForEach(ppArray, (x) => DetectBasedOnPp(x, tstr));

            return tstr;
        }

        /// <summary>
        /// PPの前置詞が正しいかどうかを検証する
        /// </summary>
        /// <param name="pp"></param>
        /// <param name="tstr"></param>
        private void DetectBasedOnPp(Chunk pp, TagInsertableString tstr)
        {
            // 前置詞
            var prp = pp.LastOrDefault(x => x.Tag == "IN" || x.Tag == "TO");
            if (prp == null) return;

            string prpStr = prp.Text.ToLower();

            // 前置詞が対象外ならやめる
            if (!targetPrps.Contains(prpStr))
                return;

            // 使われている前置詞の確率を計算する
            var classified = Cls.ClassifyAndProb(new PrpInstance().Build(pp, NAnalyzer),
                prpStr, Path.Combine(ModelDir, prpStr + ".train.model"));

            // 確率20%未満で誤りと判断
            if (classified != null && classified.Item2 < 0.1)
            {
                tstr.TagSpans.Add(new TagSpan(prp.Parent.GetBeginEndPosition(), "prp"));
                Console.Error.WriteLine("s: {0}", pp.ParentSentence.ToString());
            }
        }
        #endregion
    }
}
