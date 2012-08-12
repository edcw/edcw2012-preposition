#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


import tempfile
def getTmpName():
    f = tempfile.NamedTemporaryFile(delete=True)
    name = f.name
    f.close()
    return name

import os
def mkdirs(folder_name):
    assert isinstance(folder_name, str)
    try:
        os.makedirs(folder_name)
    except OSError as exc: # Python >2.5
        import errno
        if exc.errno == errno.EEXIST:
            pass
        else: raise

def getMetas(START_TIME):
    import corrcha.tool.setting
    metas = {}
    for (k,v) in corrcha.tool.setting.val.items():
        metas[k] = v
    import socket
    import email.Utils
    metas["host"] = socket.gethostname()
    metas["finish_time"] = email.Utils.formatdate(localtime=True)

    import time
    TIME = time.time() - START_TIME
    metas["time"] = int(TIME)
    return metas


def argwrapper(args):
    return args[0](*args[1:])


def getClass(path_to_class):
    """Import the module where the class belongs, and r0eturn class."""
    target = path_to_class.split('.')
    (package, module, cls_name)  = (target[0], '.'.join(target[:-1]), target[-1])
    class_target = getattr(__import__(module, fromlist=[package]), cls_name)  
    return class_target


if __name__=='__main__':
    pass

