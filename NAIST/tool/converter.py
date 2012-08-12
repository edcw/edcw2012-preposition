#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Converter of learners corpus
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

#import sys, os.path
#sys.path.append( os.path.dirname(os.path.abspath(__file__))+'/../') 
#sys.path.append( os.path.dirname(os.path.abspath(__file__))+'/../corpus/') #for cPickle

import corrcha.corpus.document as Document
import corrcha.corpus.mistake as Mistake

def __kj_parser(dep_parser):
    import corrcha.corpus.parser.kj
    return corrcha.corpus.parser.kj.KJParser(dep_parser)

SUPPORT_FORMAT = {
        'kj'   : __kj_parser ,
        }


def _set_up_dep_parser():
    import corrcha.core.parser.default
    return corrcha.core.parser.default.getDefaultParser()


def _out(parser, filename, outfilename, number=None):
    from time import time
    START_TIME = time()
    import sys

    for i, corpus in enumerate(parser.convert(filename, number)):
        #set meta data
        import corrcha.tool.setting
        for (k,v) in corrcha.tool.setting.val.items():
            corpus.set_meta(k, v)
        import socket
        import email.Utils
        corpus.set_meta( "host", socket.gethostname() )
        corpus.set_meta( "time", email.Utils.formatdate(localtime=True) )

        TIME = time() - START_TIME
        corpus.set_meta( "convert_time", int(TIME) )

        import corrcha.tool.serialize
        if number is None:
            _outname = outfilename
            corrcha.tool.serialize.write(_outname, corpus)
        else:
            _outname = outfilename + "." + str(i)
            corrcha.tool.serialize.write(_outname, corpus)
        sys.stderr.write(" \rSaved to %s\n" % (_outname.ljust(30)) )
    sys.stderr.write("\rFinished! (%0.3f sec)\n" % (TIME ) )
    sys.stderr.flush()


def convert(filename, outfilename, mode, number=None):
    dep_parser = _set_up_dep_parser()
    try:
        parser = SUPPORT_FORMAT[ mode ](dep_parser)
    except KeyError:
        sys.stderr.write("Mode [%s] is not supported.\n" % mode)
        quit()

    _out(parser, filename, outfilename, number)

def read(filename):
    import corrcha.tool.serialize
    corpus = corrcha.tool.serialize.read(filename)
    print corpus.get_meta()
    for d in corpus:
        print "============"
        st =  unicode(d)
        print st.encode('utf-8')
        print "============"


def makeFixDocument(filename, outfilename, keep_error):
    assert isinstance(filename, str)
    assert isinstance(outfilename, str)
    assert isinstance(keep_error, str)
    keep_errors_list = [int(i) for i in keep_error.split(',')]

    def keep_errors(miss):
        return miss.error_type in keep_errors_list


    import corrcha.tool.serialize
    corpus = corrcha.tool.serialize.read(filename)
    newcorpus = corrcha.corpus.corpus.Corpus()
    dep_parser = _set_up_dep_parser()
    leng = len(corpus)
    for i, doc in enumerate(corpus):
        sys.stderr.write("Processing %4d/%4d\r"%(i,leng) )
        sys.stdout.flush()
        fixdoc = corrcha.corpus.document.getFixDocument(doc, dep_parser, keep_errors)
        newcorpus.append(fixdoc)
    import corrcha.tool.serialize
    corrcha.tool.serialize.write(outfilename, newcorpus)
    sys.stderr.write("Finished           %4d\r"%(leng) )



if __name__=='__main__':
    import sys
    argv = sys.argv
    argc = len(argv)

    USAGE = """Convert corpus"""

    import optparse
    oparser = optparse.OptionParser(usage=USAGE)
#    oparser.add_option("-r", action="store_true", dest="read", default=False, help="Set read mode")
    oparser.add_option('-i', dest = 'input_filename', action="append", default=[])
    oparser.add_option('-o', dest = 'output_filename')
    oparser.add_option('-m', dest = 'mode', help='Choose original corpus name from %s.'% SUPPORT_FORMAT.keys())
    oparser.add_option('-f', dest = 'fixdoc', type="string", default=False, help="Arguments are types which you want to keep. If None, designate ','")
    oparser.add_option('-n', dest = 'number', type="int", default=False, help="The number of limit in converting corpus")
    (opts, args) = oparser.parse_args()


    if len(opts.input_filename)==0:
        opts.input_filename = None
    elif len(opts.input_filename)==1:
        opts.input_filename = opts.input_filename[0]
    
    if (opts.input_filename and  opts.output_filename):
        if opts.mode:
            if opts.number:
                convert(opts.input_filename, opts.output_filename, opts.mode, opts.number)
            else:
                convert(opts.input_filename, opts.output_filename, opts.mode)
        elif opts.fixdoc:
            makeFixDocument(opts.input_filename, opts.output_filename, opts.fixdoc)
        else:
            print "Designate mode!"
    elif opts.input_filename:
        read(opts.input_filename)
    else:
        oparser.print_help()

    quit()

