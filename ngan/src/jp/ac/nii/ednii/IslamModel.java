package jp.ac.nii.ednii;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.googlecode.jweb1t.JWeb1TSearcher;

/**
 * Islam model for correction of multiple types of errors.
 * This is a reimplementation of the paper "Correct different types of error in texts": 
 * http://www.site.uottawa.ca/~diana/publications/aminul_CAI2011-1.pdf
 * 
 * @author nltngan
 *
 */
public class IslamModel implements ErrorCorrectionInterface {
	
	
	public IslamModel(String[] gweb1Tindexfiles) {
		try {
			web1t = new JWeb1TSearcher (
					gweb1Tindexfiles[1],
					gweb1Tindexfiles[2],
					gweb1Tindexfiles[3],
					gweb1Tindexfiles[4],
					gweb1Tindexfiles[5]
					
				);
			
			System.out.println("Test JWeb1T " + web1t.getFrequency("I do n't know about"));
			
			System.out.println("Initialize JWeb1T finished");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//*/
	}



	public static final String ARTICLES[]={"a", "an", "the"};
	public static final String PREPOSITIONS[]={"on", "in", "at", "for", "to", "of", "about"};
	
	public static final int N = 3;
	private long[] maxFrequencies = new long[100];	//max frequency at each step, max step = 100
	
	private static JWeb1TSearcher web1t;	

	
	public ArrayList<LearnerError> corrects(LearnerDocument document){
		System.out.println("DEBUG ISLAM MODEL - corrects file : " + document.getFileName());
		ArrayList<LearnerError> ret = new ArrayList<LearnerError>();	
		ArrayList<String> textLines = document.getTextLines();
		ArrayList<ArrayList<Chunk>> chunks = document.getChunks();
		ArrayList<ArrayList<Token>> tokens = document.getTokens();
		int numLine = textLines.size();
		for (int i = 0; i < numLine; i++) {
			ArrayList<LearnerError> errorsInSentence = correctSentence(textLines.get(i), i, tokens.get(i), chunks.get(i));
			if (errorsInSentence != null)
				ret.addAll(errorsInSentence);
		}
		
		return ret;
	}
	
	public ArrayList<LearnerError> correctSentence(String line, int lineInDoc, ArrayList<Token> tokens, ArrayList<Chunk> chunks){
		System.out.println("DEBUG ISLAM MODEL - corrects sentence : " + line);
		ArrayList<LearnerError> ret = null;
		ArrayList<Token> allTokens = new ArrayList<Token>(tokens);	// new tokens will be appended to this array
		int orginialTokenLen = tokens.size();
		
		//generate candidate N-grams
		int numberOfStep = 0;
		ArrayList<ArrayList<FiveGram>> candidateFiveGrams;
		
		candidateFiveGrams = generateCandidateFiveGrams(line, lineInDoc, allTokens, chunks);
		numberOfStep = candidateFiveGrams.size();
		
		//concatenate candidate N-grams to form candidate texts
		ArrayList<IslamCandidateText> candidateTexts = generateCandidateTextsFromFiveGrams(candidateFiveGrams, allTokens, orginialTokenLen);
		
		//calculate scores for candidate texts, sort and select the best candidate text
		IslamCandidateText bestCandidate = scoreAndSortCandidateTexts(candidateTexts, allTokens, orginialTokenLen, numberOfStep);
		
		
		//create learner errors
		if (bestCandidate != null){
			System.out.println("DEBUG ISLAM MODEL - bestCandidate : " + getCandidateTextString(bestCandidate, allTokens));
			
			ret = createLearnerErrorsFromCandidateText(bestCandidate, allTokens, orginialTokenLen, lineInDoc);
		}
		
		//print out for debug
		if (candidateTexts != null){
			System.out.println("DEBUG ISLAM MODEL - candidate texts : ");
			
			for (IslamCandidateText islamCandidateText : candidateTexts) {
				System.out.println("DEBUG ISLAM MODEL ------------------"
						+ getCandidateTextString(islamCandidateText, allTokens));
			}
		}
		
		return ret;
	}
	
	private String getCandidateTextString(IslamCandidateText candi, ArrayList<Token> allTokens){
		
		StringBuffer sb = new StringBuffer();
		ArrayList<Integer> tokens = candi.getTokens();
		for (int i = tokens.size()-1 ; i >= 0; i--) {
			sb.append(allTokens.get(tokens.get(i)).getString());
			sb.append(" ");
		}
		sb.append(" [ S2= " + candi.getS2() + " S3= " + candi.getS3() + " S4= " + candi.getS4() + " S= " + candi.getS() + " ]");
		return sb.toString();
		
	}


	private IslamCandidateText scoreAndSortCandidateTexts(
			ArrayList<IslamCandidateText> candidateTexts, ArrayList<Token> allTokens, int originalTokenLen, int numberOfStep) {
		// TODO Auto-generated method stub
		if (numberOfStep == 0 || candidateTexts.size() == 0)
			return null;
		
		//calculate sums of max frequencies at each step;
		long[] maxSum = new long[numberOfStep];
		maxSum[0] = this.maxFrequencies[0];		
		for (int i = 1; i < numberOfStep; i++) {
			maxSum[i] = maxSum[i-1] + this.maxFrequencies[i];
		}
		
		//calculate S2, S3, S4		
		for (IslamCandidateText islamCandidateText : candidateTexts) {
			ArrayList<FiveGram> fiveGramsOfCandidateText = islamCandidateText.getFiveGrams();
			ArrayList<Token> tokensOfCandidateText = getTokensOfCandidateText(islamCandidateText, allTokens);
			
			//calculate S2
			ArrayList<Token> commonWords = new ArrayList<Token>(); 
			ArrayList<Token> uncommonWordsInResp = new ArrayList<Token>();
			ArrayList<Token> uncommonWordsInInput = new ArrayList<Token>();
			double S2 = calculateS2(tokensOfCandidateText, allTokens, originalTokenLen, 
					commonWords, uncommonWordsInResp, uncommonWordsInInput);
			islamCandidateText.setS2(S2);
			
			//calculate S3
			double S3 = calculateS3(uncommonWordsInInput, uncommonWordsInResp);
			islamCandidateText.setS3(S3);
			
			//calculate S4
			//System.out.println(fiveGramsOfCandidateText.size());
			//System.out.println(maxSum.length);
			long denominator = maxSum[fiveGramsOfCandidateText.size() - 1];
			long nominator = 0;
			for (FiveGram fiveGram : fiveGramsOfCandidateText) {
				nominator += fiveGram.getFrequency();
			}
			islamCandidateText.setS4(nominator/(double)denominator);
			
			//calculate S
			islamCandidateText.calculateS();
		}
		
		//sort candidates
		Collections.sort(candidateTexts);
		
		/*System.out.println("DEBUG IslamModel - scoreAndSortCandidateTexts - AFTER SORT: ");
		for (IslamCandidateText candi : candidateTexts ) {
			System.out.println(getCandidateTextString(candi, allTokens));
		}*/
		
		return candidateTexts.get(candidateTexts.size() - 1);	//the best candidate after sorting
	}

	private double calculateS3(ArrayList<Token> uncommonWordsInInput,
			ArrayList<Token> uncommonWordsInResp) {
		// TODO Auto-generated method stub
		
		int m = uncommonWordsInInput.size();
		int n = uncommonWordsInResp.size();
		int smaller = m > n ? n : m;
		int bigger = m > n ? m : n;
		
		double[][] valueMatrix = new double[m][n];
		int[][] markmatrix = new int[m][n];
		double[] p = new double[smaller];
			
		for (int j = 0; j < m; j++) {
			for (int k = 0; k < n; k++) {
				valueMatrix[j][k] = StringSimilarity.getIslamStringSimilarity(uncommonWordsInInput.get(j).getString(), 
						uncommonWordsInResp.get(k).getString(), 0.25, 0.25, 0.25, 0.25);
			}
		}
		
		double sum = 0;
		for (int i = 0; i < smaller; i++) {
			double max = 0; 
			int remJ = 0;
			int remK = 0;
			for (int j = 0; j < m; j++) {
				for (int k = 0; k < n; k++) {
					if (markmatrix[j][k]!=1 && valueMatrix[j][k] > max){
						max = valueMatrix[j][k];
						remJ = j;
						remK = k;
					}
				}
			}
			
			p[i] = max;
			sum += max;
			
			//mark
			for (int k = 0; k < n; k++)
				markmatrix[remJ][k] = 1;
			
			for (int j = 0; j < m; j++) 
				markmatrix[j][remK] = 1;			
			
			
		}
		
		
		return sum/(double)bigger;
	}

	private double calculateS2(ArrayList<Token> tokensOfCandidateText,
			ArrayList<Token> allTokens, int originalTokenLen, ArrayList<Token> commonWords, 
			ArrayList<Token> uncommonWordsInResp, ArrayList<Token> uncommonWordsInInput) {
		// TODO Auto-generated method stub
		int numCommonWords = 0;
		for (Token token : tokensOfCandidateText) {
			boolean foundCommonWord = false;
			for (int i = 0; i < originalTokenLen && !foundCommonWord; i++){
				if (token == allTokens.get(i)){
					numCommonWords++;
					commonWords.add(token);
				}
			}
			if (!foundCommonWord){
				uncommonWordsInResp.add(token);
			}	
		}
		
		for (int i = 0; i < originalTokenLen; i++){
			Token token = allTokens.get(i);
			if (!commonWords.contains(token)){
				uncommonWordsInInput.add(token);
			}
		}
		return 2 * numCommonWords/(double) (tokensOfCandidateText.size() + originalTokenLen);
	}

	private ArrayList<Token> getTokensOfCandidateText(
			IslamCandidateText islamCandidateText, ArrayList<Token> allTokens) {
		// TODO Auto-generated method stub
		ArrayList<Token> ret = new ArrayList<Token>();
		ArrayList<Integer> tokenIndexes = islamCandidateText.getTokens();
		for (int i = tokenIndexes.size() - 1; i >= 0; i--) {
			ret.add(allTokens.get(tokenIndexes.get(i)));
		}
		return ret;
	}

	private ArrayList<LearnerError> createLearnerErrorsFromCandidateText(
			IslamCandidateText bestCandidate, ArrayList<Token> allTokens, int originalLen, int lineInDoc) {
		// TODO Auto-generated method stub		
		ArrayList<LearnerError> errors = new ArrayList<LearnerError>();
		
		//collect all tokens of the bestCandidate and save into respTokens 
		ArrayList<Token> respTokens = new ArrayList<Token>();
		
		ArrayList<Integer> tokenIndexes = bestCandidate.getTokens(); 
		for (int i = tokenIndexes.size() - 1; i >= 0; i--)
			respTokens.add(allTokens.get(tokenIndexes.get(i)));
		
		//Now, align respTokens with originalLen tokens of allTokens, which are exactly tokens of the input text
		HashMap<Integer, Integer> orgTok2respTok = new HashMap<Integer, Integer>();
		//int prevJ = 0;
		for (int i = 0; i < originalLen; i++) {
			Token originalToken = allTokens.get(i);			
			boolean found = false;
			int j;
			for (j = 0; j < respTokens.size() && !found; j++){
				if (originalToken == respTokens.get(j)){
					found = true;
					//prevJ = j + 1;
					orgTok2respTok.put(i, j);
				}
			}
			if (!found){//this token has been deleted, so create a delete LearnerError
				//prevJ = i + 1;
				LearnerError error = createADeleteLearnerError(lineInDoc, originalToken);
				if (error != null){
					errors.add(error);
				}
			}			
		}
		
		//create insert or replace LearnerErrors
		for (int i = 0; i < respTokens.size(); i++){
			Token respToken = respTokens.get(i);
			if (!orgTok2respTok.containsValue(i)){	//this is a replace or insert token
				LearnerError error = createAReplaceOrInsertLearnerError(lineInDoc, respToken);
				if (error != null)
					errors.add(error);
			}
				
		}
		return errors;
	}

	private LearnerError createAReplaceOrInsertLearnerError(int lineInDoc,
			Token respToken) {
		// TODO Auto-generated method stub
		LearnerError error = null;
		Token replaceOfToken = respToken.getReplaceOfToken();
		Token insertAfterToken = respToken.getInsertAfterToken();
		
		String postag;
		
		if (replaceOfToken != null){
			postag = replaceOfToken.getPostag();
			if (postag.startsWith("NN"))
				error = new LearnerError(replaceOfToken.getBegin(), replaceOfToken.getEnd(), true, "n_num", replaceOfToken.getString(), lineInDoc);
			else if (postag.equals("DT"))
				error = new LearnerError(replaceOfToken.getBegin(), replaceOfToken.getEnd(), true, "at", replaceOfToken.getString(), lineInDoc);
			else if (postag.equals("IN"))
				error = new LearnerError(replaceOfToken.getBegin(), replaceOfToken.getEnd(), true, "prp", replaceOfToken.getString(), lineInDoc);
			else 
				error = new LearnerError(replaceOfToken.getBegin(), replaceOfToken.getEnd(), true, "gen", replaceOfToken.getString(), lineInDoc);
			
		}
		else if (insertAfterToken != null){
			postag = insertAfterToken.getPostag();
			//if (postag.startsWith("NN"))
			//	error = new LearnerError(insertAfterToken.getEnd(), insertAfterToken.getEnd(), true, "n_num", insertAfterToken.getString(), lineInDoc);
			//else 
			if (postag.equals("DT"))
				error = new LearnerError(insertAfterToken.getEnd(), insertAfterToken.getEnd(), true, "at", insertAfterToken.getString(), lineInDoc);
			else if (postag.equals("IN"))
				error = new LearnerError(insertAfterToken.getEnd(), insertAfterToken.getEnd(), true, "prp", insertAfterToken.getString(), lineInDoc);
			else 
				error = new LearnerError(insertAfterToken.getBegin(), insertAfterToken.getEnd(), true, "gen", insertAfterToken.getString(), lineInDoc);
			
		}
			
		return error;
	}

	private LearnerError createADeleteLearnerError(int lineInDoc, Token originalToken) {
		// TODO Auto-generated method stub
		LearnerError error = null;
		String postag = originalToken.getPostag();
		if (postag.startsWith("NN"))
			error = new LearnerError(originalToken.getBegin(), originalToken.getEnd(), true, "n_num", "", lineInDoc);
		else if (postag.equals("DT"))
			error = new LearnerError(originalToken.getBegin(), originalToken.getEnd(), true, "at", "", lineInDoc);
		else if (postag.equals("IN"))
			error = new LearnerError(originalToken.getBegin(), originalToken.getEnd(), true, "prp", "", lineInDoc);
		else 
			error = new LearnerError(originalToken.getBegin(), originalToken.getEnd(), true, "gen", "", lineInDoc);
		return error;
	}

	

	/**
	 * Note that five grams in the candidate texts are in last-first order.s
	 * @param candidateFiveGrams
	 * @return
	 */
	private ArrayList<IslamCandidateText> generateCandidateTextsFromFiveGrams(
			ArrayList<ArrayList<FiveGram>> candidateFiveGrams, ArrayList<Token> allTokens, int originalTokenLen) {
		// TODO Auto-generated method stub
		System.out.println("DEBUG ISLAM MODEL - BEGIN generateCandidateTextsFromFiveGrams");
		HashMap<FiveGram, FiveGram> fiveGramEdges = new HashMap<FiveGram, FiveGram>();
		
		ArrayList<IslamCandidateText> candidateTexts = new ArrayList<IslamCandidateText>();
		//ArrayList<ArrayList<FiveGram>> candidateTexts = new ArrayList<ArrayList<FiveGram>>();
		int numOfSteps = candidateFiveGrams.size();
		if (numOfSteps >= 1){
			
			/*for (FiveGram fiveGram : prevStepFiveGrams) {
				ArrayList<FiveGram> arrayList = new ArrayList<FiveGram>();
				arrayList.add(fiveGram);
				candidateTexts.add(arrayList);
			}*/
			ArrayList<FiveGram> curStepFiveGrams = candidateFiveGrams.get(0);
			ArrayList<FiveGram> matchedNextStepFiveGrams = new ArrayList<FiveGram>();
			
			
			for (int i = 1; i < numOfSteps; i++) {
				//concatenate 5-grams in step i and 5-grams in step i+1
				
				ArrayList<FiveGram> nextStepFiveGrams = candidateFiveGrams.get(i);
				
				for (FiveGram curFiveGram : curStepFiveGrams) {
					
					boolean foundConcatCurFiveGram = false;
					for (FiveGram nextFiveGram : nextStepFiveGrams) {
						if (canConcatenate(curFiveGram, nextFiveGram)){
							fiveGramEdges.put(nextFiveGram, curFiveGram);
							foundConcatCurFiveGram = true;
							matchedNextStepFiveGrams.add(nextFiveGram);								
						}
					}
					
					if (!foundConcatCurFiveGram){	//cannot concatenate this prevFiveGram with any curFiveGram then create a candidate text
						//trace back from curFiveGram						
						candidateTexts.add(getCandidateText(fiveGramEdges, curFiveGram, allTokens, originalTokenLen));
						
					}
				}
				
				curStepFiveGrams.clear();
				curStepFiveGrams.addAll(matchedNextStepFiveGrams);
				matchedNextStepFiveGrams.clear();
			}
				
			for (FiveGram fiveGram : curStepFiveGrams) {
				candidateTexts.add(getCandidateText(fiveGramEdges, fiveGram, allTokens, originalTokenLen));
			}
			
		}
		return candidateTexts;
	}
	
	private IslamCandidateText getCandidateText(HashMap<FiveGram, FiveGram> fiveGramEdges, FiveGram curFiveGram, ArrayList<Token> allTokens, int originalTokenLen) {
		// TODO Auto-generated method stub
		int maxNextApplyPos = -1;
		
		FiveGram tmp1 = curFiveGram;
		FiveGram tmp2;
		ArrayList<FiveGram> candidateTextFiveGrams = new ArrayList<FiveGram>();
		ArrayList<Integer> candidateTextTokens = new ArrayList<Integer>();
		candidateTextFiveGrams.add(tmp1);
		if (tmp1.getNextApplyPos() > maxNextApplyPos)
			maxNextApplyPos = tmp1.getNextApplyPos();
		
		int[] fiveTokens = tmp1.getFiveTokens();
		candidateTextTokens.add(fiveTokens[4]);
		
		while (fiveGramEdges.containsKey(tmp1)){
			tmp2 = fiveGramEdges.get(tmp1);
			candidateTextFiveGrams.add(tmp2);
			if (tmp2.getNextApplyPos() > maxNextApplyPos)
				maxNextApplyPos = tmp2.getNextApplyPos();
			fiveTokens = tmp2.getFiveTokens();
			candidateTextTokens.add(fiveTokens[4]);			
			tmp1 = tmp2;
		}
		
		fiveGramEdges.remove(curFiveGram);
		fiveTokens = tmp1.getFiveTokens();	//add 4 first tokens of the first 5-grams to the tokens of candidate text
		candidateTextTokens.add(fiveTokens[3]);
		candidateTextTokens.add(fiveTokens[2]);
		candidateTextTokens.add(fiveTokens[1]);
		candidateTextTokens.add(fiveTokens[0]);
		
		for (int i = maxNextApplyPos; i < originalTokenLen; i ++){
			candidateTextTokens.add(0, i);			
		}
		return new IslamCandidateText(candidateTextFiveGrams, candidateTextTokens);
	}
	private boolean canConcatenate(FiveGram prevFiveGram, FiveGram curFiveGram) {
		// TODO Auto-generated method stub
		boolean ret = true;
		int[] prevTokens = prevFiveGram.getFiveTokens();
		int[] curTokens = curFiveGram.getFiveTokens();
		for (int i = 1; i < curTokens.length; i++) {
			if (prevTokens[i] != (curTokens[i-1]))
				ret = false;
		}
		return ret;
	}

	private ArrayList<ArrayList<FiveGram>> generateCandidateFiveGrams(
			String line, int lineInDoc, ArrayList<Token> allTokens,
			ArrayList<Chunk> chunks) {
		// TODO Auto-generated method stub
		//System.out.println("DEBUG ISLAM MODEL - generateCandidateFiveGrams : ");
		ArrayList<ArrayList<FiveGram>> allFiveGrams = new ArrayList<ArrayList<FiveGram>>(); //number of step = number of ArrayList<FiveGram>
		
		int originalTokenLen = allTokens.size();
		
		//step 1
		int step = 0;
		ArrayList<FiveGram> generated5grams = null;
		ArrayList<FiveGram> stepFiveGrams = new ArrayList<FiveGram>(); 
		for (int i = 0; i < ruleSet1_Operators.length; i++) {
			generated5grams = generate5grams(line, lineInDoc, ruleSet1_Operators[i], ruleSet1_Offsets[i], 
					allTokens, originalTokenLen, chunks, 0, null, step);
			stepFiveGrams.addAll(generated5grams);
		} 
		//System.out.println("DEBUG ISLAM MODEL - stepFiveGrams : ");
		//printFiveGrams(allTokens, stepFiveGrams);	//for debug
		
		//keep top N 5-grams in stepFiveGrams		
		ArrayList<FiveGram> topN5grams = null;
		//System.out.println("DEBUG ISLAM MODEL - stepFiveGrams STEP : " + step); 
		//printFiveGrams(allTokens, stepFiveGrams);
		topN5grams = selectTopN(stepFiveGrams, N, allTokens, step);		
		allFiveGrams.add(topN5grams);	
		System.out.println("DEBUG ISLAM MODEL - topNFiveGrams STEP : " + step); 
		printFiveGrams(allTokens, topN5grams);
		
		//step 2~		
		while (topN5grams != null){			
			step++;			
			
			stepFiveGrams.clear();
			for (FiveGram fiveGram : topN5grams) {
				if (fiveGram.getNextApplyPos() == -1)	//this five gram has consume the last character in the input text T
					continue;
				
				int[] fiveTokens = fiveGram.getFiveTokens();
				
				if (fiveTokens[4] < originalTokenLen){	//the last token is in the input text, then use rules 2.1
					for (int i = 0; i < ruleSet21_Operators.length; i++) {
						generated5grams = generate5grams(line, lineInDoc, ruleSet21_Operators[i], ruleSet21_Offsets[i], 
								allTokens, originalTokenLen, chunks, fiveGram.getNextApplyPos(), fiveGram, step);
						stepFiveGrams.addAll(generated5grams);
					} 
				}else{//the last token is not in the input text, then use rules 2.2
					for (int i = 0; i < ruleSet22_Operators.length; i++) {
						generated5grams = generate5grams(line, lineInDoc, ruleSet22_Operators[i], ruleSet22_Offsets[i], 
								allTokens, originalTokenLen, chunks, fiveGram.getNextApplyPos(), fiveGram, step);
						stepFiveGrams.addAll(generated5grams);
					} 
				}
			}
			
			if (stepFiveGrams.size() > 0){
				//System.out.println("DEBUG ISLAM MODEL - stepFiveGrams STEP : " + step); 
				//printFiveGrams(allTokens, stepFiveGrams);
				topN5grams = selectTopN(stepFiveGrams, N, allTokens, step);				
				System.out.println("DEBUG ISLAM MODEL - topNFiveGrams STEP : " + step); 
				printFiveGrams(allTokens, topN5grams);
				allFiveGrams.add(topN5grams);
			}else
				topN5grams = null;
				
		}
		
		return allFiveGrams;
	}



	private String printFiveGrams(ArrayList<Token> allTokens,
			ArrayList<FiveGram> stepFiveGrams) {
		// TODO Auto-generated method stub
		for (FiveGram fiveGram : stepFiveGrams) {
			int[] fiveTokens = fiveGram.getFiveTokens();
			System.out.print("[");
			for (int i = 0; i < fiveTokens.length; i++) {
				System.out.print(" " + fiveTokens[i]);
			}
			System.out.print("]");
			System.out.print("\t[");
			for (int i = 0; i < fiveTokens.length; i++) {
				System.out.print(" " + allTokens.get(fiveTokens[i]).getString());
			}
			System.out.print(" ] f=");
			System.out.println(fiveGram.getFrequency());
		}
		return null;
	}

	private ArrayList<FiveGram> selectTopN(ArrayList<FiveGram> stepFiveGrams, int n, ArrayList<Token> allTokens, int step) {
		// TODO Auto-generated method stub
		ArrayList<FiveGram> ret = new ArrayList<FiveGram>();
		
		for (FiveGram fiveGram : stepFiveGrams) {
			StringBuffer bf = new StringBuffer();
			for (int i = 0; i < 4; i ++){
				bf.append(allTokens.get(fiveGram.getAt(i)).getString() + " ");
			}
			bf.append(allTokens.get(fiveGram.getAt(4)).getString());
			
			//get Google frequency for this 5-gram
			//System.out.println(web1t.getFrequency("influx takes"));
			
			try {
				
				long frequency = web1t.getFrequency(bf.toString());	//true code
				System.out.println("DEBUG ISLAM MODEL - selectTopN FREQUENCY OF : " + bf.toString() + " = " + frequency);
				//long frequency = 100 + (long)((new Random()).nextDouble()*(500-100));	//fake code
				if (frequency > this.maxFrequencies[step])
					this.maxFrequencies[step] = frequency;
				fiveGram.setFrequency(frequency); 
								
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		//remove fiveGrams with frequency = 0
		int size = stepFiveGrams.size();
		for (int i = size - 1; i >= 0; i--)
			if (stepFiveGrams.get(i).getFrequency() == 0)
				stepFiveGrams.remove(i);
		
		//sort theo frequency
		Collections.sort(stepFiveGrams);
		
		//get top N with frequency > 0
		size = stepFiveGrams.size();
		for (int i = 0; i < n && i < stepFiveGrams.size(); i ++){
			ret.add(stepFiveGrams.get(size - 1 - i));
		}
		return ret;
	}



	private char[][] ruleSet1_Offsets = {
			{0, 1, 2, 3, 4},
		
			{0, 1, 1, 2, 3},
			{0, 1, 2, 2, 3},
			{0, 1, 2, 3, 3},
			{0, 1, 2, 3, 4},
			
			{0, 1, 1, 3, 2},
			{0, 1, 2, 2, 4},
			{0, 1, 1, 2, 4},
			
			{0, 1, 2, 3, 4},
			{0, 1, 2, 3, 4},
			{0, 1, 2, 3, 4},
			{0, 1, 2, 3, 4},
			
			{0, 1, 2, 3, 4},
			{0, 1, 2, 3, 4},
			{0, 1, 2, 3, 4},
			
			{0, 2, 3, 4, 5},
			{0, 1, 3, 4, 5},
			{0, 1, 2, 4, 5},
			{0, 1, 2, 3, 5},
			
			{0, 2, 4, 5, 6},
			{0, 1, 3, 5, 6},
			{0, 2, 3, 5, 6},
			{0, 1, 3, 4, 6},
			{0, 1, 2, 4, 6},
			{0, 2, 3, 4, 6},
			
			{0, 2, 2, 3, 4},
			{0, 2, 3, 3, 4},
			{0, 1, 3, 3, 4},
			{0, 1, 3, 4, 4},
			{0, 2, 3, 4, 4},
			{0, 1, 2, 4, 4},
			
			{0, 2, 3, 4, 5},
			{0, 1, 2, 4, 5},
			{0, 2, 3, 4, 5},
			{0, 1, 3, 4, 5},
			{0, 2, 3, 4, 5},
			{0, 1, 3, 4, 5},
			
			{0, 1, 2, 4, 5},
			{0, 1, 2, 3, 5},
			{0, 1, 2, 3, 5},
			
			{0, 1, 1, 2, 3},
			{0, 1, 2, 2, 3},
			{0, 1, 1, 3, 5},
			
			{0, 1, 2, 3, 3},
			{0, 1, 2, 3, 4},
			{0, 1, 2, 3, 4},
			
			{0, 1, 1, 3, 4},
			{0, 1, 2, 2, 4},
			{0, 1, 1, 2, 4}
	};
	private char[][] ruleSet1_Operators = {
			{'w', 'w', 'w', 'w', 'w'},
			
			{'w', 'I', 'w', 'w', 'w'},
			{'w', 'w', 'I', 'w', 'w'},
			{'w', 'w', 'w', 'I', 'w'},
			{'w', 'w', 'w', 'w', 'I'},
			
			{'w', 'I', 'w', 'I', 'w'},
			{'w', 'w', 'I', 'w', 'I'},
			{'w', 'I', 'w', 'w', 'I'},
			
			{'w', 'R', 'w', 'w', 'w'},
			{'w', 'w', 'R', 'w', 'w'},
			{'w', 'w', 'w', 'R', 'w'},
			{'w', 'w', 'w', 'w', 'R'},
			
			{'w', 'R', 'w', 'R', 'w'},
			{'w', 'w', 'R', 'w', 'R'},
			{'w', 'R', 'w', 'w', 'R'},
			
			{'w', 'w', 'w', 'w', 'w'},
			{'w', 'w', 'w', 'w', 'w'},
			{'w', 'w', 'w', 'w', 'w'},
			{'w', 'w', 'w', 'w', 'w'},
			
			{'w', 'w', 'w', 'w', 'w'},
			{'w', 'w', 'w', 'w', 'w'},
			{'w', 'w', 'w', 'w', 'w'},
			{'w', 'w', 'w', 'w', 'w'},
			{'w', 'w', 'w', 'w', 'w'},
			{'w', 'w', 'w', 'w', 'w'},
			
			{'w', 'w', 'I', 'w', 'w'},
			{'w', 'w', 'w', 'I', 'w'},
			{'w', 'w', 'w', 'I', 'w'},
			{'w', 'w', 'w', 'w', 'I'},
			{'w', 'w', 'w', 'w', 'I'},
			{'w', 'w', 'w', 'w', 'I'},
			
			{'w', 'w', 'w', 'w', 'R'},
			{'w', 'w', 'w', 'w', 'R'},
			{'w', 'w', 'R', 'w', 'w'},
			{'w', 'w', 'w', 'R', 'w'},
			{'w', 'w', 'w', 'R', 'w'},
			{'w', 'w', 'w', 'w', 'R'},
			
			{'w', 'R', 'w', 'w', 'w'},
			{'w', 'w', 'R', 'w', 'w'},
			{'w', 'R', 'w', 'w', 'w'},
			
			{'w', 'I', 'w', 'R', 'w'},
			{'w', 'w', 'R', 'w', 'R'},
			{'w', 'I', 'w', 'w', 'R'},
			
			{'w', 'R', 'w', 'I', 'w'},
			{'w', 'w', 'R', 'w', 'I'},
			{'w', 'R', 'w', 'w', 'I'},
			
			{'w', 'I', 'w', 'w', 'w'},
			{'w', 'w', 'I', 'w', 'w'},
			{'w', 'R', 'w', 'w', 'I'}
	};
	
	private char[][] ruleSet21_Offsets = {
			{'X', 'X', 'X', 'X', 0},
			{'X', 'X', 'X', 'X', 0},
			{'X', 'X', 'X', 'X', 1},
			{'X', 'X', 'X', 'X', 0}
	};
	private char[][] ruleSet21_Operators = {
			{'X', 'X', 'X', 'X', 'w'},
			{'X', 'X', 'X', 'X', 'I'},
			{'X', 'X', 'X', 'X', 'w'},
			{'X', 'X', 'X', 'X', 'R'}
	};
	
	private char[][] ruleSet22_Offsets = {
			{'X', 'X', 'X', 'X', 0},
			{'X', 'X', 'X', 'X', 1}
	};
	private char[][] ruleSet22_Operators = {
			{'X', 'X', 'X', 'X', 'w'},
			{'X', 'X', 'X', 'X', 'w'}
	};
	
	
	
	/**
	 * 
	 * @param ruleOperators
	 * @param ruleOffsets
	 * @param tokens
	 * @param chunks
	 * @param startApplyTokenPos corresponds to Offset-0 token in the rule
	 */
	private ArrayList<FiveGram> generate5grams(String line, int lineInDoc, char[] ruleOperators, char[] ruleOffsets, ArrayList<Token> tokens, int originalTokenLen,
			ArrayList<Chunk> chunks, int startApplyTokenPos, FiveGram inputFiveGram, int step){
		//System.out.println("DEBUG ISLAM MODEL - generate5grams : ");
		
		ArrayList<FiveGram> ret = new ArrayList<FiveGram>();
		FiveGram patternFG = new FiveGram();
		patternFG.setStep(step);
		HashMap<Integer, ArrayList<Token>> pos2possibleValuesMap = new HashMap<Integer, ArrayList<Token>>();
		
		//create a query pattern and generate possible insert and replace values.  
		boolean endOfText = false;
		boolean hasAfault = false;
		int nextApplyPos = startApplyTokenPos;
		
		for (int i = 0; i < 5 && !endOfText && !hasAfault; i ++){
			switch (ruleOperators[i]){
			case 'X': //copy from inputFiveGram
				patternFG.setAt(i, inputFiveGram.getAt(i + 1));
				break;
			case 'w':
				int indexOfThisToken = startApplyTokenPos + ruleOffsets[i];				
				if (indexOfThisToken >= originalTokenLen){
					endOfText = true;
					nextApplyPos = -1;
					break;
				}else{
					nextApplyPos = indexOfThisToken + 1;	
					patternFG.setAt(i, startApplyTokenPos + ruleOffsets[i]);
				}
				
				//check the deleted token, if having. If the deleted token is invalid, then this pattern contains a fault and cannot be used for generating candidate 5-grams
				Token deletedToken = null;
				if (i > 0){
					if (ruleOperators[i-1] == 'X' && ruleOperators[i] == 'w' && ruleOffsets[i] == 1)
						deletedToken = tokens.get(startApplyTokenPos);
					else if (ruleOperators[i] == 'w' &&  ruleOperators[i-1] == 'w' && (ruleOffsets[i] - ruleOffsets[i-1]) == 2)
						deletedToken = tokens.get(patternFG.getAt(i-1) + 1);
				}
				if (deletedToken != null){
					if (!isValidDeletedToken(deletedToken))
						hasAfault = true;
				}
				
				break;
			case 'I':					
				Token insertAfterToken = tokens.get(patternFG.getAt(i-1));
				int insertBeforeTokenIndex = -1;
				if (nextApplyPos >= 0 && nextApplyPos < originalTokenLen)
					insertBeforeTokenIndex = nextApplyPos;
				pos2possibleValuesMap.put(i, createInsertTokens(lineInDoc, insertAfterToken, insertBeforeTokenIndex, chunks));
				break;
			case 'R':
				indexOfThisToken = startApplyTokenPos + ruleOffsets[i];
				if (indexOfThisToken >= originalTokenLen){
					endOfText = true;
					break;
				}else{				
					Token replaceThisToken = tokens.get(startApplyTokenPos + ruleOffsets[i]);	
					pos2possibleValuesMap.put(i, createReplaceTokens(replaceThisToken, line, lineInDoc, replaceThisToken));
				}
				break;
			}
		}
		
		//create FiveGrams from pattern and replace/insert tokens
		patternFG.setNextApplyPos(nextApplyPos);
		if (!endOfText && !hasAfault){
			Set<Integer> keySet = pos2possibleValuesMap.keySet();
			Iterator<Integer> iterator = keySet.iterator();
			if (keySet.size() == 0){	//no insert or replace= delete or just get 5-tokens from input				
				ret.add(patternFG);
			}else
			if (keySet.size() == 1){	//pattern contains 1 operation
				int posIndex1 = iterator.next();
				ArrayList<Token> newTokens1 = pos2possibleValuesMap.get(posIndex1);
				for (Token token1 : newTokens1) {
					patternFG.setAt(posIndex1, tokens.size());
					tokens.add(token1);
					
					FiveGram tmpFG = new FiveGram(patternFG);					
					ret.add(tmpFG);					
				}
			}else
			if (keySet.size() == 2){	//pattern contains at most 2 operations
				int posIndex1 = iterator.next();
				int posIndex2 = iterator.next();
				ArrayList<Token> newTokens1 = pos2possibleValuesMap.get(posIndex1);
				ArrayList<Token> newTokens2 = pos2possibleValuesMap.get(posIndex2);
				for (Token token1 : newTokens1) {
					for (Token token2 : newTokens2) {
						patternFG.setAt(posIndex1, tokens.size());
						tokens.add(token1);
						patternFG.setAt(posIndex2, tokens.size());
						tokens.add(token2);							
						
						FiveGram tmpFG = new FiveGram(patternFG);						
						ret.add(tmpFG);
					}
				}
			}
		}
		
		return ret;
	}
	
	

	private boolean isValidDeletedToken(Token deletedToken) {
		// TODO Auto-generated method stub
		boolean ret = false;
		String pos = deletedToken.getPostag();
		if (pos.equals("DT") && isInArticleList(deletedToken.getString()))
			ret = true;
		else if (pos.equals("IN") && isInPrepositionList(deletedToken.getString())) 	//can only delete prepositions and articles
			ret = true;
		return ret;
	}

	private boolean isInArticleList(String string) {
		// TODO Auto-generated method stub
		boolean ret = false;
		for (int i = 0; i < ARTICLES.length && !ret; i++) {
			if (ARTICLES[i].equals(string))
				ret = true;
		}
		return ret;
	}
	
	private boolean isInPrepositionList(String string) {
		// TODO Auto-generated method stub
		boolean ret = false;
		for (int i = 0; i < PREPOSITIONS.length && !ret; i++) {
			if (PREPOSITIONS[i].equals(string))
				ret = true;
		}
		return ret;
	}

	private ArrayList<Token> createReplaceTokens(Token replaceThisToken, String line,
			int lineInDoc, Token replaceToken) {
		// TODO Auto-generated method stub
		ArrayList<Token> ret = new ArrayList<Token>();
		String postag = replaceThisToken.getPostag();
		if (postag.equals("DT")){
			String tokenStr= line.substring(replaceThisToken.getBegin(), replaceThisToken.getEnd()).toLowerCase();
			//create article tokens
			for(int i = 0; i < ARTICLES.length; i++) {
				if (!ARTICLES[i].equals(tokenStr)){
					Token token = new Token(lineInDoc, ARTICLES[i], ARTICLES[i], "DT", replaceToken.getBegin(), replaceToken.getEnd());
					token.setReplaceOfToken(replaceToken);
					ret.add(token);
				}
			}
		}else if (postag.equals("IN")){
			String tokenStr= line.substring(replaceThisToken.getBegin(), replaceThisToken.getEnd()).toLowerCase();
			//create article tokens
			for(int i = 0; i < PREPOSITIONS.length; i++) {
				if (!PREPOSITIONS[i].equals(tokenStr)){
					Token token = new Token(lineInDoc, PREPOSITIONS[i], PREPOSITIONS[i], "IN", replaceToken.getBegin(), replaceToken.getEnd());				
					token.setReplaceOfToken(replaceToken);
					ret.add(token);				
				}
			}
		}
		return ret;
	}

	private ArrayList<Token> createInsertTokens(int lineInDoc, Token insertAfterToken, int insertBeforeTokenIndex, ArrayList<Chunk> chunks) {
		// TODO Auto-generated method stub
		ArrayList<Token> ret = new ArrayList<Token>();
		
		//check if the insertBeforeToken (The token follows the inserted token) is a start of an NP or not
		boolean beforeNPChunk = false;
		for (Chunk chunk: chunks) {
			if (chunk.getChunkLabel().equals("NP") && chunk.getBeginTokenIndex() == insertBeforeTokenIndex)
				beforeNPChunk = true;
		}
		
		//insert an article or a preposition only before a NP chunk.
		if (beforeNPChunk){
			//create article tokens
			for(int i = 0; i < ARTICLES.length; i++) {
				Token token = new Token(lineInDoc, ARTICLES[i], ARTICLES[i], "DT", insertAfterToken.getEnd(), insertAfterToken.getEnd());
				token.setInsertAfterToken(token);
				ret.add(token);
			}
			
			//create preposition tokens
			if (!insertAfterToken.getPostag().equals("IN"))	//before the inserted token is not a preposition
				for(int i = 0; i < PREPOSITIONS.length; i++) {
					Token token = new Token(lineInDoc, PREPOSITIONS[i], PREPOSITIONS[i], "IN", insertAfterToken.getEnd(), insertAfterToken.getEnd()); 
					token.setInsertAfterToken(token);
					ret.add(token);
				}
		}
		return ret;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
