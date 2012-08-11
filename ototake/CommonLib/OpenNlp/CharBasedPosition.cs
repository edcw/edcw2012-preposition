
namespace Ototake.Edcw.OpenNlp
{
    /// <summary>
    /// 「どの文字が何回目に出現した場所か」をベースにした文字列位置情報
    /// </summary>
    public struct CharBasedPosition
    {
        /// <summary>
        /// 文字
        /// </summary>
        public char Letter { get; set; }

        /// <summary>
        /// 何回目の出現？（1以上）
        /// </summary>
        public int Times { get; set; }

        /// <summary>
        /// このインスタンスが表わす位置情報を，指定した文字列の実際のindexに変換する．
        /// </summary>
        /// <param name="str"></param>
        /// <returns></returns>
        public int GetIndex(string str)
        {
            int ret = -1;
            for (int i = 0; i < Times; i++)
            {
                ret = str.IndexOf(Letter, ret + 1);
            }
            return ret;
        }
    }
}
