package jp.ac.nii.ednii.utility;
import java.util.HashMap;
import java.util.Set;


public class MorphologyDictionaryEntry {
	
	private String lemma;
	private HashMap<String, String> pos2wordformMap;
	
	
	public MorphologyDictionaryEntry(String lemma){
		this.lemma = lemma.toLowerCase();
		this.pos2wordformMap = new HashMap<String, String>();
	}
	
	public static MorphologyDictionaryEntry createMorphologyDictionaryEntry(String line){
		MorphologyDictionaryEntry entry;
		//read a line in dictionary file and create an entry
		String tmpLine = line + " ";
		String[] splits = tmpLine.split(" ");
		entry = new MorphologyDictionaryEntry(splits[0]);
		
		for (int i = 1; i < (splits.length - 1); i++) {
			entry.addAWordform(splits[i], splits[i+1]);
			i++;
		}		
		return entry;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		sb.append(lemma);		
		Set<String> pos2WordformMapKeys = pos2wordformMap.keySet();
		for (String pos : pos2WordformMapKeys) {
			String wordform = pos2wordformMap.get(pos);
			sb.append(' ');
			sb.append(pos);
			sb.append(' ');
			sb.append(wordform);
		}
		return sb.toString();
	}
	
	public String getWordForm(String pos){
		String ret = null;
		if (this.pos2wordformMap.containsKey(pos))
			ret = this.pos2wordformMap.get(pos);
		
		return ret;
	}
	public MorphologyDictionaryEntry(String lemma,
			HashMap<String, String> pos2wordformMap) {
		super();
		this.lemma = lemma;
		this.pos2wordformMap = pos2wordformMap;
	}
	
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public HashMap<String, String> getPos2wordformMap() {
		return pos2wordformMap;
	}
	public void setPos2wordformMap(HashMap<String, String> pos2wordformMap) {
		this.pos2wordformMap = pos2wordformMap;
	}
	
	public boolean addAWordform(String pos, String wordform){
		boolean ret = false;
		if (!pos2wordformMap.containsKey(pos)){
			ret = true;
			pos2wordformMap.put(pos, wordform.toLowerCase());
		}
		return ret;
	}
}
