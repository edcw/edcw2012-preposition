using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ototake.Edcw.OpenNlp.Wrapper
{
    using ModelOrg = opennlp.tools.tokenize.TokenizerModel;

    /// <summary>
    /// トークン分割モデル
    /// </summary>
    public sealed class TokenizerModel
    {
        #region プロパティ
        /// <summary>
        /// モデルの本体
        /// </summary>
        public ModelOrg Model { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 指定したモデルファイルからインスタンスを構築．
        /// </summary>
        /// <param name="modelPath"></param>
        public TokenizerModel(string modelPath)
        {
            using (var r = new java.io.FileInputStream(modelPath))
            {
                this.Model = new ModelOrg(r);
            }
        }
        #endregion
    }
}
