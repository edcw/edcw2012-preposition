using Org = opennlp.tools.chunker.ChunkerME;
namespace Ototake.Edcw.OpenNlp.Wrapper
{
    /// <summary>
    /// OpenNLPのChunker実装
    /// </summary>
    public sealed class Chunker
    {
        #region プロパティ
        /// <summary>
        /// Chunker本体
        /// </summary>
        public Org ChunkerOrg { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 新しくChunkerを作成．
        /// </summary>
        /// <param name="model"></param>
        public Chunker(ChunkerModel model)
        {
            ChunkerOrg = new Org(model.Model);
        }
        #endregion

        #region メソッド
        /// <summary>
        /// Chunkする．
        /// </summary>
        /// <param name="sent">文</param>
        /// <param name="pos">POSタグ</param>
        /// <returns></returns>
        public string[] Chunk(string[] sent, string[] pos)
        {
            return ChunkerOrg.chunk(sent, pos);
        }

        /// <summary>
        /// Chunkする．
        /// </summary>
        /// <param name="sent">文</param>
        /// <param name="pos">POSタグ</param>
        /// <returns></returns>
        public opennlp.tools.util.Span[] ChunkAsSpans(string[] sent, string[] pos)
        {
            return ChunkerOrg.chunkAsSpans(sent, pos);
        }
        #endregion
    }
}
