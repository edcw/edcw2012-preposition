using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Data
{
    /// <summary>
    /// EDR電子化辞書管理クラス
    /// </summary>
    public sealed class EDRManager
    {
        #region プロパティ
        /// <summary>
        /// 辞書ファイル
        /// </summary>
        public string DicFile { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// EDR電子化辞書のパスを指定
        /// </summary>
        /// <param name="dicPath"></param>
        public EDRManager(string dicPath)
        {
            DicFile = dicPath;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// トレーニングデータを列挙する．
        /// </summary>
        /// <param name="analyzer"></param>
        /// <returns></returns>
        public IEnumerable<TrainingData> EnumerateTrainingData()
        {
            foreach (var line in File.ReadLines(DicFile))
            {
                var split = line.Split('\t');
                if (split.Length < 4) continue;

                var text = split[3];
                yield return new TrainingData(text);
            }
        }
        #endregion
    }
}
