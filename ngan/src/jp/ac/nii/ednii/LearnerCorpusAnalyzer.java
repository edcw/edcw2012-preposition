package jp.ac.nii.ednii;

import java.util.ArrayList;
import java.util.HashMap;

public class LearnerCorpusAnalyzer {

	public static void analyzePrepositionErrors(LearnerDocument[] documents, String errorType){
		HashMap<String, Integer> prep2CountMap = new HashMap<String, Integer>();
		ArrayList<String> confuses = new ArrayList<String>();
		ArrayList<Integer> confuseCounts = new ArrayList<Integer>();
		int totalError = 0;
		for (LearnerDocument learnerDocument : documents) {
			ArrayList<String> textLines = learnerDocument.getTextLines();
			ArrayList<LearnerError> errors = learnerDocument.getErrors();
			for (LearnerError learnerError : errors) {
				if (learnerError.getErrorType().equals(errorType)){
					totalError++;
					String correction = learnerError.getCorrection().trim().toLowerCase();
					String original = textLines.get(learnerError.getLineInDoc()).substring(learnerError.getBegin(), learnerError.getEnd()).trim().toLowerCase();
					String confuse = original + "_" + correction;
					int indexOfConfuse = confuses.indexOf(confuse);
					if (indexOfConfuse > -1)
						confuseCounts.set(indexOfConfuse, confuseCounts.get(indexOfConfuse) + 1);
					else{
						confuses.add(confuse);
						confuseCounts.add(1);
					}
						
				}
			}
		}
		
		//print out statistics
		System.out.println("Total errors: " + totalError);
		System.out.println("CONFUSING PAIR \t TYPE \t COUNT \t PERCENTAGE");
		for (int i = 0; i < confuses.size(); i++) {
			String confuse = confuses.get(i);
			String confuseType = null;
			if (confuse.startsWith("_"))
				confuseType = "insert";
			else if (confuse.endsWith("_"))
				confuseType = "delete";
			else
				confuseType = "replace";
			System.out.println(confuses.get(i) + "\t" + confuseType + "\t" + confuseCounts.get(i) + "\t" + ((double)confuseCounts.get(i)/totalError));
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//load KJ corpus
		LearnerDocument[] documents = LearnerDocument.loadACollectionFromFolderRecursively("/home/nltngan/Research/EDCW/data/dryrun/KJCorpus", "txt");
		//LearnerDocument[] documents = LearnerDocument.loadACollectionFromFolderRecursively("/home/nltngan/Research/EDCW/data/formalrun/KJFormalrun", "txt");
		analyzePrepositionErrors(documents, "v_agr");
		System.out.println("Done.");
	}

}
