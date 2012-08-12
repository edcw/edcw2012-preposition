package jp.ac.nii.ednii;

/**
 * Data structure to store five grams.
 * 
 * @author nltngan
 *
 */
public class FiveGram implements Comparable<FiveGram>{
	public FiveGram() {
		super();
		this.fiveTokens = new int[5];
		this.nextApplyPos = 0;
	}

	public FiveGram(int[] fiveTokens, int nextApplyPos) {
		super();
		this.fiveTokens = fiveTokens;
		this.nextApplyPos = nextApplyPos;
	}

	public FiveGram(FiveGram other) {
		super();
		this.fiveTokens = new int[5];
		for (int i = 0; i < this.fiveTokens.length; i++) {
			this.fiveTokens[i] = other.getAt(i);
		}
		this.nextApplyPos = other.getNextApplyPos();
		this.step = other.step;
	}
	
	private int[] fiveTokens;
	private int nextApplyPos;
	private long frequency = 0;
	private int step;
	
	@Override
	public int compareTo(FiveGram o) {
		// TODO Auto-generated method stub
		if (this.frequency > o.frequency)
			return 1;
		else if (this.frequency < o.frequency)
			return -1;
		return 0;
	}
	
	public int getAt(int i){
		if (i < 0 && i > 4)
			return -1;
		return fiveTokens[i];
	}
	
	public boolean setAt(int i, int tokenIndex){
		if (i < 0 && i > 4)
			return false;
		fiveTokens[i] = tokenIndex;
		return true;
	}

	public int[] getFiveTokens() {
		return fiveTokens;
	}

	public void setFiveTokens(int[] fiveTokens) {
		this.fiveTokens = fiveTokens;
	}

	public int getNextApplyPos() {
		return nextApplyPos;
	}

	public void setNextApplyPos(int nextApplyPos) {
		this.nextApplyPos = nextApplyPos;
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	
}
