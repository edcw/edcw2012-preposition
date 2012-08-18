package jp.ac.nii.ednii;

public class Token {
	public Token(int line, String string, String lemma, String postag,
			int beginIndex, int endIndex) {
		super();
		this.line = line;
		this.string = string;
		this.lemma = lemma;
		this.postag = postag;
		this.begin = beginIndex;
		this.end = endIndex;
	}
	public Token(String string, String lemma, String postag) {
		super();
		this.string = string;
		this.lemma = lemma;
		this.postag = postag;
	}
	private int line;
	private String string;
	private String lemma;
	private String postag;
	private int begin;	//in a text line
	private int end;	// in a text line
	private Token replaceOfToken;	//for Islam model
	private Token insertAfterToken;	//for Islam model
	
	public String getString() {
		return string;
	}
	public void setString(String string) {
		this.string = string;
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String getPostag() {
		return postag;
	}
	public void setPostag(String postag) {
		this.postag = postag;
	}
	public int getBegin() {
		return begin;
	}
	public void setBeginIndex(int beginIndex) {
		this.begin = beginIndex;
	}
	public int getEnd() {
		return end;
	}
	public void setEndIndex(int endIndex) {
		this.end = endIndex;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	@Override
	public String toString() {
		return "Token [line=" + line + ", string=" + string + ", lemma="
				+ lemma + ", postag=" + postag + ", beginIndex=" + begin
				+ ", endIndex=" + end + "]";
	}
	public Token getReplaceOfToken() {
		return replaceOfToken;
	}
	public void setReplaceOfToken(Token replaceOfToken) {
		this.replaceOfToken = replaceOfToken;
	}
	public Token getInsertAfterToken() {
		return insertAfterToken;
	}
	public void setInsertAfterToken(Token insertAfterToken) {
		this.insertAfterToken = insertAfterToken;
	}
	
	
}
