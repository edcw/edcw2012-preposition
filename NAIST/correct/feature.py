#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.corpus.document

import corrcha.tool.setting
import corrcha.tool.wordnet

import math
import corrcha.constant
import corrcha.tool.myssgnc


class Head_prec(object):
    def __init__(self):
        self.wordnet = corrcha.tool.wordnet.WordnetHelper()

    def __do(self, features, my_id, offset, pos, feature_prefix, sentence):
        if my_id is None:
            return

        my_token = sentence.getToken(my_id)
        while True:
            my_token = sentence.getNext(my_token, offset)
            if my_token is None:
                break
            my_node = sentence.getNode(my_token)
            my_tag = my_node.getTag()
            if my_tag.startswith(pos):
                features.append(u"%s:%s" % (feature_prefix, my_token.getLowerSurface()))
                for ln in self.wordnet.getLexnames(my_token.getLowerSurface()):
                    features.append(u"%s_WORDNET:%s" % (feature_prefix, ln))
                return
        return

    def __call__(self, features,  token_id, prep_id, argid, sentence, document):
        assert isinstance(features, list)
        assert isinstance(token_id, int) or token_id is None
        assert isinstance(prep_id, int) or prep_id is None
        assert isinstance(argid, int) or argid is None
        assert type(sentence) is corrcha.corpus.sentence.ParsedSentence
        assert type(document) is corrcha.corpus.document.Document

        self.__do(features, prep_id, +1, u"NN", u"HEAD_FOLLOW_NOUNP", sentence)
        self.__do(features, prep_id, -1, u"NN", u"HEAD_PREC_NOUNP", sentence)
        self.__do(features, prep_id, -1, u"VB", u"HEAD_PREC_NOUNP", sentence)


import corrcha.corpus.util


def getPropotion(features, _total, _vals, title):
    if _total >0:
        for (k,v) in _vals.items():
            proportion = v/float(_total)
            for i in xrange(1, 10):
                th = i/10.0
                if proportion > th:
                    features.append(u"%s_[%s]:%s"%(title, k, "> %0.1f"%th))

class Parsing(object):
    """Tetreault:2010:ACL"""

    def __call__(self, features,  token_id, prep_id, argid, sent, document):
        assert isinstance(features, list)
        assert isinstance(token_id, int) or token_id is None
        assert isinstance(prep_id, int) or prep_id is None
        assert isinstance(argid, int) or argid is None
        assert type(sent) is corrcha.corpus.sentence.ParsedSentence
        assert type(document) is corrcha.corpus.document.Document

        if prep_id is None:
            return
        from_id, to_token_id = corrcha.corpus.util.getPrepDepAndGov(sent, prep_id)

        head_surf, head_tag, comp_surf, comp_tag = None, None, None, None
        if from_id:
            token = sent.getToken(from_id)
            node = sent.getNode(token)
            relname = token.getRelations()[prep_id]
            head_surf, head_tag, head_relname = token.getSurface(), node.getTag(), relname
            features.append(u"HEAD:%s" % head_surf)
            features.append(u"HEAD_TAG:%s" % head_tag)
            features.append(u"HEAD_RELATION:%s" % head_relname)

        if to_token_id:
            token = sent.getToken(to_token_id)
            node = sent.getNode(token)
            prep_token = sent.getToken(prep_id)
            try:
                relname = prep_token.getRelations()[to_token_id]
            except:
                relname = u"UNK"
            comp_surf, comp_tag, comp_relname =  token.getSurface(), node.getTag(), relname
            features.append(u"HEAD:%s" % comp_surf)
            features.append(u"HEAD_TAG:%s" % comp_tag)
            features.append(u"HEAD_RELATION:%s" % comp_relname)

#        if (from_id is not None) and (to_token_id is not None):
        if (head_surf is not None) and (head_tag is not None) \
                and (comp_surf is not None) and (comp_tag is not None) \
                and (head_tag != "None") and (comp_tag != "None")  :
            features.append(u"HEAD&COMP:%s-%s" % (head_surf, comp_surf))
            features.append(u"HEAD_TAG&COMPLEMENT_TAG:%s-%s" % (head_tag, comp_tag))
            features.append(u"HEAD_TAG&COMPLEMENT:%s-%s" % (head_tag, comp_surf))
            features.append(u"HEAD&COMPLEMENT_TAG:%s-%s" % (head_surf, comp_tag))

        return


class PhraseStructure(object):

    def __call__(self, features,  token_id, prep_id, argid, sent, document):
        assert isinstance(features, list)
        assert isinstance(token_id, int) or token_id is None
        assert isinstance(prep_id, int) or prep_id is None
        assert isinstance(argid, int) or argid is None
        assert type(sent) is corrcha.corpus.sentence.ParsedSentence
        assert type(document) is corrcha.corpus.document.Document

        if prep_id is None:
            return
        token = sent.getToken(prep_id)

        token_position = token.getPosition()
        parent = sent.getParentNode(token)
        grand_parent = sent.getParentNode(parent)
        node = sent.getNode(token)
        parent_nodeid = node.getParentNodeid()
 
        #check grand_parent's children
        grand_parents_children = sent.getChildNodes(grand_parent)
        for i, v in enumerate(grand_parents_children):
            if v == parent_nodeid:
                if i >= 1:
                    left_node_id = grand_parents_children[i-1]
                    left_node = sent.getNode(left_node_id)
                    features.append(u"PARENT_CONTEXT_LEFT:%s" % left_node.getTag())
                else:
                    features.append(u"PARENT_CONTEXT_LEFT:%s" % u"-")

                if i < len(grand_parents_children)-1:
                    right_node_id = grand_parents_children[i+1]
                    right_node = sent.getNode(right_node_id)
                    features.append(u"PARENT_CONTEXT_RIGHT:%s" % right_node.getTag())
                else:
                    features.append(u"PARENT_CONTEXT_RIGHT:%s" % u"-")
                return

class Web_n_gram(object):

    def __init__(self):
        INDEX_PATH = corrcha.tool.setting.val['ssgnc']['index']
        self.myagent = corrcha.tool.myssgnc.MyAgent(INDEX_PATH)

    def get_frequency(self, query):
        return self.myagent.get_frequency(query)

    def _isContentWord(self, tag):
        if (len(tag)>0) and (tag[0] in [u"V", u"N", u"P", u"J"]):
            return True
        return False

    def _SmartQuery(self, context_tags):
        left = None
        right = None
        for i in [-1, -2]:
            if self._isContentWord(context_tags[i]):
                left = i
                break
        for i in xrange(1, 5+1):
            if self._isContentWord(context_tags[i]):
                right = i
                break
        if left is None or right is None:
            raise KeyError
        return left, right

    def __call__(self, features,  token_id, prep_id, argid, sent, document):
        assert isinstance(features, list)
        assert isinstance(token_id, int) or token_id is None
        assert isinstance(prep_id, int) or prep_id is None
        assert isinstance(argid, int) or argid is None
        assert type(sent) is corrcha.corpus.sentence.ParsedSentence
        assert type(document) is corrcha.corpus.document.Document

        context_surfaces = ["", "", "", "", "", "", "", ""]
        context_tags = ["", "", "", "", "", "", "", ""]

        if prep_id is None: #This may be because relation is dobj
            assert isinstance(token_id, int)
            verb_token = sent.getToken(token_id) #XXX
            context_surfaces[-1] = verb_token.getSurface()
            context_tags[-1] = sent.getNode(verb_token).getTag()
            prev_token = sent.getNext(verb_token, -1)
            if prev_token:
                context_surfaces[-2] = prev_token.getSurface()
                context_tags[-2] = sent.getNode(prev_token).getTag()
            next_token = sent.getNext(verb_token, 1)
            if next_token:
                context_surfaces[1] = next_token.getSurface()
                context_tags[1] = sent.getNode(next_token).getTag()
                next_token = sent.getNext(next_token, 1)
                if next_token:
                    context_surfaces[2] = next_token.getSurface()
                    context_tags[2] = sent.getNode(next_token).getTag()
        else:
            token = sent.getToken(prep_id)
            for i in range(-2, 2+1):
                my_token = sent.getNext(token, i)
                if my_token:
                    my_surface = my_token.getLowerSurface()
                    context_surfaces[i] = my_surface
                    my_node = sent.getNode(my_token)
                    my_tag = my_node.getTag()
                    context_tags[i] = my_tag
                else:
                    if i < 0:
                        context_tags[i] = "<s>"
                        context_surfaces[i] = "<s>"
                    if i == -1:
                        context_tags[-2] = ""
                        context_surfaces[-2] = ""

        #------------------------------------------------------
        total = 0
        _vals = {}
        for item in corrcha.constant.PREPOSITIONS + [u""]:
            try:
                left, right = self._SmartQuery(context_tags)
                win = context_surfaces[left] + " " + item + " " + context_surfaces[right]
                _count =  self.get_frequency(win)
                _vals[item] = _count
                total += _count
            except:
                pass
        getPropotion(features, total, _vals, u"SmartQuery")
        #------------------------------------------------------


        for (left, right) in [(-1, 1), (-2, 1), (-1, 2), (-2, 2)]:
            _left = " ".join(context_surfaces[left:]) 
            _right = " ".join(context_surfaces[1:right+1])
            _left_tags = " ".join(context_tags[left:]) 
            _right_tags = " ".join(context_tags[1:right+1])

            _window = _left + " " + context_surfaces[0] + " " + _right
#            print "======[%s]" % _window
            _window_tags = _left_tags + " " + context_tags[0] + " " + _right_tags
            features.append(u"N-GRAM_(%d,%d):%s" % (left, right, _window))
            features.append(u"N-GRAM_TAGS_(%d,%d):%s" % (left, right, _window_tags))

            count =  self.get_frequency(_window) +1
            val = int(math.log(count, 100))
            for i in xrange(0,val+1):
                features.append(u"WEB_N-GRAM_COUNT_(%d,%d):%s" % (left, right, "> %d"%i))

            #proportion
#            original = context_surfaces[0]
            total = 0
            _vals = {}
            for item in corrcha.constant.PREPOSITIONS + [u""]:
                _query = _left + " " + item + " " +_right
                _count =  self.get_frequency(_query)
                _vals[item] = _count
                total += _count
#                print _query, _count, total
            getPropotion(features, total, _vals, u"WEB_N-GRAM_PROPORTION_(%d,%d)" % (left, right))

