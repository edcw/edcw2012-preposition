using System;
using System.Linq;
using Ototake.Edcw.Lexicon;

namespace Ototake.Edcw.OpenNlp
{
    /// <summary>
    /// 単語を表す．
    /// </summary>
    public sealed class Word
    {
        #region フィールド
        private ILexicon lexicon;
        private ILemma lemma;
        private string baseform;
        #endregion

        #region プロパティ
        /// <summary>
        /// 親チャンクを取得する．
        /// </summary>
        public Chunk Parent { get; internal set; }

        /// <summary>
        /// 単語文字列を取得する．
        /// </summary>
        public string Text { get; private set; }

        /// <summary>
        /// POSタグを取得する．
        /// </summary>
        public string Tag { get; private set; }

        /// <summary>
        /// 原形を取得する．
        /// </summary>
        public string BaseForm
        {
            get
            {
                if (baseform == null)
                    baseform = ComputeBaseForm();
                return baseform;
            }
        }

        /// <summary>
        /// Sentence中のインデックスを取得する．
        /// </summary>
        public int IndexInSentence { get; private set; }

        /// <summary>
        /// Chunk中のインデックスを取得する．
        /// </summary>
        public int IndexInChunk { get; set; }

        /// <summary>
        /// WordNetのLemmaを取得する．
        /// </summary>
        public ILemma Lemma
        {
            get
            {
                if (lemma == null)
                    lemma = ComputeLemma();
                return lemma;
            }
        }

        /// <summary>
        /// Chunk内の次の単語があれば取得
        /// </summary>
        public Word NextInChunk
        {
            get
            {
                if (IndexInChunk >= Parent.Count - 1) return null;
                return Parent[IndexInChunk + 1];
            }
        }

        /// <summary>
        /// Chunk内の前の単語があれば取得
        /// </summary>
        public Word PrevInChunk
        {
            get
            {
                if (IndexInChunk <= 0) return null;
                return Parent[IndexInChunk - 1];
            }
        }

        /// <summary>
        /// Sentence内の次の単語があれば取得
        /// </summary>
        public Word NextInSentence
        {
            get
            {
                if (IndexInSentence >= Parent.ParentSentence.Words.Length - 1)
                    return null;
                return Parent.ParentSentence[IndexInSentence + 1];
            }
        }

        /// <summary>
        /// Sentence内の前の単語があれば取得
        /// </summary>
        public Word PrevInSentence
        {
            get
            {
                if (IndexInSentence <= 0) return null;
                return Parent.ParentSentence[IndexInSentence - 1];
            }
        }
        #endregion

        #region コンストラクタ
        internal Word(ILexicon lex, int index, string token, string tag)
        {
            this.lexicon = lex;
            this.IndexInSentence = index;
            this.Text = token;
            this.Tag = tag;
        }

        internal static Word[] CreateWords(ILexicon lex, string[] token, string[] pos)
        {
            if (token.Length != pos.Length)
                throw new ArgumentException("token と posの配列サイズが一致していない．");

            var ret = new Word[token.Length];
            for (int i = 0; i < ret.Length; i++)
            {
                ret[i] = new Word(lex, i, token[i], pos[i]);
            }

            return ret;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 文字列表現は単語とPOSタグのペア．
        /// </summary>
        /// <returns></returns>
        public override string ToString()
        {
            return string.Format("{0}/{1}", Text, Tag);
        }

        /// <summary>
        /// Text/Tag 形式文字列からインスタンス復元．
        /// </summary>
        /// <param name="index">インデックス番号</param>
        /// <param name="str">文字列</param>
        /// <param name="lex">レキシコン</param>
        /// <returns></returns>
        public static Word FromString(ILexicon lex, string str, int index)
        {
            var tmp = str.Split('/');
            if (tmp.Length < 2) throw new Exception("不正な形式: " + str);
            return new Word(lex, index, tmp[0], tmp.Last());
        }
        #endregion

        #region プライベートメソッド
        /// <summary>
        /// レキシコンによって原形を推測します
        /// </summary>
        /// <param name="parser">解析器</param>
        /// <returns></returns>
        private string ComputeBaseForm()
        {
            var ret = lexicon.Lemmatize(Text, Tag.GetSynsetType());
            return ret;
        }

        /// <summary>
        /// レキシコンによってLemmaを推測します
        /// </summary>
        /// <param name="lexicon">WordNetレキシコン</param>
        /// <returns></returns>
        private ILemma ComputeLemma()
        {
            var synsetType = this.Tag.GetSynsetType();
            if (!synsetType.HasValue) return null;
            var ret = lexicon.GetLemma(synsetType.Value, BaseForm);
            return ret;
        }
        #endregion

    }
}
