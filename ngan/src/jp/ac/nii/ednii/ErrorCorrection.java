package jp.ac.nii.ednii;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import edu.smu.tspell.wordnet.WordNetDatabase;

/**
 * ErrorCorrection class allows to execute different error detection models for three EDCW tracks.
 * - Discourse model: rule-based model for detection of article errors, subject-verb agreement errors, and verb tense errors. 
 * - Classifier model: is a reimplementation of Tetreault model (http://www.aclweb.org/anthology-new/P/P10/P10-2065.pdf)
 *  
 * @author nltngan
 * @version 0.1
 */
public class ErrorCorrection {
	public static final int DISCOURSE_MODEL = 1;
	public static final int ISLAM_MODEL = 2;
	
	/**
	 * Detects errors in a document.
	 * 
	 * @param wordnetDatabase
	 * @param learnerErrorFile
	 * @param poschunkedFile
	 * @param savetoPath
	 * @param model
	 */
	public static void correctAFile(WordNetDatabase wordnetDatabase, String learnerErrorFile, String poschunkedFile, String savetoPath, ErrorCorrectionInterface model){
		LearnerDocument learnerDocument = new LearnerDocument(wordnetDatabase,
				new File(learnerErrorFile),
				new File(poschunkedFile));
		//System.out.println(learnerDocument.toString());
		
		
		ArrayList<LearnerError> errors = correctADocument(learnerDocument, model);
		
		//create output file
		LearnerDocument.saveToFile(learnerDocument.getTextLines(), errors, savetoPath);
	}

	/**
	 * Detects errors in a collection of documents.
	 * 
	 * @param wordnetDatabase
	 * @param dir
	 * @param errorFileExt
	 * @param poschunkFileExt
	 * @param savetoDir
	 * @param model
	 */
	public static void correctACollection(WordNetDatabase wordnetDatabase, String dir, String errorFileExt, String poschunkFileExt, String savetoDir, ErrorCorrectionInterface model){
		
		LearnerDocument[] documents = LearnerDocument.loadACollectionFromFolderRecursivelyWithPosChunk(dir, errorFileExt, poschunkFileExt, wordnetDatabase);
		for (int i = 0; i < documents.length; i++) {			
			ArrayList<LearnerError> errors = correctADocument(documents[i], model);
			
			//post processing errors: remove errors that contain texts but inside and outside texts are continuous like <error>can</error><error>not</error>.
			postProcessing(errors, documents[i]);
 			
			//create output file
			LearnerDocument.saveToFile(documents[i].getTextLines(), errors, savetoDir + "/" + documents[i].getFileName() + ".sys");
			
		}
	}
	
	/**
	 * Removes invalid errors from the error set. 
	 * Invalid errors are those that do not conform to evaluation format or criteria.
	 * 
	 * @param errors
	 * @param learnerDocument
	 */
	private static void postProcessing(ArrayList<LearnerError> errors,
			LearnerDocument learnerDocument) {
		// TODO Auto-generated method stub
		ArrayList<String> textLines = learnerDocument.getTextLines();
		int size = errors.size();
		for (int i = size - 1; i >= 0; i--) {
			LearnerError learnerError = errors.get(i);
			int begin = learnerError.getBegin();
			int end = learnerError.getEnd();
			boolean removeThis = false;
			if (end > begin){
				int lineInDoc = learnerError.getLineInDoc();
				String line = textLines.get(lineInDoc);
				if (begin > 0)
					if (line.charAt(begin - 1) != ' ')
						removeThis = true;
				
				if (end < line.length())
					if (line.charAt(end) != ' ')
						removeThis = true;
				
			}
			
			if (removeThis)
				errors.remove(i);
		}
	}


	/**
	 * Detects errors in a document.
	 * 
	 * @param wordnetDatabase
	 * @param dir
	 * @param errorFileExt
	 * @param poschunkFileExt
	 * @param savetoDir
	 * @param model
	 */
	public static ArrayList<LearnerError> correctADocument(LearnerDocument learnerDocument, ErrorCorrectionInterface model){
		ArrayList<LearnerError> ret = new ArrayList<LearnerError>();
		//correct verb tense using DiscourseModel
		ArrayList<LearnerError> errors1 = null;
		
		errors1 = model.corrects(learnerDocument);
		/*
		for (LearnerError learnerError : errors1) {
			System.out.println(learnerError.toString());
		}
		*/
		
		ret.addAll(errors1);		
		
		
		return ret;
	}
	
	/**
	 * args[0] - model type: discourse, classifier, or islam.
	 * args[1]
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Version 1.0");
		System.out.println("Author: Ngan Nguyen");
		
		//read LearnerDocument error file and poschunk file
		WordNetDatabase wordnetDatabase = WordNetDatabase.getFileInstance();
		
		PrintWriter instanceFile = null;
		boolean train = false;
		int nextpos = 0;
		ErrorCorrectionInterface model = null;
		if (args[0].equals("discourse")){
			model = new DiscourseModel();
			nextpos = 1;
		}else if (args[0].equals("islam")){			
			model = new IslamModel(args);			
			nextpos = 6;
		}else if (args[0].equals("classifier")){
			
			try {
				instanceFile = new PrintWriter(new File(args[2]));
				if (args[1].startsWith("train")){//args[1] = train/apply; args[2] = instanceFilePath; args[3] = modelFilePath				
					model = new ClassifierModel(args[1], instanceFile, null, null);	
					nextpos = 3;
					train = true;
				}else if (args[1].startsWith("predict")){//args[1] = train/apply; args[2] = instanceFilePath; args[3] = model property file, args[4] = serialized model file
					model = new ClassifierModel(args[1], instanceFile, args[3], args[4]);
					nextpos = 5;
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
		}
		
		System.out.println();
		
		if (model != null){
			if (args[nextpos].equals("document")){
				if (args[1].startsWith("train"))
					System.out.println("Create training data from a DOCUMENT for " + args[0].toUpperCase() + " model ...");
				else
					System.out.println("Detecting errors in a DOCUMENT using " + args[0].toUpperCase() + " model ...");
				correctAFile(wordnetDatabase, 
						args[nextpos + 1],
						args[nextpos + 2],
						args[nextpos + 3],						     
						model);
				
			}else if (args[nextpos].equals("collection")){
				if (args[1].startsWith("train"))
					System.out.println("Create training data from a COLLECTION for " + args[0].toUpperCase() + " model ...");
				else
					System.out.println("Detecting errors in a COLLECTION of documents using " + args[0].toUpperCase() + " model ...");
				correctACollection(wordnetDatabase, args[nextpos + 1],
							args[nextpos + 2],
							args[nextpos + 3],		
							args[nextpos + 4],
						model);
			}
			
		}
		
		if (instanceFile != null){
			instanceFile.close();
		}
		System.out.println("Done.");
	}

}
