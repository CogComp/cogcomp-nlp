package edu.illinois.cs.cogcomp.datalessclassification.hierarchy;

import java.io.Serializable;

/**
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class TreeNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected String label;
	
	public static TreeNode makeBasicNode (String label) {
		TreeNode node = new TreeNode(label);
		return node;
	}
	
	//Copy Constructor
	public TreeNode (TreeNode thatNode) {
		this(thatNode.getLabel());
	}
	
	public TreeNode (String label) {
		this.label = label;
	}
	
	public String getLabel () {
		return this.label;
	}
	
	public void setLabel (String str) {
		this.label = str;
	}
	
	@Override
	public String toString () {
		return label;
	}

	@Override
	public boolean equals (Object o) {
		if ((o instanceof TreeNode) == false)
			return false;
		
		TreeNode other = (TreeNode) o;
		return this.label.equals(other.label);
	}
	
	@Override
	public int hashCode () {
		return label.hashCode();
	}
}