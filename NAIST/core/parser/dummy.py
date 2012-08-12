#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.core.singleton

import corrcha.corpus.sentence
import corrcha.corpus.token
import corrcha.core.parser.parser

class DummyParser(corrcha.core.parser.parser.Parser):
    __metaclass__= corrcha.core.singleton.Singleton

    def __init__(self):
        pass


    def __unicode__(self):
        buf = u"This is dummy parser."
        return buf


    def parse(self, sentence):
        assert type(sentence) is unicode
        parsed_sentence = corrcha.corpus.sentence.ParsedSentence(sentence)
        tokenized = sentence.split(u" ")

        position = 0
        if len(sentence) != 0:
            for i, t in enumerate(tokenized):
                #tokens[i].endPosition() )
                parsed_token = corrcha.corpus.token.Token(i, t, position )
                parsed_sentence.append(parsed_token)
                position += len(t)

        return parsed_sentence

if __name__=='__main__':
    pass


