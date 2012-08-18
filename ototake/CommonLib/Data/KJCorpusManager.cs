using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace Ototake.Edcw.Data
{
    /// <summary>
    /// KJCorpus管理クラス
    /// </summary>
    public sealed class KJCorpusManager
    {
        #region プロパティ
        /// <summary>
        /// コーパスデータディレクトリ
        /// </summary>
        public string Dir { get; private set; }

        /// <summary>
        /// フォーマルデータ用？
        /// </summary>
        public bool IsFormal { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// コーパスデータディレクトリを指定して新しいインスタンスを作成する．
        /// </summary>
        /// <param name="dir"></param>
        public KJCorpusManager(string dir, bool isFormal = false)
        {
            this.Dir = dir;
            this.IsFormal = isFormal;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// コーパスに含まれるファイル名ベースを列挙する
        /// </summary>
        /// <returns></returns>
        public IEnumerable<string> EnumerateTargetFilePath()
        {
            // ディレクトリ展開
            foreach (var subdir in Directory.GetDirectories(Dir))
            {
                foreach (var subsubdir in Directory.GetDirectories(subdir))
                {
                    var targetFile = Directory.GetFiles(subsubdir, "*.pos").FirstOrDefault();
                    if (targetFile != default(string))
                        yield return targetFile.Substring(0, targetFile.Length - 4);
                }
            }
        }

        /// <summary>
        /// コーパスに含まれるデータを列挙する
        /// </summary>
        /// <returns></returns>
        public IEnumerable<KJCorpusData> EnumerateKJCorpusData()
        {
            return EnumerateTargetFilePath().Select(x => new KJCorpusData(x, IsFormal));
        }
        #endregion
    }
}
