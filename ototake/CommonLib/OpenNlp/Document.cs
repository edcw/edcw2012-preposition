using System.Collections.Generic;
using System.Linq;

namespace Ototake.Edcw.OpenNlp
{
    /// <summary>
    /// 文章を表す．
    /// </summary>
    public class Document : IEnumerable<Sentence>
    {
        #region プロパティ
        /// <summary>
        /// ドキュメントに含まれる文配列
        /// </summary>
        public Sentence[] Sentences { get; private set; }
        #endregion

        #region コンストラクタ
        internal Document(Sentence[] ss)
        {
            Sentences = ss;
        }
        #endregion

        public IEnumerator<Sentence> GetEnumerator()
        {
            return Sentences.AsEnumerable().GetEnumerator();
        }

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return Sentences.GetEnumerator();
        }
    }
}
