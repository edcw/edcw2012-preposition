#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""


__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


class Features(object):
    def __init__(self):
        self.__binary_feature = []
        self.__real_value_feature = {}
    
    def fire(self, number, value=None):
        assert isinstance(number, int)
        if value == None:
            self.__binary_feature.append(number)
        else:
            self.__real_value_feature[number] = value

    def getDict(self):
        dic = {}
        for n in self.__binary_feature:
            dic[n] = 1
        for (n, val) in self.__real_value_feature.items():
            dic[n] = val
        return dic

    def update(self, features):
        assert isinstance(features, Features)
        for (number, value) in features.getDict().items():
            if value == 1:
                self.__binary_feature.append(number)
            else:
               self.__real_value_feature[number] = value

    def __unicode__(self):
        dump = u""
        for n in self.__binary_feature:
            dump += " %d:%d" % (n, 1)
        for (n, val) in self.__real_value_feature.items():
            dump += " %d:%d" % (n, val)
        return dump


