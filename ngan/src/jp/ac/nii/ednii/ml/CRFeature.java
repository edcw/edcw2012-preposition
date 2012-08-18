package jp.ac.nii.ednii.ml;

import java.util.ArrayList;

public class CRFeature {
	private String name;
	private ArrayList<String> values = new ArrayList<String>();
	private int index;
	private double scaleFactor = 1;
	
	public double getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public CRFeature(int index){
		this.index = index;		
	}
	
	public CRFeature(String name, int index){
		this.name = name;
		this.index = index;		
	}
	
	public CRFeature(String name, int index, int rangeMin, int rangeMax) {
		super();
		this.name = name;
		this.index = index;		
	}
	
	public int getFeatureValueIndex(String valueStr, boolean training){
		int ret = 0;
		
		if (values.contains(valueStr))
			ret = values.indexOf(valueStr);
		else{
			if (training){	//add new feature value to values
				ret = values.size();				
				values.add(valueStr);
			}
		}
		
		return ret;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	

	public ArrayList<String> getValues() {
		return values;
	}

	public void resetValues() {
		this.values.clear();
	}
	
	public void addNewValue(String value) {
		this.values.add(value);
	}
	
	public void calculateScaleFactor() {
		if (values.size() == 1)
			scaleFactor = 1;
		else
			scaleFactor = 1/(double)(values.size()-1);
	}
	
}
