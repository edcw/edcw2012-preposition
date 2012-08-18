using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ototake.Edcw.Lexicon
{
    using LemmaMap = Dictionary<string, Lemma>;
    using MyKeyValuePair = KeyValuePair<Tuple<SynsetType, string>, Lemma>;

    /// <summary>
    /// 品詞をキーとするSenseIndexマップ
    /// </summary>
    public sealed class SenseIndexMap : IEnumerable<MyKeyValuePair>
    {
        #region フィールド
        private Dictionary<SynsetType, LemmaMap> lemmaMaps = new Dictionary<SynsetType, LemmaMap>(5)
        {
            {SynsetType.Noun, new LemmaMap()},
            {SynsetType.Verb, new LemmaMap()},
            {SynsetType.Adjective, new LemmaMap()},
            {SynsetType.Adverb, new LemmaMap()},
            {SynsetType.AdjectiveSatellite, new LemmaMap()},
        };
        #endregion

        #region プロパティ
        /// <summary>
        /// キーがないときはnullを返す．
        /// </summary>
        /// <param name="ssType"></param>
        /// <param name="key"></param>
        /// <returns></returns>
        public Lemma this[SynsetType ssType, string key]
        {
            get
            {
                return ContainsKey(ssType, key) ?
                    lemmaMaps[ssType][key] : null;
            }
        }

        public int Count
        {
            get { return lemmaMaps.Values.Select(x => x.Count).Sum(); }
        }


        #endregion

        #region コンストラクタ
        /// <summary>
        /// SenseIndexMapを構築する．
        /// </summary>
        public SenseIndexMap() { }
        #endregion

        #region メソッド
        /// <summary>
        /// 品詞を指定してキーとLemmaを追加
        /// </summary>
        /// <param name="ssType"></param>
        /// <param name="key"></param>
        /// <param name="lemma"></param>
        public void Add(SynsetType ssType, string key, Lemma lemma)
        {
            lemmaMaps[ssType].Add(key, lemma);
        }

        /// <summary>
        /// 各品詞におけるLemmaマップの初期化
        /// </summary>
        public void Clear()
        {
            foreach (var item in lemmaMaps.Values)
            {
                item.Clear();
            }
        }

        public bool ContainsKey(SynsetType ssType, string key)
        {
            return lemmaMaps[ssType].ContainsKey(key);
        }

        /// <summary>
        /// キーを列挙します．
        /// </summary>
        /// <returns></returns>
        public IEnumerable<Tuple<SynsetType, string>> GetKeys()
        {
            foreach (var item1 in lemmaMaps)
                foreach (var item2 in item1.Value)
                    yield return new Tuple<SynsetType, string>(item1.Key, item2.Key);
        }
        #endregion

        #region IEnumerable<KeyValuePair<Tuple<SynsetType,string>,Lemma>> メンバー

        public IEnumerator<MyKeyValuePair> GetEnumerator()
        {
            foreach (var item1 in lemmaMaps)
                foreach (var item2 in item1.Value)
                    yield return new MyKeyValuePair(new Tuple<SynsetType, string>(item1.Key, item2.Key), item2.Value);
        }

        #endregion

        #region IEnumerable メンバー

        System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
        {
            return this.GetEnumerator();
        }

        #endregion
    }
}
