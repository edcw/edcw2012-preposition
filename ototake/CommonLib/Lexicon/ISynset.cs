using System.Collections.Generic;

namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// WordNet Synsetを表す．
    /// </summary>
    public interface ISynset
    {
        /// <summary>
        /// Synsetを識別するIDを取得する．
        /// </summary>
        int Id { get; }

        /// <summary>
        /// Synsetのカテゴリを取得する．
        /// </summary>
        LexicographerTypes LexCategory { get; }

        /// <summary>
        /// Synsetタイプを取得する．
        /// </summary>
        SynsetType Type { get; }

        /// <summary>
        /// このSynsetをもつSenseリストを取得する．
        /// </summary>
        IList<ISense> Senses { get; }

        /// <summary>
        /// 定義文を取得する．
        /// </summary>
        string Definition { get; }

        /// <summary>
        /// 例文集合を取得する．
        /// </summary>
        string[] Examples { get; }

        /// <summary>
        /// 指定した引数に関連するSynset集合を取得する．
        /// </summary>
        /// <param name="relationType"></param>
        /// <returns></returns>
        ISynset[] RelatedSynsets(RelationType relationType);

        /// <summary>
        /// 指定した引数に関連するSense集合を取得する．
        /// </summary>
        /// <param name="relationType"></param>
        /// <param name="sourceSense"></param>
        /// <returns></returns>
        ISense[] RelatedSenses(RelationType relationType, ISense sourceSense);
    }
}
