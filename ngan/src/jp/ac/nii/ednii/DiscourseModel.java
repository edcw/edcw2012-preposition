package jp.ac.nii.ednii;


import java.util.ArrayList;


import jp.ac.nii.ednii.utility.MorphologyDictionary;

/**
 * A rule-based model for grammatical error detection.
 *  
 * The main entry point is corrects(LearnerDocument doc).
 * 
 * @author Ngan L.T. Nguyen
 *
 */
public class DiscourseModel implements ErrorCorrectionInterface{
	public static final int SUBJECT_I = 1;
	public static final int SUBJECT_WE = 2;
	public static final int SUBJECT_SINGULAR = 3;
	public static final int SUBJECT_PLURAL = 4;
	
	public static final int NUMBER_UNKNOWN = 0;
	public static final int NUMBER_SINGULAR = 1;
	public static final int NUMBER_PLURAL = 2;
	
	public static MorphologyDictionary morphoDict;
	
	static{
		morphoDict = new MorphologyDictionary();
		morphoDict.loadDictFromFile("./data/morphodict.dic");
	}
	
	/**
	 * Corrects verb-tense, subject-verb agreement, and article errors using rule-based methods.
	 * @param document An input document
	 * @return A list of leaner errors detected by the model
	 */
	public ArrayList<LearnerError> corrects(LearnerDocument document){
		ArrayList<LearnerError> ret = new ArrayList<LearnerError>();
		ArrayList<ArrayList<Chunk>> allChunks = document.getChunks();
		ArrayList<ArrayList<Token>> allTokens = document.getTokens();
		ArrayList<String> textLines = document.getTextLines();
				
		ArrayList<Clause> discourseClauses = new ArrayList<Clause>();	//clauses in previous lines		
		int numLine = textLines.size();
		String line = null;
		ArrayList<Chunk> chunksOfLine = null;
		ArrayList<Token> tokensOfLine = null;
		for (int i = 0; i < numLine; i++) {
			line = textLines.get(i);
			
			chunksOfLine = allChunks.get(i);
			tokensOfLine = allTokens.get(i);
			
			//analyze structures of sentence, finding: clauses, temporal clues, ...
			ArrayList<Clause> clauses = ChunkedSentenceAnalyzer.analyzeStructure(line, chunksOfLine, tokensOfLine);
			
			ret.addAll(correctVerbtenseErrors(line, i, chunksOfLine, tokensOfLine, clauses, discourseClauses));
			ret.addAll(correctSubjVerbAgreementErrors(line, i, chunksOfLine, tokensOfLine, clauses, discourseClauses));
			ret.addAll(correctArticleAndNounNumberErrors(line, i, chunksOfLine, tokensOfLine, clauses, discourseClauses, allChunks, allTokens));
			discourseClauses.addAll(clauses);
		}
		return ret;
	}

	private static ArrayList<LearnerError> correctArticleAndNounNumberErrors(
			String line, int lineInDoc, ArrayList<Chunk> chunksOfLine,
			ArrayList<Token> tokensOfLine, ArrayList<Clause> clauses,
			ArrayList<Clause> discourseClauses, ArrayList<ArrayList<Chunk>> allChunks, ArrayList<ArrayList<Token>> allTokens){
		// TODO Auto-generated method stub
		ArrayList<LearnerError> ret = new ArrayList<LearnerError>();
		
		
		int noOfChunks = chunksOfLine.size();
		for (int i = 0; i < noOfChunks; i++) {
			Chunk chunk = chunksOfLine.get(i);			
			
			if (chunk.getChunkLabel().equals("NP")){//if this chunk is an NP, then check its articles and the head noun's forms
				int beginTokenIndex = chunk.getBeginTokenIndex();
				int endTokenIndex = chunk.getEndTokenIndex();
				Token article = null;
				Token determiner = null;
				int articleType = ArticleRules.ARTICLE_0;
				String chunkStr = line.substring(chunk.getBegin(), chunk.getEnd());
				//if (TemporalExpAndAspect.getAspectOfString(chunkStr.toLowerCase()) != TemporalExpAndAspect.UNKNOWN_ASPECT)	//this is a temporal expression
				//	continue;
				
				//find articles and determiners
				for (int j = beginTokenIndex; j < endTokenIndex; j ++) {
					Token token = tokensOfLine.get(j);
					String postag = token.getPostag();				
					if (postag.equals("DT") || postag.equals("PRP$")){
						String tokenStr = line.substring(token.getBegin(), token.getEnd());
						String tokenStrLowerCase = tokenStr.toLowerCase();
						if (tokenStrLowerCase.equals("a")){
							article = token;
							articleType = ArticleRules.ARTICLE_A;
						}else if (tokenStrLowerCase.equals("an")){
							article = token;
							articleType = ArticleRules.ARTICLE_AN;
						}else if (tokenStrLowerCase.equals("the")){
							article = token;
							articleType = ArticleRules.ARTICLE_THE;
						}else if (ArticleRules.DETERMINERS.contains('#' + tokenStrLowerCase + '#')){
							determiner = token;
						}
					
					}
				}
				
				//find head noun and characterize 
				Token headNoun = tokensOfLine.get(endTokenIndex);	//suppose that the right most token is the head noun
				int headIndex = endTokenIndex;
				boolean isCountable = isCountable(headNoun);
				int headNounNumber = NUMBER_UNKNOWN;	//pronouns are classified into UNKNOWN and will be ignored in this correction
				if (headNoun.getPostag().equals("NN") || headNoun.getPostag().equals("VBG") )
					headNounNumber = NUMBER_SINGULAR;
				else if (headNoun.getPostag().equals("NNS"))
					headNounNumber = NUMBER_PLURAL;
				
				//check article errors using rules
				boolean inWithoutArticles = ArticleRules.isInWithoutArticles(tokensOfLine, headIndex);
				boolean inWithArticles = ArticleRules.isInWithArticles(tokensOfLine, headIndex);
				if (inWithoutArticles){
					if (article != null){ 
						ret.add(new LearnerError(article.getBegin(), article.getEnd(), false, "at", null, lineInDoc));
					}
				}else 				
				if (inWithArticles){ 
					if (article == null){				
						if (chunk.getBegin() > 0 && line.charAt(chunk.getBegin() - 1)==' ')
							ret.add(new LearnerError(chunk.getBegin()-1, chunk.getBegin()-1, false, "at", null, lineInDoc));
					}
				}else 
				if (isCountable && headNounNumber == NUMBER_PLURAL && determiner == null && article != null &&
						(articleType == ArticleRules.ARTICLE_A || articleType == ArticleRules.ARTICLE_AN)){	//has an article error
					ret.add(new LearnerError(article.getBegin(), article.getEnd(), false, "at", null, lineInDoc));
				}else 
				if (isCountable && headNounNumber == NUMBER_SINGULAR && determiner == null && article == null){	//an article should be added or noun form should be changed into plural forms
					if (chunk.getBegin() > 0 && line.charAt(chunk.getBegin() - 1)==' ')
						ret.add(new LearnerError(chunk.getBegin()-1, chunk.getBegin()-1, false, "at", null, lineInDoc));
					//else
					//	ret.add(new LearnerError(chunk.getBegin(), chunk.getBegin(), false, "at", null, lineInDoc));
					ret.add(new LearnerError(headNoun.getBegin(), headNoun.getEnd(), false, "n_num", null, lineInDoc));
				}
			}
		}
		return ret;
	}

	private static boolean isFirstMentionInDiscourse(ArrayList<ArrayList<Chunk>> allChunks, ArrayList<ArrayList<Token>> allTokens, int lineInDoc, Chunk chunk) {
		// TODO Auto-generated method stub
		boolean ret = false;
		Token headToken = allTokens.get(lineInDoc).get(chunk.getEndTokenIndex());
		for (int i = lineInDoc; i > -1; i --){
			ArrayList<Chunk> prevLineChunks = allChunks.get(i);
			ArrayList<Token> prevLineTokens = allTokens.get(i);
			for (Chunk prevChunk : prevLineChunks) {
				
				if (prevChunk.getBegin() < chunk.getBegin()){
					Token prevChunkHeadToken = prevLineTokens.get(prevChunk.getEndTokenIndex());
					if (headToken.getLemma().equals(prevChunkHeadToken.getLemma()))
						ret = true;
				}
				
				if (ret == true)
					break;
			}
			
			if (ret == true)
				break;
		}
			
		return ret;
	}

	/**
	 * Note that this function may return incorrect result because the morphology dictionary does not cover all the wordforms
	 * @param headNoun
	 * @return
	 */
	private static boolean isCountable(Token headNoun) {
		// TODO Auto-generated method stub
		boolean ret = false;
		if (headNoun.getPostag().equals("NN")){
			String lookUpForWordForm = morphoDict.lookUpForWordForm(headNoun.getLemma(), "NNS");	//must ensure that one word have only one sense
			if (lookUpForWordForm != null)	
				ret = true;
		}
		return ret;
	}

	private static ArrayList<LearnerError> correctSubjVerbAgreementErrors(
			String line, int lineInDoc, ArrayList<Chunk> chunksOfLine,
			ArrayList<Token> tokensOfLine, ArrayList<Clause> clauses,
			ArrayList<Clause> discourseClauses) {
		// TODO Auto-generated method stub
		
		ArrayList<LearnerError> ret = new ArrayList<LearnerError>();
		int clausesSize = clauses.size();
		for (int i = 0; i < clausesSize; i++) {
			Clause clause = clauses.get(i);	
			
			Chunk verbChunk = clause.getVerbChunk();
			int numberPropertyOfVerb = VerbNumber.determineNumberOfVerb(line, verbChunk, tokensOfLine);
			int aspectOfVerb = TemporalExpAndAspect.determineAspectOfVerbTense(line, verbChunk, tokensOfLine);
			String verbStr = line.substring(verbChunk.getBegin(), verbChunk.getEnd());
			
			Chunk subjectChunk = clause.getSubjectChunk();
			int numberPropertyOfSubject = determinNumberOfNounPhrase(line, subjectChunk, tokensOfLine);
			String subjectStr = line.substring(subjectChunk.getBegin(), subjectChunk.getEnd()).toLowerCase();
				
			
			boolean isAgreed = true;
			//check agreement
			switch (aspectOfVerb){
			case TemporalExpAndAspect.SIMPLE_PRESENT:
				if (numberPropertyOfSubject == SUBJECT_I){  
					if (numberPropertyOfVerb == VerbNumber.NUMBER_BE_PRESENT_IS || 
								numberPropertyOfVerb == VerbNumber.NUMBER_BE_PRESENT_ARE || 
								numberPropertyOfVerb == VerbNumber.NUMBER_OTHERS_SINGULAR)
					isAgreed = false;
				}else if (numberPropertyOfSubject == SUBJECT_SINGULAR){
					if (numberPropertyOfVerb == VerbNumber.NUMBER_BE_PRESENT_AM || 
							numberPropertyOfVerb == VerbNumber.NUMBER_BE_PRESENT_ARE || 
							numberPropertyOfVerb == VerbNumber.NUMBER_OTHERS_PLURAL)
						isAgreed = false;
				}else {	//we or plural 
					if (numberPropertyOfVerb == VerbNumber.NUMBER_BE_PRESENT_AM || 
							numberPropertyOfVerb == VerbNumber.NUMBER_BE_PRESENT_IS || 
							numberPropertyOfVerb == VerbNumber.NUMBER_OTHERS_SINGULAR)
						isAgreed = false;
				}
				break;
			case TemporalExpAndAspect.PRESENT_PROGRESSIVE:
				if (numberPropertyOfSubject == SUBJECT_I && numberPropertyOfVerb != VerbNumber.NUMBER_BE_PRESENT_AM)
					isAgreed = false;
				else
					if ((numberPropertyOfSubject == SUBJECT_PLURAL && numberPropertyOfVerb == VerbNumber.NUMBER_OTHERS_SINGULAR) ||
							(numberPropertyOfSubject == SUBJECT_SINGULAR && numberPropertyOfVerb == VerbNumber.NUMBER_OTHERS_PLURAL) )
						isAgreed = false;
				break;
			case TemporalExpAndAspect.PRESENT_PERFECT:
			case TemporalExpAndAspect.PRESENT_PERFECT_PROGRESSIVE:
				if (numberPropertyOfSubject == SUBJECT_I){
					if (numberPropertyOfVerb == SUBJECT_SINGULAR)
						isAgreed = false;
					
				}else{
					if ((numberPropertyOfSubject == SUBJECT_PLURAL && numberPropertyOfVerb == VerbNumber.NUMBER_OTHERS_SINGULAR) ||
							(numberPropertyOfSubject == SUBJECT_SINGULAR && numberPropertyOfVerb == VerbNumber.NUMBER_OTHERS_PLURAL) )
						isAgreed = false;
				}
					
				break;
			case TemporalExpAndAspect.SIMPLE_FUTURE:
			case TemporalExpAndAspect.FUTURE_PROGRESSIVE:
			case TemporalExpAndAspect.FUTURE_PERFECT:
			case TemporalExpAndAspect.FUTURE_PERFECT_PROGRESSIVE:
				if (!(subjectStr.equals("i") || subjectStr.equals("we")) && verbStr.contains("shall")){
					isAgreed = false;
					break;
				}
			case TemporalExpAndAspect.BARE_FORM:
			case TemporalExpAndAspect.UNKNOWN_ASPECT:
			case TemporalExpAndAspect.PAST_PERFECT:
			case TemporalExpAndAspect.PAST_PERFECT_PROGRESSIVE:
				default: isAgreed = true;
			}
			
			
			if (!isAgreed){ //then create an agreement error
				if (!(subjectStr.equals("there") && (numberPropertyOfVerb == VerbNumber.NUMBER_BE_PRESENT_ARE || numberPropertyOfVerb == VerbNumber.NUMBER_BE_PRESENT_IS))){	//this structure is not there is, there are
					LearnerError learnerError = new LearnerError(verbChunk.getBegin(), verbChunk.getEnd(), false, "v_agr", null, lineInDoc);
					ret.add(learnerError);
				}
			}
		}
		return ret;
	}

	private static int determinNumberOfNounPhrase(String line, Chunk nounphrase, ArrayList<Token> tokens) {
		// TODO Auto-generated method stub
		int number = SUBJECT_PLURAL;
		int beginTokenIndex = nounphrase.getBeginTokenIndex();
		int endTokenIndex = nounphrase.getEndTokenIndex();
		Token endToken = tokens.get(endTokenIndex);
		Token beginToken = tokens.get(beginTokenIndex);
		
		String npStr = line.substring(nounphrase.getBegin(), nounphrase.getEnd()).toLowerCase();
		if (npStr.contains(" and ") || npStr.contains(" or ")){
			return SUBJECT_PLURAL;
		}
			
		//find head noun. Suppose that the right most is the head.
		Token headToken = null;
		if (beginToken.getPostag().equals("VBG"))
			headToken = beginToken;
		else 
			headToken = tokens.get(endTokenIndex);
		
		String headTokenStr = line.substring(headToken.getBegin(), headToken.getEnd()).toLowerCase();
		if (headToken.getPostag().equals("PRP") || headToken.getPostag().equals("DT")){
			if (headTokenStr.equals("i"))
				number = SUBJECT_I;
			else if (headTokenStr.equals("we"))
				number = SUBJECT_WE;
			else if (headTokenStr.equals("he") || headTokenStr.equals("she") || headTokenStr.equals("it") || headTokenStr.equals("this") || headTokenStr.equals("that"))
				number = SUBJECT_SINGULAR;
			else
				number = SUBJECT_PLURAL;
		}else if (headToken.getPostag().equals("NN") || headToken.getPostag().equals("NNP") || headToken.getPostag().equals("VBG") || headToken.getPostag().equals("JJ"))
			number = SUBJECT_SINGULAR;
		else if (headToken.getPostag().equals("NNS") || headToken.getPostag().equals("NNPS"))
			number = SUBJECT_PLURAL;
		
		return number;
	}

	/**
	 * 
	 * @param line
	 * @param lineInDoc
	 * @param chunksOfLine
	 * @param tokensOfLine
	 * @param clauses	
	 * @param discourseClauses
	 * @return
	 */
	private static ArrayList<LearnerError> correctVerbtenseErrors(String line, int lineInDoc, ArrayList<Chunk> chunksOfLine,
			ArrayList<Token> tokensOfLine, ArrayList<Clause> clauses, ArrayList<Clause> discourseClauses) {
		// TODO Auto-generated method stub
		ArrayList<LearnerError> ret = new ArrayList<LearnerError>();
				
		int clausesSize = clauses.size();
		int prevAspect = TemporalExpAndAspect.UNKNOWN_ASPECT;
		boolean prevClauseHasVerbTenseError = false;
		if (discourseClauses.size() > 0){
			Clause clause = discourseClauses.get(discourseClauses.size()-1);
			
			if (clause.getLineInDoc() == lineInDoc - 1){				
				prevAspect = clause.getAspect();
				prevClauseHasVerbTenseError = clause.isHasVerbTenseError();
			}
		}
		
		for (int i = 0; i < clausesSize; i++) {
			Clause clause = clauses.get(i);			
			
			
			int aspectOfVerbTense = clause.getAspectOfVerbTense();
			int aspectOfTemporalClue = clause.getAspectOfTemporalClue();
						
			if (aspectOfTemporalClue != TemporalExpAndAspect.UNKNOWN_ASPECT){	//this sentense has a temporal clue but the verb tense does not agree with the temporal clue's aspect.
				if (TemporalExpAndAspect.isConflicted(aspectOfTemporalClue,aspectOfVerbTense)){	//aspect of temporal clue is different from verb tense
					//create a verb tense error
					Chunk verbChunk = clause.getVerbChunk();
					LearnerError learnerError = new LearnerError(verbChunk.getBegin(), verbChunk.getEnd(), false, "v_tns", null, lineInDoc);
					ret.add(learnerError);
					clause.setHasVerbTenseError(true);
					clause.setRevisedAspect(aspectOfTemporalClue);	
					
				}
			}else{// this sentence does not have a temporal clue, so the aspect is considered to continue the previous sentence's aspect
				if (prevClauseHasVerbTenseError == false && prevAspect != TemporalExpAndAspect.UNKNOWN_ASPECT
						&& TemporalExpAndAspect.isConflicted(prevAspect, aspectOfVerbTense)){	//may be an error source
					//create a verb tense error
					Chunk verbChunk = clause.getVerbChunk();
					LearnerError learnerError = new LearnerError(verbChunk.getBegin(), verbChunk.getEnd(), false, "v_tns", null, lineInDoc);
					ret.add(learnerError);
					clause.setHasVerbTenseError(true);
					clause.setRevisedAspect(prevAspect);			
					
				}
			}
			prevAspect = clause.getAspect();
			prevClauseHasVerbTenseError = clause.isHasVerbTenseError();
		}
		return ret;
	}
}
