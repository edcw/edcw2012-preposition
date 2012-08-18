package jp.ac.nii.ednii;

public class Chunk {
	
	public Chunk(int line, int beginTokenIndex, int endTokenIndex,
			String chunkLabel) {
		super();
		this.line = line;
		this.beginTokenIndex = beginTokenIndex;
		this.endTokenIndex = endTokenIndex;
		this.chunkLabel = chunkLabel;
	}
	private int line;
	private int beginTokenIndex;
	private int endTokenIndex;
	private String chunkLabel;
	private int begin;	//character offset in sentence
	private int end;
	
	public int getBeginTokenIndex() {
		return beginTokenIndex;
	}
	public void setBeginTokenIndex(int beginTokenIndex) {
		this.beginTokenIndex = beginTokenIndex;
	}
	public int getEndTokenIndex() {
		return endTokenIndex;
	}
	public void setEndTokenIndex(int endTokenIndex) {
		this.endTokenIndex = endTokenIndex;
	}
	public String getChunkLabel() {
		return chunkLabel;
	}
	public void setChunkLabel(String chunkLabel) {
		this.chunkLabel = chunkLabel;
	}
	
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getBegin() {
		return begin;
	}
	public void setBegin(int begin) {
		this.begin = begin;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	@Override
	public String toString() {
		return "Chunk [line=" + line + ", beginTokenIndex=" + beginTokenIndex
				+ ", endTokenIndex=" + endTokenIndex + ", chunkLabel="
				+ chunkLabel + ", begin=" + begin + ", end=" + end + "]";
	}
	
}
