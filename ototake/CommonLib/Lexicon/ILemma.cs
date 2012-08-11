using System.Collections.Generic;

namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// WordNetの見出し語を表す．
    /// </summary>
    public interface ILemma
    {
        /// <summary>
        /// 見出し語文字列．
        /// </summary>
        string Text { get; }

        /// <summary>
        /// 品詞．
        /// </summary>
        SynsetType Pos { get; }

        /// <summary>
        /// 見出し語に付与されたSenseリスト．
        /// </summary>
        List<ISense> Senses { get; }

        /// <summary>
        /// 指定したインデックスのSenseを取得する．
        /// </summary>
        /// <param name="i"></param>
        /// <returns></returns>
        ISense this[int i] { get; }

        /// <summary>
        /// 指定したSynsetを持つSenseを取得する．
        /// ない場合はnullを返す．
        /// </summary>
        /// <param name="synset"></param>
        /// <returns></returns>
        ISense this[ISynset synset] { get; }
    }

    /// <summary>
    /// 一つの意味の単位を表す．
    /// </summary>
    public interface ISense
    {
        /// <summary>
        /// 見出し語を取得する．
        /// </summary>
        ILemma Lemma { get; }

        /// <summary>
        /// LexIdを取得する．
        /// </summary>
        byte LexId { get; }

        /// <summary>
        /// 関連付けられたSynsetを取得する．
        /// </summary>
        ISynset Synset { get; }

        /// <summary>
        /// HeadWord
        /// </summary>
        string HeadWord { get; }

        /// <summary>
        /// Sense Number
        /// </summary>
        int SenseNo { get; }

        /// <summary>
        /// 指定した引数に関連するSense集合を取得する．
        /// </summary>
        /// <param name="relation"></param>
        /// <returns></returns>
        ISense[] RelatedSenses(RelationType relation);
    }
}
