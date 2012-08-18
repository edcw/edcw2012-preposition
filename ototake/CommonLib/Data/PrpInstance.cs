using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Ototake.Edcw.Detect;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Data
{
    /// <summary>
    /// 機械学習用の前置詞インスタンス．
    /// </summary>
    public class PrpInstance
    {
        private static HashSet<string> defaultPrps = new HashSet<string>(PrpDetectorME.TargetPrps);

        #region プロパティ
        /// <summary>
        /// 前置詞
        /// </summary>
        public string Preposition { get; set; }

        /// <summary>
        /// 素性セット
        /// </summary>
        public Dictionary<string, string> Features { get; protected set; }

        /// <summary>
        /// このインスタンスはVPから作られた？
        /// </summary>
        public bool IsBuildFromVp { get; private set; }
        #endregion

        #region コンストラクタ
        public PrpInstance()
        {
            Features = new Dictionary<string, string>();
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 句からインスタンスを構築する．
        /// </summary>
        /// <param name="pp"></param>
        /// <returns></returns>
        public PrpInstance BuildForAny(Chunk ch, NameAnalyzer na)
        {
            if (ch.Tag == "PP") return BuildForPp(ch, na);
            if (ch.Tag == "VP") return BuildForVp(ch, na);
            return null;
        }

        /// <summary>
        /// 前置詞句からインスタンスを構築する．
        /// </summary>
        /// <param name="pp"></param>
        /// <param name="na"></param>
        /// <returns></returns>
        public PrpInstance BuildForPp(Chunk pp, NameAnalyzer na, bool isBuildForVp = false)
        {
            IsBuildFromVp = isBuildForVp;
            return Build(pp, na);
        }

        /// <summary>
        /// 動詞句からインスタンスを構築する．
        /// </summary>
        /// <param name="vp"></param>
        /// <param name="na"></param>
        /// <returns></returns>
        public PrpInstance BuildForVp(Chunk vp, NameAnalyzer na)
        {
            // vpの後ろ隣りがppの場合はそれに準じる
            if (vp.Next != null && vp.Next.Tag == "PP")
                return BuildForPp(vp.Next, na, true);

            IsBuildFromVp = true;
            bool dummy;

            // 前置詞
            Preposition = string.Empty;

            // VP
            var verb = Common.FindMainVerb(vp, out dummy);
            if (verb != null)
            {
                PosModified = "verb";
                ItemModified = verb.BaseForm.ToLower();
                if (verb.Lemma != null)
                    CatModified = verb.Lemma.Senses.First().Synset.LexCategory.ToString();
            }
            else
            {
                PosModified = vp.Tail.Tag;
                ItemModified = vp.Tail.BaseForm.ToLower();
            }

            // vpの後ろ隣りを見る
            if (vp.Next != null)
            {
                // 後方はNP
                if (vp.Next.Tag == "NP")
                {
                    var noun = vp.Next.Tail;
                    PosObject = "noun";
                    ItemObject = noun.BaseForm;

                    var entities = na.Find(noun);
                    if (entities.Length > 0)
                        NamedEntityObject = entities.First();
                }
            }

            // 動詞まわり
            if (verb != null)
            {
                int idx = verb.IndexInSentence;
                var s = vp.ParentSentence;
                for (int i = 1; i <= 3; i++)
                {
                    if (idx + i < s.Words.Length)
                        SetPos3Range(s[idx + i].Tag, string.Format("p{0}", i));
                    if (idx - i > 0)
                        SetPos3Range(s[idx - i].Tag, string.Format("m{0}", i));
                }
            }

            return this;
        }

        /// <summary>
        /// 前置詞句からインスタンスを構築する．
        /// </summary>
        /// <param name="pp"></param>
        /// <returns></returns>
        public PrpInstance Build(Chunk pp, NameAnalyzer na)
        {
            bool dummy;

            // 前置詞
            var prp = pp.LastOrDefault(x => x.Tag == "IN" || x.Tag == "TO");
            if (prp == null) return null;
            Preposition = prp.Text.ToLower();

            // 前方を見る
            if (pp.Prev != null)
            {
                // 前方はVP？
                if (pp.Prev.Tag == "VP")
                {
                    var verb = Common.FindMainVerb(pp.Prev, out dummy);
                    if (verb != null)
                    {
                        PosModified = "verb";
                        ItemModified = verb.BaseForm.ToLower();
                        if (verb.Lemma != null)
                            CatModified = verb.Lemma.Senses.First().Synset.LexCategory.ToString();
                    }
                    else
                    {
                        PosModified = pp.Prev.Tail.Tag;
                        ItemModified = pp.Prev.Tail.BaseForm.ToLower();
                    }
                }
                else if (pp.Prev.Tag == "NP")
                {
                    var noun = pp.Prev.Tail;
                    PosModified = "noun";
                    ItemModified = noun.BaseForm;
                    if (noun.Lemma != null)
                        CatModified = noun.Lemma.Senses.First().Synset.LexCategory.ToString();
                }
            }

            // 後方を見る
            if (pp.Next != null)
            {
                // 後方はNP
                if (pp.Next.Tag == "NP")
                {
                    var noun = pp.Next.Tail;
                    PosObject = "noun";
                    ItemObject = noun.BaseForm;

                    var entities = na.Find(noun);
                    if (entities.Length > 0)
                        NamedEntityObject = entities.First();
                }
            }

            // まわり
            int idx = prp.IndexInSentence;
            var s = pp.ParentSentence;
            for (int i = 1; i <= 3; i++)
            {
                if (idx + i < s.Words.Length)
                    SetPos3Range(s[idx + i].Tag, string.Format("p{0}", i));
                if (idx - i > 0)
                    SetPos3Range(s[idx - i].Tag, string.Format("m{0}", i));
            }

            return this;
        }

        /// <summary>
        /// Classias用のインスタンス1行分に変換する
        /// </summary>
        /// <returns></returns>
        public string ClassiasLine(string targetPrp)
        {
            return string.Format("{0} {1}",
                (Preposition == targetPrp) ? "+1" : "-1",
                string.Join(" ", Features.Select(x => string.Format("{0}_{1}", x.Key, x.Value.RepCl()))));
        }

        /// <summary>
        /// 前置詞の有無用Classiasインスタンス1行分に変換する
        /// </summary>
        /// <returns></returns>
        public string ClassiasLineForYesNo()
        {
            return string.Format("{0} {1}",
                (string.IsNullOrEmpty(Preposition)) ? "-1" : "+1",
                string.Join(" ", Features.Select(x => string.Format("{0}_{1}", x.Key, x.Value.RepCl()))));
        }

        /// <summary>
        /// 前置詞の多値分類用インスタンス１行分に変換する．
        /// 対象前置詞ではない場合はnull
        /// </summary>
        /// <returns></returns>
        public string ClassiasLineForMulti()
        {
            if (!defaultPrps.Contains(Preposition)) return null;
            return string.Format("{0} {1}",
                Preposition,
                string.Join(" ", Features.Select(x => string.Format("{0}_{1}", x.Key, x.Value.RepCl()))));
        }
        #endregion

        #region 素性
        public string PosModified
        {
            set { Features["pm"] = value; }
            get { return Features.ContainsKey("pm") ? Features["pm"] : null; }
        }

        public string ItemModified
        {
            set { Features["im"] = value; }
            get { return Features.ContainsKey("im") ? Features["im"] : null; }
        }

        public string CatModified
        {
            set { Features["cm"] = value; }
            get { return Features.ContainsKey("cm") ? Features["cm"] : null; }
        }

        public string PosObject
        {
            set { Features["po"] = value; }
            get { return Features.ContainsKey("po") ? Features["po"] : null; }
        }

        public string ItemObject
        {
            set { Features["io"] = value; }
            get { return Features.ContainsKey("io") ? Features["io"] : null; }
        }

        public string NamedEntityObject
        {
            set { Features["neo"] = value; }
            get { return Features.ContainsKey("neo") ? Features["neo"] : null; }
        }

        public void SetPos3Range(string pos, string position)
        {
            Features["pos_" + position] = pos;
        }
        #endregion
    }

    #region 専用拡張メソッド
    static class PrpInstanceExtension
    {
        /// <summary>
        /// Instance用コロンの書き換え
        /// </summary>
        /// <param name="str"></param>
        /// <returns></returns>
        public static string RepCl(this string str)
        {
            return str.Replace(":", "__colon__");
        }
    }
    #endregion
}
