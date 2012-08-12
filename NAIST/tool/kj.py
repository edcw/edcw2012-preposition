#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""
__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.constant

import corrcha.core.parser.default
import corrcha.corpus.document

import sys
import lxml.html

import corrcha.tool.setting                                                              
import nltk.data
import corrcha.tool.util

import corrcha.corpus.parser.kj

import unicodedata

ERROR2CHAR = dict([(v, k) for (k, v) in corrcha.corpus.parser.kj.KJParser.TAGSET.iteritems()])

def check(tokenizer, correcters, fname, parser, output_dir):
    fname_base = fname.split('/')[-1].split('.')[0]

    doc = corrcha.corpus.document.Document()
    lines = []
    for line in open(fname):
        line = unicode(line, 'euc_jp').rstrip()
        lines.append(line)
        parsed_sentence = parser.parse(line)
        doc.append_paragraph([parsed_sentence])

    misses = corrcha.corpus.mistake.Mistakes()
    for correcter in correcters:
        for miss in correcter.check(doc):
            misses.append(miss)

    output = u""
    lid = 0
    while lid < len(lines):
        pos = 0
        if lid !=0:
            output += u"\n"

        while pos < len(lines[lid]):
            m = misses.get(lid, pos, True)
            if m:
                corr = m.corr 
                org = m.original
                error_type = ERROR2CHAR[m.error_type]
                if pos == 0:
                    org = org.capitalize()
                    corr = corr.capitalize()
                output += u"""<%s crr="%s">%s</%s>""" % (error_type, corr, org, error_type)
                pos += len(org)
            else:
                char = lines[lid][pos]
                if char == u"「":
                    char = "\""
                elif char == u"」":
                    char = "\" "
                elif char == u"“":
                    char = "\""
                elif char.isnumeric():
                    char = unicodedata.normalize('NFKC', char)
                elif output.endswith(u"and roma and I can'") and char== u" ": #58-60-9.sys
                    char = u""
                output += char
                pos += 1
        lid += 1
    
    import codecs
    f = codecs.open(output_dir + '/' + fname_base + '.sys', 'w', 'euc_jp')
    f.write(output)


def makecommands(input_filename, model_dir, output_dir):
    assert isinstance(input_filename[0], str)
    a_model_dir = model_dir[0]
    import os
    PARALELL = 20
    files = [[] for i in xrange(PARALELL)]
    f_root = input_filename[0]
    for i, fname in enumerate(os.listdir(f_root)):
        gid = i % PARALELL
        _fname = f_root + '/' + fname
        files[gid].append(_fname)

#    for a_model_dir in os.listdir(model_dir):
#        print a_model_dir ,"|||"
    models = [ ("-m %s/  " % (a_model_dir), "") ]
#        ("-m %s/prep.fix -m %s/prep-rep.fix " % (a_model_dir, a_model_dir), "fix") ]

    for model in models:
        for i in xrange(PARALELL):
            input = ""
            for fname in files[i]:
                input += " -i %s " % fname
            print "python -O %s %s %s -o %s  " % (__file__, model[0], input, output_dir + '/' + model[1])

if __name__=='__main__':

    import optparse, sys
    oparser = optparse.OptionParser()
    oparser.add_option('-m', action="append", dest = 'model_dir', help='Model directory', default=[]) #TODO accept multi correctors?
    oparser.add_option('-i', action="append", dest = 'input_filename', help='Raw text', default=[])
    oparser.add_option('-o', dest = 'output_dir', help='Output directory')
    oparser.add_option("--makecommands", action="store_true", dest="makecommands", default=False)


    (opts, args) = oparser.parse_args()

    if not (opts.model_dir) or len(opts.model_dir) == 0:
        sys.stderr.write("Designate the model directory name\n")
        quit()
    if not (opts.output_dir):
        sys.stderr.write("Designate the output directory name\n")
        quit()
    corrcha.tool.util.mkdirs(opts.output_dir)

    if opts.makecommands:
        makecommands(opts.input_filename, opts.model_dir, opts.output_dir)
        quit()


    #--- main ---#
    import corrcha.correct.correcter


    correcters = []
    for model_dir in opts.model_dir:
        correcter_class = corrcha.correct.correcter.getCorrecterClass(model_dir)
        correcter = correcter_class(model_dir)
        correcters.append(correcter)

    parser = corrcha.core.parser.default.getDefaultParser()
    _SENTENCE_TOKENIZE_MODEL = corrcha.tool.setting.val['corpus']['sentence_tokenize_model']
    tokenizer = nltk.data.load(_SENTENCE_TOKENIZE_MODEL) 

    for fname in opts.input_filename:
        check(tokenizer, correcters, fname, parser, opts.output_dir)


