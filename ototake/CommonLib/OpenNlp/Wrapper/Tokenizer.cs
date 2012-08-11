using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ototake.Edcw.OpenNlp.Wrapper
{
    using Org = opennlp.tools.tokenize.TokenizerME;

    /// <summary>
    /// OpenNLPのトークン分割器ME版
    /// </summary>
    public sealed class Tokenizer
    {
        #region プロパティ
        /// <summary>
        /// トークン分割器本体
        /// </summary>
        public Org TokenizerOrg { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 新しくトークン分割器を作成．
        /// </summary>
        /// <param name="model"></param>
        public Tokenizer(TokenizerModel model)
        {
            TokenizerOrg = new Org(model.Model);
        }
        #endregion

        #region メソッド
        /// <summary>
        /// トークン分割を行う．
        /// </summary>
        /// <param name="s">文</param>
        /// <returns></returns>
        public string[] Tokenize(string s)
        {
            return TokenizerOrg.tokenize(s);
        }

        /// <summary>
        /// トークン分割を行う．
        /// </summary>
        /// <param name="s"></param>
        /// <returns></returns>
        public opennlp.tools.util.Span[] TokenizeSpans(string s)
        {
            return TokenizerOrg.tokenizePos(s);
        }
        #endregion
    }
}
