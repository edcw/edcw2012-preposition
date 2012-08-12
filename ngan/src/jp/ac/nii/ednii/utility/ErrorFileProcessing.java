package jp.ac.nii.ednii.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jp.ac.nii.ednii.FileListing;
import jp.ac.nii.ednii.LearnerDocument;
import jp.ac.nii.ednii.LearnerError;

/**
 * Tools for processing learner documents (containing errors).
 * 
 * @author nltngan
 *
 */
public class ErrorFileProcessing {
	
	public static void replaceAllTags(String inputFolder, String outputFolder, String extension, String oldtag, String newtag){
		try {
			List<File> files = FileListing.getFileListing(new File(inputFolder));
			
			for (File file : files) {
				if (file.getName().endsWith(extension)){
					PrintWriter printWriter = new PrintWriter(new File(outputFolder  + "/" + file.getName().replace("." + extension, ".sys")));
					System.out.println(outputFolder + "/" + file.getName().replace("." + extension, ".sys"));
					replace(file, printWriter, oldtag, newtag);
					printWriter.close();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
	}
	
	private static void replace(File file, PrintWriter printWriter, String oldtag,
			String newtag) {
		// TODO Auto-generated method stub
		StringBuffer sb= new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String strLine;
			
			while ((strLine = br.readLine()) != null)   {	
				int beginTagName = -1;
				StringBuffer s = new StringBuffer(strLine);
				do{
					beginTagName = s.indexOf("<", beginTagName + 1);
					if (beginTagName > -1){
						boolean isOpenTag = true;
						if (s.charAt(beginTagName + 1) == '/'){
							beginTagName++;
							isOpenTag = false;
						}
						
						int endTagName = s.indexOf(">", beginTagName);
						int spaceInTag = s.indexOf(" ", beginTagName);
						if (spaceInTag < endTagName && spaceInTag > -1)
							endTagName = spaceInTag;
						
						String tag;
						if (endTagName > -1){				
							tag = s.substring(beginTagName + 1, endTagName);
							
							
							if (oldtag == null){
								if (isOpenTag){
									s.replace(beginTagName + 1, endTagName, newtag);
									beginTagName = beginTagName + newtag.length() + 1;
								}else{
									s.replace(beginTagName + 1, endTagName, newtag);
									beginTagName = beginTagName + newtag.length() + 1;
								}
									
							}else
								if (tag.equals(oldtag)){
									if (isOpenTag){
										s.replace(beginTagName + 1, endTagName, newtag);
										beginTagName = beginTagName + newtag.length() + 1;
									}else{
										s.replace(beginTagName + 1, endTagName, newtag);
										beginTagName = beginTagName + newtag.length() + 1;
									}
								}
						}
					}
				}while (beginTagName > -1);
				
				printWriter.append(s.toString());
				printWriter.append("\n");
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length > 4)
			replaceAllTags(args[0], args[1], args[2], args[3], args[4]);
		else 
			replaceAllTags(args[0], args[1], args[2], null, args[3]);
		System.out.println("Done");
	}

}
