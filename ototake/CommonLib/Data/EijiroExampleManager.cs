using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Data
{
    /// <summary>
    /// 英辞郎例示集管理クラス
    /// </summary>
    public sealed class EijiroExampleManager
    {
        #region プロパティ
        /// <summary>
        /// 辞書ファイル
        /// </summary>
        public string ReijiFile { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// EDR電子化辞書のパスを指定
        /// </summary>
        /// <param name="dicPath"></param>
        public EijiroExampleManager(string reijiPath)
        {
            ReijiFile = reijiPath;
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
            var regex = new System.Text.RegularExpressions.Regex(@"^■(?<text>.+)\s:\s");
            foreach (var line in File.ReadLines(ReijiFile))
            {
                var m = regex.Match(line);
                if (!m.Groups["text"].Success)
                    continue;

                var text = m.Groups["text"].Value;
                yield return new TrainingData(text);
            }
        }
        #endregion
    }
}
