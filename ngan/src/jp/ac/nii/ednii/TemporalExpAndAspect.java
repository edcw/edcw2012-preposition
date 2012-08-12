package jp.ac.nii.ednii;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import edu.smu.tspell.wordnet.WordNetDatabase;

public class TemporalExpAndAspect {	
	public static final int SIMPLE_PRESENT = 1;
	public static final int PRESENT_PROGRESSIVE = 2;
	public static final int PRESENT_PERFECT = 3;
	public static final int PRESENT_PERFECT_PROGRESSIVE = 4;
	
	public static final int SIMPLE_PAST = 5;	
	public static final int PAST_PROGRESSIVE = 6;	
	public static final int PAST_PERFECT = 7;
	public static final int PAST_PERFECT_PROGRESSIVE = 8;
	
	public static final int SIMPLE_FUTURE = 9;
	public static final int FUTURE_PROGRESSIVE = 10;	
	public static final int FUTURE_PERFECT = 11;	
	public static final int FUTURE_PERFECT_PROGRESSIVE = 12;
	
	public static final int BARE_FORM = 13;
	
	public static final int UNKNOWN_ASPECT = 0;
	
	public static String[] temporalNouns = {"second", "minute", "hour", "day", "week", "month", "year", "semester", "night", "weekend",
		"seconds", "minutes", "hours", "days", "weeks", "months", "years", "semesters", "nights", "weekends",
		"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
		"Mondays", "Tuesdays", "Wednesdays", "Thursdays", "Fridays", "Saturdays", "Sundays",
		"January", "February", "April", "May", "June", "July", "August", "September", "October", "November", "December",
		"Januaries", "Februaries", "Aprils", "Mays", "Junes", "Julies", "Augusts", "Septembers", "Octobers", "Novembers", "Decembers",
		"Spring", "Summer", "Autumn", "Winter",
		"Springs", "Summers", "Autumns", "Winters",
		"vacation", "holiday"};
	public static HashMap<String, Integer> temporalPattern2AspectMap= new HashMap<String, Integer>();
	static {
		temporalPattern2AspectMap.put("every CD T", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("on CD T", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("always", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("usually", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("often", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("generally", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("sometimes", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("occasionally", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("once in a while", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("rarely", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("seldom", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("hardly ever", SIMPLE_PRESENT);
		temporalPattern2AspectMap.put("never", SIMPLE_PRESENT);
		
		temporalPattern2AspectMap.put("now", PRESENT_PROGRESSIVE);
		temporalPattern2AspectMap.put("at this moment", PRESENT_PROGRESSIVE);
		temporalPattern2AspectMap.put("for the moment", PRESENT_PROGRESSIVE);
		temporalPattern2AspectMap.put("currently", PRESENT_PROGRESSIVE);
		temporalPattern2AspectMap.put("at the present", PRESENT_PROGRESSIVE);
		temporalPattern2AspectMap.put("temporarily", PRESENT_PROGRESSIVE);
		temporalPattern2AspectMap.put("this T", PRESENT_PROGRESSIVE);
		temporalPattern2AspectMap.put("today", PRESENT_PROGRESSIVE);
		
		temporalPattern2AspectMap.put("yesterday", SIMPLE_PAST);
		temporalPattern2AspectMap.put("last T", SIMPLE_PAST);
		temporalPattern2AspectMap.put("the last time", SIMPLE_PAST);
		temporalPattern2AspectMap.put("the first time", SIMPLE_PAST);
		temporalPattern2AspectMap.put("CD T ago", SIMPLE_PAST);	//CD includes a few, several
		temporalPattern2AspectMap.put("earlier today", SIMPLE_PAST);
		temporalPattern2AspectMap.put("earlier this T", SIMPLE_PAST);
		
		temporalPattern2AspectMap.put("tomorrow", SIMPLE_FUTURE);
		temporalPattern2AspectMap.put("tonight", SIMPLE_FUTURE);
		temporalPattern2AspectMap.put("next T", SIMPLE_FUTURE);
		temporalPattern2AspectMap.put("the next time", SIMPLE_FUTURE);
		temporalPattern2AspectMap.put("in CD T", SIMPLE_FUTURE);
		temporalPattern2AspectMap.put("soon", SIMPLE_FUTURE);	//???
		
		temporalPattern2AspectMap.put("many times", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("a couple of times", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("several times", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("since yesterday", PRESENT_PERFECT);	//since SIMPLE PAST, since can be also used with PRESENT PERFECT PROGRESSIVE
		temporalPattern2AspectMap.put("since last T", PRESENT_PERFECT);	//since SIMPLE PAST
		temporalPattern2AspectMap.put("since CD", PRESENT_PERFECT);	//since 1983
		temporalPattern2AspectMap.put("in the last CD T", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("in the past CD T", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("recently", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("up to now", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("so far", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("lately", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("already", PRESENT_PERFECT);		
		temporalPattern2AspectMap.put("in a long time", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("yet", PRESENT_PERFECT);
		temporalPattern2AspectMap.put("how long", PRESENT_PERFECT);	//question
		temporalPattern2AspectMap.put("ever", PRESENT_PERFECT);	//question
		
		temporalPattern2AspectMap.put("all T long", PRESENT_PERFECT_PROGRESSIVE);	//since can be also used with PRESENT PERFECT PROGRESSIVE
		temporalPattern2AspectMap.put("for the last CD T", PRESENT_PERFECT_PROGRESSIVE);
		temporalPattern2AspectMap.put("for the past CD T", PRESENT_PERFECT_PROGRESSIVE);
		temporalPattern2AspectMap.put("for CD T", PRESENT_PERFECT_PROGRESSIVE);
		temporalPattern2AspectMap.put("how long", PRESENT_PERFECT_PROGRESSIVE);	//question
	}
	
	private static String temporalNounMatchPatternStr;
	static{
		//create pattern for matching temporal nouns
		StringBuffer temporalNounMatchPattern = new StringBuffer(temporalNouns[0]);
		for (int i = 1; i < temporalNouns.length; i++) {
			temporalNounMatchPattern.append('|');
			temporalNounMatchPattern.append(temporalNouns[i]);
		}
		temporalNounMatchPatternStr = '(' + temporalNounMatchPattern.toString() + ')';
	}
	
	public static int getAspectOfString(String inputString){
		int aspect = UNKNOWN_ASPECT;

		Set<String> keySet = temporalPattern2AspectMap.keySet();
		String matchPattern = null;
		for (String temporalPattern : keySet) {
			matchPattern = temporalPattern.replace("CD", ".*").replace("T", temporalNounMatchPatternStr);			
			if (inputString.matches(matchPattern)){
				aspect = temporalPattern2AspectMap.get(temporalPattern);
				break;
			}
		}
		return aspect;
	}
	
	/**
	 * Determines the verb tense of a verb phrase
	 * @param sentenceStr
	 * @param verbChunk
	 * @param tokens
	 * @return
	 */
	public static int determineAspectOfVerbTense(String sentenceStr, Chunk verbChunk, ArrayList<Token> tokens) {
		// TODO Auto-generated method stub
		int aspect = TemporalExpAndAspect.UNKNOWN_ASPECT;		
		int beginTokenIndex = verbChunk.getBeginTokenIndex();
		int endTokenIndex = verbChunk.getEndTokenIndex();
		
		//
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
		
		//match this str with patterns to decide aspect of verb (verb tense)
		if (pattern.matches(".*(VBZ|VBP)"))
			aspect = TemporalExpAndAspect.SIMPLE_PRESENT;
		if (pattern.matches(".*(do|do n't|do not|can|ca n't|won't|cannot|may|might).*(VB)"))	//present tense modal verb
			aspect = TemporalExpAndAspect.SIMPLE_PRESENT;
		else if (pattern.matches(".*(will|shall).*(VBZ|VBP)"))
			aspect = TemporalExpAndAspect.SIMPLE_FUTURE;
		else if (pattern.matches(".*(VBD)"))
			aspect = TemporalExpAndAspect.SIMPLE_PAST;
		else if (pattern.matches(".*(did|did n't|did not|does|does n't|doesn't|could|could n't|could not).*(VB)"))
			aspect = TemporalExpAndAspect.SIMPLE_PAST;
		else if (pattern.matches(".*(is|am|are|is n't|isn't|aren't|are n't).*(VBG)"))
			aspect = TemporalExpAndAspect.PRESENT_PROGRESSIVE;
		else if (pattern.matches(".*(was|wasn't|was n't|were|weren't|were not).*(VBG)"))
			aspect = TemporalExpAndAspect.PAST_PROGRESSIVE;
		else if (pattern.matches(".*(will|shall).*(be VBG)"))
			aspect = TemporalExpAndAspect.PAST_PROGRESSIVE;
		else if (pattern.matches(".*(has|have|has n't|have n't|hasn't|haven't).*(VBN)"))
			aspect = TemporalExpAndAspect.PRESENT_PERFECT;
		else if (pattern.matches(".*(has|have|has n't|have n't|hasn't|haven't).*(been VBG)"))
			aspect = TemporalExpAndAspect.PRESENT_PERFECT_PROGRESSIVE;
		else if (pattern.matches(".*(had|had n't|hadn't).*(VBN)"))
			aspect = TemporalExpAndAspect.PAST_PERFECT;
		else if (pattern.matches(".*(had|had n't|hadn't).*(been VBG)"))
			aspect = TemporalExpAndAspect.PAST_PERFECT_PROGRESSIVE;
		else if (pattern.matches(".*(will|shall).*(have VBN)"))
			aspect = TemporalExpAndAspect.FUTURE_PERFECT;
		else if (pattern.matches(".*(had|hadn't|had not).*(will have been VBG)"))
			aspect = TemporalExpAndAspect.FUTURE_PERFECT_PROGRESSIVE;
		else if (pattern.matches("/*VB"))
			aspect = TemporalExpAndAspect.BARE_FORM;
		
		return aspect;
	}
	
	
	
	public static boolean isConflicted(int aspect1, int aspect2){
		boolean ret = true;
		
		if (aspect1 == aspect2)
			ret = false;
		else if ((aspect1 == SIMPLE_PRESENT && aspect2 == PRESENT_PROGRESSIVE) ||
				(aspect1 == SIMPLE_PAST && aspect2 == PAST_PROGRESSIVE) ||
				(aspect1 == SIMPLE_FUTURE && aspect2 == FUTURE_PROGRESSIVE))
			ret = false;
		return ret;
	}
	
	public static void main(String args[]){
		System.out.println(getAspectOfString("soon"));
	
		/*
		public static int SIMPLE_PRESENT = 0;
		public static int PRESENT_PROGESSIVE = 1;
		public static int SIMPLE_PAST = 2;
		public static int SIMPLE_FUTURE = 3;
		public static int PRESENT_PERFECT = 4;
		public static int PRESENT_PERFECT_PROGRESSIVE = 4;
		public static int UNKNOWN_ASPECT = 5;
		*/
		System.out.println("Done.");
		
	}
	
}
