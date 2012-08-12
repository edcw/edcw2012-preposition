package jp.ac.nii.ednii;

/**
 * Data structure to store a learner error.
 * 
 * @author nltngan
 *
 */
public class LearnerError {
	
	public LearnerError(int begin, int end, boolean correctable,
			String errorType, String correction, int lineInDoc) {
		super();
		this.begin = begin;
		this.end = end;
		this.correctable = correctable;
		this.errorType = errorType;
		this.correction = correction;
		this.lineInDoc = lineInDoc;
	}
	
	//KJ corpus error tag set
	public static final String ERROR_KJ_AT = "at";
	public static final String ERROR_KJ_NNUM = "n_num";
	public static final String ERROR_KJ_PRP = "prp";
	public static final String ERROR_KJ_VTNS = "v_tns";
	public static final String ERROR_KJ_NLXC = "n_lxc";
	public static final String ERROR_KJ_VLXC = "v_lxc";
	public static final String ERROR_KJ_PN = "pn";
	public static final String ERROR_KJ_VAGR = "v_agr";
	public static final String ERROR_KJ_AJ = "aj";
	public static final String ERROR_KJ_vo = "v_o";
	public static final String ERROR_KJ_AV = "av";
	public static final String ERROR_KJ_CON = "con";
	public static final String ERROR_KJ_ORD = "ord";
	public static final String ERROR_KJ_no = "n_o";
	public static final String ERROR_KJ_MO = "mo";
	public static final String ERROR_KJ_OLXC = "o_lxc";
	public static final String ERROR_KJ_REL = "rel";
	public static final String ERROR_KJ_ITR = "itr";
	public static final String ERROR_KJ_UK = "uk";
	
	private int begin;
	private int end;
	private boolean correctable = false;
	private String errorType;
	private String correction;
	private int lineInDoc;
	
	
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
	public boolean isCorrectable() {
		return correctable;
	}
	public void setCorrectable(boolean correctable) {
		this.correctable = correctable;
	}
	public String getErrorType() {
		return errorType;
	}
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	public String getCorrection() {
		return correction;
	}
	public void setCorrection(String correction) {
		this.correction = correction;
	}
	
	public int getLineInDoc() {
		return lineInDoc;
	}
	public void setLineInDoc(int lineInDoc) {
		this.lineInDoc = lineInDoc;
	}
	
	public String getXMLOpenTag(){
		if (this.correction != null)
			return  "<" + this.errorType + " crr=\"" + this.correction + "\"" + ">";
		else
			return "<" + this.errorType + " crr=\"" + "\"" + ">";
	}
	
	public String getXMLCloseTag(){
		String ret = "</" + this.errorType + ">";
		return ret;
	}
	
	@Override
	public String toString() {
		/* "LearnerError [begin=" + begin + ", end=" + end
				+ ", correctable=" + correctable + ", errorType=" + errorType
				+ ", correction=" + correction + ", lineInDoc=" + lineInDoc
				+ "]";*/
		return lineInDoc + "\t" + begin + "\t" + end
		+ "\t" + correctable + "\t" + errorType
		+ "\t" + correction;
	}

		
}
