#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""


__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


class Element(object):
    """This represents an abstract element which can store meta data."""

    def __init__(self, metadata=None):
        if metadata is None:
            self.__metadata = {}
        else:
            self.__metadata = metadata

    def set_meta(self, key, val):
        self.__metadata[key]=val

    def set_metas(self, metas):
        assert isinstance(metas, dict)
        self.__metadata.update(metas)

    def get_meta(self, key=None):
        if key is None:
            return self.__metadata
        return self.__metadata[key]

    def del_meta(self, key):
        del self.__metadata[key]


    def __unicode__(self):
        return  str(self.__metadata)

if __name__=='__main__':
    pass

