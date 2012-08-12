#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


class Token(object):
    def __init__(self, nodeid, surface, position):
        assert type(nodeid) is int
        assert type(surface) is unicode
        assert type(position) is int

        self.__nodeid = nodeid
        self.__surface = surface
        self.__position = position
        self.__rels = {}
        self.__depended_rels = []

    def appendRelation(self, token_id, relation):
        assert type(token_id) is int
        assert type(relation) is unicode
        self.__rels[ token_id ] = relation
 
    def appendDependedTokenId(self, token_id):
        assert type(token_id) is int
        self.__depended_rels.append(token_id)

    def getDependedTokenIds(self):
        return self.__depended_rels

    def getRelations(self):
        return self.__rels
           
    def getPosition(self):
        return self.__position
     
    def getNodeid(self):
        return self.__nodeid
        
    def getSurface(self):
        return self.__surface

    def getLowerSurface(self):
        return self.__surface.lower()

    def __str__(self):
        return "%s %d %s %s" % (self.__surface.ljust(10), self.__nodeid, str(self.__rels), str(self.__position))

    def __len__(self):
        return len(self.__surface)

class Node(object):
    def __init__(self, node_id, tag, parent_node_id):
        assert type(node_id) is int
        assert type(parent_node_id) is int
#        assert type(tag) is unicode
        assert (type(tag) is unicode) or (tag is None) #if None, it's dummy node

        self.__tag = tag
        self.__node_id = node_id
        self.__parent_node_id = parent_node_id

    def __str__(self):
        return "%d %s %d " % (self.__node_id, self.__tag, self.__parent_node_id)

    def getTag(self):
        if self.__tag:
            return self.__tag
        return u"None"

    def getParentNodeid(self):
        return self.__parent_node_id

    def getNodeid(self):
        return self.__node_id

if __name__=='__main__':
    pass

