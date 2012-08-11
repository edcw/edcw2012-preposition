using System.IO;
using Ototake.Edcw.Lexicon;
using Ototake.Edcw.OpenNlp.Wrapper;

namespace Ototake.Edcw.OpenNlp
{
    /// <summary>
    /// 解析のためのリソース群
    /// </summary>
    public class DocumentResources
    {
        #region 定数
        public const string DefaultTokenizerModelFile = @"en-token.bin";
        public const string DefaultSentenceModelFile = @"en-sent.bin";
        public const string DefaultPosModelFile = @"en-pos-maxent-fix.bin";
        public const string DefaultChunkerModelFile = @"en-chunker.bin";
        public const string DefaultLexiconFile = @"wn.zip";
        #endregion

        #region プロパティ
        public TokenizerModel TokenizerModel { get; private set; }
        public SentenceModel SentenceModel { get; private set; }
        public POSModel POSModel { get; private set; }
        public ChunkerModel ChunkerModel { get; private set; }
        public ILexicon Lexicon { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// 指定したディレクトリからデフォルトのファイルを使用してモデルを構築する．
        /// </summary>
        /// <param name="dir"></param>
        public DocumentResources(string dir)
        {
            this.TokenizerModel = new TokenizerModel(Path.Combine(dir, DefaultTokenizerModelFile));
            this.SentenceModel = new SentenceModel(Path.Combine(dir, DefaultSentenceModelFile));
            this.POSModel = new POSModel(Path.Combine(dir, DefaultPosModelFile));
            this.ChunkerModel = new ChunkerModel(Path.Combine(dir, DefaultChunkerModelFile));
            this.Lexicon = global::Ototake.Edcw.Lexicon.Lexicon.CreateFromZip(Path.Combine(dir, DefaultLexiconFile));
        }
        #endregion
    }

    /// <summary>
    /// 解析器集合
    /// </summary>
    public class DocumentAnalyzer
    {
        #region プロパティ
        public Tokenizer Tokenizer { get; private set; }
        public SentenceDetector SentenceDetector { get; private set; }
        public POSTagger POSTagger { get; private set; }
        public Chunker Chunker { get; private set; }
        public ILexicon Lexicon { get; private set; }
        #endregion

        #region コンストラクタ
        /// <summary>
        /// モデル集合を用いて解析器集合インスタンスを生成．
        /// </summary>
        /// <param name="resources"></param>
        public DocumentAnalyzer(DocumentResources resources)
        {
            this.Tokenizer = new Tokenizer(resources.TokenizerModel);
            this.SentenceDetector = new SentenceDetector(resources.SentenceModel);
            this.POSTagger = new POSTagger(resources.POSModel);
            this.Chunker = new Chunker(resources.ChunkerModel);
            this.Lexicon = resources.Lexicon;
        }
        #endregion

        #region メソッド
        /// <summary>
        /// 指定した文字列（文章）から解析済みドキュメントを作成する．
        /// </summary>
        /// <param name="s">解析する文章</param>
        /// <returns></returns>
        public Document CreateDocument(string s)
        {
            // 文分割
            var sents = SentenceDetector.Detect(s);

            // ドキュメント付属のSentence配列
            var rsents = new Sentence[sents.Length];

            for (int i = 0; i < sents.Length; i++)
            {
                // トークン分割
                var tokens = Tokenizer.Tokenize(sents[i]);

                // POS
                var pos = POSTagger.Tag(tokens);

                // Chunk
                var chk = Chunker.Chunk(tokens, pos);

                // Word,Chunk配列
                var words = Word.CreateWords(this.Lexicon, tokens, pos);
                var chunks = Chunk.CreateChunks(chk, words);

                rsents[i] = new Sentence(chunks, words, sents[i]);
            }

            return new Document(rsents);
        }
        #endregion
    }
}
