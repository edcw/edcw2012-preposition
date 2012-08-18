
#pragma warning disable 1591
namespace Ototake.Edcw.Lexicon
{
    /// <summary>
    /// WordNet Lexicographer．
    /// </summary>
    public enum LexicographerTypes
    {
        adj_all = 0,
        adj_pert = 1,
        adv_all = 2,
        noun_Tops = 3,
        noun_act = 4,
        noun_animal = 5,
        noun_artifact = 6,
        noun_attribute = 7,
        noun_body = 8,
        noun_cognition = 9,
        noun_communication = 10,
        noun_event = 11,
        noun_feeling = 12,
        noun_food = 13,
        noun_group = 14,
        noun_location = 15,
        noun_motive = 16,
        noun_object = 17,
        noun_person = 18,
        noun_phenomenon = 19,
        noun_plant = 20,
        noun_possession = 21,
        noun_process = 22,
        noun_quantity = 23,
        noun_relation = 24,
        noun_shape = 25,
        noun_state = 26,
        noun_substance = 27,
        noun_time = 28,
        verb_body = 29,
        verb_change = 30,
        verb_cognition = 31,
        verb_communication = 32,
        verb_competition = 33,
        verb_consumption = 34,
        verb_contact = 35,
        verb_creation = 36,
        verb_emotion = 37,
        verb_motion = 38,
        verb_perception = 39,
        verb_possession = 40,
        verb_social = 41,
        verb_stative = 42,
        verb_weather = 43,
        adj_ppl = 44
    }

    /// <summary>
    /// Synsetタイプを表します．
    /// </summary>
    public enum SynsetType
    {
        Noun = 1,
        Verb = 2,
        Adjective = 3,
        AdjectiveSatellite = 5,
        Adverb = 4,
        Adjective_AdjectiveSatellite = 99
    }
}
#pragma warning restore 1591