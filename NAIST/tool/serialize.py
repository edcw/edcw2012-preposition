#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Serialize objects
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


import cPickle

def read(filename, mode='rb'):
    '''
    pythonオブジェクトを読み込み，返す
    @param filename:読み込むファイル名
    '''
    obj_file = open(filename, mode)
    data = cPickle.load(obj_file)
    obj_file.close()
    return data

def write(filename, data, isbin=True, mode='wb'):
    '''
    pythonオブジェクトを外部に保存する
    @param filename:書き込むファイル名
    @param data:書き込むデータ
    @param mode:書き込みモード(デフォルトw)
    '''
    obj_file = open(filename, mode)
    cPickle.dump(data, obj_file, isbin)
    obj_file.close()


if __name__=='__main__':
    USAGE = """"""
    DESCRIPTION = """Inspect serialized file"""

    import optparse, sys
    oparser = optparse.OptionParser(usage=USAGE, description=DESCRIPTION)
    oparser.add_option('-i', dest = 'input_filename', help='')
    oparser.add_option('-a', dest = 'array', action='store_true', default=False, help='')
    (opts, args) = oparser.parse_args()


    if opts.input_filename:
        import corrcha.tool.serialize
        data =  corrcha.tool.serialize.read(opts.input_filename)
        if opts.array:
            for item in data:
                print unicode(item).encode('utf-8')
        else:
            print unicode(data).encode('utf-8')
    else:
        oparser.print_help()

