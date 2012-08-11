using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Xml;
using System.Xml.Linq;
using Sgml;

namespace Ototake.Edcw.Data
{
    /// <summary>
    /// KJ Corpus データ1個分
    /// </summary>
    public class KJCorpusData
    {
        #region フィールド
        private HashSet<string> errorTags;
        #endregion

        #region プロパティ
        /// <summary>
        /// ファイル名
        /// </summary>
        public string Name { get; protected set; }

        /// <summary>
        /// オリジナル文章
        /// </summary>
        public string Org { get; protected set; }

        /// <summary>
        /// エラータグ付き文章
        /// </summary>
        public string Annotated { get; protected set; }

        /// <summary>
        /// EDCW用オリジナル文章
        /// </summary>
        public string Edc { get; protected set; }

        /// <summary>
        /// 機械的に付与されたChunk・Pos情報
        /// </summary>
        public string Pos { get; protected set; }

        /// <summary>
        /// 人手で付与されたChunk・Pos情報
        /// </summary>
        public string Mos { get; protected set; }

        /// <summary>
        /// 文書に含まれるエラータグ
        /// </summary>
        public string ErrorTags
        {
            get { return string.Join(" ", errorTags); }
            set { errorTags = new HashSet<string>(value.Split(default(char[]), StringSplitOptions.RemoveEmptyEntries)); }
        }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// デフォルトコンストラクタでは何も初期化しない
        /// </summary>
        public KJCorpusData() { }

        /// <summary>
        /// データのファイル名ベース（拡張子抜き）からインスタンス生成
        /// </summary>
        /// <param name="fileNameBase"></param>
        /// <param name="isFormal">フォーマルテスト用だとedcとposのみ</param>
        public KJCorpusData(string fileNameBase, bool isFormal = false)
        {
            Name = Path.GetFileName(fileNameBase);
            Edc = File.ReadAllText(fileNameBase + ".edc");
            Pos = File.ReadAllText(fileNameBase + ".pos");

            if (!isFormal)
            {
                Org = File.ReadAllText(fileNameBase + ".org");
                Annotated = File.ReadAllText(fileNameBase + ".txt");
                Mos = File.ReadAllText(fileNameBase + ".mos");

                errorTags = new HashSet<string>();
                SetTag();
            }
        }
        #endregion

        #region メソッド
        protected void SetTag()
        {
            string str = "<html>" + Annotated + "</html>";
            var doc = ParseHtml(new StringReader(str)).Root;

            foreach (var node in doc.Nodes())
            {
                if (node.NodeType == XmlNodeType.Element)
                {
                    var child = node as XElement;
                    errorTags.Add(child.Name.LocalName);
                }
            }
        }

        private static XDocument ParseHtml(TextReader reader)
        {
            using (var sgmlReader = new SgmlReader() { DocType = "HTML", WhitespaceHandling = WhitespaceHandling.All })
            {
                sgmlReader.InputStream = reader;
                return XDocument.Load(sgmlReader);
            }
        }
        #endregion
    }
}
