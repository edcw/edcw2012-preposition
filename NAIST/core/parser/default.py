#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


def getDefaultParser():
    import corrcha.tool.setting
    default_name = corrcha.tool.setting.val['default']['parser']


    if default_name == u"stanford":
        import corrcha.core.parser.stanford
        model_fname = corrcha.tool.setting.val['stanford']['model']
        jar_path = corrcha.tool.setting.val['stanford']['jar']
        return corrcha.core.parser.stanford.StanfordParser(jar_path, model_fname)

    elif default_name == u"dummy":
        import corrcha.core.parser.dummy
        return corrcha.core.parser.dummy.DummyParser()
    else:
        raise KeyError


if __name__=='__main__':
    pass


