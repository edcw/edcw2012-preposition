using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using Ionic.Zip;
using Ototake.Edcw.Data;

namespace Ototake.Edcw.Detect
{
    /// <summary>
    /// 誤り検出結果
    /// </summary>
    public class DetectResult
    {
        /// <summary>
        /// Recordsに対応した検出結果リスト
        /// </summary>
        public List<string> DetectRecords { get; internal set; }

        /// <summary>
        /// ベースとなるオブジェクト
        /// </summary>
        public List<KJCorpusData> BaseResult { get; internal set; }

        /// <summary>
        /// 誤り検出にかかった時間
        /// </summary>
        public long ProcessMillisec { get; internal set; }

        /// <summary>
        /// Lucene結果オブジェクトから誤り検出結果オブジェクトを作る
        /// </summary>
        /// <param name="baseResult"></param>
        /// <param name="detector">誤り検出メソッド</param>
        public DetectResult(List<KJCorpusData> baseResult, Func<KJCorpusData, string> detector)
        {
            BaseResult = baseResult;

            var sw = new Stopwatch();
            sw.Start();
            DetectRecords = new List<string>(BaseResult.Select(x => detector(x)));
            ProcessMillisec = sw.ElapsedMilliseconds;
            sw.Stop();
        }

        /// <summary>
        /// 結果をZip圧縮したストリームを取得．
        /// </summary>
        /// <returns></returns>
        public byte[] Zipped()
        {
            var euc = Encoding.GetEncoding("euc-jp");
            using (var memst = new MemoryStream())
            {
                using (var zip = new ZipFile())
                {
                    // 最大圧縮
                    zip.CompressionLevel = Ionic.Zlib.CompressionLevel.BestCompression;

                    // 各ファイルを圧縮していく
                    for (int i = 0; i < DetectRecords.Count; i++)
                    {
                        var name = BaseResult[i].Name + ".sys";
                        zip.AddEntry(name, DetectRecords[i], euc);
                    }

                    // 保存
                    zip.Save(memst);
                }
                return memst.ToArray();
            }
        }
    }
}
