package jp.ac.nii.ednii;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.ac.nii.ednii.ml.Instance;
import jp.ac.nii.ednii.ml.PrepositionSelector;

import com.googlecode.jweb1t.JWeb1TSearcher;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.trees.Tree;

public class PresenceClassifierModel implements ErrorCorrectionInterface {
	public static final String HASPREP_YES = "YES";
	public static final String HASPREP_NO = "NO";
	public static final String PREP_REMAIN = "OK";
	public static final String PREP_DELETE = "DEL";
	public static final String PREP_UNKOWN = "UNK";
	private static final List PREP_LIST = new ArrayList();
	static{
		String[] myStringArray = new String[]{"on", "in", "at", "for", "to", "of", "about"
				//"about", "above", "across", "after", "against", "along", "among", "around", "at", "before", "behind", "below", 
				//"beneath", "beside", "between", "beyond", "but", "by", "despite", "down", "during", "except", "for", "from", "in", "inside",
				//"into", "like", "near", "of", "off", "on", "onto", "out", "outside", "over", 
				//"past", "since", "through", "throughout", "till", "to", "toward", "under", "underneath", "until", "up", "upon", "with", "within", "without"
				};
		Collections.addAll(PREP_LIST, myStringArray);
	}
	/**
	 * args[0] = train/predict
	 * args[1] = .prop file
	 * args[2] = .train (train)
	 * 	       = .model (predict)
	 * args[3] = .model (train)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ColumnDataClassifier cdc = new ColumnDataClassifier(args[1]
		);
		
		if (args[0].equals("train")){
			Classifier<String, String> cl =
			// cdc.makeClassifier(cdc.readTrainingExamples("/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/cheeseDisease.train"));
			cdc.makeClassifier(cdc
					.readTrainingExamples(args[2]));
			
			// serialize model
			try {
				// use buffering
				OutputStream file = new FileOutputStream(args[3]);
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);
				try {
					output.writeObject(cl);
					System.out.println("SUCCESFULLY SERIALIZING MODEL!");
				} finally {
					output.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			
		}
		else if (args[0].equals("predict")){
			//deserialize model
			try {
				// use buffering
				InputStream file = new FileInputStream(args[2]);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);
				try {
					// deserialize the model
					Classifier<String, String> lc = (Classifier<String, String>) input
							.readObject();
					
					System.out.println("SUCCESFULLY DESERIALIZING MODEL!");
					
					String line = "?			the_book	DT_NN	null	null	null	finished	VBD	null	null	cc	null	null	null	null	null	null	null	null	null	null	null";
					// String line =
					// "?			I_do	PRP_VBP	null	null	null	study	VB	null	null	cc	null	null	null	null	null	null	null	null	null	null	null";
					Datum<String, String> d = cdc.makeDatumFromLine(line, 0);
					System.out.println(line);
					System.out.println("  ==>  " + lc.classOf(d));
					
					
				} finally {
					input.close();
				}
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		/*
		 * for (String line : ObjectBank.getLineIterator(
		 * "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/cheeseDisease.test"
		 * )) { Datum<String,String> d = cdc.makeDatumFromLine(line, 0);
		 * System.out.println(line + "  ==>  " + cl.classOf(d)); }
		 */

	
	}
	
	private LexicalizedParser lp;
	private String mode;
	private PrepositionSelector prepClassifier;
	private PrintWriter instancesFile;
	private ColumnDataClassifier cdc = null;
	private Classifier<String, String> lc = null;
	private LinearClassifier classifier = null;
	
	public PresenceClassifierModel(String mode, PrintWriter instancesFile, String propFile, String weightFile) {		
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		this.mode = mode;
		prepClassifier = new PrepositionSelector(false, null);
		this.instancesFile = instancesFile;
		if (mode.startsWith("predict")){//predict mode; then load the model from modelFile path			
			//deserialize model
			try {
				// use buffering
				System.out.println("DEBUG ClassifierModel - Constructor - PropFile: " + weightFile);
				InputStream file = new FileInputStream(weightFile);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);
				try {
					System.out.println(propFile);
					this.cdc = new ColumnDataClassifier(propFile);				
					
					// deserialize the model
					this.lc = (Classifier<String, String>) input
							.readObject();
					
					System.out.println("SUCCESFULLY DESERIALIZING MODEL!");
					
					//String line = "?			the_book	DT_NN	null	null	null	finished	VBD	null	null	cc	null	null	null	null	null	null	null	null	null	null	null";
					// String line =
					// "?			I_do	PRP_VBP	null	null	null	study	VB	null	null	cc	null	null	null	null	null	null	null	null	null	null	null";
					//Datum<String, String> d = cdc.makeDatumFromLine(line, 0);
					//System.out.println(line);
					//System.out.println("  ==>  " + lc.classOf(d));
					
					
				} finally {
					input.close();
				}
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			
			
			classifier = LinearClassifier.readClassifier(weightFile);
		}
	}	
	
	public ArrayList<LearnerError> corrects(LearnerDocument document){//train = false: apply mode
		System.out.println("DEBUG CLASSIFIER MODEL - corrects document : " + document);
		ArrayList<LearnerError> ret = new ArrayList<LearnerError>();	
		ArrayList<String> textLines = document.getTextLines();
		ArrayList<ArrayList<Chunk>> chunks = document.getChunks();
		ArrayList<ArrayList<Token>> tokens = document.getTokens();
		ArrayList<LearnerError> goldErrors = document.getErrors();
		int numLine = textLines.size();
		for (int i = 0; i < numLine; i++) {
			ArrayList<LearnerError> errorsInSentence = correctSentence(document, textLines.get(i), i, tokens.get(i), chunks.get(i), this.mode, goldErrors);
			if (errorsInSentence != null)
				ret.addAll(errorsInSentence);
		}
		
		return ret;
	}
	
	public ArrayList<LearnerError> correctSentence(LearnerDocument document, String line, int lineInDoc, ArrayList<Token> tokens, ArrayList<Chunk> chunks, String mode, ArrayList<LearnerError> goldErrors){
		System.out.println("DEBUG CLASSIFIER MODEL - corrects sentence : " + line);
		ArrayList<LearnerError> ret = new ArrayList<LearnerError>();
		
		//collect preposition tokens and create a sentence for parsing	
		ArrayList<Integer> prepTokenIndexs = new ArrayList<Integer>();
		ArrayList<String> words = new ArrayList<String>();		
		for (int i = 0; i< tokens.size(); i++) {
			Token token = tokens.get(i);
			words.add(token.getString());
			
			if (token.getPostag().equals("IN") && PREP_LIST.contains(token.getString()))
				prepTokenIndexs.add(i);
		}
		
		//collect gold corrections in training mode
		ArrayList<String> prepTokenCorrections = new ArrayList<String>();
		if (mode.equals("train-esl")){
			for (Integer prepIndex : prepTokenIndexs) {
				Token token = tokens.get(prepIndex);
				int begin = token.getBegin();
				int end = token.getEnd();
				boolean found = false;
				for (LearnerError error : goldErrors) {
					if (error.getErrorType().equals("prp") && error.getBegin() == begin && error.getEnd() == end){
						prepTokenCorrections.add(error.getCorrection());
						found = true;
						break;
					}
				}
				if (!found)//this is a correct preposition
					prepTokenCorrections.add(PREP_REMAIN);
			}
		}else if (mode.equals("train-native")){
			for (Integer prepIndex : prepTokenIndexs) {
				Token token = tokens.get(prepIndex);
				int begin = token.getBegin();
				int end = token.getEnd();
				prepTokenCorrections.add(token.getString().toLowerCase());
			}
		}
		
		//parse this sentence		
		String[] sent = new String[words.size()];
		words.toArray(sent);
		List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
		Tree parse = lp.apply(rawWords);
		System.out.println("DEBUG CLASSIFIER MODEL - correct sentence - parse " + parse);
		
		//create instances
		if (mode.startsWith("train")){
			for (int j = 0; j < prepTokenIndexs.size(); j++) {
				Instance instance = this.prepClassifier.extractFeatureValues(prepTokenIndexs.get(j), lineInDoc, chunks, tokens, parse);	
				String label = prepTokenCorrections.get(j);
				if (label.equals(""))
					label = this.PREP_DELETE;
				
				instance.setLabel(label);
				//save instance
				Token token = tokens.get(prepTokenIndexs.get(j));
				this.instancesFile.append(printToString(instance, document, token));	
				this.instancesFile.append("\n");
			}
		}else if (mode.equals("predict-esl")){
			for (int j = 0; j < prepTokenIndexs.size(); j++) {
				Instance instance = this.prepClassifier.extractFeatureValues(prepTokenIndexs.get(j), lineInDoc, chunks, tokens, parse);
				instance.setLabel("UNK");	//unknown
				//save instance
				Token token = tokens.get(prepTokenIndexs.get(j));
				String dataLine = printToString(instance, document, token);
				System.out.println("DEBUG ClassifierModel - correctSentence - dataLine: " + dataLine);
				this.instancesFile.append(dataLine);
				this.instancesFile.append("\n");
				Datum<String, String> d = cdc.makeDatumFromLine(dataLine, 0);
				
				//System.out.println(line);
				//System.out.println("  ==>  " + lc.classOf(d));
				//String classOfdatLine = lc.classOf(d);
				//Counter<String> scoresOfdataLine = lc.scoresOf(d);
				
				//System.out.println("DEBUG ClassifierModel - correctSentence - classifier : " + classifier);
				String classOfdatLine2 = (String) classifier.classOf(d);
				
				//System.out.println("DEBUG ClassifierModel ==> " + classOfdatLine + " " + classOfdatLine2);
				//-----------------------------------------------
				//decoding, create LearnerErrors from classifier output
				if (classOfdatLine2.equals("DEL")){//delete this preposition
					LearnerError error = new LearnerError(token.getBegin(), token.getEnd(), true, "prp", "", lineInDoc);
					ret.add(error);
				}else if (!classOfdatLine2.equals("UNK") && !classOfdatLine2.equals("OK")){	//replace this preposition with another one
					LearnerError error = new LearnerError(token.getBegin(), token.getEnd(), true, "prp", classOfdatLine2, lineInDoc);
					ret.add(error);
				}
					
				
			}
		}else if (mode.equals("predict-native")){
			for (int j = 0; j < prepTokenIndexs.size(); j++) {
				Instance instance = this.prepClassifier.extractFeatureValues(prepTokenIndexs.get(j), lineInDoc, chunks, tokens, parse);
				instance.setLabel("UNK");	//unknown
				//save instance
				Token token = tokens.get(prepTokenIndexs.get(j));
				String dataLine = printToString(instance, document, token);
				System.out.println("DEBUG ClassifierModel - correctSentence - dataLine: " + dataLine);
				this.instancesFile.append(dataLine);
				this.instancesFile.append("\n");
				System.out.println("DEBUG ClassifierModel " + (cdc==null) + (dataLine== null));
				Datum<String, String> d = cdc.makeDatumFromLine(dataLine, 0);
				
				//System.out.println(line);
				//System.out.println("  ==>  " + lc.classOf(d));
				//String classOfdatLine = lc.classOf(d);
				//Counter<String> scoresOfdataLine = lc.scoresOf(d);
				
				//System.out.println("DEBUG ClassifierModel - correctSentence - classifier : " + classifier);
				String classOfdatLine2 = (String) classifier.classOf(d);
				
				//System.out.println("DEBUG ClassifierModel ==> " + classOfdatLine + " " + classOfdatLine2);
				//-----------------------------------------------
				//decoding, create LearnerErrors from classifier output
				LearnerError error = new LearnerError(token.getBegin(), token.getEnd(), true, "prp", classOfdatLine2, lineInDoc);
				ret.add(error);				
				
			}
		}
		
		return ret;
	}

	private String printToString(Instance instance, LearnerDocument document, Token prepToken){
		StringBuffer ret = new StringBuffer();
		String label = instance.getLabel();
		String[] featureValues = instance.getFeatureValues();
		
		//ret.append(document.getFileName());	//DEBUG
		//ret.append("_");	//DEBUG
		//ret.append(Integer.toString(prepToken.getLine()));	//DEBUG
		//ret.append("_");	//DEBUG
		//ret.append(prepToken.getString());		//DEBUG	
		//ret.append("\t");	//DEBUG
		ret.append(label);
		for (int i = 0; i < featureValues.length; i ++) {
			ret.append("\t");
			ret.append(prepClassifier.allFeatureNames.get(i));	//DEBUG
			ret.append("=");	//DEBUG
			ret.append(featureValues[i]);
		}
		
		return ret.toString();
	}
	
	
	
	private void writeToFile(Instance instance, PrintWriter file, LearnerDocument document, Token prepToken) {
		// TODO Auto-generated method stub
		file.append(printToString(instance, document, prepToken));
	}
}
