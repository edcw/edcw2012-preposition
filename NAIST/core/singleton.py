#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
http://python.g.hatena.ne.jp/nelnal_programing/20080225/1203927879
"""


__author__ = '' 
__version__ = ""
__copyright__ = ""
__license__ = ""

class Singleton( type ):
    def __init__( self, *args, **kwargs ):
        type.__init__( self, *args, **kwargs )
        self._instances = {}

    def __call__( self, *args, **kwargs ):
        if not args in self._instances:
            self._instances[args] = type.__call__(self, *args)
        return self._instances[args]


