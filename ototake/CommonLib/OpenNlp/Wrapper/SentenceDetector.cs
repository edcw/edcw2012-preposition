using Detector = opennlp.tools.sentdetect.SentenceDetectorME;
namespace Ototake.Edcw.OpenNlp.Wrapper
{
    /// <summary>
    /// OpenNLPの文分割器実装ME版
    /// </summary>
    public sealed class SentenceDetector
    {
        #region プロパティ
        /// <summary>
        /// 文分割器本体
        /// </summary>
        public Detector DetectorOrg { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 新しく文分割器を作成．
        /// </summary>
        /// <param name="model"></param>
        public SentenceDetector(SentenceModel model)
        {
            DetectorOrg = new Detector(model.Model);
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 引数で指定した文章を文に分割する．
        /// </summary>
        /// <param name="s">文章</param>
        /// <returns></returns>
        public string[] Detect(string s)
        {
            return DetectorOrg.sentDetect(s);
        }
        #endregion
    }
}
