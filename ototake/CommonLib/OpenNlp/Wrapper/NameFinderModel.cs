
namespace Ototake.Edcw.OpenNlp.Wrapper
{
    using ModelOrg = opennlp.tools.namefind.TokenNameFinderModel;

    /// <summary>
    /// トークンの固有表現識別器モデル
    /// </summary>
    public sealed class NameFinderModel
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
        public NameFinderModel(string modelPath)
        {
            using (var r = new java.io.FileInputStream(modelPath))
            {
                this.Model = new ModelOrg(r);
            }
        }
        #endregion
    }
}
