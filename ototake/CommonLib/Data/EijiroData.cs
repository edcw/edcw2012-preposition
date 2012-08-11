using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Data
{
    /// <summary>
    /// 英辞郎データ1個分
    /// </summary>
    public class EijiroData
    {
        #region フィールド
        private HashSet<string> postags;
        #endregion

        #region プロパティ
        /// <summary>
        /// 見出し語
        /// </summary>
        public string Word { get; protected set; }

        /// <summary>
        /// 日本語訳，例文等
        /// </summary>
        public string Trans { get; protected set; }

        /// <summary>
        /// Chunking済みテキスト
        /// </summary>
        public string Annotated { get; protected set; }

        /// <summary>
        /// 含まれるPOSタグ
        /// </summary>
        public string PosTags
        {
            get { return string.Join(" ", postags); }
            set { postags = new HashSet<string>(value.Split(default(char[]), StringSplitOptions.RemoveEmptyEntries)); }
        }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// デフォルトコンストラクタでは何も初期化しない
        /// </summary>
        public EijiroData() { }

        /// <summary>
        /// XML形式の英辞郎辞書のrecordエレメントから1つ分のデータを作成
        /// </summary>
        /// <param name="record"></param>
        public EijiroData(XElement record, DocumentAnalyzer analyzer)
        {
            var w = record.Element("word");
            var t = record.Element("trans");

            Word = w.Value;
            if (t != null) Trans = t.Value;

            var doc = analyzer.CreateDocument(Word);
            var s = doc.First();

            Annotated = s.ToString();
            postags = new HashSet<string>(s.Select(x => x.Tag));
        }
        #endregion
    }
}
