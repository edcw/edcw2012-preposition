
namespace Ototake.Edcw.OpenNlp.Wrapper
{
    using Org = opennlp.tools.namefind.NameFinderME;

    /// <summary>
    /// OpenNLPの固有表現識別器実装ME版
    /// </summary>
    public sealed class NameFinder
    {
        #region プロパティ
        /// <summary>
        /// 固有表現識別器本体
        /// </summary>
        public Org NameFinderOrg { get; private set; }
        #endregion

        #region コンストラクタ
        public NameFinder(NameFinderModel model)
        {
            NameFinderOrg = new Org(model.Model);
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 前回コール時の適応データを消去する．
        /// </summary>
        public void ClearAdaptiveData()
        {
            this.NameFinderOrg.clearAdaptiveData();
        }

        /// <summary>
        /// 名前タグを付与したSpanオブジェクトを返す．
        /// </summary>
        /// <param name="tokens"></param>
        /// <returns></returns>
        public opennlp.tools.util.Span[] Find(string[] tokens)
        {
            return this.NameFinderOrg.find(tokens);
        }
        #endregion
    }
}
