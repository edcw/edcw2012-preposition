
namespace Ototake.Edcw.OpenNlp.Wrapper
{
    using SentenceModelOrg = opennlp.tools.sentdetect.SentenceModel;

    /// <summary>
    /// 文分割器のモデル
    /// </summary>
    public sealed class SentenceModel
    {
        #region プロパティ
        /// <summary>
        /// モデルの本体
        /// </summary>
        public SentenceModelOrg Model { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 指定したモデルファイルからインスタンスを構築．
        /// </summary>
        /// <param name="modelPath"></param>
        public SentenceModel(string modelPath)
        {
            using (var r = new java.io.FileInputStream(modelPath))
            {
                this.Model = new SentenceModelOrg(r);
            }
        }
        #endregion
    }
}
