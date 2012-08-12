#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


      
import nltk 
class WordnetHelper(nltk.corpus.reader.wordnet.WordNetCorpusReader):

    def __init__(self):
        nltk.corpus.reader.wordnet.WordNetCorpusReader.__init__(self, nltk.data.find('corpora/wordnet')) 

    def getLexnames(self, word):
        assert isinstance(word, unicode)
        lexnames = set([])
        word = word.encode('utf-8', 'ignore')
        try:
            synsets = nltk.corpus.reader.wordnet.WordNetCorpusReader.synsets(self, word)
        except:
            return lexnames
        for d in synsets:
            lexnames.add(d.lexname)
        return lexnames
        
if __name__ == '__main__':
    wh = WordnetHelper()

    print wh.getLexnames(u'tomato')
    print wh.getLexnames(u'japan')
    print wh.getLexnames(u'drive')

