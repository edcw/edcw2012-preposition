#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Serialize objects
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


#FIXME
import sys, os

def getRcFile():
    fname = os.environ.get('CORRCHARC')
    if fname is not None:
        yield fname

    fname = os.environ['HOME'] + '/' + '.corrcharc'
    yield fname

    PARENT_PATH =  os.path.dirname(os.path.abspath(__file__))+'/../'
    fname = PARENT_PATH+ 'setting.json'
    yield fname

def load_setting(fname = None):
    if fname is None:
        for f in getRcFile():
            if os.path.isfile(f):
                fname = f
                break
    import json
    f = open(fname)
    _val = json.load(f)
    f.close()
    return _val

val = load_setting()

if __name__=='__main__':
    print val

