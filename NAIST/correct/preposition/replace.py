#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.corpus.document
import corrcha.corpus.sentence
import corrcha.corpus.mistake
import corrcha.correct.correcter

import corrcha.correct.feature
import corrcha.constant
import logging
import os
import corrcha.core.parser.default
import corrcha.corpus.mistake

import corrcha.corpus.util

class Correcter(corrcha.correct.correcter.Correcter):
    IDDIC_PATH = "/iddic"
    ORIGINAL_SURFACE = "ORIGINAL_SURFACE:"
    LEN_ORIGINAL_SURFACE = len(ORIGINAL_SURFACE)


    def __readyFeatures(self):
        #for lazy ready
        if not self.feature_funcs:
            logging.info("Preparing feature extractors...")
            self.feature_funcs = [ corrcha.correct.feature.Head_prec()  \
                    ,corrcha.correct.feature.Parsing()  \
                    ,corrcha.correct.feature.PhraseStructure()  \
                    ,corrcha.correct.feature.Web_n_gram()  \
                    ]

    def __init__(self, model):
        assert (type(model) is type) or isinstance(model, (str, unicode))

        self.classifier = corrcha.core.classify.classifier.SingleClassifier(model)
        self.iddic = None

        if isinstance(model, (str, unicode)):
            dicpath = model + self.IDDIC_PATH
            if os.path.exists(dicpath):
                self.iddic = corrcha.tool.serialize.read(dicpath)
            else:
                logging.error("Cannot find the id dictionary at [%s]" % dicpath)
                quit()

        self.feature_funcs = None 
        self.parser = corrcha.core.parser.default.getDefaultParser()


    def __extract(self, doc, doFunc):
#        assert isinstance(f, file)
        assert isinstance(doc, corrcha.corpus.document.Document)

        for lineid, sent in enumerate(doc):
            for prep_id, token in enumerate(sent):
                #do only the surface of the token is `preposition'
                original_surf = token.getSurface()
                if not corrcha.constant.isPreposition(original_surf):
                    continue

                #get label for training
                miss = doc.find_mistake(lineid, token.getPosition())
                if miss:
                    label = miss.corr.lower()
#                    logging.debug("Found miss [%s]" % (label))
                    if label == u"":
                        label = u"NONE"
                else:
                    label = original_surf.lower()

                features = []
                features.append(u"%s%s" % (self.ORIGINAL_SURFACE, original_surf.lower()))
                for func in self.feature_funcs:
                    func(features, None, prep_id, None, sent, doc)
                
                doFunc(token.getSurface().lower(), label, features, prep_id, sent, lineid)



    def extract(self, corpus, outfolder, param):
        """extract features and save them to file(s)"""
        assert isinstance(corpus, corrcha.corpus.corpus.Corpus)
        assert isinstance(outfolder, str)
        assert isinstance(param, str)

        self.__readyFeatures()
#        f = open(outfolder, 'w')
        import codecs, sys
        f = codecs.open(outfolder, 'w', encoding=sys.getfilesystemencoding())
        def writeFeature(original_relation, gold_relation, features, prep_id, sentence, lindid):
            f.write("%s\t" % gold_relation)
            f.write(u"%s" % u"\t".join(features))
            f.write(u"\t###\t%s" % sentence.getSurface())
            f.write(u"\n")

        total = len(corpus)
        for i, doc in enumerate(corpus): #no parallel mode to be simpler
            if i % 10 == 0:
                logging.info("Extracted %4d/%4d" % (i, total))
            self.__extract(doc, writeFeature)
        f.close()


    def train(self, inputfolder, outfolder, param):
        assert isinstance(inputfolder, str) or isinstance(inputfolder, list)
        assert isinstance(outfolder, str)
        assert isinstance(param, str)
        params = param.split()
        ignorefname = []
        for param in params:
            if param.startswith("IGNORE="):
                ignorefname.append(param[len("IGNORE="):])

        if isinstance(inputfolder, str):
            inputs = [inputfolder]
        else:
            inputs = inputfolder
            
        ## convert training file format ##
        #get feature names
        from collections import defaultdict
        namedic = defaultdict(int)
        self.iddic = {}
        for fname in inputs:
            for line in open(fname, 'r'):
                features = line[:-1].split("\t")[1:]
                for f in features:
                    #check list
                    ok = True
                    for ign in ignorefname:
                        if f.startswith(ign):
                            ok = False
                            break
                    if ok:
                        namedic[f] += 1

        #sort by frequencies of features and give ids to them
        fid = 0
        for k, v in sorted(namedic.items(), key=lambda x:x[1], reverse=True):
            self.iddic[k] = fid
            fid += 1

        #convert training file format
        tmp_train_name = corrcha.tool.util.getTmpName()
        f = open(tmp_train_name, 'w')
        logging.info("Temporary file : %s" % tmp_train_name)

        for fname in inputs:
            for line in open(fname, 'r'):
                features = line[:-1].split("\t")
                label = features[0]

                #XXX Ignore out of scope
                if label not in corrcha.constant.PREPOSITIONS + [u"NONE"]:
                    continue
                elif features[1][self.LEN_ORIGINAL_SURFACE:] not in corrcha.constant.PREPOSITIONS + [u"NONE"]:
                    continue

                f.write("%s\t" % label)
                for fname in features[1:]:
                    if fname == u"###":
                        break
                    fid = self.iddic.get(fname, None)
                    if fid:
                        f.write("%d:1 " % (fid))
                f.write("\n")
        f.close()
        self.classifier.train(tmp_train_name, outfolder, param)

        corrcha.tool.serialize.write(outfolder + self.IDDIC_PATH, self.iddic)

        import os
        os.remove(tmp_train_name)



    def test(self, inputfolder):
        assert isinstance(inputfolder, str)

        gold = []
        result = []
        orgs = []
        memos = []
        import codecs
        for line in codecs.open(inputfolder, 'r', 'utf-8'):
            features = line[:-1].split("\t")
            gold_label = features[0]
            fdic = {}
            original = features[1][self.LEN_ORIGINAL_SURFACE:]

            #XXX Ignore out of scope
            if original not in corrcha.constant.PREPOSITIONS + [u"NONE"]:
                continue

            for j, fname in enumerate(features[1:]):
                if fname == r"###":
                    memo = u"\t".join(features[2:4])
                    memo += u"\t" + features[j+2]
                    memos.append(memo)
                    break
                fid = self.iddic.get(fname, None)
                if fid:
                    fdic[fid] = 1
            label, value = self.classifier.predict(fdic)
            label = unicode(label, 'utf-8')
            if label == r'none':
                label = u''
            if gold_label == r'none':
                gold_label = u''
            if original == r'none':
                original = u''
            result.append(label)
            gold.append(gold_label)
            orgs.append(original)

#            if original == gold_label: #no error
#                gold.append(None)
#                if original == label:
#                    result.append(None) #no error
#                else:
#                    result.append(gold_label)
#            else:
#                gold.append(gold_label)
#                if label == original: #system says no error
#                    result.append(None)
#                else:
#                    result.append(label)

        return orgs, gold, result, memos


    def check(self, doc, getOK=False, getGold=False):
        assert isinstance(doc, corrcha.corpus.document.Document)
        assert isinstance(getOK, bool)
        assert isinstance(getGold, bool)
        self.__readyFeatures()

        mistakes = []
        def predictFromFeature(original_relation, gold_relation, features,  prep_id, sentence, lineid):
            token = sentence.getToken(prep_id)

            #do only the surface of the token is `preposition'
            if not corrcha.constant.isPreposition(token.getSurface()):
                return

            if getGold:
                miss = doc.find_mistake(lineid, token.getPosition())
                if miss:
                    mistakes.append(miss)
                else:
                    mistakes.append(None)
                return

            #predict
            features = []
            original = token.getSurface().lower()
            if original not in corrcha.constant.PREPOSITIONS: #XXX Ignore
                return

            features.append("%s%s" % (self.ORIGINAL_SURFACE, original))
            for func in self.feature_funcs:
                func(features, None, prep_id, None, sentence, doc)

            fdic = {}
            for f in features:
                fid = self.iddic.get(f, None)
                if fid:
                    fdic[fid] = 1
            label, value = self.classifier.predict(fdic)

            label = unicode(label)
            if label == u"NONE":
                label = u""
            t_pos = token.getPosition()
            t_len = len(original)
            position = ((lineid, t_pos), (lineid, t_pos + t_len))
            if original != label: #detect an errors
                miss = corrcha.corpus.mistake.Mistake(position, ["PREP"], original, label, corrcha.corpus.mistake.Error.PREPOSITION)
                mistakes.append(miss)
            elif getOK:
                mistakes.append(None)
            else:
                pass

        self.__extract(doc, predictFromFeature)
        return mistakes
                

if __name__=='__main__':
    pass

