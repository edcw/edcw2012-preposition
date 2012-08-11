using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// 見出し語を表す．
    /// </summary>
    public class Lemma : ILemma
    {
        protected Dictionary<ISynset, ISense> synsetSenseMap;

        /// <summary>
        /// 見出し語文字列．
        /// </summary>
        public virtual string Text { get; protected internal set; }

        /// <summary>
        /// 品詞．
        /// </summary>
        public SynsetType Pos { get; protected internal set; }

        /// <summary>
        /// 見出し語に付与されたSenseリスト．
        /// </summary>
        public List<ISense> Senses { get; protected internal set; }

        /// <summary>
        /// 指定したインデックスのSenseを取得する．
        /// </summary>
        /// <param name="i"></param>
        /// <returns></returns>
        public ISense this[int i]
        {
            get { return Senses[i]; }
        }

        /// <summary>
        /// 指定したSynsetを持つSenseを取得する．
        /// ない場合はnullを返す．
        /// </summary>
        /// <param name="synset"></param>
        /// <returns></returns>
        public ISense this[ISynset synset]
        {
            get { return synsetSenseMap.ContainsKey(synset) ? synsetSenseMap[synset] : null; }
        }

        #region メソッド等
        internal Lemma(string text, SynsetType pos)
        {
            this.Text = text;
            this.Pos = pos;
            this.Senses = new List<ISense>();
            this.synsetSenseMap = new Dictionary<ISynset, ISense>();
        }

        internal void TrimAndSortBySenseNum()
        {
            Senses.Sort((x, y) => x.SenseNo - y.SenseNo);
            Senses.TrimExcess();
        }

        internal void AddSense(Synset synset, Sense sense)
        {
            Senses.Add(sense);
            synsetSenseMap.Add(synset, sense);
        }

        public override string ToString()
        {
            return string.Format("{0} / {1} ({2} senses)", Text, Pos, Senses.Count);
        }
        #endregion
    }

    /// <summary>
    /// 一つの意味の単位を表す．
    /// </summary>
    public class Sense : ISense
    {
        public ILemma Lemma { get; protected set; }
        public byte LexId { get; protected internal set; }
        public ISynset Synset { get; protected internal set; }
        public string HeadWord { get; protected internal set; }
        public int SenseNo { get; protected internal set; }

        public Sense(ILemma parentLemma)
        {
            this.Lemma = parentLemma;
        }

        public ISense[] RelatedSenses(RelationType relation)
        {
            return this.Synset.RelatedSenses(relation, this);
        }

        public override string ToString()
        {
            return string.Format("[{0}] {1} (syn:{2})", SenseNo, Lemma.Text, Synset);
        }
    }

    /// <summary>
    /// Adjective and Adjective Satellite 用 複数Lemmaを含むILemma実装
    /// </summary>
    public class MultiLemma : ILemma
    {
        private Lemma[] lemmas;
        private List<ISense> senses;

        public MultiLemma(Lemma adjLemma, Lemma adjsLemma)
        {
            lemmas = new Lemma[2] { adjLemma, adjsLemma };
        }

        #region ILemma メンバー

        public string Text
        {
            get { return lemmas[0].Text; }
        }

        public SynsetType Pos
        {
            get { return SynsetType.Adjective_AdjectiveSatellite; }
        }

        public List<ISense> Senses
        {
            get
            {
                if (senses == null)
                {
                    senses = new List<ISense>();
                    foreach (var l in lemmas)
                        senses.AddRange(l.Senses);
                }
                return senses;
            }
        }

        public ISense this[int i]
        {
            get { return Senses[i]; }
        }

        public ISense this[ISynset synset]
        {
            get
            {
                foreach (var l in lemmas)
                {
                    if (l[synset] != null)
                        return l[synset];
                }
                return null;
            }
        }

        public override string ToString()
        {
            return string.Format("{0} / {1} ({2} senses)", Text, Pos, Senses.Count);
        }
        #endregion
    }
}
