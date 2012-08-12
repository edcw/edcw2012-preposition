package jp.ac.nii.ednii;

import java.util.ArrayList;

public class XMLTree {
	public XMLTree(XMLTreeNode root) {
		super();
		this.root = root;
	}

	private XMLTreeNode root;

	public XMLTreeNode getRoot() {
		return root;
	}

	public void setRoot(XMLTreeNode root) {
		this.root = root;
	}
	
	public boolean addXMLNode(LearnerError error){
		
		return addXMLNode(this.root, error);
	}

	private boolean addXMLNode(XMLTreeNode curNode, LearnerError error) {
		// TODO Auto-generated method stub
		boolean ret = true;
		XMLTreeNode node = new XMLTreeNode(error);
		
		ArrayList<XMLTreeNode> children = curNode.getChildren();
		int size = children.size();
		if (size == 0){			
			curNode.addChild(node);
		}else{
			LearnerError firstChild = children.get(0).getRange();
			LearnerError lastChild = children.get(size - 1).getRange();
			if (error.getEnd() <= firstChild.getBegin()){// insert node before first child
				curNode.insertNode(node, 0);				
			}else if (error.getBegin() >= lastChild.getEnd()){
				curNode.insertNode(node, size);
			}else{ //find children nodes that are covered by errors
				int from = -1;
				int i = size - 1;
				while (i >= 0 && children.get(i).getRange().getBegin() >= error.getBegin()){
					i--;
					from = i;
				}
				
				int to = -1;
				i = 0;
				while (i < size && children.get(i).getRange().getBegin() >= error.getBegin()){
					i++;
					to = i;
				}
				
				if (from != -1 && to != -1){
					//move from-to children into the new node
					for (i = from; i <= to; i++){
						XMLTreeNode xmlTreeNode = children.get(i);
						xmlTreeNode.setParent(node);
						node.addChild(xmlTreeNode);
					}
					
					for (i = to; i >= from; i--){
						curNode.removeChild(i);
					}						
				}else{
					for (int j = 0; j < size; j++) {
						XMLTreeNode xmlTreeNode = children.get(j);
						if (xmlTreeNode.getRange().getBegin() <= error.getBegin() &&
								xmlTreeNode.getRange().getEnd() >= error.getEnd())
							return addXMLNode(xmlTreeNode, error);
					}
				}
				
			}
		}
				
		return ret;
	}
	
	public ArrayList<XMLTagInfo> collectXMLTags(){
		ArrayList<XMLTagInfo> ret = new ArrayList<XMLTagInfo>();
		collectXMLTags(this.root, ret);
		return ret;
	}

	private void collectXMLTags(XMLTreeNode node, ArrayList<XMLTagInfo> ret) {
		// TODO Auto-generated method stub
		LearnerError error = node.getRange();
		XMLTagInfo openTag = new XMLTagInfo(0, error.getXMLOpenTag(), error.getBegin(), true);
		if (node != this.root)
			ret.add(openTag);
		ArrayList<XMLTreeNode> children = node.getChildren();
		for (int i = 0; i < children.size(); i++) {
			XMLTreeNode xmlTreeNode = children.get(i);
			collectXMLTags(xmlTreeNode, ret);
		}
		XMLTagInfo closeTag = new XMLTagInfo(0, error.getXMLCloseTag(), error.getEnd(), false);
		if (node != this.root)
			ret.add(closeTag);
	}
}
