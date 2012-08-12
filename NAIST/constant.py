#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""
__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


import logging
import corrcha.tool.logger
import corrcha.corpus.corpus
import corrcha.tool.serialize

FEATURE_ID_MAP_NAME="feature_id_map"
INFO_NAME="info"
HANDLING_ERROR = "HANDLING_ERROR"
CORRECTER_CLASS_PATH = "CORRECTER_CLASS"



CORRECTER = {
        'prep-rep' : 'corrcha.correct.preposition.replace.Correcter'   ,
        'prep-gn' : 'corrcha.correct.preposition.gngram.Correcter'   ,
}

import corrcha.core.classify.multi
CLASSIFIER={
#    None: corrcha.core.classify.binary.LibLinear ,
    None: corrcha.core.classify.multi.Maxent ,
    'maxent': corrcha.core.classify.multi.Maxent ,
}


ARTICLE = [u"a", u"an", u"the"]
DETERMINER = [u"a", u"an", u"the" \
        , u"this", u"that", u"these", u"those" \
        , u"my", u"your", u"his", u"her", u"its", u"our", u"their", u"its" \
        , u"what", u"which", u"whose" \
        , u"no" \
        , u"some" \
        ]

PREPOSITIONS = [u'of', u'in', u'for', u'to', u'by', u'with', u'at', u'on', u'from', u'as', u'about', u'since']

def isPreposition(surf):
    return surf.lower() in PREPOSITIONS

def isVerb(node):
    return node.getTag().startswith(u'VB')




