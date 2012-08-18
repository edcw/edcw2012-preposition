using Org = opennlp.tools.postag.POSTaggerME;
namespace Ototake.Edcw.OpenNlp.Wrapper
{
    /// <summary>
    /// OpenNLPのPOSタグ付け器ME版
    /// </summary>
    public sealed class POSTagger
    {
        #region プロパティ
        /// <summary>
        /// POSタグ付け器本体
        /// </summary>
        public Org TaggerOrg { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 新しくPOSタグ付け器を作成．
        /// </summary>
        /// <param name="model"></param>
        public POSTagger(POSModel model)
        {
            TaggerOrg = new Org(model.Model);
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 指定した文にタグをつける．
        /// </summary>
        /// <param name="sent"></param>
        /// <returns></returns>
        public string[] Tag(string[] sent)
        {
            return TaggerOrg.tag(sent);
        }
        #endregion
    }
}
