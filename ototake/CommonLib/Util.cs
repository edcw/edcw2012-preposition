using System.Collections.Generic;
using System.IO;
using System.Text;

namespace Ototake.Edcw
{
    /// <summary>
    /// ユーティリティクラス
    /// </summary>
    public static class Util
    {
        /// <summary>
        /// フォルダのサイズを取得する
        /// </summary>
        /// <param name="dirInfo">サイズを取得するフォルダ</param>
        /// <returns>フォルダのサイズ（バイト）</returns>
        public static long GetDirectorySize(this DirectoryInfo dirInfo)
        {
            long size = 0;

            //フォルダ内の全ファイルの合計サイズを計算する
            foreach (FileInfo fi in dirInfo.GetFiles())
                size += fi.Length;

            //サブフォルダのサイズを合計していく
            foreach (DirectoryInfo di in dirInfo.GetDirectories())
                size += di.GetDirectorySize();

            //結果を返す
            return size;
        }

        /// <summary>
        /// 指定した文字列を改行区切りする．
        /// 空行は除く．
        /// </summary>
        /// <param name="str"></param>
        /// <returns></returns>
        public static string[] SplitLine(this string str)
        {
            var ret = new List<string>();
            var r = new StringReader(str);
            for (var l = r.ReadLine(); l != null; l = r.ReadLine())
            {
                if (!string.IsNullOrWhiteSpace(l))
                    ret.Add(l);
            }
            return ret.ToArray();
        }

        /// <summary>
        /// ネストした[]を別文字に置き換える
        /// </summary>
        /// <param name="str"></param>
        /// <returns></returns>
        public static string ReplaceNestedBracket(this string str)
        {
            var ret = new StringBuilder(str);
            int level = 0;
            for (int i = 0; i < str.Length; i++)
            {
                // [ が出現
                if (str[i] == '[')
                {
                    level++;

                    // ネストしてるなら全角に置き換える
                    if (level > 1)
                        ret[i] = '【';
                }
                // ] が出現
                else if (str[i] == ']')
                {
                    level--;

                    // ネストしてたなら全角に置き換える
                    if (level > 0)
                        ret[i] = '】';
                }
            }
            return ret.ToString();
        }

        /// <summary>
        /// ReplaceNestedBracketメソッドで置き換えた[]を元に戻す
        /// </summary>
        /// <param name="str"></param>
        /// <returns></returns>
        public static string RestoreNestedBracket(this string str)
        {
            return str.Replace('【', '[').Replace('】', ']');
        }
    }
}
