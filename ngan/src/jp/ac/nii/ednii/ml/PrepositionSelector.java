package jp.ac.nii.ednii.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jp.ac.nii.ednii.Chunk;
import jp.ac.nii.ednii.Token;

import de.bwaldvogel.liblinear.*;
import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class PrepositionSelector {
	static private ArrayList<CRFeature> allFeatures;
	static public ArrayList<String> allFeatureNames;

	static{
		allFeatures = new ArrayList<CRFeature>();
		allFeatureNames = new ArrayList<String>();	
		
		//atom features
		//examples:	addNewAtomFeature("a_type");
		addNewAtomFeature("l_left2tok");
		addNewAtomFeature("l_left2pos");
		addNewAtomFeature("l_right2tok");
		addNewAtomFeature("l_right2pos");
		addNewAtomFeature("l_pv");
		addNewAtomFeature("l_pn");
		addNewAtomFeature("l_fn");
		
		
		addNewAtomFeature("p1_head");
		addNewAtomFeature("p2_pos");
		addNewAtomFeature("p3_compTok");
		addNewAtomFeature("p4_compPos");
		addNewAtomFeature("p5_headRel");
		addNewAtomFeature("p6_compRel");
		addNewAtomFeature("p11_parent");
		addNewAtomFeature("p12_grand");
		addNewAtomFeature("p13_left");
		addNewAtomFeature("p14_right");
		
		
		//combination features
		//examples:
		//addNewCombFeature((new String[]{"m1m2_enju","m1m2_scla"}));	//28
		//addNewCombFeature((new String[]{"a_numb","m1_numb","m2_numb"}));
		addNewCombFeature((new String[]{"l_pn","l_fn"}));
		addNewCombFeature((new String[]{"l_pv","l_pn","l_fn"}));
		addNewCombFeature((new String[]{"p1_head","p3_compTok"}));
		addNewCombFeature((new String[]{"p2_pos","p4_compPos"}));
		addNewCombFeature((new String[]{"p2_pos","p3_compTok"}));
		addNewCombFeature((new String[]{"p1_head","p4_compPos"}));
	
	}
	
	private HashMap<String, String> atomFeatureName2ValueMap;
	
	/**
	 * Notes: adjustments made to the commands addNewAtomFeature and addNewCombFeature
	 * will change the set of features used in the ML model.
	 * 
	 */
	public PrepositionSelector(boolean loadFeatureValueOption, String featureValueFileName){		
		this.atomFeatureName2ValueMap = new HashMap<String, String>();		
		if (loadFeatureValueOption == true)
			loadFeatureValues(featureValueFileName);
	}
	
	static public void addNewAtomFeature(String name){
		allFeatures.add(new AtomFeature(name, allFeatures.size()));
		allFeatureNames.add(name);
	}
	
	static public void addNewCombFeature(String[] names){
		CombinationFeature comf = new CombinationFeature(names, allFeatures.size());
		allFeatures.add(comf);
		allFeatureNames.add(comf.getName());
	}
	
	/**
	 * This is an important function of this class.
	 * This function creates an instance from data.
	 * 
	 * 
	 * @param training = true: training instance; in this mode, new feature values are added one by one to each feature,
	 *  while training = false: predict instance; in this mode, feature value is looked up in available feature values of each feature
	 *  (which have been loaded by loadFeatureValue(fileName)).
	 * @return
	 */
	public Instance extractFeatureValues(int prepTokenIndex, int lineInDoc,
			ArrayList<Chunk> chunks, ArrayList<Token> tokens, 
			Tree root){
		
		//System.out.println("DEBUG PrepositionSelector extractFeatureValues CALL!!");
		Instance instance = new Instance(allFeatureNames.size());
		
		//initialize
		HashMap<Tree, Integer> tree2beginTokenIndex = new HashMap<Tree, Integer>();
		HashMap<Tree, Integer> tree2endTokenIndex = new HashMap<Tree, Integer>();
		Tree prepTree = null;
		Tree parentTree = null;
		Tree grandParentTree = null;
		
		//get initial values from parse tree		
		List<CoreLabel> coreLabels = root.taggedLabeledYield();		
		int numToken = tokens.size();
		ArrayList<TaggedWord> allTaggedYields = root.taggedYield();
		
		Iterator<Tree> iterator = root.iterator();
		boolean found = false;
		while (iterator.hasNext() && !found) {
			Tree next = iterator.next();
			if (!next.isLeaf()) {
				ArrayList<TaggedWord> taggedYields = next.taggedYield();
				int beginTokenIndex = allTaggedYields.indexOf(taggedYields.get(0)); 
				int endTokenIndex = allTaggedYields.indexOf(taggedYields.get(taggedYields.size() - 1)); 
				tree2beginTokenIndex.put(next, beginTokenIndex);
				tree2endTokenIndex.put(next, endTokenIndex);		
				
				if (!found && next.isPreTerminal() && (beginTokenIndex == prepTokenIndex)){
					prepTree = next;					
					parentTree = next.parent(root);
					if (parentTree != null){
						grandParentTree = parentTree.parent(root);						
					}
					found = true;					
				}
			}
		}
		
		
		
		//get the preceding and following chunks
		Chunk followingChunk = null;
		Chunk precedingChunk = null;
		for (Chunk chunk : chunks) {
			if (chunk.getBeginTokenIndex() == (prepTokenIndex + 1))
				followingChunk = chunk;
			else if (chunk.getEndTokenIndex() == (prepTokenIndex - 1))
				precedingChunk = chunk;
		}
		
		//-----------------------------------------------------------
		//create atom feature values or feature nodes
		//lexical features
		
		//left 2 tokens string, and left 2 tokens pos
		StringBuffer s1 = new StringBuffer();	
		StringBuffer s2 = new StringBuffer();	
		if (prepTokenIndex > 1){
			s1.append(tokens.get(prepTokenIndex - 2).getString());
			s2.append(tokens.get(prepTokenIndex - 2).getPostag());
		}
		
		if (prepTokenIndex > 0){
			s1.append("_");
			s1.append(tokens.get(prepTokenIndex - 1).getString());
			s2.append("_");
			s2.append(tokens.get(prepTokenIndex - 1).getPostag());
		}
		
		createAtomFeatureNode("l_left2tok", s1.toString(), instance);
		createAtomFeatureNode("l_left2pos", s2.toString(), instance);
		
		//right 2 tokens string, and right 2 tokens pos
		s1 = new StringBuffer();		
		s2 = new StringBuffer();		
		if (prepTokenIndex < numToken - 1){
			s1.append(tokens.get(prepTokenIndex + 1).getString());
			s2.append(tokens.get(prepTokenIndex + 1).getPostag());
		}
		if (prepTokenIndex < numToken - 2){
			s1.append("_");
			s1.append(tokens.get(prepTokenIndex + 2).getString());
			s2.append("_");
			s2.append(tokens.get(prepTokenIndex + 2).getPostag());
		}	
		createAtomFeatureNode("l_right2tok", s1.toString(), instance);
		createAtomFeatureNode("l_right2pos", s2.toString(), instance);
		
		//head of the preceding verb phrase
		String pv = null;
		if (precedingChunk != null && precedingChunk.getChunkLabel().equals("VP"))
			pv = tokens.get(precedingChunk.getEndTokenIndex()).getString();	//suppose that the past word in the verb phrase is head
		createAtomFeatureNode("l_pv", pv, instance);
		
		//head of the preceding noun phrase
		String pn = null;
		if (precedingChunk != null && precedingChunk.getChunkLabel().equals("NP"))
			pn = tokens.get(precedingChunk.getEndTokenIndex()).getString();	//suppose that the past word in the noun phrase is head
		createAtomFeatureNode("l_pn", pn, instance);
		
		//head of the following noun phrase
		String fn = null;
		if (followingChunk != null && followingChunk.getChunkLabel().equals("NP"))
			fn = tokens.get(followingChunk.getEndTokenIndex()).getString();	//suppose that the past word in the noun phrase is head
		createAtomFeatureNode("l_fn", fn, instance);
		
		//-----------------------------------------------------------
		//create parse features
		String p1_head = null;	//govern token of prep
		String p2_pos = null;	//pos of the govern token
		String p3_compTok = null;	//dependent token of prep
		String p4_compPos = null;	//pos of the dependent token of prep
		String p5_headRel = null;
		String p6_compRel = null;
		
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(root);
		// List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		Collection<TypedDependency> tdl = gs.allTypedDependencies();

		Iterator<TypedDependency> iter = tdl.iterator();
		while (iter.hasNext()) {
			TypedDependency next = iter.next();
			GrammaticalRelation reln = next.reln();
			TreeGraphNode gov = next.gov();
			TreeGraphNode dep = next.dep();
			int govIndex = gov.index();		
			int depIndex = dep.index();
			
			if (depIndex - 1 == prepTokenIndex){//because dependency nodes are 0-based
				if (govIndex > 0){
					p1_head = tokens.get(govIndex - 1).getString();
					p2_pos = coreLabels.get(govIndex - 1).tag();		
				}else{
					p1_head = "ROOT";
					p2_pos = "ROOT";
				}
				p5_headRel = reln.getShortName();
			}
			
			if (govIndex - 1 == prepTokenIndex){//because dependency nodes are 0-based
				if (depIndex > 0){
					p3_compTok = tokens.get(depIndex - 1).getString();
					p4_compPos = coreLabels.get(depIndex - 1).tag();	
				}else{
					p3_compTok = "ROOT";
					p4_compPos = "ROOT";
				}
				
				p6_compRel = reln.getShortName();
			}
			
			//System.out.println("NGAN " + next.reln() + " " + next.gov() + " "
			//		+ next.dep());
		}
			
		createAtomFeatureNode("p1_head", p1_head, instance);
		createAtomFeatureNode("p2_pos", p2_pos, instance);
		createAtomFeatureNode("p3_compTok", p3_compTok, instance);
		createAtomFeatureNode("p4_compPos", p4_compPos, instance);
		createAtomFeatureNode("p5_headRel", p5_headRel, instance);
		createAtomFeatureNode("p6_compRel", p6_compRel, instance);
		
		//-----------------------------------------------------------
		//phrase structure features
		String p11_parent = null;
		String p12_grand = null;
		String p13_left = null;
		String p14_right = null;
		
		if (parentTree != null)
			p11_parent = parentTree.label().value();
		createAtomFeatureNode("p11_parent", p11_parent, instance);
		
		if (grandParentTree != null)
			p12_grand = grandParentTree.label().value();
		createAtomFeatureNode("p12_grand", p12_grand, instance);
		
		
		Tree[] children = null;
		if (grandParentTree != null)
			children = grandParentTree.children();
		int parentIndex = findPosInChildren(parentTree, children);
		
		if (parentIndex > -1){
			if (parentIndex > 0)
				p13_left = children[parentIndex - 1].label().value();
			if (parentIndex < children.length - 1)
				p14_right = children[parentIndex + 1].label().value();
		}
		createAtomFeatureNode("p13_left", p13_left, instance);
		createAtomFeatureNode("p14_right", p14_right, instance);
		
		//-----------------------------------------------------------------------------------------------------
		//iterate through combination feature and create feature nodes for combination features
		for (CRFeature feature : allFeatures) {
			if (feature instanceof CombinationFeature){//this is a combination feature
				CombinationFeature comf = (CombinationFeature) feature;
				if (comf != null){
					createCombFeatureNode(comf, instance);
					//System.out.println("\tcomf name = " + comf.getName());
				}
			}
		}
		//-Finished feature value extraction, create output feature nodes--------------------------------------
		//add feature nodes (only output those features want to use in the model)
		return instance;
	}
	
	
	
	private int findPosInChildren(Tree parentTree, Tree[] children) {
		// TODO Auto-generated method stub
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				if (children[i] == parentTree)
					return i;
			}
		return -1;
	}


	public FeatureNode createCombFeatureNode(CombinationFeature comf, Instance instance){
		FeatureNode ret = null;
		boolean hasError = false;
		
		ArrayList<String> atomFeatureNames = comf.getAtomFeatureNames();
		StringBuffer valueStr = new StringBuffer();
		for (String aName : atomFeatureNames) {
			if (atomFeatureName2ValueMap.containsKey(aName)){ 
				String atomValue = atomFeatureName2ValueMap.get(aName);
				valueStr.append(atomValue);
				valueStr.append('_');
			}else{
				hasError = true;
				break;
			}			
		}
		
		if (!hasError){
			int index = allFeatures.indexOf(comf);
			instance.addFeatureValue(index, valueStr.toString());
		}
		
		return ret;
	}
	
	public void createAtomFeatureNode(String atomFeatureName, String valueStr, Instance instance){
		int index = allFeatureNames.indexOf(atomFeatureName);
		if (valueStr != null && valueStr.equals(""))
			valueStr = "null";
		if (index >= 0) {			
			instance.addFeatureValue(index, valueStr);			
			atomFeatureName2ValueMap.put(allFeatureNames.get(index), valueStr);
		}
		
	}
	
	
	/**
	 * Saves feature values to file, so that we can use for feature value mapping in predict phase coz the same feature values 
	 * should be mapped to same integer values. 
	 * @param fileName
	 */
	public void saveFeatureValues(String fileName){
		try {
			PrintWriter featureValueFile = new PrintWriter(new File (fileName));
			
			int n = allFeatures.size();
			for (int i = 0; i < n; i++){
				CRFeature feature = allFeatures.get(i);
				ArrayList<String> values = feature.getValues();
				int valuesSize = values.size();
				featureValueFile.append("F" + "\t" + feature.getName() + "\t" + i + "\t" + valuesSize +  "\t" + feature.getScaleFactor() + "\n");
				for (int j=0; j<valuesSize; j++){
					featureValueFile.append("V" + "\t" + j + "\t" + values.get(j) + "\n");
				}
			}
			
			featureValueFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void loadFeatureValues(String fileName){

		BufferedReader scanner = null;
		try {
			scanner = new BufferedReader(new FileReader(new File(fileName)));
		
			String line = null;	
			CRFeature curFeature = null;
			while ((line = scanner.readLine()) != null) {
				String[] strings = line.split("\\t");
				if (line.startsWith("F")){	//new feature
					int featureIndex = Integer.valueOf(strings[2]);
					curFeature = allFeatures.get(featureIndex);
					curFeature.resetValues();
					curFeature.setScaleFactor(Double.valueOf(strings[4]));
				}else if (line.startsWith("V")){	//feature values
					String featureValue = strings[2];
					curFeature.addNewValue(featureValue);
				}
				
			}
			scanner.close();
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public void calculateScaleFactors(){
		for (CRFeature f : allFeatures) {
			f.calculateScaleFactor();
		}
	}
	
	public void scale(ArrayList<FeatureNode> nodes){
		for (FeatureNode node : nodes) {
			scale(node);
		}
		
	}
	/**
	 * transform {0,1}-valued feature node to multi {-1,1}-valued feature nodes.
	 * @param node
	 * @return
	 */
	public static ArrayList<FeatureNode> transformToOneOne(FeatureNode node){
		ArrayList<FeatureNode> newNodes = null;		
		
		int index = node.index / 1000;
		int indexstart = index * 1000;
		int valueindex = node.index % 1000;
		
		CRFeature feature = allFeatures.get(index);
		int nr_values = feature.getValues().size();
		newNodes = new ArrayList<FeatureNode>(nr_values);
		for (int i = 0; i < nr_values; i++) {
			if (i != valueindex){
				newNodes.add(new FeatureNode(indexstart + i, -1));
			}else{
				newNodes.add(node);
			}
		}				
		
		
		return newNodes;
	}
	
	public static void normalize(ArrayList<FeatureNode> nodes){
		double sum = 0;
		for (FeatureNode node : nodes) {
			sum += Math.pow(node.value, 2);						
		}
		double len = Math.sqrt(sum);
		//System.out.println("Normalize with len = " + len);
		
		for (FeatureNode node : nodes) {
			//System.out.print( node.value + " \t ");
			
			node.value = node.value/(double)len;
			//System.out.println( node.value);
		}
	}
	
	public  static ArrayList<FeatureNode> transformToOneOneAndNormalize(ArrayList<FeatureNode> nodes){
		ArrayList<FeatureNode> newNodes = new ArrayList<FeatureNode>();		
		
		for (FeatureNode featureNode : nodes) {
			newNodes.addAll(transformToOneOne(featureNode));
		}
		
		normalize(newNodes);
		
		return newNodes;
	}
	
	public void scale(FeatureNode node){
		CRFeature feature = allFeatures.get(node.index);
		node.value = node.value * feature.getScaleFactor();
	}
	
	public static void main(String[] args){
		if (args[0].equals("train")){
			ColumnDataClassifier cdc = new ColumnDataClassifier(
					"/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/prep.prop");
			Classifier<String, String> cl = cdc
					.makeClassifier(cdc
							.readTrainingExamples("/home/nltngan/Research/EDCW/stanford-classifier-2012-05-22/examples/prep.train"));
			
			//serialize
			
		}else if (args[0].equals("predict")){
			
		}
			
	}
	
	
}
