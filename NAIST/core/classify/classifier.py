#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""


__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


class Classifier(object):
    def __init__(self, model_file_name=None):
        raise NotImplementedError

    def load(self, model_file_name):       
        raise NotImplementedError

    def save(self, model_file_name):
        raise NotImplementedError

    def predict(self, x):      
        raise NotImplementedError

    def train(self, x, y=None):
        raise NotImplementedError



import logging
import corrcha.core.classify.feature
import corrcha.constant
import corrcha.tool.util
import os
import json
class SingleClassifier(Classifier):
    __classifier_type_item = 'classifier_type'

    def __setName(self, model):
        self.__classify_model = model + "/model" 
        self.__classify_info = model + '/' + corrcha.constant.INFO_NAME

    def __init__(self, model):
        """model is classifier type or model filname"""

        if type(model) is type:
            self.__classifier_type = model
            self.classifier = self.__classifier_type()
        else:
            assert isinstance(model, (str, unicode))
            self.__setName(model)
            info_dic = json.load(file(self.__classify_info, 'r'))
            
            target = info_dic[self.__classifier_type_item].split('.')
            (package, module, cls_name)  = (target[0], '.'.join(target[:-1]), target[-1])
            classifier_type = getattr(__import__(module, fromlist=[package]), cls_name)  

            self.classifier = classifier_type(self.__classify_model)


    def train(self, train_file, outfolder, param):
        logging.info("Start training!")
        assert isinstance(outfolder, str)
        assert isinstance(param, str)

        corrcha.tool.util.mkdirs(outfolder)

        logging.info("Learning classifier...")
        self.classifier.train(train_file, param=param)

        logging.info("Saving models...")
        self.__setName(outfolder)
        self.classifier.save(self.__classify_model)
        __name = self.__classifier_type.__module__ + '.' + self.__classifier_type.__name__

        info_name = self.__classify_info
        try:
            f = open(info_name)
            info_dic = json.load(f)
            f.close()
            info_dic[self.__classifier_type_item] = __name
        except:
            info_dic = {self.__classifier_type_item : __name}
        f = open(info_name,'w')
        json.dump(info_dic, f)

        logging.info("Finished training!")


    def predict(self, feature):
        """This get document and returns mistakes."""
        assert isinstance(feature, dict)
        label, value = self.classifier.predict(feature)
        return label, value

