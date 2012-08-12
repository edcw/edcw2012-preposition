package jp.ac.nii.ednii;

public class Clause {
	
	public Clause() {
		setAspect(TemporalExpAndAspect.UNKNOWN_ASPECT);
		setAspectOfVerbTense(TemporalExpAndAspect.UNKNOWN_ASPECT);
		
	}
	public static int CLAUSE_INDEPENDENT = 1;
	public static int CLAUSE_DEPENDENT = 1;
	private Chunk subjectChunk;		//mandatory
	private Chunk verbChunk;	//mandatory
	private String premodifierChunkStr;	//optional
	private Chunk temporalClueChunk;	//optional
	private String temporalClue;	//optional
	private int aspect;	//mandatory, TemporalExpAndAspect	
	private int clauseType;	//mandatory
	private int aspectOfVerbTense;	//mandatory
	private int aspectOfTemporalClue;	//mandatory
	private boolean hasVerbTenseError = false;
	private int revisedAspect = 0;
	private int lineInDoc;
	
	public Chunk getSubjectChunk() {
		return subjectChunk;
	}
	public void setSubjectChunk(Chunk subjectChunk) {
		this.subjectChunk = subjectChunk;
	}
	public Chunk getVerbChunk() {
		return verbChunk;
	}
	public void setVerbChunk(Chunk verbChunk) {
		this.verbChunk = verbChunk;
	}
	public String getPremodifierChunkStr() {
		return premodifierChunkStr;
	}
	public void setPremodifierChunkStr(String premodifierChunkStr) {
		this.premodifierChunkStr = premodifierChunkStr;
	}
	public String getTemporalClue() {
		return temporalClue;
	}
	public void setTemporalClue(String temporalClue) {
		this.temporalClue = temporalClue;
	}
	public int getAspect() {
		return aspect;
	}
	public void setAspect(int aspect) {
		this.aspect = aspect;
	}
	public int getClauseType() {
		return clauseType;
	}
	public void setClauseType(int clauseType) {
		this.clauseType = clauseType;
	}
	public Chunk getTemporalClueChunk() {
		return temporalClueChunk;
	}
	public void setTemporalClueChunk(Chunk temporalClueChunk) {
		this.temporalClueChunk = temporalClueChunk;
	}
	public int getAspectOfVerbTense() {
		return aspectOfVerbTense;
	}
	public void setAspectOfVerbTense(int aspectOfVerbTense) {
		this.aspectOfVerbTense = aspectOfVerbTense;
	}
	
	public int getAspectOfTemporalClue() {
		return aspectOfTemporalClue;
	}
	public void setAspectOfTemporalClue(int aspectOfTemporalClue) {
		this.aspectOfTemporalClue = aspectOfTemporalClue;
	}
	
	public boolean isHasVerbTenseError() {
		return hasVerbTenseError;
	}
	public void setHasVerbTenseError(boolean hasVerbTenseError) {
		this.hasVerbTenseError = hasVerbTenseError;
	}
	public int getRevisedAspect() {
		return revisedAspect;
	}
	public void setRevisedAspect(int revisedAspect) {
		this.revisedAspect = revisedAspect;
	}
	
	public int getLineInDoc() {
		return lineInDoc;
	}
	public void setLineInDoc(int lineInDoc) {
		this.lineInDoc = lineInDoc;
	}
	@Override
	public String toString() {
		return "Clause [subjectChunk=" + subjectChunk + ", verbChunk="
				+ verbChunk + ", premodifierChunkStr=" + premodifierChunkStr
				+ ", temporalClueChunk=" + temporalClueChunk
				+ ", temporalClue=" + temporalClue + ", aspect=" + aspect
				+ ", clauseType=" + clauseType + ", aspectOfVerbTense="
				+ aspectOfVerbTense + ", aspectOfTemporalClue="
				+ aspectOfTemporalClue + ", hasVerbTenseError="
				+ hasVerbTenseError + ", revisedAspect=" + revisedAspect
				+ ", lineInDoc=" + lineInDoc + "]";
	}
	
}
