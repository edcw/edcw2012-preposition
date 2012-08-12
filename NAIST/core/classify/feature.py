#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""


__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.corpus.document
import corrcha.tool.serialize

#class Elements(list):
#    def writeFile(self, filename, isTrainTargetToken, getMyLabel=None):
#        f = open(filename, 'w')
#        for el in self:
#            if not(isTrainTargetToken(el)):
#                continue
#            if getMyLabel:
#                f.write(u"%s\t" % getMyLabel(el))
#            else:
#                f.write(u"%s\t" % el.getLabel())

#            for (key, val) in el.getFeature().items():
#                f.write(u"%d:%d " % (key, val))
#            f.write(u"\n")
#        f.close()

#    def __unicode__(self):
#        dump = u""
#        for el in self:
#            dump += u"%s\n" % unicode(el)
#        return dump

#class ElementsFromFiles(Elements):
#    """The class to handle files serialized from Elements"""
#    def __init__(self, filenames=None):
#        Elements.__init__(self)
#        if filenames is None:
#            self.__files = []
#        else:
#            assert isinstance(filenames, list)
#            self.__files = filenames

#    def __iter__(self):
#        for f in self.__files:
#            elements = corrcha.tool.serialize.read(f)
#            for el in elements:
#                yield el


#import threading
#LOCK = threading.Lock()
#class FeatureIdMap(object):
#    def __init__(self):
#        self.__used_id = 0
#        self.__used_dictname = []
#        self.__dictmap = {}
#        pass

#    def getNewID(self):
#        """Be sure to fix, when you use multi thread or process."""
#        self.__used_id += 1
#        id = self.__used_id
#        return id

#    def isUsedDictName(self, name):
#        """Be sure to fix, when you use multi thread or process."""
#        return (name in self.__used_dictname)

#    def getID(self, dictname, string):
#        """Return id which depends on given uuid and string.
#        If there isn't, this function generates and returns new ID."""
#        with LOCK:
#            dictionary = self.__dictmap.get(dictname)
#            if dictionary is None:
#                self.__used_dictname.append(dictname)
#                id = self.getNewID()
#                self.__dictmap[ dictname ] = {string:id}
#            else:
#                id = dictionary.get(string)
#                if id is None:
#                    id = self.getNewID()
#                    dictionary[ string ] = id

#        return id


#import threading
#LOCK = threading.Lock()
#        with LOCK: #???
import multiprocessing
class FeatureIdMap(object):
    def __init__(self, filename=None):
        manager = multiprocessing.Manager()
        self.__used_id = manager.Value('i', 0)
        self.__dictmap = manager.dict()

        if filename is not None:
            data = corrcha.tool.serialize.read(filename)
            self.setData(data)

    def getData(self):
        ret = {}
        for k,v in self.__dictmap.items():
            ret[k] = v
        return (self.__used_id.value, ret)

    def setData(self, data):
        assert isinstance(data, tuple)
        assert len(data) == 2
        self.__used_id.value += data[0]
        for k,v in data[1].items():
            self.__dictmap[k] = v
        
    def getNewID(self):
        """Be sure to fix, when you use multi thread or process."""
        self.__used_id.value += 1
        id = self.__used_id.value
        return id

    def getID(self, dictname, string):
        """Return id which depends on given uuid and string.
        If there isn't, this function generates and returns new ID."""
        key = unicode(dictname) + u"\t" + unicode(string)
        id = self.__dictmap.get(key)
        if id is None:
            id = self.getNewID()
            self.__dictmap[ key ] = id
        return id

    def __unicode__(self):
        import sys
        dump = u""
        leng = len(self.__dictmap)
        for i, (k,v) in enumerate(self.__dictmap.items()):
            dump += u"%s\t%s\n" % (v, k.replace(u"\t", u":"))
        return dump


if __name__=='__main__':
    USAGE = """"""
    DESCRIPTION = """Inspect serialized file"""

    import optparse, sys
    oparser = optparse.OptionParser(usage=USAGE, description=DESCRIPTION)
    oparser.add_option('-i', dest = 'input_filename', help='')
    (opts, args) = oparser.parse_args()


    if opts.input_filename:
        import corrcha.tool.serialize
        data =  corrcha.tool.serialize.read(opts.input_filename)
        for k,v in data[1].items():
            print unicode("%s   %s"%(v, k)).encode('utf-8')
    else:
        oparser.print_help()

