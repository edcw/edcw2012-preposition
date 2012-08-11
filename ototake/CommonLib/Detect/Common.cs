using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Detect
{
    /// <summary>
    /// 誤り検出で共通に使うもの
    /// </summary>
    static class Common
    {
        /// <summary>
        /// be動詞セット
        /// </summary>
        internal static HashSet<string> beVerb = new HashSet<string>
        {
            "am", "are", "is", "was", "were", "'m", "'s", "'re", "been"
        };

        /// <summary>
        /// 人称変化をする動詞部分を見つける
        /// </summary>
        /// <param name="vp"></param>
        /// <returns></returns>
        internal static Word FindVerb(Chunk vp)
        {
            // 先頭から見ていって，VBの部分を見つける
            foreach (var w in vp)
            {
                if (w.IsVerb())
                    return w;
            }
            return null;
        }

        /// <summary>
        /// 動詞句のメインとなる動詞を見つける
        /// </summary>
        /// <param name="vp"></param>
        /// <param name="isPassive">受動態かどうか</param>
        /// <returns></returns>
        internal static Word FindMainVerb(Chunk vp, out bool isPassive)
        {
            Word ret = null;
            isPassive = false;

            // 後方から見ていく
            foreach (var w in vp.Reverse())
            {
                if (w.IsVerb())
                {
                    if (ret == null) ret = w;
                    else if (w.IsBeVerb() && w.Tag == "VBN")
                        isPassive = true;
                }
            }

            return ret;
        }

        /// <summary>
        /// 指定した動詞のあとにTO不定詞が続いてるかどうか
        /// </summary>
        /// <param name="verb"></param>
        /// <returns></returns>
        internal static bool IsInfForm(Word verb)
        {
            var ch = verb.Parent;

            // チャンクがない場合は不定詞じゃない
            if (ch.Tag == "O") return false;

            // 動詞より後ろにTOがある
            var to = ch.LastOrDefault(x => x.Tag == "TO");
            if (to == null) return false;
            if (to.IndexInSentence < verb.IndexInSentence) return false;

            // TOの後に動詞が続く
            if (to.NextInChunk != null && to.NextInChunk.IsVerb())
                return true;

            return false;
        }

        internal static bool HasBy(this Word verb)
        {
            return verb.NextInSentence != null &&
                verb.NextInSentence.Text.ToLower() == "by";
        }

        internal static bool IsBeVerb(this Word w)
        {
            return w.IsVerb() && beVerb.Contains(w.Text.ToLower());
        }

        internal static bool IsVerb(this Word w)
        {
            if (w.Tag.Length < 2) return false;
            return w.Tag.Substring(0, 2) == "VB";
        }
    }
}
