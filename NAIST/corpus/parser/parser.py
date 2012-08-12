#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
parser template
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


class CorpusParser(object):
    def __init__(self):
        import corrcha.tool.setting
        _SENTENCE_TOKENIZE_MODEL = corrcha.tool.setting.val['corpus']['sentence_tokenize_model']

        import nltk.data
        self.tokenizer = nltk.data.load(_SENTENCE_TOKENIZE_MODEL) 

    def convert(self, path, number=None):
        """return Corpus """
        assert type(path) is unicode

        raise NotImplementedError
