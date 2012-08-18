package jp.ac.nii.ednii;

import java.util.Comparator;


public class XMLTagInfoOpenTagComparator implements Comparator {
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		XMLTagInfo info1 = (XMLTagInfo) o1;
		XMLTagInfo info2 = (XMLTagInfo) o2;
		if (info1.offset < info2.offset)
			return -1;
		else if (info1.offset > info2.offset)
			return 1;
		return 0;
	}
	
}
