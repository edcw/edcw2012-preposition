#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


import corrcha.corpus.sentence

class Parser(object):
    def __init__(self):
        raise NotImplementedError

    def __unicode__(self):
        raise NotImplementedError

    def parse(self, sentence):
        assert type(sentence) is unicode
        raise NotImplementedError
        

if __name__=='__main__':
    pass
