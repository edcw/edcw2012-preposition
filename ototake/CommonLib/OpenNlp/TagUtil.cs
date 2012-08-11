using Ototake.Edcw.Lexicon;

namespace Ototake.Edcw.OpenNlp
{
    /// <summary>
    /// OSタグに関するユーティリティクラス
    /// </summary>
    public static class TagUtil
    {
        /// <summary>
        /// 文字列をPOSタグとして，対応するSynsetTypeを取得する．
        /// </summary>
        /// <param name="pos"></param>
        /// <returns></returns>
        public static SynsetType? GetSynsetType(this string pos)
        {
            switch (pos)
            {
                case "JJ":
                case "JJR":
                case "JJS":
                    return SynsetType.Adjective;
                case "NN":
                case "NNP":
                case "NNPS":
                case "NNS":
                    return SynsetType.Noun;
                case "RB":
                case "RBR":
                case "RBS":
                    return SynsetType.Adverb;
                case "VB":
                case "VBD":
                case "VBG":
                case "VBN":
                case "VBP":
                case "VBZ":
                    return SynsetType.Verb;
                default:
                    return null;
            }
        }
    }
}
