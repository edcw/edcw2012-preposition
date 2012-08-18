using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ototake.Edcw.Lexicon
{
    using SMap = Dictionary<int, Synset>;
    using MyKeyValuePair = KeyValuePair<Tuple<SynsetType, int>, Synset>;

    public sealed class SynsetMap : IEnumerable<MyKeyValuePair>
    {
        #region フィールド
        private Dictionary<SynsetType, SMap> synsetMaps = new Dictionary<SynsetType, SMap>(5)
        {
            {SynsetType.Noun, new SMap()},
            {SynsetType.Verb, new SMap()},
            {SynsetType.Adjective, new SMap()},
            {SynsetType.Adverb, new SMap()},
            {SynsetType.AdjectiveSatellite, new SMap()},
        };
        #endregion

        #region プロパティ
        /// <summary>
        /// キーがないときはnullを返す．
        /// </summary>
        /// <param name="ssType"></param>
        /// <param name="key"></param>
        /// <returns></returns>
        public Synset this[SynsetType ssType, int key]
        {
            get
            {
                return ContainsKey(ssType, key) ?
                    synsetMaps[ssType][key] : null;
            }
        }

        /// <summary>
        /// 指定したSynsetTypeのSynsetを列挙する．
        /// </summary>
        /// <param name="ssType"></param>
        /// <returns></returns>
        public IEnumerable<Synset> this[SynsetType ssType]
        {
            get { return synsetMaps[ssType].Values; }
        }

        public int Count
        {
            get { return synsetMaps.Values.Select(x => x.Count).Sum(); }
        }


        #endregion

        #region コンストラクタ
        /// <summary>
        /// SynsetMapを構築する．
        /// </summary>
        public SynsetMap() { }
        #endregion

        #region メソッド

        /// <summary>
        /// 品詞を指定してキーとsynsetを追加
        /// </summary>
        /// <param name="ssType"></param>
        /// <param name="key"></param>
        /// <param name="lemma"></param>
        public void Add(SynsetType ssType, int key, Synset synset)
        {
            synsetMaps[ssType].Add(key, synset);
        }

        /// <summary>
        /// 各品詞におけるSynsetマップの初期化
        /// </summary>
        public void Clear()
        {
            foreach (var item in synsetMaps.Values)
            {
                item.Clear();
            }
        }

        public bool ContainsKey(SynsetType ssType, int key)
        {
            return synsetMaps[ssType].ContainsKey(key);
        }

        /// <summary>
        /// キーを列挙する．
        /// </summary>
        /// <returns></returns>
        public IEnumerable<Tuple<SynsetType, int>> GetKeys()
        {
            foreach (var item1 in synsetMaps)
                foreach (var item2 in item1.Value)
                    yield return new Tuple<SynsetType, int>(item1.Key, item2.Key);
        }
        #endregion

        #region IEnumerable<KeyValuePair<Tuple<SynsetType,string>,Synset>> メンバー

        public IEnumerator<MyKeyValuePair> GetEnumerator()
        {
            foreach (var item1 in synsetMaps)
                foreach (var item2 in item1.Value)
                    yield return new MyKeyValuePair(new Tuple<SynsetType, int>(item1.Key, item2.Key), item2.Value);
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
