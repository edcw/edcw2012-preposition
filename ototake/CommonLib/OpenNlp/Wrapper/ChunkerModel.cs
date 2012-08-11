using ModelOrg = opennlp.tools.chunker.ChunkerModel;
namespace Ototake.Edcw.OpenNlp.Wrapper
{
    /// <summary>
    /// Chunkerモデル
    /// </summary>
    public sealed class ChunkerModel
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
        public ChunkerModel(string modelPath)
        {
            using (var r = new java.io.FileInputStream(modelPath))
            {
                this.Model = new ModelOrg(r);
            }
        }
        #endregion
    }
}
