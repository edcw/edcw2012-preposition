using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Data
{
    /// <summary>
    /// トレーニング用の1文書を表すクラス．
    /// 文，チャンキング，含まれるPOSタグ等の情報を保持する．
    /// </summary>
    public class TrainingData
    {
        #region プロパティ
        /// <summary>
        /// 文
        /// </summary>
        public string Text { get; protected set; }
        #endregion

        #region コンストラクタ
        public TrainingData() { }

        /// <summary>
        /// 本文を指定してインスタンスを生成
        /// </summary>
        /// <param name="text"></param>
        /// <param name="analyzer"></param>
        public TrainingData(string text)
        {
            this.Text = text;
        }
        #endregion

        #region 機械学習インスタンス生成
        /// <summary>
        /// 機械学習用インスタンスを作成
        /// </summary>
        /// <param name="an"></param>
        /// <param name="na"></param>
        /// <returns></returns>
        public IEnumerable<PrpInstance> CreatePrpInstances(DocumentAnalyzer an, NameAnalyzer na)
        {
            var doc = an.CreateDocument(Text);
            return doc.Sentences.SelectMany(x => x.Chunks)
                                .Where(x => x.Tag == "PP")
                                .Select(x => new PrpInstance().Build(x, na));
        }
        #endregion
    }
}
