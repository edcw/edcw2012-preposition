
#pragma warning disable 1591
namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// WordNetのSynset間の関係を表す．
    /// </summary>
    public enum RelationType
    {
        Hypernym = 0,
        Hyponym = 1,
        InstanceHypernym = 2,
        InstanceHyponym = 3,
        IsPart = 4,
        HasPart = 5,
        IsMember = 6,
        HasMember = 7,
        IsSubstance = 8,
        HasSubstance = 9,
        Attribute = 10,
        VerbGroup = 11,
        VerbEntailment = 12,
        VerbCause = 13,
        AdjectiveSimilar = 14,
        HasTopicDomain = 15,
        IsTopicDomain = 16,
        HasRegionDomain = 17,
        IsRegionDomain = 18,
        HasUsageDomain = 19,
        IsUsageDomain = 20,
        SeeAlso = 21,
        AdjectiveParticiple = 22,
        Antonym = 23,
        Pertainym = 24,
        Derivation = 25,
        AdjectiveClusterHead = 26,
        AdjectiveClusterMember = 27,
    }

    /// <summary>
    /// VerbFrameType
    /// </summary>
    public enum VerbFrameType
    {
        None = 0,
        SomethingVerb = 1,
        SomebodyVerb = 2,
        ItIsVerbing = 3,
        SomethingIsVerbingPP = 4,
        SomethingVerbSomethingAdjectiveOrNoun = 5,
        SomethingVerbAdjectiveOrNoun = 6,
        SomebodyVerbAdjective = 7,
        SomebodyVerbSomething = 8,
        SomebodyVerbSomebody = 9,
        SomethingVerbSomebody = 10,
        SomethingVerbSomething = 11,
        SomethingVerbToSomebody = 12,
        SomebodyVerbOnSomething = 13,
        SomebodyVerbSomebodySomething = 14,
        SomebodyVerbSomethingToSomebody = 15,
        SomebodyVerbSomethingFromSomebody = 16,
        SomebodyVerbSomebodyWithSomething = 17,
        SomebodyVerbSomebodyOfSomething = 18,
        SomebodyVerbSomethingOnSomebody = 19,
        SomebodyVerbSomebodyPP = 20,
        SomebodyVerbSomethingPP = 21,
        SomebodyVerbPP = 22,
        SomebodysBodyPartVerb = 23,
        SomebodyVerbSomebodyToInfinitive = 24,
        SomebodyVerbSomebodyInfinitive = 25,
        SomebodyVerbThatClause = 26,
        SomebodyVerbToSomebody = 27,
        SomebodyVerbToInfinitive = 28,
        SomebodyVerbWhetherInfinitive = 29,
        SomebodyVerbSomebodyIntoVerbingSomething = 30,
        SomebodyVerbSomethingWithSomething = 31,
        SomebodyVerbInfinitive = 32,
        SomebodyVerbVerbing = 33,
        ItVerbThatClause = 34,
        SomethingVerbInfinitive = 35,
    }
}
#pragma warning restore 1591