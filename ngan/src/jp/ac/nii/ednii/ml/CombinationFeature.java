package jp.ac.nii.ednii.ml;

import java.util.ArrayList;

public class CombinationFeature extends CRFeature {
	
	private ArrayList<String> atomFeatureNames;
	
	public CombinationFeature(String[] atomFeatureNames, int index){
		super(index);
		
		this.atomFeatureNames = new ArrayList<String>();
		
		StringBuffer name = new StringBuffer();
		
		for (int i = 0; i < atomFeatureNames.length; i++) {
			this.atomFeatureNames.add(atomFeatureNames[i]);
			name.append(atomFeatureNames[i]);
		}
		
		this.setName(name.toString());
		//System.out.println("DEBUG CombinationFeature " + this.getName());
	}

	public ArrayList<String> getAtomFeatureNames() {
		return atomFeatureNames;
	}

	public void setAtomFeatureNames(ArrayList<String> atomFeatureNames) {
		this.atomFeatureNames = atomFeatureNames;
	}

	
	
	
}
