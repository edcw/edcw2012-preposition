package jp.ac.nii.ednii;

import java.util.ArrayList;

public class ArticleRules {
	public static final int ARTICLE_0 = 0;
	public static final int ARTICLE_A = 1;
	public static final int ARTICLE_AN = 2;
	public static final int ARTICLE_THE = 3;
	public static final String DETERMINERS = "#my#your#her#his#it#their#this#that#these#those#every#any#some#all#each#such#no#";
	public static final String[] USE_WITHOUT_ARTICLES = {
		//meals, places, transports
		"breakfast", "lunch", "dinner", "supper", "by|taxi", "by|bus", "by|plane", "by|car", "by|boat",
		"go|to|school", "go|to|work", "go|to|hospital", "watch|movie",
		//countries, states, counties or provinces, 
		"Japan", "Vietnam",
		//lakes and mountains
		"Mount Fuji",
		//others
		"something", "anything", "nothing", "everything", 
		"someone", "anyone", "noone", "everyone",
		"somebody", "anybody", "nobody", "everybody",
		"somewhere", "anywhere", "nowhere", "everywhere",
		"yesterday", "today", "tonight", "weekend", "noon"
	};
	
	private static final String[] USE_WITH_ARTICLES = {
		//bodies of water, oceans and seas 
		"Pacific Ocean"
	};
	
	public static boolean isInWithoutArticles(ArrayList<Token> tokens, int headIndex){
		
		return isInThisList(USE_WITHOUT_ARTICLES, tokens, headIndex);
	}
	
	public static boolean isInWithArticles(ArrayList<Token> tokens, int headIndex){
		
		return isInThisList(USE_WITH_ARTICLES, tokens, headIndex);
	}
	
	private static boolean isInThisList(String[] patterns, ArrayList<Token> tokens, int headIndex){
		boolean ret = false;
		String string = null;
		for (int i = 0; i < patterns.length && ret == false; i++) {
			string = patterns[i];
			String[] splits = string.split("\\|");		
			int tokenIndex = headIndex;
			boolean matchThisPattern = true;
			for (int j = splits.length - 1 ; j > -1 && matchThisPattern && tokenIndex > -1; j--) {
				if (!splits[j].equals(tokens.get(tokenIndex).getLemma()))
					matchThisPattern = false;
				tokenIndex --;
			}
			if (matchThisPattern){
				ret = true;
			}
		}
		return ret;
	}
}
