#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.corpus.token


def mark(raw_sentence, range_):
    (start_offset, end_offset) = range_
    return raw_sentence[:start_offset] \
            + ">" +  raw_sentence[start_offset:end_offset] \
            + "<" + raw_sentence[end_offset:]


class ParsedSentence(object):
    def __init__(self, surface):
        assert isinstance(surface, unicode)
        self.__surface = surface
        self.__tokens = []
        self.__token_ids = []
        self.__nodes = {}
        self.__child_nodes = {}
        self.__nid2tokenid = {}

    def __iter__(self):
        return iter(self.__tokens)

    def __len__(self):
        return len(self.__surface)

    def getSurface(self):
        return self.__surface

    def append(self, token):
        #TODO is it need to resort by token's id??
        assert isinstance(token, corrcha.corpus.token.Token)

        newtokneid = len(self.__tokens)
        self.__nid2tokenid[token.getNodeid()] = newtokneid

        self.__tokens.append( token )
        self.__token_ids.append( token.getPosition()  )

    def appendNode(self, tree_id, node):
        assert isinstance(tree_id, int)
        assert isinstance(node, corrcha.corpus.token.Node)
        self.__nodes[tree_id] =  node 
        parent_id = node.getParentNodeid()
        if self.__child_nodes.has_key(parent_id):
            self.__child_nodes[parent_id].append(tree_id)
        else:
            self.__child_nodes[parent_id] = [tree_id]

    def getNode(self, token):
        if type(token) is int:
            return self.__nodes[ token ]
        assert isinstance(token, corrcha.corpus.token.Token)
        tokenid = token.getNodeid()
        return self.__nodes[ tokenid ]
     
    def getTokenIdByPosition(self, position, getRight=False):
        assert isinstance(position, int)
        assert isinstance(getRight, bool)

        __id = len(self.__tokens)
        if __id != 0:
            __id -= 1
        for id, token in enumerate(self.__tokens):
            token_position = token.getPosition()
#            if token_position <= position < token_position + len(token) :
            if position <= token_position + len(token) :
                __id = id
                break
        if getRight is False:
            return __id
        else:
            __id += 1
            if __id < len(self.__tokens):
                return __id
            else:
                return None

   
    def getTokenByPosition(self, position, getRight=False):
        assert isinstance(position, int)
        assert isinstance(getRight, bool)

        token_id = self.getTokenIdByPosition(position, getRight)
        if token_id is None:
            return None
        else:
            return self.__tokens[token_id]


    def getToken(self, node):
        if type(node) is int:
            return self.__tokens[ node ]
        assert isinstance(node, corrcha.corpus.token.Node)
        tokenid = self.__nid2tokenid.get(node.getNodeid())
        if tokenid is None:
            return None
        return self.__tokens[tokenid]

    def appendRelation(self, gov, dep, rel):
        """gov and dep are the indexs of the tokens"""
        assert isinstance(gov, int)
        assert isinstance(dep, int)
        assert isinstance(rel, unicode)

        gov_obj = self.__tokens[ gov ]
        gov_obj.appendRelation( dep, rel)

        dep_obj = self.__tokens[ dep ]
        dep_obj.appendDependedTokenId(gov)

    def getParentNode(self, node, depth=1):
        if type(node) is corrcha.corpus.token.Token:
            node = self.getNode(node)

        assert isinstance(node, corrcha.corpus.token.Node)
        if depth==0:
            return node
        else:
            pid = node.getParentNodeid()
            pnode = self.__nodes.get(pid)
            if pnode is None:
                return None
            return self.getParentNode(pnode, depth-1)

    def getChildNodes(self, node):
        if type(node) is corrcha.corpus.token.Token:
            node = self.getNode(node)
        assert isinstance(node, corrcha.corpus.token.Node)
        nid = node.getNodeid()
        nodes = self.__child_nodes.get(nid)
        if nodes is None:
            return []
        return nodes

    def getIndex(self, token):
        assert isinstance(token, corrcha.corpus.token.Token)
        import bisect
        return bisect.bisect_left(self.__token_ids, token.getPosition())

    def getNext(self, token, offset=1):
        assert isinstance(token, corrcha.corpus.token.Token)
        token_position = self.getIndex(token)
        if (token_position +offset <0) or (token_position +offset >= len(self.__token_ids)):
            return None
        else:
            return self.__tokens[token_position+offset]

    def getTokens(self):
        return self.__tokens

    def getNodes(self):
        return self.__nodes

    def __unicode__(self):
        dump = u"[%s]" % self.__surface
        dump += u"\n"
#        print self.__tokens, self.__nodes

        for t in self.__tokens:
            dump += u"%s\n" % unicode(t)
        dump += u"\n"

        for (k,v) in self.__nodes.items():
            dump += u"[%d]%s " % (k, v)
        dump += u"\n----\n"
        return dump


if __name__=='__main__':
    pass

