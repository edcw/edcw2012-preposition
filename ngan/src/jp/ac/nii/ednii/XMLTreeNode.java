package jp.ac.nii.ednii;

import java.util.ArrayList;

public class XMLTreeNode {
	public XMLTreeNode(XMLTreeNode parent, ArrayList<XMLTreeNode> children,
			LearnerError range) {
		super();
		this.parent = parent;
		this.children = children;
		this.range = range;
	}
	
	public XMLTreeNode(){
		children = new ArrayList<XMLTreeNode>();
	}
	
	public XMLTreeNode(LearnerError error) {
		// TODO Auto-generated constructor stub
		this.range = error;
		children = new ArrayList<XMLTreeNode>();
	}

	private XMLTreeNode parent;
	private ArrayList<XMLTreeNode> children;
	private LearnerError range;

	public void addChild(XMLTreeNode node){
		children.add(node);
	}
	
	public void insertNode(XMLTreeNode node, int index){
		
		if (index >= 0 || index <= this.children.size())
			children.add(index, node);
	}
	
	public void removeChild(int index){
		
		if (index >= 0 || index <= this.children.size())
			children.remove(index);
	}
	
	
	public XMLTreeNode getParent() {
		return parent;
	}
	public void setParent(XMLTreeNode parent) {
		this.parent = parent;
	}
	public ArrayList<XMLTreeNode> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<XMLTreeNode> children) {
		this.children = children;
	}
	public LearnerError getRange() {
		return range;
	}
	public void setRange(LearnerError range) {
		this.range = range;
	}
	
	
}
