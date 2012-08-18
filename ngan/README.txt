EDNII
Version: 1.0
Authors: Ngan L.T. Nguyen, Yusuke Miyao

----Introduction----
EDNII is an error detection tool for ESL student essays. EDNII was originally developed for the Error Detection and Correction Workshop 2012 (EDCW 2012). EDNII is released under MIT License.

EDNII implements three different error detection methods: discourse-based, classifier-based, and language model based method, which are called Discourse Model, Classifier Model and Islam Model correspondingly. For the dry run, Discourse Model were used for the Verb-agreement track and Open track, and Islam Model was used for the Preposition track. However, for the formal run, Classifier Model was used in place of Islam Model.
Discourse Model uses rules for both sentence analysis and error detection, based on POS-tagging and chunking results. This model aims to detect subject-verb agreement errors (Verb-agreement track), verb tense, article, and noun number errors. All types of errors are included in the Open track. 

Classifier Model is a reimplementation of Tetreault et al.’s system described in [1]. Similarly, Islam Model is a reimplementation of Islam et al.’s system described in [2]. However, perhaps due to some differences in implementation, the EDNII’s performances are not so good as those reported in the papers. We suggest that further analysis should be done for these two models.


----File structure----
-bin: binary files
-src: source code
-lib: third-party libraries and data
-data: necessary data for running EDNII
--morphodic.dict: morphology dictionary file used by Discourse Model
-examples: sample files. Users can test the system by running it on this sample file structure. 
---train: sample training data (two documents in the EDCW training data)
---test: sample test data (two documents in the EDCW training data)
---classifier: Classifier Model 
-----model:
-------edcw-ngan.model: model used in EDCW 2012 dryrun.
-------edcw.prop: property file of the classifier in Stanford Classifier format.
-------esl-instances.train: instance file generated with train-esl option.
-------native-instances.train: instance file generated with train-native option.
-------native.model: output model (binary format) after training with Stanford Classifer.
-------native-model.txt: output model (text format) after training with Stanford Classifer.
-----output: error detection output by Classifier Model.
-----temp: unimportant output, can be ignored.
---discourse:
-----output: error detection output by Discourse Model.
---islam:
-----output: error detection output by Discourse Model.


----Performances in EDCW 2012----
Dry run: (F, P, R)
- Preposition track: 0.093	0.128	0.073
- Verb track:	0.416	0.619	0.313
- Open track:	0.230	0.310	0.183

Formal run: (F, P, R)
- Preposition track: 0.227	0.171	0.336
- Verb track: 0.432	0.571	0.348
- Open track: 0.122	0.192	0.089

----Contact----
ednii.tech@gmail.com


For further information, please read the User Manual.

