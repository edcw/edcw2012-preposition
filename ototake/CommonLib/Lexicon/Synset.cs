using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// WordNet Synsetを表す．
    /// </summary>
    public class Synset : ISynset, IEquatable<ISynset>, IEquatable<Synset>
    {
        protected readonly MultiDictionary<RelationType, RelationalSynsetInfo> relationSynsetOffSetMap;
        protected List<Tuple<VerbFrameType, int>> frames = null;
        protected SynsetMap synsetMap;

        /// <summary>
        /// 識別ID
        /// </summary>
        public int Id { get; protected internal set; }

        /// <summary>
        /// Synsetのカテゴリを取得します．
        /// </summary>
        public LexicographerTypes LexCategory { get; protected internal set; }

        /// <summary>
        /// Synsetタイプを取得します．
        /// </summary>
        public SynsetType Type { get; protected internal set; }

        /// <summary>
        /// このSynsetをもつSenseリストを取得します．
        /// </summary>
        public IList<ISense> Senses { get; protected internal set; }

        /// <summary>
        /// 定義文を取得します．
        /// </summary>
        public string Definition { get; protected internal set; }

        /// <summary>
        /// 例文集合を取得します．
        /// </summary>
        public string[] Examples { get; protected internal set; }

        public override string ToString()
        {
            return string.Format("[{0}] {1}", LexCategory, Definition);
        }

        #region internal methods

        /// <summary>
        /// MultiDictionaryのRelationsはここで初期化
        /// </summary>
        internal Synset(SynsetMap synsetMap, int id)
        {
            this.Id = id;
            this.synsetMap = synsetMap;
            this.Senses = new List<ISense>();
            this.relationSynsetOffSetMap = new MultiDictionary<RelationType, RelationalSynsetInfo>();
        }

        internal void AddRelation(RelationType r, int offset, SynsetType s)
        {
            relationSynsetOffSetMap.Add(r, new RelationalSynsetInfo(s, offset));
        }

        internal void AddRelation(RelationType r, int offset, SynsetType s, short sourceLemmaIndex, short targetLemmaIndex)
        {
            relationSynsetOffSetMap.Add(r, new RelationalSynsetInfo(s, offset, sourceLemmaIndex, targetLemmaIndex));
        }

        internal void AddFrame(VerbFrameType frameType, int lemmaIndex)
        {
            if (frames == null) frames = new List<Tuple<VerbFrameType, int>>();
            frames.Add(Tuple.Create(frameType, lemmaIndex));
        }
        #endregion
        /// <summary>
        /// 指定した引数に関連するSynset集合を取得する．
        /// </summary>
        /// <param name="relationType"></param>
        /// <returns></returns>
        public ISynset[] RelatedSynsets(RelationType relationType)
        {
            if (!relationSynsetOffSetMap.ContainsKey(relationType))
                return Enumerable.Empty<ISynset>().ToArray();
            return relationSynsetOffSetMap[relationType].Where(x => !(x.HasLemmaIndex)).Select(x => synsetMap[x.Type, x.TargetSynsetOffset]).ToArray();
        }

        /// <summary>
        /// 指定した引数に関連するSense集合を取得する．
        /// </summary>
        /// <param name="relationType"></param>
        /// <param name="sourceSense"></param>
        /// <returns></returns>
        public ISense[] RelatedSenses(RelationType relationType, ISense sourceSense)
        {
            if (!relationSynsetOffSetMap.ContainsKey(relationType))
                return Enumerable.Empty<ISense>().ToArray();

            sbyte lemmaIdx = (sbyte)Senses.IndexOf(sourceSense);
            if (lemmaIdx == -1) return Enumerable.Empty<ISense>().ToArray();

            var ret = new List<ISense>();
            foreach (var item in relationSynsetOffSetMap[relationType])
            {
                if (item.SourceLemmaIndex < 0)
                    ret.AddRange(synsetMap[item.Type, item.TargetSynsetOffset].Senses);
                else if (item.SourceLemmaIndex == lemmaIdx)
                {
                    if (item.TargetLemmaIndex < 0)
                        ret.AddRange(synsetMap[item.Type, item.TargetSynsetOffset].Senses);
                    else
                        ret.Add(synsetMap[item.Type, item.TargetSynsetOffset].Senses[item.TargetLemmaIndex]);
                }
            }
            return ret.ToArray();
        }

        public bool Equals(Synset other)
        {
            if (other == null) return false;
            return this.Id == other.Id && this.Type == other.Type;
        }

        public bool Equals(ISynset other)
        {
            return Equals(other as Synset);
        }

        public override int GetHashCode()
        {
            return Id.GetHashCode() ^ Type.GetHashCode();
        }
    }

    /// <summary>
    /// 重複要素を許すDictionary
    /// </summary>
    /// <typeparam name="TKey"></typeparam>
    /// <typeparam name="TValue"></typeparam>
    public sealed class MultiDictionary<TKey, TValue> : Dictionary<TKey, List<TValue>>
    {
        public void Add(TKey key, TValue value)
        {
            if (ContainsKey(key))
                this[key].Add(value);
            else
            {
                var l = new List<TValue>();
                l.Add(value);
                base.Add(key, l);
            }
        }
    }

    /// <summary>
    /// Relationで用いるSynset情報構造体
    /// </summary>
    public struct RelationalSynsetInfo
    {
        public SynsetType Type { get; private set; }
        public int TargetSynsetOffset { get; private set; }
        public short SourceLemmaIndex { get; private set; }
        public short TargetLemmaIndex { get; private set; }

        public bool HasLemmaIndex
        {
            get { return SourceLemmaIndex >= 0 && TargetLemmaIndex >= 0; }
        }

        public RelationalSynsetInfo(SynsetType type, int offset)
            : this(type, offset, -1, -1)
        { }

        public RelationalSynsetInfo(SynsetType type, int offset, short sourceLemmaIndex, short targetLemmaIndex)
            : this()
        {
            this.Type = type;
            this.TargetSynsetOffset = offset;
            this.SourceLemmaIndex = sourceLemmaIndex;
            this.TargetLemmaIndex = targetLemmaIndex;
        }
    }
}
