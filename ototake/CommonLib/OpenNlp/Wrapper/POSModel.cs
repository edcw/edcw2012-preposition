using ModelOrg = opennlp.tools.postag.POSModel;
namespace Ototake.Edcw.OpenNlp.Wrapper
{
    /// <summary>
    /// POSタグ付けモデル
    /// </summary>
    public sealed class POSModel
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
        public POSModel(string modelPath)
        {
            using (var r = new java.io.BufferedInputStream(new java.io.FileInputStream(modelPath)))
            {
                this.Model = new ModelOrg(r);
            }
        }
        #endregion
    }
}
