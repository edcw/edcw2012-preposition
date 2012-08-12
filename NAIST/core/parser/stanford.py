#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.core.singleton
import jpype
import corrcha.tool.myjpype

import corrcha.corpus.sentence
import corrcha.corpus.token
import corrcha.core.parser.parser

class StanfordParser(corrcha.core.parser.parser.Parser):
    __metaclass__= corrcha.core.singleton.Singleton
    PACKAGE_NAME = "edu.stanford.nlp"
    LEX_PARSER = "edu.stanford.nlp.parser.lexparser.LexicalizedParser"
    DEFAULT_OPTION = \
            ["-retainTmpSubcategories", #get best performance in recognizing temporal dependencies \
#             "-makeCopulaHead", \
            ]

    def __init__(self, jar_path, pcfg_model_fname, option=DEFAULT_OPTION):
        assert type(jar_path) is unicode
        assert type(pcfg_model_fname) is unicode
        assert isinstance(option, (list, tuple))
        corrcha.tool.myjpype.addClassPath(jar_path)
        self.pcfg_model_fname =  pcfg_model_fname
        self.parser_class = jpype.JClass(self.LEX_PARSER)
        self.parser = self.parser_class.getParserFromSerializedFile(self.pcfg_model_fname)

        self.parser.setOptionFlags(option)

        self.package = jpype.JPackage(self.PACKAGE_NAME)
        tokenizerFactoryClass = self.package.process.__getattribute__("PTBTokenizer$PTBTokenizerFactory")
        self.tokenizerFactory = tokenizerFactoryClass.newPTBTokenizerFactory(True, True)

        self.puncFilter = self.package.trees.PennTreebankLanguagePack().punctuationWordRejectFilter()
        if "-makeCopulaHead" in option:
            self.headFinder = self.package.trees.SemanticHeadFinder(False)
        else:
            self.headFinder = self.package.trees.SemanticHeadFinder(True)


    def __unicode__(self):
        buf = u""
        buf += "ParserPack is %s" % self.parser.op.tlpParams.getClass()
        buf += "PARSER_TYPE : %s" % PARSER_TYPE
        buf += "PACKAGE_NAME : %s" % PACKAGE_NAME
#        self.parser.op.display()
#        print "Test parameters"
#        self.parser.op.testOptions.display();
#        self.parser.op.tlpParams.display();
        return buf

    def _parse(self, sentence):
        """
        Parses the sentence string, returning the tokens, and the parse tree as a tuple.
        """
        assert type(sentence) is unicode
        _tokenizer = self.tokenizerFactory.getTokenizer(  jpype.java.io.BufferedReader( jpype.java.io.StringReader( sentence) )  )
        tokens = _tokenizer.tokenize();
        
        if len(tokens) == 0:
            return None, tokens

        pq = self.parser.parserQuery()
        wasParsed = pq.parse(tokens)
        if not wasParsed:
            raise
        bestparse = pq.getBestParse()
        bestparse.setScore(pq.getPCFGScore() % -10000.0) #-10000 denotes unknown words
        return bestparse, tokens
    
    
    def _parse_tree(self, tree, parsed_sentence, tokenized, parentID, lastID):
        #use changeability for lastID
        assert type(tree) is jpype.JPackage('edu').stanford.nlp.trees.LabeledScoredTreeNode
        assert type(tokenized) is list
        assert type(parentID) is int
        assert type(lastID) is list
        assert type(lastID[0]) is int
        
        kids = tree.children();
        lastID[0] += 1
        this_tree_id = lastID[0]
        this_node = corrcha.corpus.token.Node(this_tree_id, tree.label().category(), parentID)
        parsed_sentence.appendNode(this_tree_id, this_node)
        if ( len(kids) == 1 and kids[0].isLeaf()):
            leaf = ( this_tree_id, kids[0].label().word() )
            tokenized.append( leaf );
        else:
            for i, kid in enumerate(kids):
                self._parse_tree(kid, parsed_sentence, tokenized, this_tree_id, lastID)

    def parse(self, sentence):
        """Parse a raw sentence with Stanford parser.
        This returns an array of tokens, a list of tuples which contain a 'tree-tag' and 'parent tree id',
        and a list of typed dependencies.
        """
        assert type(sentence) is unicode
        parsed_sentence = corrcha.corpus.sentence.ParsedSentence(sentence)

        tree, tokens = self._parse(sentence)
        if tree is not None:
            result = self.package.trees.EnglishGrammaticalStructure(tree, self.puncFilter, self.headFinder, True)
            tokenized = []
            self._parse_tree(tree, parsed_sentence, tokenized, 0, [0])
            #           #set offset
            for i, t in enumerate(tokenized):
                position = tokens[i].beginPosition()
                #tokens[i].endPosition() )
                parsed_token = corrcha.corpus.token.Token(t[0], t[1], position )
                parsed_sentence.append(parsed_token)

#            for dependency in result.typedDependenciesCollapsedTree(): #TODO enable switching?
            for dependency in result.typedDependencies():
                #http://reason.cs.uiuc.edu/mtyoung/parser/javadoc/edu/stanford/nlp/trees/TypedDependency.html
                rel = unicode(dependency.reln())
                gov = dependency.gov().index() - 1
                dep = dependency.dep().index() - 1
                
                #gov & dep ARE NOT the number of tree-id but the index of the array
                parsed_sentence.appendRelation(gov, dep, rel)

        return parsed_sentence

if __name__=='__main__':
    import sys
    options = sys.argv[:]
    options.pop(0) #remove this filename
    options = tuple(options)

    import corrcha.tool.setting

    model_fname = corrcha.tool.setting.val['stanford']['model']
    jar_path = corrcha.tool.setting.val['stanford']['jar']
    parser = StanfordParser(jar_path, model_fname, options)
#    parser.printInfo()


    lines=[ "Mr. Smith is interested on music.",
            "I gave you the watch.",
            "I down a shot of tequila of bot.",
            u"にほんご is difficult to learn for me.",
            "" # length is 0
            ]

    import sys
    enc = sys.stdin.encoding
    if enc is None:
        enc = sys.getfilesystemencoding()

    for line in iter(sys.stdin.readline, ""):
#    for line in lines:
        try:
            parsd_sentence = parser.parse(unicode(line.strip(), enc))
            print unicode(parsd_sentence)
        except:
            raise
            sys.stderr.write("[*] Parsing Error!!\n")

    quit()

