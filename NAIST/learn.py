#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""
__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"

import corrcha.constant
import logging
import corrcha.tool.logger

if __name__=='__main__':
    USAGE = """"""

    import optparse, sys
    oparser = optparse.OptionParser(usage=USAGE)

    oparser.add_option('-i', action="append", dest = 'input_filename', help='Converted corpus', default=[])
    oparser.add_option('-o', dest = 'output_filename', help='The path to save model files')

    oparser.add_option("--extract-only", action="store_true", dest="extract_only", default=False, help="just extract and save them. Don't train.")
    oparser.add_option("--extract-skip", action="store_true", dest="extract_skip", default=False, help="skip extraction and train with the input file.")

    oparser.add_option('-e', dest = 'handling_errors', help='[prep]')

    #for logging
    oparser.add_option('-l', '--log', dest='log', default='', help="Filename for log output",)
    oparser.add_option("--debug", action="store_true", dest="debug", default=False)

    oparser.add_option('--param', dest = 'parameter', help='Set parameters for the classifier', default="")
    (opts, args) = oparser.parse_args()

    corrcha.tool.logger.setup_logger(opts)

    if not (opts.input_filename and  opts.output_filename):
        logging.error("Designate input and output filename")
        quit()

    if not opts.handling_errors:
        logging.error("Designate handling_errors!")
        quit()


    #--- main ---#
    classifier = corrcha.constant.CLASSIFIER[None] #use default
    import corrcha.tool.util

    correcter_class_path = corrcha.constant.CORRECTER.get(opts.handling_errors, None)
    if correcter_class_path:
        _correcter_class = corrcha.tool.util.getClass(correcter_class_path)
        correcter = _correcter_class(classifier)
    else:
        logging.error("Error type %s not defined!" % opts.handling_errors)
        quit()



    #set temporary filename
    if not opts.extract_skip and not len(opts.output_filename) != 1:
        logging.error("You can designate one file.")
        quit()
    if opts.extract_only:
        tmp_file_name = opts.output_filename
    elif opts.extract_skip:
        tmp_file_name = opts.input_filename
    else:
        tmp_file_name = opts.output_filename + ".str"
    logging.info("Temporary file (folder) is [%s]" % tmp_file_name)

    #extraction
    if not opts.extract_skip:
        corpusname = opts.input_filename[0]
        logging.info("Opening the corpus... [%s]" % corpusname)
        corpus = corrcha.tool.serialize.read(corpusname)
        logging.info("Finished loading!")

        logging.info("Extracting...")
        correcter.extract(corpus, tmp_file_name, opts.parameter)
        logging.info("Finished writing")
    if opts.extract_only:
        quit()

    #training
    logging.info("Training...")
    correcter.train(tmp_file_name, opts.output_filename, opts.parameter)


    #save correcter name
    import corrcha.constant
    info_name = opts.output_filename + '/' + corrcha.constant.INFO_NAME
    import json
    try:
        f = open(info_name)
        info_dic = json.load(f)
        f.close()
        info_dic[corrcha.constant.CORRECTER_CLASS_PATH] = correcter_class_path
    except:
        info_dic = {corrcha.constant.CORRECTER_CLASS_PATH : correcter_class_path}
    f = open(info_name,'w')
    json.dump(info_dic, f)

