#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
"""

__author__ = 'Yuta Hayashibe' 
__version__ = ""
__copyright__ = ""
__license__ = "GPL v3, MIT"


import logging
import sys


def setup_logger(opts):
    if opts.log:
        hdlr = logging.FileHandler(opts.log, 'a')
    else:
       hdlr = logging.StreamHandler(sys.stderr)
    formatter = logging.Formatter('%(asctime)s %(name)-12s %(levelname)-8s %(message)s')
    hdlr.setFormatter(formatter)
    logger = logging.getLogger('')
    logger.addHandler(hdlr)
    if opts.debug:
        logger.setLevel(logging.DEBUG)
    else:
        logger.setLevel(logging.INFO)
    # setup error output
#    hdlr = logging.StreamHandler()
#    hdlr.setLevel(logging.ERROR)
#    hdlr.setFormatter(formatter)
#    logger.addHandler(hdlr)

if __name__=='__main__':
    pass
