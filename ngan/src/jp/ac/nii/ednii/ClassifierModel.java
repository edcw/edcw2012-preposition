package jp.ac.nii.ednii;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jp.ac.nii.ednii.ml.Instance;
import jp.ac.nii.ednii.ml.PrepositionSelector;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

public class ClassifierModel implements ErrorCorrectionInterface {
	public static final String PREP_REMAIN = "OK";
	public static final String PREP_DELETE = "DEL";
	public static final String PREP_UNKOWN = "UNK";
	private static final List PREP_LIST = new ArrayList();
	static{
		String[] myStringArray = new String[]{"to", "in", "at", "for", "as", "of", "by", "with", "on", "into", "since", "like", "from", "about", "during", "including", "than", "throughout", 
				"according to", "until", "throgh", "around", "thanks to", "such", "such as", "out of", "among", "through"};	//KJ-train
		
		//String[] myStringArray = new String[]{"on", "in", "at", "for", "to", "of", "about"	//top prepositions
				//"about", "above", "across", "after", "against", "along", "among", "around", "at", "before", "behind", "below", 
				//"beneath", "beside", "between", "beyond", "but", "by", "despite", "down", "during", "except", "for", "from", "in", "inside",
				//"into", "like", "near", "of", "off", "on", "onto", "out", "outside", "over", 
				//"past", "since", "through", "throughout", "till", "to", "toward", "under", "underneath", "until", "up", "upon", "with", "within", "without"
				//};
		Collections.addAll(PREP_LIST, myStringArray);
	}
		
	private LexicalizedParser lp;
	private String mode;
	private PrepositionSelector prepClassifier;
	private PrintWriter instancesFile;
	private ColumnDataClassifier cdc = null;
	private Classifier<String, String> lc = null;
	private LinearClassifier classifier = null;
	
	public ClassifierModel(String mode, PrintWriter instancesFile, String propFile, String weightFile) {		
		lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		this.mode = mode;
		prepClassifier = new PrepositionSelector(false, null);
		this.instancesFile = instancesFile;
		if (mode.startsWith("predict")){//predict mode; then load the model from modelFile path			
			//deserialize model
			try {								
				InputStream file = new FileInputStream(weightFile);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);
				try {
					//System.out.println(propFile);
					this.cdc = new ColumnDataClassifier(propFile);				
					
					// deserialize the model
					this.lc = (Classifier<String, String>) input
							.readObject();
					
					//System.out.println("Successfully deserialized classifier model.");
					
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
		}else if (mode.equals("predict-esl-esl")){
			for (int j = 0; j < prepTokenIndexs.size(); j++) {
				Instance instance = this.prepClassifier.extractFeatureValues(prepTokenIndexs.get(j), lineInDoc, chunks, tokens, parse);
				instance.setLabel("UNK");	//unknown
				//save instance
				Token token = tokens.get(prepTokenIndexs.get(j));
				String dataLine = printToString(instance, document, token);
				
				this.instancesFile.append(dataLine);
				this.instancesFile.append("\n");
				Datum<String, String> d = cdc.makeDatumFromLine(dataLine, 0);
				
			
				String classOfdatLine2 = (String) classifier.classOf(d);
				
				
				//-----------------------------------------------
				//decoding, create LearnerErrors from classifier output
				if (classOfdatLine2.equals("DEL")){//delete this preposition
					LearnerError error = new LearnerError(token.getBegin(), token.getEnd(), true, "prp", "", lineInDoc);
					ret.add(error);
				}else if (!classOfdatLine2.equals("UNK") && !classOfdatLine2.equals("OK") && !classOfdatLine2.equals(token.getLemma())){	//replace this preposition with another one
					LearnerError error = new LearnerError(token.getBegin(), token.getEnd(), true, "prp", classOfdatLine2, lineInDoc);
					ret.add(error);
				}
					
				
			}
		}if (mode.equals("predict-native-esl")){
			for (int j = 0; j < prepTokenIndexs.size(); j++) {
				Instance instance = this.prepClassifier.extractFeatureValues(prepTokenIndexs.get(j), lineInDoc, chunks, tokens, parse);
				instance.setLabel("UNK");	//unknown
				//save instance
				Token token = tokens.get(prepTokenIndexs.get(j));
				String dataLine = printToString(instance, document, token);
				
				this.instancesFile.append(dataLine);
				this.instancesFile.append("\n");
				Datum<String, String> d = cdc.makeDatumFromLine(dataLine, 0);
				
				
				
				String bestLabel = (String) classifier.classOf(d);
				Collection<String> asFeatures = d.asFeatures();
				String esllabel = d.label();
				Collection labels = classifier.labels();
				
				
				if (labels.contains(esllabel)){
					double scoreOfEslLabel = classifier.scoreOf(d, esllabel);
					double scoreOfBestLabel = classifier.scoreOf(d, bestLabel);
				
					if (scoreOfBestLabel > scoreOfEslLabel){
						LearnerError error = new LearnerError(token.getBegin(), token.getEnd(), true, "prp", bestLabel, lineInDoc);
						ret.add(error);
					}	
				}
				
			}
		}else if (mode.equals("predict-native-native")){
			for (int j = 0; j < prepTokenIndexs.size(); j++) {
				Instance instance = this.prepClassifier.extractFeatureValues(prepTokenIndexs.get(j), lineInDoc, chunks, tokens, parse);
				instance.setLabel("UNK");	//unknown
				//save instance
				Token token = tokens.get(prepTokenIndexs.get(j));
				String dataLine = printToString(instance, document, token);
			
				this.instancesFile.append(dataLine);
				this.instancesFile.append("\n");
				
				Datum<String, String> d = cdc.makeDatumFromLine(dataLine, 0);
				
				
				String classOfdatLine2 = (String) classifier.classOf(d);
				
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
