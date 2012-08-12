#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


import corrcha.corpus.document

def getPrepDepAndGov(parsed_sentence, target_token_id):
    """Return designated preposition's from and to token ids"""
    assert isinstance(parsed_sentence, corrcha.corpus.sentence.ParsedSentence)
    assert isinstance(target_token_id, int)

    target_token = parsed_sentence.getToken(target_token_id)

    #FROM
#    print target_token.getDependedTokenIds(), parsed_sentence.getSurface()
    from_id = None
    if len(target_token.getDependedTokenIds()) == 0:
        pass
    else:
        for id in target_token.getDependedTokenIds():
            if id > 0:
                from_id = id #XXX OK to return the first id?
                break

    #TO
    rels = target_token.getRelations()
    to_id = None
    if len(rels) == 0:
        if from_id:
            from_token = parsed_sentence.getToken(from_id)
            #something depends the verb?
            #The verb may have 
            # This is the [place|rcmod] which [you|nsubj] <go>  to.
            for id in from_token.getDependedTokenIds():
                if id >0:
                    token = parsed_sentence.getToken(id)
                    #TODO restrict the relation??? for instance [u'infmod', u'rcmod', u'prt']
#                rel = token.getRelations()[from_id]
                    return from_id, id

            #the verb has subj?
            for id, rel in from_token.getRelations().items():
                if rel.endswith(u'subj'):
#                    print rel, from_id, id, parsed_sentence.getSurface()
                    return from_id, id


    else:
        for id in rels:
            if id > 0:
                to_id = id
                break

    return from_id, to_id


def getPrepDepAndGovFromMiss(miss, doc, parser):
    """Return Tokens' id which govern the preposition and is governed by it
        I went <NONE|to> Osaka.         => went, Osaka
        I went there <with|by> a big bus.     => went, bus
        We discussed <about|NONE> it.   => discussed, it
    """
    assert isinstance(doc, corrcha.corpus.document.Document)

    sent = doc.get_sentence(miss.getLineID())
    if len(miss.original) == 0:
        original_text = sent.getSurface()
        miss_position = miss.offset[0][1]
        corrected_text = original_text[:miss_position] + u" " + miss.corr + u" " + original_text[miss_position:]
        corr_len = len(miss.corr)

        parsed_sentence = parser.parse(corrected_text)
        inserted_token_id = parsed_sentence.getTokenIdByPosition(miss_position + 1)

        from_id, to_token_id = getPrepDepAndGov(parsed_sentence, inserted_token_id)

        if from_id is None:
            from_org_token_id = None
        else:
            from_token = parsed_sentence.getToken(from_id)
            from_pos = from_token.getPosition()

            if from_pos > miss_position:
                from_pos -= (corr_len + 2)
            from_org_token_id = sent.getTokenIdByPosition(from_pos)

        if to_token_id is None:
            return from_org_token_id, None

        to_token = parsed_sentence.getToken(to_token_id)
        to_pos = to_token.getPosition()

        if to_pos > miss_position:
            to_pos -= (corr_len + 2)
        to_org_token_id = sent.getTokenIdByPosition(to_pos)
        return from_org_token_id, to_org_token_id

    else:
        miss_position = miss.offset[0][1]
        target_token_id = sent.getTokenIdByPosition(miss_position)
        if target_token_id is None:
            raise
        return getPrepDepAndGov(sent, target_token_id)



def getArgs(sentence, target_token_id):
    """"return {original_relation : (argid, relation_id)}
    TODO This order OK?? """
    assert isinstance(sentence, corrcha.corpus.sentence.ParsedSentence)
    assert isinstance(target_token_id, int)

    args = {}
    tmp_infmod = None

    target_token = sentence.getToken(target_token_id)

    for id in target_token.getDependedTokenIds():
        if id >0:
            token = sentence.getToken(id)
            rel = token.getRelations()[target_token_id]
            if rel == u'rcmod':
                args[rel] = (id, None)
            elif rel == u'infmod':
                tmp_infmod = id
#            elif rel in [u'ccomp']
#                args[u'ccomped'] = (id, None)

    for argid, rel in target_token.getRelations().items():
        if rel in [u"prt", u"prep"]:
            prep_id = argid

            from_id, to_token_id = getPrepDepAndGov(sentence, prep_id)
            if (from_id is not None) and (to_token_id is not None):
                prep_surf = sentence.getToken(prep_id).getSurface()
                args[prep_surf] = (to_token_id, prep_id)

                if tmp_infmod == to_token_id: #remove
                    tmp_infmod = None

        elif rel in [u"rel"]:
            pass
        else:
            args[rel] = (argid, None)

    if tmp_infmod: #still alive
        args[u'infmod'] = (tmp_infmod, None)

    return args

if __name__=='__main__':
    pass

