#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

#PREPOSITIONS = ['of', 'in', 'for', 'to', 'by', 'with', 'at', 'on', 'from', 'as', 'about', 'since']

class Error(object):
    OTHER, PRONOUN, CONJUNCTION, DETERMINER, ADJECTIVE, NOUN, \
            QUANTIFIER, PREPOSITION, VERB, ADVERB, \
            PUNCTUATION, \
            SPELL, AUXILIARY, RELATIVE, INTERROGATIVE = range(15)

class Correct(object):
    OTHER, NOCHANGE, INSERT, DELETE, REPLACE = range(5)

class Mistake(object):

    def __init__(self, offset, kind, original, corr, error_type = Error.OTHER):
        assert type(offset) in [list, tuple ]
        assert len(offset) == 2
        assert len(offset[0]) == 2
        assert len(offset[1]) == 2
        if type(kind) is unicode:
            kind = [kind]
        assert type(kind) in [list, tuple ]
        assert type(original) is unicode
        assert type(corr) is unicode

        self.offset = offset
        self.kind = kind #the tags annotated in the original dataset.
        self.corr = corr
        self.corr_type = Correct.OTHER
        self.error_type = error_type
        self.original =  original
        
        if self.original == u"*": #FIXME
            self.corr_type = Correct.OTHER
            return

        if self.corr == self.original:
            self.corr_type = Correct.NOCHANGE
        elif self.corr == u"":
            self.corr_type = Correct.DELETE
        elif self.original == u"":
            self.corr_type = Correct.INSERT
        else:
            self.corr_type = Correct.REPLACE

    def getLineID(self):
        #FIXME what should we do when end point is in a different line
        return self.offset[0][0]

    def __str__(self):
        dump = ""
        dump += "CORR_TYPE: %d\n" % ( self.corr_type )
        dump += "ERR_TYPE: %d\n" % ( self.error_type ) 
        dump += "KIND: %s\n" % ( self.kind ) 
        dump += "%s\n" % ( str(self.offset) )
        dump += "ORGINAL : [%s]\n" % (self.original)
        dump += "%s : [%s]\n" % (self.kind, self.corr)
        return dump

#    def __eq__(self, other):
#        return (self.offset[0][0] == other.offset[0][0])    \
#                and (self.offset[0][1] == other.offset[0][1])

    def __lt__(self, other):
        """This returns whether start point is former than another or not."""
        if self.offset[0][0] < other.offset[0][0]:
            return True
        elif self.offset[0][0] == other.offset[0][0]:
            return ( self.offset[0][1] < other.offset[0][1] )
        else:
            return False


    def __gt__(self, other):
        """This returns whether start point is former than another or not."""
        if self.offset[0][0] > other.offset[0][0]:
            return True
        elif self.offset[0][0] == other.offset[0][0]:
            return ( self.offset[0][1] > other.offset[0][1] )
        else:
            return False


import bisect #to keep the order of mistakes
class Mistakes(object):
    def __init__(self):
        self.__mistakes = []

    def append(self, mistake):
        """Insert keeping the order of mistakes"""
        assert type(mistake) is Mistake
        bisect.insort_left( self.__mistakes ,mistake )


    def get(self, start_lineid, start_position, exact=True):
        'Locate the leftmost value exactly equal to (start_lineid, start_position)'

        tmp_miss = type('', (), {
                'offset': [[start_lineid, start_position]],
              })
        miss_index = bisect.bisect_left(self.__mistakes, tmp_miss)
        if miss_index == len(self.__mistakes):
            return None
        miss = self.__mistakes[miss_index]
        if (start_lineid == miss.offset[0][0]):
            if exact:
                if start_position == miss.offset[0][1]:
                    return miss
            else:
                return miss
        return None

    def __iter__(self):
        return iter(self.__mistakes)

    def __len__(self):
        return len(self.__mistakes)

    def __getitem__(self, key):
        return self.__mistakes[key]



if __name__=='__main__':
    pass

