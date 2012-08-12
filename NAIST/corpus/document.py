#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""


__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.corpus.sentence
import corrcha.corpus.element
import corrcha.corpus.mistake

super = corrcha.corpus.element.Element
class Document(super):
    def __init__(self, metadata=None):
        super.__init__(self, metadata)
        self.__lines = [] #ParsedSentence
        self.__paragraphs = []
        self.__mistakes = corrcha.corpus.mistake.Mistakes()

    def append_paragraph(self, txts):
        assert type(txts) in [list, tuple]
        assert type(txts[0]) is corrcha.corpus.sentence.ParsedSentence

        self.__paragraphs.append( len( self.__lines ) )#store the line number where this paragraphs
        for txt in txts:
            self.__lines.append( txt )
    
    def get_sentence(self, lineid ):
        return self.__lines[lineid]

    def get_string(self, offset):
        assert isinstance(offset, (list,tuple))
        assert len(offset) == 2
        assert len(offset[0]) == 2
        assert len(offset[1]) == 2
        ((start_line_num, start_offset), (end_line_num, end_offset)) = offset
        if start_line_num == end_line_num:
            return self.__lines[ start_line_num ].getSurface()[start_offset:end_offset]
        elif start_line_num > end_line_num:
            raise KeyError
        else:
            ret = self.__lines[ start_line_num ].getSurface()[start_offset:]
            for i in range(start_line_num+1, end_line_num):
                ret += u"\n" + self.__lines[ i ].getSurface()
            ret += u"\n" + self.__lines[ end_line_num ].getSurface()[:end_offset]
            return ret


    def append_mistake(self, mistake):
        """Insert keeping the order of mistakes.
        Do after all paragraph is inserted."""
        assert type(mistake) is corrcha.corpus.mistake.Mistake
        assert len(self.__paragraphs) > 0, "Paragraphs have not inserted yet."

#        lineid  = mistake.offset[0][0]
#        if mistake.offset[0][1] == -1:
#            lineid -= 1
#        self.lines[ lineid ].append_mistake( mistake )
        self.__mistakes.append(mistake)

    def get_mistakes(self):
        for miss in self.__mistakes:
            yield miss

    def find_mistake(self, start_lineid, start_position, exact=True):
        return self.__mistakes.get(start_lineid, start_position, exact)

    def get_new_offset(self, old_par, old_off, tail=False):
        """Inputs are old_paragraph_id and offset.
        Returns new lineid and offset."""
        if old_par!=0 and old_off==0 and tail==True:
            if len(self.__paragraphs) <= old_par:
                line_num = len( self.__lines ) -1
            else:
                line_num = self.__paragraphs[ old_par ] -1
            new_off = len( self.__lines[line_num] ) #!
            return (line_num, new_off)
            
        head_line_num = self.__paragraphs[ old_par ]
        lnum = head_line_num
        length = 0
        while True:
            if len( self.__lines)==lnum: #reached the end of this document
                new_off = old_off - length
                #TODO is this ok?
                if new_off in ( 0, -1):
                    prev_line_length = len( self.__lines[ lnum-1 ] )
                    return ( lnum-1, prev_line_length)
                else:
                    print ( lnum, new_off)
                    raise

            this_line_length = len( self.__lines[ lnum ] )
            if length + this_line_length > old_off:
                new_off = old_off - length
                return ( lnum, new_off)
            else:
                length += this_line_length +1 #Consider the count of "\n" character
                lnum += 1


    def get_new_offsets(self, start_par, start_off, end_par, end_off):
        """Return 
        [ (start_line_num, start_offset), (end_line_num, end_offset)]
        """
        return [ self.get_new_offset(start_par, start_off), self.get_new_offset(end_par, end_off, True) ]

    def mark(self, offset):
        ((start_line_num, start_offset), (end_line_num, end_offset)) = offset
        if start_line_num != end_line_num:
            return u"*** Cross lines ***"
        else:
            return u"%s" % corrcha.corpus.sentence.mark( self.__lines[start_line_num].getSurface(), (start_offset, end_offset) )


    def __unicode__(self):
        dump = u""
        dump += unicode(super.__unicode__(self))
        dump += u"\n"
        dump += u"Paragraphs Head Line number:\t"
        dump += u"%s\n" % str(self.__paragraphs)

        dump += u"[Lines]\n"
        for p in self.__lines:
            dump += unicode(p)

        dump += u"[Mistakes]\n"
        for m in self.__mistakes :
            dump += self.mark(m.offset)
            dump += u"\n%s\n" % m
        return dump

    def __iter__(self):
        return iter(self.__lines)

    def __len__(self):
        return len(self.__lines)


import types
import copy
def getFixDocument(doc, dep_parser, keep_error=None):
    assert isinstance(doc, Document)
    assert keep_error is None or isinstance(keep_error, types.FunctionType)

    fixdoc = Document(doc.get_meta())
    rest_misses = corrcha.corpus.mistake.Mistakes()
    lines = []

    for lineid, sentence in enumerate(doc):
        fixline = u""

        oldline_leng = len(sentence.getSurface())
        surf = sentence.getSurface()
        pos = 0
        newmisses = corrcha.corpus.mistake.Mistakes()
        while pos <= len(surf):
            miss = doc.find_mistake(lineid, pos)
            if (miss is None):
                if (pos<len(surf)):
                    fixline += surf[pos]
                pos += 1
            else:
                #FIXME across several tokens.
                leng = len(miss.original)
                if (keep_error is None) or (not keep_error(miss)):
                    fixline += miss.corr
                else: #keep miss
                    start_pos = len(fixline)
                    fixline += miss.original
                    newmiss =  copy.deepcopy(miss)
                    newmiss.offset = ((lineid, start_pos),(lineid, len(fixline)))
                    newmisses.append(newmiss)

                if leng==0:
                    if (pos<len(surf)):
                        fixline += surf[pos] #add itself
                    pos += 1
                else:
                    pos += len(miss.original)

        #FIXME don't ferrite original paragraph info
        parsed_sentence = dep_parser.parse(fixline)
        fixdoc.append_paragraph([parsed_sentence])
        for m in newmisses:
            fixdoc.append_mistake(m)
    return fixdoc




if __name__=='__main__':
    pass

