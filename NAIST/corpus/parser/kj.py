#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


import corrcha.corpus.document as Document
import corrcha.corpus.mistake as Mistake
import corrcha.corpus.parser.parser as parser
import corrcha.corpus.corpus
import corrcha.core.parser


import corrcha.constant

class KJParser(parser.CorpusParser):

    TAGSET = { \
        u'n' : Mistake.Error.NOUN,  \
        u'v' : Mistake.Error.VERB,  \
        u'mo' : Mistake.Error.AUXILIARY,    \
        u'aj' : Mistake.Error.ADJECTIVE,    \
        u'av' : Mistake.Error.ADVERB,    \
        u'prp' : Mistake.Error.PREPOSITION, \
        u'at' : Mistake.Error.DETERMINER, \
        u'pn' : Mistake.Error.PRONOUN, \
        u'con' : Mistake.Error.CONJUNCTION, \
        u'rel' : Mistake.Error.RELATIVE, \
        u'itr' : Mistake.Error.INTERROGATIVE, \
        u'o' : Mistake.Error.OTHER, \
        u'ord' : Mistake.Error.OTHER, \
        u'uk' : Mistake.Error.OTHER, \
        }


    def __init__(self, dep_parser):
        parser.CorpusParser.__init__(self)
        self.dep_parser = dep_parser

    def tagname2miss(self, name):
        assert isinstance(name, unicode)
        key = name.split("_")[0] #XXX not use [1]???
        return self.TAGSET.get(key, Mistake.Error.OTHER)

    def _getMiss(self, lineid, ERR_start_pos, line, now_pos):
        assert isinstance(lineid, int)
        assert isinstance(ERR_start_pos, int)
        assert isinstance(line, unicode)
        assert isinstance(now_pos, int)

        pos = now_pos
        while line[pos] not in [u" ", u">"]:
            pos += 1

        tagname = line[now_pos+1:pos]
        errtype = self.tagname2miss(tagname)

        #-------------------------------------------
        # get corr
#        corr = None
        corr = u""
        old_pos = pos
        while line[pos] != u">":
            pos += 1
        pos += 1
        #ch: chain, com:comment
        attrs = line[old_pos:pos].split("\"")
        flg = False
        for attr in attrs:
            if flg:
                corr = attr
                break
            elif attr.endswith(u"crr="):
                flg = True
        #-------------------------------------------
        org = None
        old_pos = pos

        innnertag = 0
        tail = None
        while not (line[pos] == u"<" and line[pos+1] == u"/"):
            if line[pos] == u"<" and line[pos+1] != u"/":
                innnertag += 1
            pos += 1
        tmp = pos
        while line[tmp] != u">":
            tmp -= 1
        org = line[tmp+1:pos]
        while line[pos] != u">":
            pos += 1
            if line[pos] == u">" and innnertag != 0:
                innnertag -= 1
                pos += 1
        pos += 1

        offset = [[lineid, ERR_start_pos], [lineid, ERR_start_pos + len(org)]]
        miss = corrcha.corpus.mistake.Mistake(offset, [""], org, corr, errtype)
#        print unicode(miss)
        return org, miss, pos


    def getDoc(self, fname):
        assert type(fname) is str
    
        doc = corrcha.corpus.document.Document()
        misses = []
        for lineid, line in enumerate(open(fname, 'r')):
            line_tmp = unicode(line.rstrip())
            line = u""
            len_line_tmp = len(line_tmp)
            pos = 0
            while pos < len_line_tmp:
                c = line_tmp[pos]
                if c == u"<":
                    org, miss, pos  = self._getMiss(lineid, len(line), line_tmp, pos)
                    line += org
                    misses.append(miss)
                else:
                    line += c
                    pos += 1
                ps = self.dep_parser.parse(line)
            doc.append_paragraph([ps])

        for miss in misses:
            doc.append_mistake(miss)
        return doc

    def convert(self, path, number=None):
        """return parsed corpus """
        assert type(path) is str
        if number:
            raise NotImplementedError

        import os, sys

        corpus = corrcha.corpus.corpus.Corpus() #Add parsed data to this
        for k, fname in enumerate(os.listdir(path)):
            if fname in [u"58-60-5.txt"]:
                continue
            sys.stderr.write("\rProcessing... %s [%04d]" % (fname.ljust(15), k))
            sys.stderr.flush()
            doc = self.getDoc("%s/%s" % (path, fname))
            doc.set_meta('id', fname)
            corpus.append(doc)

        yield corpus


if __name__=='__main__':
    import sys
    fname = sys.argv[1]

    import corrcha.core.parser.default
#    _p = corrcha.core.parser.default.getDefaultParser()
    import corrcha.core.parser.dummy
    _p = corrcha.core.parser.dummy.DummyParser()
    p = KJParser(_p)

    doc = p.getDoc(fname)
    print unicode(doc)



