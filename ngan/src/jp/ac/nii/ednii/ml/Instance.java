package jp.ac.nii.ednii.ml;

import java.util.ArrayList;

public class Instance {
	public Instance(int numFeature) {
		this.featureValues = new String[numFeature];		
	}
	
	public Instance(String label, String[] featureValues) {
		super();
		this.label = label;
		this.featureValues = featureValues;
	}
	
	private String label;
	private String[] featureValues;
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean addFeatureValue(int index, String value){
		if (index > -1 && index < featureValues.length){
			featureValues[index] = value;
			return true;
		}
		return false;
	}
	
	public String getFeatureValue(int index){
		return featureValues[index];
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		sb.append(label);
		for (int i = 0; i < featureValues.length; i ++) {
			sb.append("\t");
			sb.append(featureValues[i]);
		}
		return sb.toString();
	}

	public String[] getFeatureValues() {
		return featureValues;
	}

	public void setFeatureValues(String[] featureValues) {
		this.featureValues = featureValues;
	}
}
