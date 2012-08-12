package jp.ac.nii.ednii;

import java.util.ArrayList;

/**
 * Inferface for error correction models.
 * 
 * @author nltngan
 *
 */
public interface ErrorCorrectionInterface {	
	public ArrayList<LearnerError> corrects(LearnerDocument document);
}
