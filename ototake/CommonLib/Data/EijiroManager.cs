using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Data
{
    /// <summary>
    /// 英辞郎辞書管理クラス
    /// </summary>
    public sealed class EijiroManager
    {
        #region プロパティ
        /// <summary>
        /// 英辞郎辞書XMLファイル
        /// </summary>
        public string Xml { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 英辞郎辞書XMLのパスを指定
        /// </summary>
        /// <param name="xmlPath"></param>
        public EijiroManager(string xmlPath)
        {
            this.Xml = xmlPath;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 英辞郎データを列挙する．
        /// </summary>
        /// <param name="analyzer"></param>
        /// <returns></returns>
        public IEnumerable<EijiroData> EnumerateEijiroData(DocumentAnalyzer analyzer)
        {
            using (var r = File.OpenRead(Xml))
            {
                var doc = XDocument.Load(r);
                return doc.Root.Descendants("record")
                               .Select(x => new EijiroData(x, analyzer));
            }
        }
        #endregion
    }
}
