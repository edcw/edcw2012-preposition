using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using Ionic.Zip;

namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// Lexiconから呼ばれるLemmatizer
    /// </summary>
    sealed class Lemmatizer
    {
        #region 定数・読み取り専用
        static readonly Dictionary<SynsetType, IList<Tuple<string, string>>> MORPHOLOGICAL_SUBSTITUTIONS = new Dictionary<SynsetType, IList<Tuple<string, string>>>()
        {
            { SynsetType.Noun, new[] {
                Tuple.Create("s", ""), Tuple.Create("ses", "s"), 
                Tuple.Create("ves", "f"), Tuple.Create("xes", "x"),
                Tuple.Create("zes", "z"), Tuple.Create("ches", "ch"),
                Tuple.Create("shes", "sh"), Tuple.Create("men", "man"),
                Tuple.Create("ies", "y") }
            },
            { SynsetType.Verb, new [] {
                Tuple.Create("s", ""), Tuple.Create("ies", "y"),
                Tuple.Create("es", "e"), Tuple.Create("es", ""),
                Tuple.Create("ed", "e"), Tuple.Create("ed", ""),
                Tuple.Create("ing", "e"), Tuple.Create("ing", "") }
            },
            { SynsetType.Adjective, new [] {
                Tuple.Create("er", ""), Tuple.Create("est", ""),
                Tuple.Create("er", "e"), Tuple.Create("est", "e") }
            },
            { SynsetType.Adverb, new Tuple<string, string>[0] }
        };
        #endregion

        #region フィールド
        Dictionary<SynsetType, HashSet<string>> wordSet;
        Dictionary<SynsetType, Dictionary<string, List<string>>> exceptions;
        #endregion

        #region コンストラクタ
        internal Lemmatizer(IEnumerable<Tuple<SynsetType, string, string>> files)
        {
            Init();
            foreach (var file in files)
            {
                using (var r1 = File.OpenText(file.Item2))
                using (var r2 = File.OpenText(file.Item3))
                {
                    LoadWordNetFiles(file.Item1, r1, r2);
                }
            }
        }

        internal Lemmatizer(IEnumerable<Tuple<SynsetType, ZipEntry, ZipEntry>> files)
        {
            Init();
            foreach (var file in files)
            {
                using (var mem1 = new MemoryStream())
                using (var mem2 = new MemoryStream())
                {
                    file.Item2.Extract(mem1);
                    file.Item3.Extract(mem2);
                    mem1.Position = 0;
                    mem2.Position = 0;
                    using (var r1 = new StreamReader(mem1))
                    using (var r2 = new StreamReader(mem2))
                    {
                        LoadWordNetFiles(file.Item1, r1, r2);
                    }
                }
            }
        }

        void Init()
        {
            wordSet = new Dictionary<SynsetType, HashSet<string>>();
            exceptions = new Dictionary<SynsetType, Dictionary<string, List<string>>>();
            foreach (var type in MORPHOLOGICAL_SUBSTITUTIONS.Keys)
            {
                wordSet.Add(type, new HashSet<string>());
                exceptions.Add(type, new Dictionary<string, List<string>>());
            }
        }
        #endregion

        #region メソッド
        internal string Lemma(string form, SynsetType type)
        {
            foreach (var item in EachLemma(form, type))
                return item;
            return form;
        }

        internal IEnumerable<string> EachSubstitutions(string form, SynsetType type)
        {
            if (!wordSet.ContainsKey(type))
                yield break;
            if (wordSet[type].Contains(form))
                yield return form;
            foreach (var entry in MORPHOLOGICAL_SUBSTITUTIONS[type])
            {
                if (form.EndsWith(entry.Item1))
                {
                    foreach (var item in EachSubstitutions(form.Substring(0, form.Length - entry.Item1.Length) + entry.Item2, type))
                        yield return item;
                }
            }
        }

        internal IEnumerable<string> EachLemma(string form, SynsetType type)
        {
            if (!exceptions.ContainsKey(type))
                yield break;
            if (exceptions[type].ContainsKey(form))
            {
                foreach (var lemma in exceptions[type][form])
                    yield return lemma;
            }
            if (type == SynsetType.Noun && form.EndsWith("ful"))
            {
                foreach (var item in EachLemma(form.Substring(0, form.Length - 3), type))
                    yield return item + "ful";
            }
            else
            {
                foreach (var item in EachSubstitutions(form, type))
                    yield return item;
            }
        }
        #endregion

        #region プライベートメソッド
        void LoadWordNetFiles(SynsetType type, TextReader indexReader, TextReader excReader)
        {
            for (string line = indexReader.ReadLine(); line != null; line = indexReader.ReadLine())
            {
                if (string.IsNullOrWhiteSpace(line) || line[0] == ' ') continue;
                var w = line.Split(' ').First();
                wordSet[type].Add(w);
            }
            for (string line = excReader.ReadLine(); line != null; line = excReader.ReadLine())
            {
                if (string.IsNullOrWhiteSpace(line) || line[0] == ' ') continue;
                var tmp = line.Split(' ');
                if (!exceptions[type].ContainsKey(tmp[0]))
                    exceptions[type].Add(tmp[0], new List<string>());
                exceptions[type][tmp[0]].Add(tmp[1]);
            }
        }
        #endregion
    }
}
