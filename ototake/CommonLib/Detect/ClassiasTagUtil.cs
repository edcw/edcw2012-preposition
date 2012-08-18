using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using Ototake.Edcw.Data;

namespace Ototake.Edcw.Detect
{
    /// <summary>
    /// Classiasの分類プログラム実行ユーティリティ
    /// </summary>
    public sealed class ClassiasTagUtil
    {
        #region プロパティ
        /// <summary>
        /// classias-tag 実行ファイルパス
        /// </summary>
        public string ExecPath { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 実行ファイルパスを指定してインスタンス生成
        /// </summary>
        /// <param name="execPath"></param>
        public ClassiasTagUtil(string execPath)
        {
            ExecPath = execPath;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 指定した前置詞について，分類を行う．
        /// </summary>
        /// <param name="ins"></param>
        /// <param name="targetPrp"></param>
        /// <param name="modelPath"></param>
        /// <returns></returns>
        public Tuple<bool, double> ClassifyAndProb(PrpInstance ins, string targetPrp, string modelPath)
        {
            // nullチェック
            var cline = ins.ClassiasLine(targetPrp);
            if (string.IsNullOrEmpty(cline))
                return null;

            cline = cline.Replace('"', '\'');
            cline = cline.Replace("$", "");
            cline = cline.Replace('-', '_');
            cline = cline.Replace(".", "");

            string cmd = string.Format(@"echo \""{0}\"" | {1} -m {2} -p", cline, ExecPath, modelPath);

            var info = new ProcessStartInfo
            {
                //FileName = ExecPath,
                //Arguments = string.Format(@"-m {0} -p", modelPath),
                FileName = "bash",
                Arguments = string.Format(@"-c ""{0}""", cmd),
                CreateNoWindow = true,
                UseShellExecute = false,
                RedirectStandardOutput = true,
                //RedirectStandardInput = true
            };

            Process p = null;
            try
            {
                p = Process.Start(info);
                //p.StandardInput.WriteLine(ins.ClassiasLine(targetPrp));
                string line = p.StandardOutput.ReadLine();
                var tmp = line.Split(':');
                if (tmp.Length != 2)
                    throw new Exception("Classiasからの出力を受け取れていない．" + info.FileName + info.Arguments + " <out>" + line);

                //p.StandardInput.Close();
                return Tuple.Create(int.Parse(tmp[0]) == 1, double.Parse(tmp[1]));
            }
            catch (System.ComponentModel.Win32Exception w)
            {
                var str = new StringBuilder();
                str.AppendLine(w.Message);
                str.AppendLine(w.ErrorCode.ToString());
                str.AppendLine(w.NativeErrorCode.ToString());
                str.AppendLine(w.StackTrace);
                str.AppendLine(w.Source);
                Exception e = w.GetBaseException();
                str.AppendLine(e.Message);
                throw new Exception(str.ToString());
            }
            finally
            {
                if (p != null)
                {
                    p.WaitForExit();
                    p.Dispose();
                }
            }
        }
        #endregion
    }
}
