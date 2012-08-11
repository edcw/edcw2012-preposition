using System.Collections.Generic;

namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// WordNet語彙集合を表す．
    /// </summary>
    public interface ILexicon
    {
        /// <summary>
        /// 登録されているSynsetの数を取得する．
        /// </summary>
        int SynsetCount { get; }

        /// <summary>
        /// 指定したディレクトリからWordNetデータベースファイルを読み込む．
        /// </summary>
        /// <param name="dir">WordNet DBファイルの配置ディレクトリ</param>
        void LoadFromDir(string dir);

        /// <summary>
        /// 指定したZipファイルからWordNetデータベースファイルを読み込む．
        /// </summary>
        /// <param name="zipPath"></param>
        void LoadFromZip(string zipPath);

        /// <summary>
        /// 指定した品詞，単語のLemmaを取得する．
        /// </summary>
        /// <param name="type">Synsetの種類</param>
        /// <param name="baseform">単語の原形</param>
        /// <param name="adjectiveMerge">Adjectiveについて Adjective Satellite を検索対象に加える．</param>
        /// <returns></returns>
        ILemma GetLemma(SynsetType type, string baseform, bool adjectiveMerge = true);

        /// <summary>
        /// 指定したSynsetTypeに含まれるすべてのSynsetを列挙する．
        /// </summary>
        /// <param name="type"></param>
        /// <param name="adjectiveMerge"></param>
        /// <returns></returns>
        IEnumerable<ISynset> AllSynsets(SynsetType type, bool adjectiveMerge = true);

        /// <summary>
        /// すべてのSynsetを列挙する．
        /// </summary>
        /// <returns></returns>
        IEnumerable<ISynset> AllSynsets();

        /// <summary>
        /// 指定したSynsetTypeの単語formを見出し語化する．不可能な場合は原形をそのまま返する．
        /// </summary>
        /// <param name="form"></param>
        /// <param name="type"></param>
        /// <returns></returns>
        string Lemmatize(string form, SynsetType? type);

        /// <summary>
        /// 指定したSynsetTypeの単語formを見出し語化し，可能なすべての見出し語を取得する．
        /// </summary>
        /// <param name="form"></param>
        /// <param name="type"></param>
        /// <returns></returns>
        IEnumerable<string> EachLemmatize(string form, SynsetType? type);
    }
}
