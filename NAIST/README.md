# CorrCha

CorrCha is the English correction system.



## System requirement

You have to prepare following systems.

* Python 2.x
    * You have to set the environment variable $PYTHONPATH to the parent directory of CorrCha
* [jpype](http://jpype.sourceforge.net/)
    * You have to set the environment variable $JAVA_HOME
* [The Stanford Parser 2.x](http://nlp.stanford.edu/software/lex-parser.shtml)
    * Please unzip the model files "stanford-parser-DATE-models.jar"
* [NLTK](http://www.nltk.org/)
    * You need sentence tokenizer 'tokenizers/punkt/english.pickle'
    * You need WordNet 'corpora/wordnet'
* [Maximum Entropy Modeling Toolkit for Python and C++](https://github.com/lzhang10/maxent)
* [SSGNC](http://code.google.com/p/ssgnc/)
    * [ssgnc-python](https://github.com/shirayu/ssgnc-python)
    * You also need the data (Indexed N-gram)
* [Classpath Loader](http://blog.daisukeyamashita.com/post/207.html)
    * Save it to ``tool/ClassPathModifier.java`` and compile it


### Recommended
* sun-java6-jdk


##Supported Corpus
* KJ

## Configuration

The configuration file is 'setting.json'.
Please set the proper string in it.
Please use absolute paths.

The priority order of reading a setting file is the following.
-The path in environment variable "CORRCHARC"
-".corrcharc" in your home directory.
-"setting.json" in the root of CorrCha

##How to use
* Make a binary from a corpus.
 * (e.g.) ` ./tool/converter.py -i data/ -o corpus.bin -m kj `
* Train a model
 * (e.g.) `python -O ./learn.py -i corpus.bin -o model -e prep-rep`
 * learn.py has two main options.
 * `--extract-only` generates intermediate file(s) to the path designated with `-o` options
     *  `python -O ./learn.py -i corpus.bin -o inter_file --extract-only -e prep-rep`
 * `--extract-skip` generates model files from intermediate file(s)
     *  `python -O ./learn.py -i inter_file -o model --extract-skip -e prep-rep`
     * You can designate more than one file as input only with this option

