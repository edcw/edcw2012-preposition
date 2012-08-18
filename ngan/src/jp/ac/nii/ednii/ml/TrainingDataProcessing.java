package jp.ac.nii.ednii.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ling.Datum;

public class TrainingDataProcessing {
	public static void samplingForUnbalancedData(String propFile, String inFileStr, String outFileStr, String[] adjustedLabels, int[] skips){		
		ColumnDataClassifier cdc;
		cdc = new ColumnDataClassifier(propFile);
		
		HashMap<String, Integer> label2countMap = new HashMap<String, Integer>();
		HashMap<String, Integer> label2RecalculatedCountMap = new HashMap<String, Integer>();
		
		try {
			PrintWriter pw = new PrintWriter(new File(outFileStr));
			
			BufferedReader br = new BufferedReader(new FileReader(new File(inFileStr)));
			String strLine = null;	
			long exampleCount = 0; 
			int sum = 0;
			while ((strLine = br.readLine()) != null){
				Datum<String, String> d = cdc.makeDatumFromLine(strLine, 0);
				String label = d.label();
				if (label2countMap.containsKey(label))
					label2countMap.put(label, label2countMap.get(label) + 1);
				else
					label2countMap.put(label, 1);
				
				sum++;
			}
			
			br.close();
			
			//print out number of instances for each class
			Set<String> keySet = label2countMap.keySet();
			for (String label : keySet) {
				System.out.println("CLASS (label, count) = (" + label + " , " + label2countMap.get(label) + ")");
			}
			
			ArrayList<String> labels = new ArrayList<String>();
			Collections.addAll(labels, adjustedLabels);
			int[] counters = new int[adjustedLabels.length];
			
			br = new BufferedReader(new FileReader(new File(inFileStr)));
			while ((strLine = br.readLine()) != null){
				Datum<String, String> d = cdc.makeDatumFromLine(strLine, 0);
				String label = d.label();
				
				int indexOfLabel = labels.indexOf(label);
				boolean writeThis = true;
				if (indexOfLabel > -1){
					counters[indexOfLabel]++;
					if (counters[indexOfLabel] < skips[indexOfLabel]){//write this instance, else skip this instance
						writeThis = false;						
					}else
						counters[indexOfLabel] = 0;
				}
				
				if (writeThis){
					pw.append(strLine);
					pw.append("\n");
				}			
				
			}		
		
			
			
			pw.close();			
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		
	}
	
	public static void main(String[] args) {
		String[] adjustedLabels = new String[]{""};
		int[] skips = new int[]{10};
		//samplingForUnbalancedData("/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/prep.prop", 
		//		"/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/prep.train", 
		//		"/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/prep.resampled", adjustedLabels, skips);
		
		samplingForUnbalancedData("/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep.prop", 
				"/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep.train", 
				"/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep.train.balanced", adjustedLabels, skips);
		System.out.println("Done");
	}
}
