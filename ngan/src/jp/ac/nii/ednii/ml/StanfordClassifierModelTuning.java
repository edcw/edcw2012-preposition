package jp.ac.nii.ednii.ml;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.CrossValidator;
import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.classify.RVFClassifier;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.ling.BasicDatum;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.stats.Distribution;
import edu.stanford.nlp.stats.MultiClassAccuracyStats;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Pair;

public class StanfordClassifierModelTuning {		
	
	public static double trainAndTest(String modelFile, String trainDataFile, String devDataFile, String outputClassifier, String serializeToFile){
		// Load a training set
		ColumnDataClassifier cdc = new ColumnDataClassifier(modelFile);
		GeneralDataset<String, String> trainData = cdc.readTrainingExamples(trainDataFile);	
		
		 
		LinearClassifier<String,String> classifier = trainAndTest(cdc, trainData, devDataFile, outputClassifier, serializeToFile);
	    
		
	    //test
	    //Load the development set
	    GeneralDataset<String, String> devData = cdc.readTrainingExamples(devDataFile);
	    
	    MultiClassAccuracyStats<String> scorer = new MultiClassAccuracyStats<String>(MultiClassAccuracyStats.USE_ACCURACY);
	    double score = scorer.score(classifier, devData);
	    int numSamples = scorer.numSamples();
	    
		System.out.println(score);
		return score;
	}
	
	public static double trainAndTest(ColumnDataClassifier cdc, List<Datum<String,String>> trainData, String devDataFile, String outputClassifier){
		
		 // Build a classifier factory
	    LinearClassifierFactory<String,String> factory = new LinearClassifierFactory<String,String>();		
	    
		//set parameters
		factory.useConjugateGradientAscent();
		    
	    // Turn on per-iteration convergence updates
	    factory.setVerbose(true);
	    
	    //Small amount of smoothing
	    //factory.setSigma(2.2069289322128336);
	    factory.setSigma(7.349066919368132);	//allPrep
	    
	    
	    // Build a classifier
	    LinearClassifier<String,String> classifier = factory.trainClassifier(trainData);
	    
	    //write classifier to file
	    LinearClassifier.writeClassifier(classifier, outputClassifier);
	    
	    //test
	    //Load the development set
	    GeneralDataset<String, String> devData = cdc.readTrainingExamples(devDataFile);
	    
	    MultiClassAccuracyStats<String> scorer = new MultiClassAccuracyStats<String>(MultiClassAccuracyStats.USE_ACCURACY);
	    double score = scorer.score(classifier, devData);
	    int numSamples = scorer.numSamples();
	    return score;
	}
	
	public static LinearClassifier<String,String> trainAndTest(ColumnDataClassifier cdc, GeneralDataset<String, String> trainData, String devDataFile, String outputClassifier, String serializeToFile){
		
		 // Build a classifier factory
	    LinearClassifierFactory<String,String> factory = new LinearClassifierFactory<String,String>();		
	    
		//set parameters
		factory.useConjugateGradientAscent();
		    
	    // Turn on per-iteration convergence updates
	    factory.setVerbose(true);
	    
	    //Small amount of smoothing
	    factory.setSigma(10);
	    
	    // Build a classifier
	    LinearClassifier<String,String> classifier = factory.trainClassifier(trainData);
	    
	    //write classifier to file
	    LinearClassifier.writeClassifier(classifier, outputClassifier);
	    
		// serialize model
	    if (serializeToFile != null){
			try {
				// use buffering
				OutputStream file = new FileOutputStream(serializeToFile);
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);
				try {
					output.writeObject(classifier);
					System.out.println("SUCCESFULLY SERIALIZING MODEL!");
				} finally {
					output.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
	    }
	   
	    return classifier;
	}
	
	public static double crossValidateTrainAndTest(String modelFile, String trainDataFile, String devDataFile, String outputClassifier, String serializeToFile){
		ColumnDataClassifier cdc = new ColumnDataClassifier(modelFile);
		GeneralDataset<String, String> trainData = cdc.readTrainingExamples(trainDataFile);	
		
		//create a scorer
		MultiClassAccuracyStats<String> scorer = new MultiClassAccuracyStats<String>(MultiClassAccuracyStats.USE_ACCURACY);
		
		  // Build a classifier factory
	    LinearClassifierFactory<String,String> factory = new LinearClassifierFactory<String,String>();		
	    
		//set parameters
		factory.useConjugateGradientAscent();
		    
	    // Turn on per-iteration convergence updates
	    factory.setVerbose(true);
	    
	    //factory.setRetrainFromScratchAfterSigmaTuning(true);
	    
	    //Set sigma
	    factory.crossValidateSetSigma(trainData, 10, scorer); 
	    
	    // Build a classifier
	    LinearClassifier<String,String> classifier = factory.trainClassifier(trainData);
	    
	    //write classifier to file
	    LinearClassifier.writeClassifier(classifier, outputClassifier);
	    
	    //test
	    //Load the development set
	    GeneralDataset<String, String> devData = cdc.readTrainingExamples(devDataFile);
	    
	   
	    double score = scorer.score(classifier, devData);
	    int numSamples = scorer.numSamples();
	    
		System.out.println(score);
		
		// serialize model
		if (serializeToFile != null){
			try {
				// use buffering
				OutputStream file = new FileOutputStream(serializeToFile);
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);
				try {
					output.writeObject(classifier);
					System.out.println("SUCCESFULLY SERIALIZING MODEL!");
				} finally {
					output.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return score;
	}
	
	public static void varyTrainDataAmountExperiment(String modelFile, String trainDataFile, String devDataFile, String outputFile){
		// Load a training set
		System.out.println("Vary Train Data Amount Experiment");
		ColumnDataClassifier cdc = new ColumnDataClassifier(modelFile);
		GeneralDataset<String, String> trainData = cdc.readTrainingExamples(trainDataFile);	
		
		//create a scorer
		MultiClassAccuracyStats<String> scorer = new MultiClassAccuracyStats<String>(MultiClassAccuracyStats.USE_ACCURACY);
		
		 //Load the development set
	    GeneralDataset<String, String> devData = cdc.readTrainingExamples(devDataFile);
	    
		int size = trainData.size();
		
		System.out.println("percentage \tscore");
		
		for (int percentage = 10; percentage < 101; percentage = percentage + 10 ){
			int subSize = ( percentage * size )/100;
			
			
			boolean sampleWithReplacement = false;
			int randomSeed = 20;
			GeneralDataset<String, String> sampleDataset = trainData.sampleDataset(randomSeed, percentage/(double)100, sampleWithReplacement);
			
			LinearClassifier<String,String> classifier = trainAndTest(cdc, sampleDataset, devDataFile, outputFile, null);
			
			double score = scorer.score(classifier, devData);
			
			System.out.println(percentage + " \t" + score);

			
		}		
		
	}
	
	public static void varyFeaturesExperiment(String modelFile, String trainDataFile, String devDataFile, String outputFile, boolean[] featureActivationStates){
		// Load a training set
		System.out.println("Vary Train Data Amount Experiment");
		ColumnDataClassifier cdc = new ColumnDataClassifier(modelFile);
		GeneralDataset<String, String> trainData = cdc.readTrainingExamples(trainDataFile);	
		
		//create a scorer
		MultiClassAccuracyStats<String> scorer = new MultiClassAccuracyStats<String>(MultiClassAccuracyStats.USE_ACCURACY);
		
		 //Load the development set
	    GeneralDataset<String, String> devData = cdc.readTrainingExamples(devDataFile);
	    
		int size = trainData.size();
		
		Datum<String, String> firstDatum = trainData.getDatum(0);
		
		int numFeatureTypes = firstDatum.asFeatures().size();
		System.out.println("Num feature types = " + numFeatureTypes);
		
		System.out.println("percentage \tscore");
		
		
		if (featureActivationStates == null){
			for (int numFeature = 1; numFeature <= numFeatureTypes; numFeature++ ){
				
				Dataset<String, String> newTrainData = new Dataset<String, String>();
				
				for (int i = 0; i < size; i++) {
					Datum<String, String> datum = trainData.getDatum(i);
					Collection<String> features = datum.asFeatures();
					String label = datum.label();
					Iterator<String> iterator = features.iterator();
					List<String> newFeatures = new ArrayList<String>(); 
					int count = 0;
					while (count < numFeature){
						count ++;
						newFeatures.add(iterator.next());
					}
					
					//create new Datum
					newTrainData.add(new BasicDatum<String,String>(newFeatures, label));
				}
				
				LinearClassifier<String,String> classifier = trainAndTest(cdc, newTrainData, devDataFile, outputFile, null);
				
				double score = scorer.score(classifier, devData);
				
				System.out.println(numFeature + " \t" + score);
	
				
			}	
		}else{
			if (featureActivationStates.length != numFeatureTypes){
				System.out.println("Invalid feature activation states");
				return;
			}
			Dataset<String, String> newTrainData = new Dataset<String, String>();
			
			for (int i = 0; i < size; i++) {
				Datum<String, String> datum = trainData.getDatum(i);
				Collection<String> features = datum.asFeatures();
				String label = datum.label();
				Iterator<String> iterator = features.iterator();
				List<String> newFeatures = new ArrayList<String>(); 
				int count = 0;
				while (count < numFeatureTypes){
					String feature = iterator.next();
					if (featureActivationStates[count] == true)
						newFeatures.add(feature);
					count ++;
				}
				
				//create new Datum
				newTrainData.add(new BasicDatum<String,String>(newFeatures, label));
			}
			
			
			LinearClassifier<String,String> classifier = trainAndTest(cdc, newTrainData, devDataFile, outputFile, null);
			
			double score = scorer.score(classifier, devData);
			
			for (int i = 0; i < featureActivationStates.length; i++) {
				System.out.print(i + " = " + featureActivationStates[i] + " , ");
			}	
			System.out.println();
			System.out.println(score);
		}
	}
	
	public static void main(String args[]){
		/*//tran on KJtrain-train and tune on KJtrain-dev
		String propFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/kjtrain.prop";
		String trainingExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/kjtrain-traindev/examples.train";
		String devExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/kjtrain-traindev/examples.dev";
		String testExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/kjtrain-traindev/examples.dev";
		String modelFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/kjtrain.classifier.txt";
		 */
		
		/*//tran on Harvard-train and tune on Harvard-dev
		String propFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvard.prop";
		String trainingExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvard-traindev/examples.train";
		String devExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvard-traindev/examples.dev";
		String testExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvard-traindev/examples.test";
		String modelFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvard.classifier.txt";
		String serializeToFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvard.model";*/
		
		//tran on Harvard-train and tune on Harvard-dev (all kj preps)
		String propFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep.prop";
		String trainingExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep-traindev/examples.train";
		String devExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep-traindev/examples.dev";
		String testExamplesFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep.test";
		String modelFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep.classifier.txt";
		String serializeToFile = "/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/harvardAllPrep.model";
		
		
		trainAndTest(propFile, trainingExamplesFile, trainingExamplesFile, modelFile, serializeToFile);
		//crossValidateTrainAndTest(propFile, trainingExamplesFile, devExamplesFile, modelFile, serializeToFile);
		//varyTrainDataAmountExperiment(propFile, trainingExamplesFile, devExamplesFile, modelFile);
		//varyFeaturesExperiment(propFile, trainingExamplesFile, devExamplesFile, modelFile, 
		//		new boolean[]{true, true, true, true, true, true, true, true, true, true, 
		//		true, true, true, true, true, true, true, true, true, true, false, false, false});
		System.out.println("Done");
	}
	
}
