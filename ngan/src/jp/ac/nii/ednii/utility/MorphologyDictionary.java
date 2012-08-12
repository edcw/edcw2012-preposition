package jp.ac.nii.ednii.utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.plaf.SliderUI;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

import jp.ac.nii.ednii.FileListing;
import jp.ac.nii.ednii.LearnerDocument;


public class MorphologyDictionary {
	
	private HashMap<String, MorphologyDictionaryEntry> lemma2entryMap;
	public WordNetDatabase wordNetDatabase;
	
	public MorphologyDictionary() {
		// TODO Auto-generated constructor stub
	}
	
	public void createMorphoDict(WordNetDatabase wordNetDatabase, String postaggedFilesDir){
		this.lemma2entryMap = new HashMap<String, MorphologyDictionaryEntry>();
		
		try {
			List<File> files = FileListing.getFileListing(new File(postaggedFilesDir));
			for (File file : files) {
				if (file.getName().endsWith(".POS") || file.getName().endsWith(".mos")){
					System.out.println("Processing file: " + file.getAbsolutePath());
					//analyse this file to get wordform and POS					
					this.analyzePosTaggedFile(wordNetDatabase, file);
				}
			}
			this.lemma2entryMap = sortHashMap(lemma2entryMap);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
	}
	
	private void analyzePosTaggedFile(WordNetDatabase wordNetDatabase, File file){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null){
				
				String[] blankSplits = line.split(" ");
				for (int i = 0; i < blankSplits.length; i++) {
					String[] split2 = blankSplits[i].split("/");
					if (split2.length == 2){	// has form Fulton/NNP
						if (!(split2[1].equals("NNP") || split2[1].equals("NNPS"))){
							//find lemma of split2[0]
							String pos = split2[1].replace("^", "");
							String wordform = split2[0];
							String lemma = findLemma(wordNetDatabase, wordform, pos);
							if (lemma == null)
								lemma = wordform;
							if (lemma != null && lemma.length()>0 && Character.isLetter(lemma.charAt(0)) ){			
								String lowerCaselemma = lemma.toLowerCase();
								
								if (this.lemma2entryMap.containsKey(lowerCaselemma)){
									MorphologyDictionaryEntry entry = this.lemma2entryMap.get(lowerCaselemma);
									entry.addAWordform(pos, wordform);
								}else{									
									MorphologyDictionaryEntry entry = new MorphologyDictionaryEntry(lowerCaselemma);
									entry.addAWordform(pos, wordform);
									this.lemma2entryMap.put(lowerCaselemma, entry);
								}
							}
								
						}
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void initializeWordNet(){
		wordNetDatabase = WordNetDatabase.getFileInstance();
	}
	/**
	 * Use WordNet to find lemma 
	 * @return
	 */
	public static String findLemma(WordNetDatabase wordNetDatabase, String wordform, String pos){
		String ret = null;
		
		if (wordNetDatabase == null){
			System.err.println("You have to call initializeWordNet() before creating morphology dictionary.");
			return null;
		}		
		
		SynsetType synsetType = SynsetType.ADJECTIVE;
		if (pos.startsWith("N"))
			synsetType = SynsetType.NOUN;
		else if (pos.startsWith("V"))
			synsetType = SynsetType.VERB;
		else if (pos.startsWith("J"))
			synsetType = SynsetType.ADJECTIVE;
		else if (pos.startsWith("R"))
			synsetType = SynsetType.ADVERB;
		
			
		//System.out.println("DEBUG 1: \"" + wordform + "\" synset type: " + synsetType);
		String[] baseFormCandidates = wordNetDatabase.getBaseFormCandidates(wordform, synsetType);
		if (baseFormCandidates.length > 0)
			ret = baseFormCandidates[0];
		
		return ret;
	}
	
	public void loadDictFromFile(String path){
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line;
			this.lemma2entryMap = new HashMap<String, MorphologyDictionaryEntry>();
			while ((line = br.readLine()) != null){
				MorphologyDictionaryEntry entry = MorphologyDictionaryEntry.createMorphologyDictionaryEntry(line);
				if (entry != null)
					this.lemma2entryMap.put(entry.getLemma(), entry);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String lookUpForWordForm(String lemma, String pos){
		String ret = null;
		String downcaseLemma = lemma.toLowerCase();
		if (this.lemma2entryMap.containsKey(downcaseLemma)){
			MorphologyDictionaryEntry entry = this.lemma2entryMap.get(downcaseLemma);
			ret = entry.getWordForm(pos);
		}
		return ret;
	}
	
	public int saveDictToFile(String path){
		int entryCount = 0;
		try {
			PrintWriter pw = new PrintWriter(new File(path));
			Set<String> keySet = lemma2entryMap.keySet();
			
			for (String lemma : keySet) {						
				MorphologyDictionaryEntry entry = lemma2entryMap.get(lemma);
				if (!entry.getPos2wordformMap().isEmpty()){
					pw.append(entry.toString());
					pw.append(System.getProperty("line.separator"));
					entryCount++;
				}
			}
			
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entryCount;
	}
	
	/**
	 * Sort hashmap
	 * @param input
	 * @return
	 */
	private HashMap<String, MorphologyDictionaryEntry> sortHashMap(HashMap<String, MorphologyDictionaryEntry> input){
	    Map<String, MorphologyDictionaryEntry> tempMap = new HashMap<String, MorphologyDictionaryEntry>();
	    for (String wsState : input.keySet()){
	        tempMap.put(wsState, input.get(wsState));
	    }

	    List<String> mapKeys = new ArrayList<String>(tempMap.keySet());
	    List<MorphologyDictionaryEntry> mapValues = new ArrayList<MorphologyDictionaryEntry>(tempMap.values());
	    HashMap<String, MorphologyDictionaryEntry> sortedMap = new LinkedHashMap<String, MorphologyDictionaryEntry>();
	    TreeSet<String> sortedSet = new TreeSet<String>(mapKeys);
	    Object[] sortedArray = sortedSet.toArray();
	    int size = sortedArray.length;
	    for (int i=0; i<size; i++){
	        sortedMap.put((String)sortedArray[i], mapValues.get(mapKeys.indexOf(sortedArray[i])));
	    }
	    return sortedMap;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*create a dict form file
		MorphologyDictionary morphoDict = new MorphologyDictionary();
		//morphoDict.initializeWordNet();
		WordNetDatabase wordNetDatabase = WordNetDatabase.getFileInstance();		
		//morphoDict.createMorphoDict(wordNetDatabase, "/home/nltngan/Research/EDCW/penn3/TAGGED/POS/BROWN");	//test
		//morphoDict.createMorphoDict(wordNetDatabase, "/media/Windows7_OS/NGAN/Research/corpus/penn3/TAGGED/POS/BROWN"); // penn brown only		
		morphoDict.createMorphoDict(wordNetDatabase, "/media/Windows7_OS/NGAN/Research/corpus/penn3/TAGGED/POS"); //all penn
		
		//save to file
		System.out.println("Number of entry" + morphoDict.saveDictToFile("/home/nltngan/Research/EDCW/morphoDict/morphodict.dic"));
		//*/
		
		//*
		//load from file
		MorphologyDictionary morphoDict = new MorphologyDictionary();
		morphoDict.loadDictFromFile("/home/nltngan/Research/EDCW/morphoDict/morphodict.dic");
		
		//look up
		System.out.println(morphoDict.lookUpForWordForm("classe", "NNS"));
		//*/
		
		System.out.println("Done.");
	}

}
