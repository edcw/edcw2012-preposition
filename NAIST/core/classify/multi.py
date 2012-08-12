#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""


__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

try:
    from maxent import MaxentModel
except ImportError:
    from pymaxent import MaxentModel

import corrcha.core.classify.classifier

class Maxent(corrcha.core.classify.classifier.Classifier):
    def __init__(self, model_file_name=None):
        if model_file_name is not None:
            assert isinstance(model_file_name, (str, unicode))
            self.load(model_file_name)
        else:
            self.model = None

    def load(self, model_file_name):       
        assert isinstance(model_file_name, (str, unicode))
        self.model = MaxentModel()
        self.model.load(model_file_name)

    def save(self, model_file_name):
        assert isinstance(model_file_name, (str, unicode))
        self.model.save(model_file_name)

    def predict(self, x):      
#        assert isinstance(x, dict)
#        result = self.model.eval_all(x)
        context = [("%d"%k, v)  for (k,v) in x.items()]
        label = self.model.predict(context)
        value = self.model.eval(context, label)
#        assert isinstance(label, int)
        return label, value

    def _add_me_from_file(self, filename, me):
        f = open(filename, 'r')
        x = []
        y = []
        for line in f:
            items = line.split("\t")
            label = items[0]
            context = [(k, float(v))  for (k, v) in (pair.split(":") for pair in items[1].split()) ]
            me.add_event(context, label)

    def train(self, x, y=None, param=None):

        me = MaxentModel()
        me.begin_add_event()

        if y is None:
            assert isinstance(x, str)
            self._add_me_from_file(x, me)
        else:
            assert isinstance(x, (list, tuple))
            assert isinstance(y, (list, tuple))
            for i, label in enumerate(y):
                context = [("%d"%k, v)  for (k,v) in x[i].items()]
                me.add_event(context, label)
        cutoff = 1
        me.end_add_event(cutoff)
        me.train(100, 'lbfgs', 0.0)
        self.model = me


