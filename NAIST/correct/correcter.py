#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


import corrcha.core.classify.data
import corrcha.correct.result

import corrcha.tool.serialize
import types

import logging

class Correcter(object):

    def __init__(self, model):
        assert (type(model) is type) or isinstance(model, (str, unicode))
        raise NotImplementedError

    def extract(self, corpus, outfolder, param):
        """extract features and save them to intermediate file(s)"""
        assert isinstance(corpus, corrcha.corpus.corpus.Corpus)
        assert isinstance(outfolder, str)
        assert isinstance(param, str)
        raise NotImplementedError

    def train(self, inputfolder, outfolder, param):
        """Train with intermediate file(s)"""
        assert isinstance(inputfolder, str) or isinstance(inputfolder, list)
        assert isinstance(outfolder, str)
        assert isinstance(param, str)
        raise NotImplementedError


    def test(self, inputfolder):
        """Test with intermediate file(s)"""
        assert isinstance(inputfolder, str)
        raise NotImplementedError

    def check(self, doc, getOK=False, getGold=False):
        """Raise mistakes from Document. 
        When getOK is True, this also raises None.
        When getGold is True this returns gold annotation of mistakes."""
        assert isinstance(doc, corrcha.corpus.document.Document)
        assert isinstance(getOK, bool)
        assert isinstance(getGold, bool)
        raise NotImplementedError


def getCorrecterClass(model_dir):
    import corrcha.tool.util
    import corrcha.constant
    import json
    try:
        info_name = model_dir + '/' + corrcha.constant.INFO_NAME

        f = open(info_name, 'r')
        info_dic = json.load(f)
        f.close()

        correcter_class_path = info_dic[corrcha.constant.CORRECTER_CLASS_PATH]
    except:
        logging.error("Loading error at %s" % info_name)
        quit()

    if correcter_class_path:
        correcter_class = corrcha.tool.util.getClass(correcter_class_path)
        return correcter_class
    else:
        logging.error("Can't load %s !" % correcter_class_path)
        quit()

