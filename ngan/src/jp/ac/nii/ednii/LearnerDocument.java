package jp.ac.nii.ednii;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jp.ac.nii.ednii.utility.MorphologyDictionary;

import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.impl.file.Morphology;

/**
 * Data structure stores information of a document in KJ Corpus format. * 
 * 
 * @author nltngan
 *
 */
public class LearnerDocument {
	private ArrayList<String> textLines;
	private ArrayList<LearnerError> errors;
	private ArrayList<ArrayList<Chunk>> chunks;
	private ArrayList<ArrayList<Token>> tokens;
	
	private String fileName;
	/**
	 * Reads a file in KJ Corpus format, strips all error annotations to create blank text lines
	 *  and errors.
	 * @param file
	 */
	public LearnerDocument(File errorFile){
		createLearnerDocument(errorFile);
	}
	
	public void createLearnerDocument(File file){
		this.errors = new ArrayList<LearnerError>();
		this.textLines = new ArrayList<String>();
		
		String filename = file.getName();
		this.fileName = filename.substring(0, filename.indexOf('.'));
		
		StringBuffer sb= new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String strLine;
			//Read File Line By Line
			
			ArrayList<Integer> openTagPos = new ArrayList<Integer>();
			ArrayList<Integer> beginDeletes = new ArrayList<Integer>();
			ArrayList<Integer> endDeletes = new ArrayList<Integer>();
			ArrayList<LearnerError> lineErrors = new ArrayList<LearnerError>(); 
			ArrayList<LearnerError> tempArray = new ArrayList<LearnerError>(); 
			int lineCount=-1;
			while ((strLine = br.readLine()) != null)   {	
				lineCount++;				
				int accumDeleteLen = 0;				
				//analyze strLine to get error annotations				
				openTagPos.clear();
				lineErrors.clear();
				beginDeletes.clear();
				endDeletes.clear();
				tempArray.clear();
				
				//int depth = -1;
				int beginErrorTag = -1;
				int endErrorTag = -1;
				do{
					beginErrorTag = strLine.indexOf("<", beginErrorTag + 1);
					if (beginErrorTag > -1){	//strLine contains xml open tag
						endErrorTag = strLine.indexOf(">", beginErrorTag + 1);
						String xmlTagContent = strLine.substring(beginErrorTag, endErrorTag + 1);					
						if (!xmlTagContent.startsWith("</")){//an open tag
							int indexOfSpace = xmlTagContent.indexOf(" ");
							String correction = null;
							if (indexOfSpace < 0)
								indexOfSpace = xmlTagContent.indexOf(">");
							else{
								int pos1 = xmlTagContent.indexOf('"');
								correction = xmlTagContent.substring(pos1 + 1, xmlTagContent.indexOf('"', pos1 + 1));
							}
							
							String errorType = xmlTagContent.substring(1, indexOfSpace);
							
							
							//create a learner error
							LearnerError learnerError = new LearnerError(beginErrorTag - accumDeleteLen, 0, false, errorType, correction, lineCount);
							accumDeleteLen += xmlTagContent.length();
							lineErrors.add(learnerError);
							tempArray.add(learnerError);
							beginDeletes.add(beginErrorTag);
							endDeletes.add(beginErrorTag + xmlTagContent.length() - 1);							
						}else{//a close tag							
							tempArray.get(tempArray.size() - 1).setEnd(beginErrorTag - accumDeleteLen);
							tempArray.remove(tempArray.size() - 1);							
							beginDeletes.add(beginErrorTag);
							endDeletes.add(beginErrorTag + xmlTagContent.length() - 1);
							accumDeleteLen += xmlTagContent.length();
							//depth--;
							
						}
							
					}
					
				}while (beginErrorTag != -1);
			
								
				//create text-only line 
				StringBuffer bfNewLine= new StringBuffer(strLine);
				
				int i= beginDeletes.size()-1;
				for (; i>=0; i--) {
					bfNewLine.delete(beginDeletes.get(i), endDeletes.get(i) + 1);
				}
				//create document text only
				this.errors.addAll(lineErrors);
				this.textLines.add(bfNewLine.toString());
				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public LearnerDocument(WordNetDatabase wordNetDatabase, File errorFile, File poschunkFile){
		
		createLearnerDocument(errorFile);
		
		//initilize
		int numLine = this.textLines.size();
		chunks = new ArrayList<ArrayList<Chunk>>(numLine);
		tokens = new ArrayList<ArrayList<Token>>(numLine);
	
		//read and analyze poschunkFile
		readPosChunkFile(wordNetDatabase, poschunkFile);
		
	}
	
	private void readPosChunkFile(WordNetDatabase wordnetDatabase, File poschunkFile) {
		// TODO Auto-generated method stub
		try {
			BufferedReader br = new BufferedReader(new FileReader(poschunkFile));
			String line;			
			String chunkLabel;
			
			int lineCount = 0;
			while ((line = br.readLine()) != null){				
				String textLine = this.textLines.get(lineCount);
				ArrayList<Chunk> chunks_i = new ArrayList<Chunk>();				
				ArrayList<Token> tokens_i = new ArrayList<Token>();
				
				/*String[] splits = line.split("[\\[\\] ]");
				for (int i = 0; i < splits.length; i++) {
					if (splits[i] != ""){
						String[] split2 = splits[i].split("/");
						if (split2.length == 1){	//split[i] is a chunk label
							
						}
					}
				}*/
				
				int tokCount = 0;
				int lastPosInTextLine = -1;
				int prevBeginChunkPos = -1;
				int prevEndChunkPos = -1;
				int beginChunkPos, endChunkPos;
				do{
					beginChunkPos = line.indexOf('[', prevEndChunkPos + 1);
					endChunkPos = line.indexOf(']', beginChunkPos + 1);
					
					if (beginChunkPos == -1 && prevEndChunkPos > -1 && prevEndChunkPos < line.length() - 1){
						String outsideChunkStr = line.substring(prevEndChunkPos + 1);
						String[] split1 = outsideChunkStr.split(" ");
						for (int i = 0; i < split1.length; i++) {
							String[] split2 = split1[i].split("/");
							int len = split2.length;
							if (len > 1){	// has form is/VBZ or make/keep/VBP
								String lemma = MorphologyDictionary.findLemma(wordnetDatabase, split2[0], split2[1]);
								
								String wordform = null; 
								if (split2.length > 2){
									wordform = split1[i].substring(0, split1[i].lastIndexOf('/'));
								}else
									wordform = split2[0];
								
								String pos = split2[len - 1];
								if (lemma == null)
									lemma = wordform;
								
								
								int beginOffset = textLine.indexOf(wordform, lastPosInTextLine);						
								lastPosInTextLine = beginOffset + wordform.length();						
								Token token = new Token(lineCount, wordform, lemma, pos, beginOffset, lastPosInTextLine);
								if (beginOffset == -1)
									System.err.println("LearnerDocument : CANNOT FIND " + token + " IN LINE : " + textLine + " IN FILE: " + fileName);
								tokens_i.add(token);								
								tokCount++;						
							}
						}
					}
					
					//analyse outside [] string
					if (beginChunkPos > prevEndChunkPos){
						String outsideChunkStr = line.substring(prevEndChunkPos + 1, beginChunkPos);
						String[] split1 = outsideChunkStr.split(" ");
						for (int i = 0; i < split1.length; i++) {
							String[] split2 = split1[i].split("/");
							int len = split2.length;
							if (len > 1){	// has form is/VBZ or make/keep/VBP
								String lemma = MorphologyDictionary.findLemma(wordnetDatabase, split2[0], split2[1]);
								
								String wordform = null; 
								if (split2.length > 2){
									wordform = split1[i].substring(0, split1[i].lastIndexOf('/'));
								}else
									wordform = split2[0];
								
								String pos = split2[len - 1];
								if (lemma == null)
									lemma = wordform;
								
								
								int beginOffset = textLine.indexOf(wordform, lastPosInTextLine);						
								lastPosInTextLine = beginOffset + wordform.length();						
								Token token = new Token(lineCount, wordform, lemma, pos, beginOffset, lastPosInTextLine);
								if (beginOffset == -1)
									System.err.println("LearnerDocument : CANNOT FIND " + token + " IN LINE : " + textLine + " IN FILE: " + fileName);
								tokens_i.add(token);								
								tokCount++;						
							}
						}
					}
					
					prevBeginChunkPos = beginChunkPos;
					prevEndChunkPos = endChunkPos;
					
					//analyse inside [] string
					if (beginChunkPos > -1 && endChunkPos > -1){						
						String insideChunkStr = line.substring(beginChunkPos + 1, endChunkPos);
						int beginToken = tokCount;
						int endToken = tokCount;	//is the index of the last token in chunk
						
						
						String[] split1 = insideChunkStr.split(" ");
						chunkLabel = split1[0];
						for (int i = 1; i < split1.length; i++) {
							//this.analyzeLemmaPosString(split1[i], wordnetDatabase, lineCount,
							//		tokCount, endToken, tokens_i, textLine, lastPosInTextLine);
							String[] split2 = split1[i].split("/");
							int len = split2.length;
							if (len > 1){	// has form is/VBZ or make/keep/VBP
								String lemma = MorphologyDictionary.findLemma(wordnetDatabase, split2[0], split2[1]);
								
								String wordform = null; 
								if (split2.length > 2){
									wordform = split1[i].substring(0, split1[i].lastIndexOf('/'));
								}else
									wordform = split2[0];
								
								String pos = split2[len - 1];
								if (lemma == null)
									lemma = wordform;
								
								
								int beginOffset = textLine.indexOf(wordform, lastPosInTextLine);						
								lastPosInTextLine = beginOffset + wordform.length();						
								Token token = new Token(lineCount, wordform, lemma, pos, beginOffset, lastPosInTextLine);
								if (beginOffset == -1)
									System.err.println("DEBUG LearnerDocument : CANT FIND " + token + " IN LINE : " + textLine + " IN FILE: " + fileName);
								tokens_i.add(token);
								endToken = tokCount;
								tokCount++;						
							}
						}
						
						
						if (chunkLabel != null){
							Chunk chunk = new Chunk(lineCount, beginToken, endToken, chunkLabel);
							chunk.setBegin(tokens_i.get(beginToken).getBegin());
							chunk.setEnd(tokens_i.get(endToken).getEnd());
							chunks_i.add(chunk);
						}
					}
				}while (beginChunkPos > -1);
				
				this.chunks.add(chunks_i);				
				this.tokens.add(tokens_i);
				lineCount++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//String textLine, String analyzedStr, String chunkLabel, int lastPosInTextLine,
	//int lineCount, int tokCount, int endToken, ArrayList<Token> tokens_i, WordNetDatabase wordnetDatabase
	public void analyzeLemmaPosString(String analyzedStr, WordNetDatabase wordnetDatabase,
			int lineCount, int tokCount, int endToken, ArrayList<Token> tokens_i, String textLine, int  lastPosInTextLine){
		String[] split2 = analyzedStr.split("/");
		int len = split2.length;
		if (len > 1){	// has form is/VBZ or make/keep/VBP
			String lemma = MorphologyDictionary.findLemma(wordnetDatabase, split2[0], split2[1]);
			
			String wordform = split2[0];
			if (split2.length > 2)
				for (int j = 1; j < len - 1; j ++){
					wordform += split2[j];
				}
			String pos = split2[len - 1];
			if (lemma == null)
				lemma = wordform;
			
			
			int beginOffset = textLine.indexOf(wordform, lastPosInTextLine);						
			lastPosInTextLine = beginOffset + wordform.length();						
			Token token = new Token(lineCount, wordform, lemma, pos, beginOffset, lastPosInTextLine);
			if (beginOffset == -1)
				System.err.println("LearnerDocument : CANT FIND " + token + " IN LINE : " + textLine + " IN FILE: " + fileName);
			tokens_i.add(token);
			endToken = tokCount;
			tokCount++;						
		}
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer bf = new StringBuffer();
		bf.append("*************LearnerDocument : " + this.fileName + " *******************\n");
		
		int n = this.textLines.size();
		for (int i = 0; i < n; i++) {
			String line = this.textLines.get(i);
			bf.append(line);
			bf.append("\n");
			System.out.println(n);
						
			for (Token token : this.tokens.get(i)){
				bf.append(token.toString());				
				bf.append(" : " + this.textLines.get(token.getLine()).substring(token.getBegin(), token.getEnd()));
				bf.append("\n");
			}
			
			for (Chunk chunk : this.chunks.get(i)){
				bf.append(chunk.toString());
				bf.append("\n");
				
			}
		}
		bf.append("********************************\n");
		
		
		return bf.toString();
	}
	/**
	 * 
	 * @param path
	 * @return
	 */
	public static boolean saveToFile(ArrayList<String> textLines, ArrayList<LearnerError> errors, String path){
		boolean ret = true;
		try {
			PrintWriter printWriter = new PrintWriter(new File(path));
			HashMap<Integer, ArrayList<LearnerError>> lineIndex2errorsMap = new HashMap<Integer, ArrayList<LearnerError>>();
			
			int lineIndex;
			ArrayList<LearnerError> arrayList = null;
			for (LearnerError error : errors) {
				lineIndex = error.getLineInDoc();
				if (lineIndex2errorsMap.containsKey(lineIndex))
					lineIndex2errorsMap.get(lineIndex).add(error);
				else{
					arrayList = new ArrayList<LearnerError>();
					arrayList.add(error);
					lineIndex2errorsMap.put(lineIndex, arrayList);
				}
					
			}
			
			//printWriter.append("<document>");
			//printWriter.append(System.getProperty("line.separator"));
			int len = textLines.size();
			for (int i = 0; i < len; i++) {			
				String textLine = textLines.get(i);
				if (lineIndex2errorsMap.containsKey(i)){	//this sentence/line has errors, then create xml tags for the line
					printWriter.append(insertXMLErrorTags2(textLine, lineIndex2errorsMap.get(i)));					
				}else{	//write the text only
					printWriter.append(textLine);					
				}
				printWriter.append(System.getProperty("line.separator"));
			}
			//printWriter.append("</document>");
			//printWriter.append(System.getProperty("line.separator"));
			
			printWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			ret = false;
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public static boolean saveToFile(LearnerDocument doc, String dir){
		return LearnerDocument.saveToFile(doc.getTextLines(), doc.getErrors(), dir + "/" + doc.getFileName() + ".sys");
	}
	
	/**
	 * Inserts XML error tags to a line, given the errors appearing in that line.
	 * 
	 * @param line
	 * @param errors
	 * @return
	 */
	public static String insertXMLErrorTags2(String line, ArrayList<LearnerError> errors){
			
		StringBuffer ret = new StringBuffer(line);
		
		//create a fake error for root node
		LearnerError rootError = new LearnerError(0, line.length(), true, "", "", 0);
		XMLTree xmlTree = new XMLTree(new XMLTreeNode(rootError));
		for (LearnerError error : errors) {
			xmlTree.addXMLNode(error);
		}
		
		ArrayList<XMLTagInfo> tags = xmlTree.collectXMLTags();
		
		int size = tags.size();
		for (int i = size - 1; i>-1; i--){	//insert tag to text line form right to left
			XMLTagInfo tag = tags.get(i);
			ret.insert(tag.offset, tag.tag);
		}
		
		return ret.toString();
	}
	
	
	public static LearnerDocument[] loadACollectionFromFolderRecursively(String dir, String extension){
		
		ArrayList<LearnerDocument> ret = new ArrayList<LearnerDocument>();
		
		try {
			List<File> files = FileListing.getFileListing(new File(dir));
			for (File file : files) {
				if (file.getName().endsWith(extension)){
					LearnerDocument learnerDocument = new LearnerDocument(file);
					ret.add(learnerDocument);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		return ret.toArray(new LearnerDocument[ret.size()]);
	}

	public static LearnerDocument[] loadACollectionFromFolderRecursivelyWithPosChunk(String dir, String extension1, String poschunkExt, WordNetDatabase wordNetDatabase){	//read pos chunk
		WordNetDatabase wordnetDatabase = WordNetDatabase.getFileInstance();
		
		ArrayList<LearnerDocument> ret = new ArrayList<LearnerDocument>();
		
		try {
			List<File> files = FileListing.getFileListing(new File(dir));
			
			HashMap<File, File> errFile2poschunkFileMap = new HashMap<File, File>();
			HashMap<String, File> fileName2errFileMap = new HashMap<String, File>();
			for (File file : files) {
				if (file.getName().endsWith(extension1))
					fileName2errFileMap.put(file.getName(), file);
			}
			
			for (File file : files) {				
				if (file.getName().endsWith(poschunkExt))
					errFile2poschunkFileMap.put(fileName2errFileMap.get(file.getName().replace(poschunkExt, extension1)), file);
			}
			
			Set<File> keySet = errFile2poschunkFileMap.keySet();
			for (File file : keySet) {		
				LearnerDocument learnerDocument = new LearnerDocument(wordnetDatabase, file, errFile2poschunkFileMap.get(file));	//error file and pos chunk file
				ret.add(learnerDocument);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		return ret.toArray(new LearnerDocument[ret.size()]);
	}
	
	
	public static boolean saveAColection(LearnerDocument[] documents, String dir){
		boolean ret = true;
		for (int i = 0; i < documents.length; i++) {
			LearnerDocument.saveToFile(documents[i], dir);
		}
		return ret;
	}
	
	
	public ArrayList<LearnerError> getErrors() {
		return errors;
	}

	public void setErrors(ArrayList<LearnerError> errors) {
		this.errors = errors;
	}

	public ArrayList<String> getTextLines() {
		return textLines;
	}

	public void setTextLines(ArrayList<String> textLines) {
		this.textLines = textLines;
	}	

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ArrayList<ArrayList<Chunk>> getChunks() {
		return chunks;
	}

	public void setChunks(ArrayList<ArrayList<Chunk>> chunks) {
		this.chunks = chunks;
	}

	public ArrayList<ArrayList<Token>> getTokens() {
		return tokens;
	}

	public void setTokens(ArrayList<ArrayList<Token>> tokens) {
		this.tokens = tokens;
	}
	
}
