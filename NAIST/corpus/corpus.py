#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.corpus.element

super = corrcha.corpus.element.Element
class Corpus(super):
    def __init__(self, metadata={}):
        super.__init__(self, metadata)
        self.__documents = []

    def append(self, document):
        self.__documents.append(document)

    def __iter__(self):
        return iter(self.__documents)

    def __len__(self):
        return len(self.__documents)

    def __getitem__(self, key):
        return self.__documents[key]

    def __delitem__(self, key):
        del self.__documents[key]

    def shuffle(self, seed=None):
        import random
        random.seed(seed)
        random.shuffle(self.__documents)

    def __unicode__(self):
        ret = u""
        ret += unicode(self.get_meta())
        ret += u"\n"
        for d in self.__documents:
            ret += "============\n"
            ret += unicode(d)
            ret += "============\n"
        return ret

if __name__=='__main__':
    pass

