package jp.ac.nii.ednii;

import java.util.ArrayList;

public class VerbNumber {
	public static final int NUMBER_ANY = 0;	
	public static final int NUMBER_BE_PRESENT_AM = 1;		//or present progressive
	public static final int NUMBER_BE_PRESENT_IS = 2;	
	public static final int NUMBER_BE_PRESENT_ARE = 3;	
	public static final int NUMBER_OTHERS_SINGULAR = 4;
	public static final int NUMBER_OTHERS_PLURAL = 5;
	
	public static int determineNumberOfVerb(String sentenceStr, Chunk verbChunk, ArrayList<Token> tokens) {
		// TODO Auto-generated method stub
		int number = NUMBER_ANY;
		int beginTokenIndex = verbChunk.getBeginTokenIndex();
		int endTokenIndex = verbChunk.getEndTokenIndex();
		StringBuffer str = new StringBuffer(); 
		String tokenStr = null;
		for (int i = beginTokenIndex; i <= endTokenIndex; i++){
			Token token = tokens.get(i);
			tokenStr = sentenceStr.substring(token.getBegin(), token.getEnd()).toLowerCase().trim();
			str.append(tokenStr + " ");
		}
		//for end token, add POS because this is the main verb
		str.append(tokens.get(endTokenIndex).getPostag());
		String pattern = str.toString();
		
		//match this str with patterns to decide number of verb (verb tense)
		String wholeVerb = sentenceStr.substring(verbChunk.getBegin(), verbChunk.getEnd());
		if (wholeVerb.equals("am") || wholeVerb.equals("'m") ||  wholeVerb.equals("am not") ||  wholeVerb.equals("ain't") || 
				pattern.matches(".*(am).*(VBG)"))			
				number = VerbNumber.NUMBER_BE_PRESENT_AM;
		else if (wholeVerb.equals("is") || wholeVerb.equals("'s")|| wholeVerb.equals("is not")  || wholeVerb.equals("isn't")|| 
				pattern.matches(".*(is|is not|is n't).*(VBG)"))			
			number = VerbNumber.NUMBER_BE_PRESENT_IS;
		else if (wholeVerb.equals("are") || wholeVerb.equals("are not") || wholeVerb.equals("'re")  || wholeVerb.equals("aren't")|| 
				pattern.matches(".*(are|are not|are n't).*(VBG)"))			
			number = VerbNumber.NUMBER_BE_PRESENT_ARE;
		else if (
				pattern.matches(".*(VBZ)") ||
				pattern.matches(".*(does|does n't|doesn't).*(VB)") ||
				pattern.matches(".*(was|wasn't|wasn't).*(VBG)") ||
				pattern.matches(".*(has|has n't|hasn't).*(VBN)") ||
				pattern.matches(".*(has|has n't|hasn't).*(been VBG)")){
			number = VerbNumber.NUMBER_OTHERS_SINGULAR;
		}else if (
				pattern.matches(".*(VBP)") ||
				pattern.matches(".*(do|do n't|do not).*(VB)") ||
				pattern.matches(".*(were|were n't|weren't).*(VBG)") ||
				pattern.matches(".*(have|have n't|haven't).*(VBN)") ||
				pattern.matches(".*(have|have n't|haven't).*(been VBG)")){
			number = VerbNumber.NUMBER_OTHERS_PLURAL;
		}
		
		//System.out.println("DEBUG VerbNumber  " + sentenceStr.substring(verbChunk.getBegin(), verbChunk.getEnd()) + " IS " + number + " PATTERN=" + pattern);
		return number;
	}
}
