#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import jpype
import os.path
MY_DIR = os.path.dirname( os.path.abspath( __file__ ) )

myclass = None

def addClassPath(path):
    assert isinstance(path, (str, unicode))
    global myclass
    if myclass is None:
        jpype.startJVM(jpype.getDefaultJVMPath(), "-Djava.class.path=%s" % (MY_DIR),)
        myclass = jpype.JClass("ClassPathModifier")

    myclass.addFile(path)


