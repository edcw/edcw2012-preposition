#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.corpus.element

class Result(corrcha.corpus.element.Element, list):
    pass



if __name__=='__main__':
    USAGE = ""
    DESCRIPTION = """Inspect serialized file"""

    import optparse, sys
    oparser = optparse.OptionParser(usage=USAGE, description=DESCRIPTION)
    oparser.add_option('-i', dest = 'input_filename', help='')
    (opts, args) = oparser.parse_args()

    w = sys.stdout.write
    if opts.input_filename:
        import corrcha.tool.serialize
        data =  corrcha.tool.serialize.read(opts.input_filename)

        metas = data.get_meta()
        w("det\n")
        _c = metas['det']
        for _k in (('tp', 'tn', 'fp', 'fn'), ('acc', 'rec', 'prec', 'f' )):
            w(" %s " % unicode(_k))
            for k in _k:
                v = _c[k]
                w("%s " % (v))
            w("\n")

        w("corr\n")
        for (k,v) in metas['corr'].items():
            w(" %s %s\n" % (k,v))
            
        for d in data:
            print "---"
            for (k,v) in d.items():
                print "%s\t%s" % (k, unicode(v))
    else:
        oparser.print_help()

