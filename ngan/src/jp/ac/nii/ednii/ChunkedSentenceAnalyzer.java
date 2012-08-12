package jp.ac.nii.ednii;

import java.util.ArrayList;

/**
 * Analyzes a sentence to find clauses, temporal clues, ...; and characterizes each clause with information such as subject, main verb, attached temporal clue, ...
 * 
 * The analysis algorithm is based on POS and chunk information.
 * The main entry point is analyzeStructure.
 * 
 * @author nltngan
 *
 */
public class ChunkedSentenceAnalyzer {
	private static String[] CLAUSE_MARKERS = {"when", "while", "whilst", "after", "before", 
		"although", "though", "even though",
		"because", "since", "for", "if", "then",
		"that"};	//that: SBAR,
	
	/**
	 * Analyzes a sentence to find dependent and independent clauses as well as their subjects and main verbs in a simple way.
	 * 
	 * @param sentenceStr
	 * @param chunks
	 * @param tokens
	 * @return Clauses and their properties.
	 */
	public static ArrayList<Clause> analyzeStructure(String sentenceStr, ArrayList<Chunk> chunks, ArrayList<Token> tokens){
		ArrayList<Clause> ret = new ArrayList<Clause>();
		
		
		//detect clauses in the line sentenceStr
		int numChunks = chunks.size();
		for (int i = 0; i < numChunks - 1; i++){
			Chunk curChunk = chunks.get(i);
			
			//check if curChunk is subjective noun phrase of some clause (has form "NP VP..." or "...SBAR/ADVP NP VP...")
			Chunk nextChunk = chunks.get(i+1);
			if (curChunk.getChunkLabel().equals("NP") && nextChunk.getChunkLabel().equals("VP")){
				if (i == 0){	//S = NP VP ...				
					Clause clause = new Clause();					
					clause.setSubjectChunk(curChunk);
					clause.setVerbChunk(nextChunk);
					clause.setClauseType(Clause.CLAUSE_INDEPENDENT);
					int aspectOfVerbTense = TemporalExpAndAspect.determineAspectOfVerbTense(sentenceStr, nextChunk, tokens);
					clause.setAspectOfVerbTense(aspectOfVerbTense);
					clause.setAspect(aspectOfVerbTense);
					clause.setLineInDoc(curChunk.getLine());
					ret.add(clause);
				}else{	
					 Chunk prevChunk = chunks.get(i-1);
					 
					 boolean prevChunkIsSubordinateMarker = false;
					 if (prevChunk.getChunkLabel().equals("SBAR") || prevChunk.getChunkLabel().equals("ADVP")){
						 String prevChunkStr = sentenceStr.substring(prevChunk.getBegin(), prevChunk.getEnd()).toLowerCase().trim();
						 for (int j = 0; j < CLAUSE_MARKERS.length; j++) {
							 	
							if (CLAUSE_MARKERS[j].equals(prevChunkStr)){								
								prevChunkIsSubordinateMarker = true;
								break;
							}
						}
					 }
					 
					 String leftPrevTokenStr = null;
					 Token leftPrevToken = null;
					 if (curChunk.getBeginTokenIndex() > 0){
						 leftPrevToken = tokens.get(curChunk.getBeginTokenIndex() - 1);	//the token before the chunk to the left
						 leftPrevTokenStr = sentenceStr.substring(leftPrevToken.getBegin(), leftPrevToken.getEnd());
					 }
					 if ((leftPrevToken != null && (leftPrevTokenStr.equals("but") || leftPrevTokenStr.equals("and")))
							 || (prevChunkIsSubordinateMarker)
							 || (i==1 && prevChunk.getChunkLabel().equals("ADVP"))){ //S = ADVP NP VP ...
						Clause clause = new Clause();
						clause.setSubjectChunk(curChunk);
						clause.setVerbChunk(nextChunk);
						clause.setClauseType(Clause.CLAUSE_DEPENDENT);
						int aspectOfVerbTense = TemporalExpAndAspect.determineAspectOfVerbTense(sentenceStr, nextChunk, tokens);
						clause.setAspectOfVerbTense(aspectOfVerbTense);
						clause.setAspect(aspectOfVerbTense);
						clause.setLineInDoc(curChunk.getLine());
						ret.add(clause);
					 }
				}
			}			
			
		}
		
		//find temporal clues
		for (int i = 0; i < numChunks; i++){
			Chunk curChunk = chunks.get(i);
			
			//check if this chunk is a temporal clue, if yes then assign it to the closest clause			
			if (!curChunk.getChunkLabel().equals("VP")){				
				String curChunkStr = sentenceStr.substring(curChunk.getBegin(), curChunk.getEnd()).toLowerCase().trim();
				int aspectOfTemporalClue = TemporalExpAndAspect.getAspectOfString(curChunkStr);
				
		
				if (aspectOfTemporalClue != TemporalExpAndAspect.UNKNOWN_ASPECT){ //this is a tempora clue, so assign it to the closest clause					
					Clause assignToClause = null;
					int distance = 0;
					
					
					if (ret.size() > 0){					
						Clause clause0 = ret.get(0);
						int minDistance = Math.abs(clause0.getSubjectChunk().getBegin() - curChunk.getBegin());
						assignToClause = clause0;
						for (int j = 1; j < ret.size(); j ++) {
							int tmpDistance = Math.abs(ret.get(j).getSubjectChunk().getBegin() - curChunk.getBegin());   
							if (tmpDistance < minDistance){
								minDistance = tmpDistance;
								assignToClause = ret.get(j);
							}
						}
					}
					
					//assign the temporal clue to the assignToClause
					if (assignToClause != null){
						assignToClause.setTemporalClue(curChunkStr);
						assignToClause.setAspectOfTemporalClue(aspectOfTemporalClue);						
						assignToClause.setTemporalClueChunk(curChunk);
						assignToClause.setAspect(aspectOfTemporalClue);
					}
				}
			}
		}
		
		return ret;
	}

	
}
