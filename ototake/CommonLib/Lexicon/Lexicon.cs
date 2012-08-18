using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using Ionic.Zip;

namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// WordNet語彙集合を表します．
    /// </summary>
    public sealed class Lexicon : ILexicon
    {
        #region フィールド
        /// <summary>
        /// RelationTypeとpointer_symbolの関係マップ
        /// </summary>
        private static Dictionary<string, RelationType> relationMap = new Dictionary<string, RelationType>()
        {
            {"@", RelationType.Hypernym},
            {"~", RelationType.Hyponym},
            {"@i", RelationType.InstanceHypernym},
            {"~i", RelationType.InstanceHyponym},
            {"#p", RelationType.IsPart},
            {"%p", RelationType.HasPart},
            {"#m", RelationType.IsMember},
            {"%m", RelationType.HasMember},
            {"#s", RelationType.IsSubstance},
            {"%s", RelationType.HasSubstance},
            {"=", RelationType.Attribute},
            {"$", RelationType.VerbGroup},
            {"*", RelationType.VerbEntailment},
            {">", RelationType.VerbCause},
            {"&", RelationType.AdjectiveSimilar},
            {";c", RelationType.HasTopicDomain},
            {"-c", RelationType.IsTopicDomain},
            {";r", RelationType.HasRegionDomain},
            {"-r", RelationType.IsRegionDomain},
            {";u", RelationType.HasUsageDomain},
            {"-u", RelationType.IsUsageDomain},
            {"^", RelationType.SeeAlso},
            {"<", RelationType.AdjectiveParticiple},
            {"!", RelationType.Antonym},
            {@"\", RelationType.Pertainym},
            {"+", RelationType.Derivation},
        };

        private static readonly SynsetType[] _typeList = { SynsetType.Noun, SynsetType.Adjective, SynsetType.Adverb, SynsetType.Verb };
        private static readonly string[] _indexFiles = { "index.noun", "index.adj", "index.adv", "index.verb" };
        private static readonly string[] _excFiles = { "noun.exc", "adj.exc", "adv.exc", "verb.exc" };
        private static readonly string[] _dataFiles = { "data.noun", "data.adj", "data.adv", "data.verb" };
        private const string _indexSenseFile = "index.sense";

        private SynsetMap _synsetMap;
        private SenseIndexMap _senseIndexMap;
        private Lemmatizer _lemmatizer;
        #endregion

        #region プロパティ
        public int SynsetCount
        {
            get { return _synsetMap.Count; }
        }
        #endregion

        #region 構築メソッド
        /// <summary>
        /// 指定したディレクトリからWordNetデータベースファイルを読み込みます．
        /// </summary>
        /// <param name="dir">WordNet DBファイルの配置ディレクトリ</param>
        /// <returns></returns>
        public static Lexicon CreateFromDir(string dir)
        {
            var ret = new Lexicon();
            Console.Error.WriteLine("Starting Lexicon construction.");
            ret.LoadFromDir(dir);
            return ret;
        }

        /// <summary>
        /// 指定したZipファイルからWordNetデータベースファイルを読み込みます．
        /// </summary>
        /// <param name="zip"></param>
        /// <returns></returns>
        public static Lexicon CreateFromZip(string zip)
        {
            var ret = new Lexicon();
            Console.Error.WriteLine("Starting Lexicon construction.");
            ret.LoadFromZip(zip);
            return ret;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 指定したディレクトリからWordNetデータベースファイルを読み込みます．
        /// </summary>
        /// <param name="dir">WordNet DBファイルの配置ディレクトリ</param>
        public void LoadFromDir(string dir)
        {
            var dataPaths = _dataFiles.Select(x => Path.Combine(dir, x));

            BuildSynsetMapFromFiles(dataPaths, out this._synsetMap, out this._senseIndexMap);
            Console.Error.WriteLine(">> Finish constructing synset map.");
            BuildSenseIndexMapFromFile(Path.Combine(dir, _indexSenseFile), this._senseIndexMap, this._synsetMap);
            Console.Error.WriteLine(">> Finish constructing sense index map.");

            var files = new Tuple<SynsetType, string, string>[_typeList.Length];
            for (int i = 0; i < files.Length; i++)
                files[i] = Tuple.Create(
                    _typeList[i],
                    Path.Combine(dir, _indexFiles[i]),
                    Path.Combine(dir, _excFiles[i]));
            _lemmatizer = new Lemmatizer(files);
            Console.Error.WriteLine(">> Finish constructing lemmatizer.");
        }

        public void LoadFromZip(string zipPath)
        {
            using (var zip = ZipFile.Read(zipPath))
            {
                var dataEntries = new List<ZipEntry>(_dataFiles.Length);
                var indexSenseEntry = default(ZipEntry);
                var indexEntries = new ZipEntry[_indexFiles.Length];
                var excEntries = new ZipEntry[_excFiles.Length];
                foreach (var entry in zip)
                {
                    var name = Path.GetFileName(entry.FileName);
                    if (_dataFiles.Contains(name))
                        dataEntries.Add(entry);
                    else if (_indexSenseFile == name)
                        indexSenseEntry = entry;
                    else if (_indexFiles.Contains(name))
                        indexEntries[Array.IndexOf(_indexFiles, name)] = entry;
                    else if (_excFiles.Contains(name))
                        excEntries[Array.IndexOf(_excFiles, name)] = entry;
                }

                BuildSynsetMapFromZipEntries(dataEntries, out this._synsetMap, out this._senseIndexMap);
                Console.Error.WriteLine(">> Finish constructing synset map.");
                BuildSenseIndexMapFromZipEntry(indexSenseEntry, this._senseIndexMap, this._synsetMap);
                Console.Error.WriteLine(">> Finish constructing sense index map.");

                var files = new Tuple<SynsetType, ZipEntry, ZipEntry>[_typeList.Length];
                for (int i = 0; i < files.Length; i++)
                    files[i] = Tuple.Create(
                        _typeList[i],
                        indexEntries[i],
                        excEntries[i]);
                _lemmatizer = new Lemmatizer(files);
                Console.Error.WriteLine(">> Finish constructing lemmatizer.");
            }
        }

        public ILemma GetLemma(SynsetType type, string baseform, bool adjectiveMerge = true)
        {
            if (adjectiveMerge && type == SynsetType.Adjective)
            {
                var ret1 = _senseIndexMap[SynsetType.Adjective, baseform];
                var ret2 = _senseIndexMap[SynsetType.AdjectiveSatellite, baseform];
                if (ret1 != null && ret2 != null)
                    return new MultiLemma(ret1, ret2);
                else
                    return ret1 ?? ret2;
            }
            return _senseIndexMap[type, baseform];
        }

        public string Lemmatize(string form, SynsetType? type)
        {
            if (!type.HasValue) return form;
            return _lemmatizer.Lemma(form, type.Value);
        }

        public IEnumerable<string> EachLemmatize(string form, SynsetType? type)
        {
            if (type.HasValue)
                return _lemmatizer.EachLemma(form, type.Value);
            else
                return Enumerable.Empty<string>();
        }

        public IEnumerable<ISynset> AllSynsets(SynsetType type, bool adjectiveMerge = true)
        {
            if (adjectiveMerge && type == SynsetType.Adjective)
            {
                foreach (var item in _synsetMap[SynsetType.Adjective])
                    yield return item;
                foreach (var item in _synsetMap[SynsetType.AdjectiveSatellite])
                    yield return item;
            }
            else
            {
                foreach (var item in _synsetMap[type])
                    yield return item;
            }
        }

        public IEnumerable<ISynset> AllSynsets()
        {
            return new[] {
                SynsetType.Noun,
                SynsetType.Verb,
                SynsetType.Adjective,
                SynsetType.Adverb
            }.SelectMany(x => AllSynsets(x));
        }
        #endregion

        #region プライベートメソッド
        private static void BuildSynsetMapFromFiles(IEnumerable<string> dataFiles, out SynsetMap synsetMap, out SenseIndexMap senseIndexMap)
        {
            synsetMap = new SynsetMap();
            senseIndexMap = new SenseIndexMap();
            var ssMap = GetSynsetTypeMap();
            int id = 1;
            foreach (var dataFile in dataFiles)
            {
                using (var dataReader = File.OpenText(dataFile))
                {
                    id = BuildSynsetMapFromStream(id, synsetMap, senseIndexMap, ssMap, dataReader);
                }
            }
        }

        private static void BuildSynsetMapFromZipEntries(IEnumerable<ZipEntry> dataEntries, out SynsetMap synsetMap, out SenseIndexMap senseIndexMap)
        {
            synsetMap = new SynsetMap();
            senseIndexMap = new SenseIndexMap();
            var ssMap = GetSynsetTypeMap();
            int id = 1;
            foreach (var dataEntry in dataEntries)
            {
                using (var mem = new MemoryStream())
                {
                    dataEntry.Extract(mem);
                    mem.Position = 0;
                    using (var dataReader = new StreamReader(mem))
                    {
                        id = BuildSynsetMapFromStream(id, synsetMap, senseIndexMap, ssMap, dataReader);
                    }
                }
            }
        }

        private static int BuildSynsetMapFromStream(int startId, SynsetMap synsetMap, SenseIndexMap senseIndexMap, Dictionary<string, SynsetType> ssMap, TextReader dataReader)
        {
            //foreach (var line in File.ReadLines(dataFile))
            int id = startId;
            for (string line = dataReader.ReadLine(); line != null; line = dataReader.ReadLine())
            {
                if (string.IsNullOrWhiteSpace(line) || line[0] == ' ') continue;
                string[] splitted = line.Split(' ');
                int synset_offset = int.Parse(splitted[0]);
                var synset = new Synset(synsetMap, id++);

                synset.LexCategory = (LexicographerTypes)int.Parse(splitted[1]);
                synset.Type = ssMap[splitted[2]];

                // synsetMapに追加
                synsetMap.Add(synset.Type, synset_offset, synset);

                // Words
                int w_cnt = int.Parse(splitted[3], System.Globalization.NumberStyles.HexNumber);
                for (int i = 0; i < w_cnt; i++)
                {
                    string word = splitted[4 + (2 * i)].Split('(')[0];
                    byte lex_id = byte.Parse(splitted[5 + (2 * i)], System.Globalization.NumberStyles.HexNumber);
                    Lemma lemma = senseIndexMap[synset.Type, word];
                    if (lemma == null)
                    {
                        lemma = new Lemma(word, synset.Type);
                        senseIndexMap.Add(synset.Type, word, lemma);
                    }

                    // LemmaにSenseを追加
                    var sense = new Sense(lemma) { LexId = lex_id, Synset = synset };
                    lemma.AddSense(synset, sense);

                    synset.Senses.Add(sense);
                }

                // Pointers
                int pidx = 4 + (2 * w_cnt);
                int p_cnt = int.Parse(splitted[pidx]);

                for (int i = 0; i < p_cnt; i++)
                {
                    string srctar = splitted[pidx + 4 + (4 * i)];
                    short srcwn = short.Parse(srctar.Substring(0, 2), System.Globalization.NumberStyles.HexNumber);
                    short tarwn = short.Parse(srctar.Substring(2, 2), System.Globalization.NumberStyles.HexNumber);
                    synset.AddRelation(relationMap[splitted[pidx + 1 + (4 * i)]], int.Parse(splitted[pidx + 2 + (4 * i)]), ssMap[splitted[pidx + 3 + (4 * i)]], (short)(srcwn - 1), (short)(tarwn - 1));
                }

                // フレーム処理は動詞のみ
                if (synset.Type == SynsetType.Verb)
                {
                    int fidx = pidx + 1 + (4 * p_cnt);
                    if (splitted[fidx] != "|")
                    {
                        int f_cnt = int.Parse(splitted[fidx]);
                        for (int i = 0; i < f_cnt; i++)
                        {
                            var frameType = (VerbFrameType)int.Parse(splitted[fidx + 2 + (3 * i)]);
                            int w_num = int.Parse(splitted[fidx + 3 + (3 * i)], System.Globalization.NumberStyles.HexNumber);
                            synset.AddFrame(frameType, w_num - 1);
                        }
                    }
                }

                // "|"を読みこむ
                string gloss = line.Substring(line.IndexOf('|') + 2);
                var glossSplit = gloss.Split(';');
                synset.Definition = glossSplit[0];
                if (glossSplit.Length > 1)
                {
                    synset.Examples = new string[glossSplit.Length - 1];
                    for (int i = 1; i < glossSplit.Length; i++)
                        synset.Examples[i - 1] = glossSplit[i].Trim();
                }
            }
            return id;
        }

        private static void BuildSenseIndexMapFromFile(string senseIndexPath, SenseIndexMap senseIndexMap, SynsetMap synsetMap)
        {
            using (var dataReader = File.OpenText(senseIndexPath))
            {
                BuildSenseIndexMap(dataReader, senseIndexMap, synsetMap);
            }
        }

        private static void BuildSenseIndexMapFromZipEntry(ZipEntry senseIndexEntry, SenseIndexMap senseIndexMap, SynsetMap synsetMap)
        {
            using (var mem = new MemoryStream())
            {
                senseIndexEntry.Extract(mem);
                mem.Position = 0;
                using (var dataReader = new StreamReader(mem))
                {
                    BuildSenseIndexMap(dataReader, senseIndexMap, synsetMap);
                }
            }
        }

        private static void BuildSenseIndexMap(TextReader dataReader, SenseIndexMap senseIndexMap, SynsetMap synsetMap)
        {
            //foreach (var line in File.ReadLines(senseIndexPath))
            for (string line = dataReader.ReadLine(); line != null; line = dataReader.ReadLine())
            {
                string[] splitted = line.Split(' ');
                if (splitted.Length < 4) continue;

                string[] senseKeys = splitted[0].Split('%');
                string[] lex_sense = senseKeys[1].Split(':');
                SynsetType ssType = (SynsetType)int.Parse(lex_sense[0]);

                // lemma
                Lemma lemma = senseIndexMap[ssType, senseKeys[0]];
                if (lemma == null)
                {
                    lemma = new Lemma(senseKeys[0], ssType);
                    senseIndexMap.Add(ssType, senseKeys[0], lemma);
                }

                int offset = int.Parse(splitted[1]);
                Sense sense = (Sense)lemma[synsetMap[ssType, offset]];
                if (sense != null)
                {
                    sense.HeadWord = (string.IsNullOrEmpty(lex_sense[3])) ? null : lex_sense[3];
                    sense.SenseNo = int.Parse(splitted[2]);
                }
                else
                {
                    lemma.Senses.Add(new Sense(lemma)
                    {
                        LexId = byte.Parse(lex_sense[2]),
                        Synset = synsetMap[ssType, offset],
                        HeadWord = (string.IsNullOrEmpty(lex_sense[3])) ? null : lex_sense[3],
                        SenseNo = int.Parse(splitted[2])
                    });
                }
            }

            foreach (var lemma in senseIndexMap.Select(x => x.Value))
            {
                lemma.TrimAndSortBySenseNum();
            }
        }

        private static Dictionary<string, SynsetType> GetSynsetTypeMap()
        {
            return new Dictionary<string, SynsetType>() 
            {
                {"n", SynsetType.Noun},
                {"v", SynsetType.Verb},
                {"a", SynsetType.Adjective},
                {"s", SynsetType.AdjectiveSatellite},
                {"r", SynsetType.Adverb}
            };
        }
        #endregion
    }
}
