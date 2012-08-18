package jp.ac.nii.ednii;

import java.util.ArrayList;

/**
 * Stores a candidate text in Islam model.
 * 
 * @author nltngan
 *
 */
public class IslamCandidateText implements Comparable<IslamCandidateText> {
	
	public static final double beta2  = 0.33;
	public static final double beta3  = 0.33;
	public static final double beta4  = 0.33;
	
	public IslamCandidateText(ArrayList<FiveGram> fiveGrams,
			ArrayList<Integer> tokens) {
		super();
		this.fiveGrams = fiveGrams;
		this.tokens = tokens;
	}
	public IslamCandidateText() {
		super();
		fiveGrams = new ArrayList<FiveGram>();
		tokens = new ArrayList<Integer>();
	}

	public void calculateS(){
		this.S = beta2 * S2 + beta3 * S3 + beta4 * S4;
	}
	
	private ArrayList<FiveGram> fiveGrams;
	private ArrayList<Integer> tokens;
	double S2, S3, S4, S;
	
	@Override
	public int compareTo(IslamCandidateText o) {
		// TODO Auto-generated method stub
		if (this.S > o.S)
			return 1;
		else if (this.S < o.S)
			return -1;
		return 0;
	}
	
	public ArrayList<FiveGram> getFiveGrams() {
		return fiveGrams;
	}
	public void setFiveGrams(ArrayList<FiveGram> fiveGrams) {
		this.fiveGrams = fiveGrams;
	}
	public ArrayList<Integer> getTokens() {
		return tokens;
	}
	public void setTokens(ArrayList<Integer> tokens) {
		this.tokens = tokens;
	}
	
	public void addFiveGram(FiveGram fiveGram){
		this.fiveGrams.add(fiveGram);		
	}
	
	public void addToken(int token){
		this.addToken(token);		
	}
	public double getS2() {
		return S2;
	}
	public void setS2(double s2) {
		S2 = s2;
	}
	public double getS3() {
		return S3;
	}
	public void setS3(double s3) {
		S3 = s3;
	}
	public double getS4() {
		return S4;
	}
	public void setS4(double s4) {
		S4 = s4;
	}
	public double getS() {
		return S;
	}
}
