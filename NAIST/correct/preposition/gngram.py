#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.corpus.document
import corrcha.corpus.sentence
import corrcha.corpus.mistake
import corrcha.correct.correcter

import corrcha.correct.feature
import corrcha.constant
import logging
import os
import corrcha.core.parser.default
import corrcha.corpus.mistake

import corrcha.corpus.util

class Correcter(corrcha.correct.correcter.Correcter):
    IDDIC_PATH = "/iddic"
    ORIGINAL_SURFACE = "ORIGINAL_SURFACE:"
    LEN_ORIGINAL_SURFACE = len(ORIGINAL_SURFACE)

    def __init__(self, model):
        assert (type(model) is type) or isinstance(model, (str, unicode))
        self.parser = corrcha.core.parser.default.getDefaultParser()

        INDEX_PATH = corrcha.tool.setting.val['ssgnc']['index']
        self.myagent = corrcha.tool.myssgnc.MyAgent(INDEX_PATH)

    def get_frequency(self, query):
#        print ">>", query,  self.myagent.get_frequency(query)
        return self.myagent.get_frequency(query)


    def extract(self, corpus, outfolder, param):
        """extract features and save them to file(s)"""
        assert isinstance(corpus, corrcha.corpus.corpus.Corpus)
        assert isinstance(outfolder, str)
        assert isinstance(param, str)
#        raise NotImplementedError
        return None


    def train(self, inputfolder, outfolder, param):
        assert isinstance(inputfolder, str) or isinstance(inputfolder, list)
        assert isinstance(outfolder, str)
        assert isinstance(param, str)
        corrcha.tool.util.mkdirs(outfolder)
#        raise NotImplementedError
        return None


    def test(self, inputfolder):
        assert isinstance(inputfolder, str)
        raise NotImplementedError
#        return orgs, gold, result, memos


    def check(self, doc, getOK=False, getGold=False):
        assert isinstance(doc, corrcha.corpus.document.Document)
        assert isinstance(getOK, bool)
        assert isinstance(getGold, bool)

        mistakes = []

        for lineid, sent in enumerate(doc):
            token_surfs = []
            for prep_id, token in enumerate(sent):
                token_surfs.append(token.getSurface())
            index_max = len(token_surfs)

            for prep_id, token in enumerate(sent):
                original = token.getSurface().lower()
                if not corrcha.constant.isPreposition(original):
                    continue

                ##predict
                #set context window
                left_start = prep_id -2
                right_end = prep_id + 3
                left_sp = ""
                right_sp = ""
                if left_start < 0:
                    right_end += (- left_start -1)
                    left_sp = "<S>"
                    left_start = 0
                if right_end >= index_max :
                    left_start = max(0, left_start - (right_end - index_max))
                    right_end = index_max
#                    right_sp = "</S>"

                left = " ".join(token_surfs[left_start:prep_id])
                right = " ".join(token_surfs[prep_id+1:right_end])


                query = "%s %s %s" % (left, original, right)
                if left_sp != "":
                    query = query.lstrip().capitalize()
                org_freq = self.get_frequency("%s %s %s" % (left_sp, query, right_sp))
                max_freq = org_freq
                label = original

                for p in corrcha.constant.PREPOSITIONS + [u""]:
                    if p == original:
                        continue
                    myquery = "%s %s %s" % (left, p, right)
                    if left_sp != "":
                        myquery = myquery.lstrip().capitalize()
                    myfreq = self.get_frequency("%s %s %s" % (left_sp, myquery, right_sp))
                    if myfreq > max_freq:
                        max_freq = myfreq
                        label = p

                t_pos = token.getPosition()
                t_len = len(original)
                position = ((lineid, t_pos), (lineid, t_pos + t_len))
                if original != label: #detect an errors
                    miss = corrcha.corpus.mistake.Mistake(position, ["PREP"], original, label, corrcha.corpus.mistake.Error.PREPOSITION)
                    mistakes.append(miss)
                elif getOK:
                    mistakes.append(None)
                else:
                    pass

        return mistakes
                

if __name__=='__main__':
    pass

