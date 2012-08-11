using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using Org.Shokai.Util.ConsoleLib;
using Ototake.Edcw.Data;
using Ototake.Edcw.Detect;
using Ototake.Edcw.OpenNlp;

namespace Ototake.Edcw.Main
{
    /// <summary>
    /// コンソールプログラムのメインクラス
    /// </summary>
    class Program
    {
        static Dictionary<string, Action<ParamsParser>> procMap = new Dictionary<string, Action<ParamsParser>>
        {
            { "t", CreateTrainingData },
            { "d", DetectErrors },
            { "r", OutputSourceText },
        };

        static void Main(string[] args)
        {
            var pp = new ParamsParser(args);

            // 処理を表す文字列
            string p = pp.First;

            // 処理
            if (p == null || !procMap.ContainsKey(p))
            {
                Usage();
            }
            else
            {
                try { procMap[p](pp); }
                catch (Exception ex)
                {
                    Debug.Fail(ex.Message, ex.StackTrace);
                }
            }
        }

        /// <summary>
        /// 前置詞用トレーニングデータを作成
        /// </summary>
        /// <param name="pp"></param>
        static void CreateTrainingData(ParamsParser pp)
        {
            // 必須パラメータチェック
            CheckArgument(pp, "out", "src", "res");

            Console.WriteLine("解析リソースの構築中．．．");
            var res = new DocumentResources(pp.Params["res"]);
            var nres = new NameResources(pp.Params["res"]);
            var an = new DocumentAnalyzer(res);
            var nan = new NameAnalyzer(nres);
            Console.WriteLine("解析リソース構築完了！");

            // 各前置詞に対応したストリームライターマップ
            var swmap = new Dictionary<string, StreamWriter>();
            try
            {
                foreach (var prp in PrpDetectorME.TargetPrps)
                {
                    swmap[prp] = new StreamWriter(Path.Combine(pp.Params["out"], prp + ".train"));
                    swmap[prp].WriteLine("# Training data for prep '{0}'", prp);
                }

                #region 進捗報告用ローカル関数
                int n = 0;  // 登録数
                int en = 0; // エラー数
                int tn = 0; // TimerCallback実行数
                TimerCallback pgr = (obj) =>
                {
                    tn++;
                    Console.WriteLine("==> Progress: {0:N0} [null:{1:N0}] ({2:N0} idx/min)",
                        n, en, n / tn * 6);
                };
                #endregion

                using (var timer = new Timer(pgr, null, 3000, 3000))
                {
                    var items = File.ReadLines(pp.Params["src"])
                                    .SelectMany(x => new TrainingData(x).CreatePrpInstances(an, nan));
                    foreach (var item in items)
                    {
                        n++;
                        if (item == null)
                        {
                            en++;
                            continue;
                        }
                        foreach (var prp in PrpDetectorME.TargetPrps)
                        {
                            swmap[prp].WriteLine(item.ClassiasLine(prp));
                        }
                    }
                }
            }
            finally
            {
                foreach (var sw in swmap.Values)
                {
                    if (sw != null) sw.Dispose();
                }
            }
        }

        /// <summary>
        /// v_agrとprpの誤り検出
        /// 各前置詞の二値分類版
        /// </summary>
        /// <param name="pp"></param>
        static void DetectErrors(ParamsParser pp)
        {
            // 必須パラメータチェック
            CheckArgument(pp, "target", "kjdir", "res", "out");

            var man = new KJCorpusManager(pp.Params["kjdir"], pp.hasSwitch("formal"));
            var docRes = new DocumentResources(pp.Params["res"]);
            var kjlist = man.EnumerateKJCorpusData().ToList();

            // classias-tagコマンドのパス
            string clsPath = pp.hasParam("classias") ? pp.Params["classias"] : "/usr/local/bin/classias-tag";

            Console.WriteLine("対象ファイルは{0}個", kjlist.Count);

            if (pp.Params["target"] == "v_agr")
            {
                var detector = new VAgrDetector(new DocumentAnalyzer(docRes));
                var ret = new DetectResult(kjlist, detector.DetectErrors);
                File.WriteAllBytes(pp.Params["out"], ret.Zipped());
            }
            else if (pp.Params["target"] == "prp")
            {
                var nres = new NameResources(pp.Params["res"]);
                var detector = new PrpDetectorME
                {
                    Analyzer = new DocumentAnalyzer(docRes),
                    NAnalyzer = new NameAnalyzer(nres),
                    ModelDir = pp.Params["model"],
                    Cls = new ClassiasTagUtil(clsPath)
                };
                try
                {
                    var ret = new DetectResult(kjlist, detector.DetectErrors);
                    File.WriteAllBytes(pp.Params["out"], ret.Zipped());
                }
                catch (Exception ex)
                {
                    Console.WriteLine(ex.Message);
                    Console.WriteLine(ex.StackTrace);
                }
            }
            else
            {
                Console.WriteLine("その誤りは対象にしていない");
            }
        }

        /// <summary>
        /// 特定の形式のデータを生テキストに変換して標準出力に出す．
        /// </summary>
        /// <param name="pp"></param>
        /// <remarks>
        /// 対応するデータ形式は，
        /// EDR電子化辞書：英語コーパス（ECO.DIC），
        /// 英辞郎の例辞郎ファイル（REIJI128.TXT）
        /// </remarks>
        static void OutputSourceText(ParamsParser pp)
        {
            // 必須パラメータチェック
            CheckArgument(pp, "type", "file");

            // 標準出力関数
            Action<IEnumerable<TrainingData>> stdout = (x) =>
            {
                foreach (var item in x)
                {
                    Console.WriteLine(item.Text);
                }
            };

            // 各タイプに応じた処理
            switch (pp.Params["type"])
            {
                case "edr":
                    stdout(new EDRManager(pp.Params["file"]).EnumerateTrainingData());
                    break;
                case "reijiro":
                    stdout(new EijiroExampleManager(pp.Params["file"]).EnumerateTrainingData());
                    break;
                default:
                    Console.Error.WriteLine("不正な type です．");
                    break;
            }
        }

        static void Usage()
        {
            Console.WriteLine("使い方:");
            Console.WriteLine("  (前置詞用トレーニングデータ作成)");
            Console.WriteLine("    Console.exe t [--out DATA-OUTPUT-DIR] [--src SOURCE-TEXT-FILE]");
            Console.WriteLine("                  [--res RESOURCE-DIR]");
            Console.WriteLine("  (誤り検出)");
            Console.WriteLine("    Console.exe d [--target TYPE (prp | v_agr)] [--kjdir KJ-CORPUS-DIR]");
            Console.WriteLine("                  [--formal] [--res RESOURCE-DIR]");
            Console.WriteLine("                  [--out OUTPUT-FILE] [--model MODELS-DIR]");
            Console.WriteLine("                  [--classias PATH-TO-CLASSIAS-TAG]");
            Console.WriteLine("  (特定形式のデータの生テキスト変換)");
            Console.WriteLine("    Console.exe r [--type DATA-TYPE (edr | reijiro)] [--file DATA-FILE]");
        }

        private static void CheckArgument(ParamsParser pp, params string[] keys)
        {
            foreach (var k in keys)
            {
                if (!pp.Params.ContainsKey(k))
                    throw new ArgumentException(string.Format("パラメータ {0} が見つかりません．", k));
            }
        }
    }
}
