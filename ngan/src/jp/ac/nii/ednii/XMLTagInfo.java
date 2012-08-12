package jp.ac.nii.ednii;

public class XMLTagInfo implements Comparable {
	public int errorIndex;
	public String tag;
	public int offset;	
	public boolean isOpenTag;
	public XMLTagInfo pairTag; //offset of the close/or open tag pairing with this
	public int point;
	
	public XMLTagInfo(int errorIndex, String tag, int offset, boolean isOpenTag){
		this.errorIndex = errorIndex;
		this.tag = tag;
		this.offset = offset;
		this.isOpenTag = isOpenTag;		
	}
	
	public void setPairTag(XMLTagInfo tag){
		this.pairTag = tag;
	}
	
	public void setPoint(int point){
		this.point = point;
	}
	
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		XMLTagInfo other = (XMLTagInfo) o;
		
		if (this.offset < other.offset)
			return -1;
		else if (this.offset > other.offset)
			return 1;
		else {	//this. offset == other.offset
			if (this.pairTag == other){
				if (this.isOpenTag)
					return -1;
				else 
					return 1;
			}else{
				XMLTagInfo pairthis = this.pairTag;
				XMLTagInfo pairother = other.pairTag;
				
				if (this.offset == pairthis.offset && other.offset == pairother.offset){ //
					if (this.point < other.point)
						return -1;
					else if (this.point > other.point)
						return 1;
					else 
						return 0;
				}
					
				//
				if (this.isOpenTag && other.isOpenTag){
					return compareo1o2(this, other, this.pairTag, other.pairTag);
				}else if (this.isOpenTag && !other.isOpenTag){
					return compareo1c2(this, other, this.pairTag, other.pairTag);
				}else if (!this.isOpenTag && other.isOpenTag){
					return comparec1o2(this, other, this.pairTag, other.pairTag);
				}else if (!this.isOpenTag && !other.isOpenTag){
					return comparec1c2(this, other, this.pairTag, other.pairTag);
				}	
				
			}
		}
		
		/*if (this.offset < other.offset)
			return -1;
		else if (this.offset > other.offset)
			return 1;
		else {	//this. offset == other.offset
			if (this.pairTag == o){
				if (this.isOpenTag)
					return -1;
				else 
					return 1;
			}else{
				int thisOpenOffset;
				int otherOpenOffset;
				
				if (this.isOpenTag) 
					thisOpenOffset = this.offset;
				else
					thisOpenOffset = this.pairTag.offset;
				
				if (other.isOpenTag) 
					otherOpenOffset = other.offset;
				else
					otherOpenOffset = other.pairTag.offset;
				
				if (thisOpenOffset < otherOpenOffset)
					return -1;
				else 
					if (thisOpenOffset > otherOpenOffset)					
						return 1;
					else{
						if (this.point < other.point)
							return -1;
						else if (this.point > other.point)
							return 1;
					}
						
			}			
			
		}
		*/
		return 0;
	}
		
	public int compareo1o2(XMLTagInfo o1, XMLTagInfo o2, XMLTagInfo c1, XMLTagInfo c2){
		
		if ((c2.offset > c1.offset && c1.offset >= o1.offset) ||
				(c1.offset > o1.offset && c2.offset == o1.offset))
			return 1;
		else
			return -1;
		
	}
	
	public int compareo1c2(XMLTagInfo o1, XMLTagInfo c2, XMLTagInfo c1, XMLTagInfo o2){
		
		if (o2.offset < c2.offset && o1.offset < c1.offset)
			return 1;
		else
			return -1;
		
	}
	
	public int comparec1o2(XMLTagInfo c1, XMLTagInfo o2, XMLTagInfo o1, XMLTagInfo c2){
		
		if (o1.offset < c1.offset && o2.offset == c2.offset)
			return -1;
		else
			return 1;
		
	}
	
	public int comparec1c2(XMLTagInfo c1, XMLTagInfo c2, XMLTagInfo o1, XMLTagInfo o2){		
	
		if ((o2.offset < c2.offset && o1.offset == c1.offset) ||
			(o1.offset < c1.offset && o1.offset < o2.offset))
			return 1;
		else 
			return -1;
		
	}
}
