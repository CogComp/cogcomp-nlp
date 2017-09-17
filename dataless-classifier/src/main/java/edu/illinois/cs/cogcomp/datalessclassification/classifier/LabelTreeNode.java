/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import edu.illinois.cs.cogcomp.datalessclassification.hierarchy.TreeNode;

/**
 * @author shashank
 */

public class LabelTreeNode extends TreeNode {
	
	private static final long serialVersionUID = 1L;
	private String labelName;
	private String labelDescription;
	
	public static LabelTreeNode makeBasicNode (String label) {
		LabelTreeNode node = new LabelTreeNode(label, "", "");
		return node;
	}
	
	/**
	 * Copy Constructor
	 */
	public LabelTreeNode (LabelTreeNode thatNode) {
		this(thatNode.getLabel(), thatNode.getLabelName(), thatNode.getLabelDescription());
	}
	
	public LabelTreeNode (String label, String labelName, String labelDesc) {
		super(label);
		setLabelName(labelName);
		setLabelDescription(labelDesc);
	}
	
	public String getLabelDescription () {
		return this.labelDescription;
	}
	
	public void setLabelDescription (String str) {
		this.labelDescription = str;
	}
	
	public String getLabelName () {
		return this.labelName;
	}
	
	public void setLabelName (String labelName) {
		this.labelName = labelName;
	}
	
	@Override
	public boolean equals (Object o) {
		if ((o instanceof LabelTreeNode) == false)
			return false;
		
		LabelTreeNode other = (LabelTreeNode) o;
		
		return this.label.equals(other.getLabel());
	}
	
	@Override
	public int hashCode () {
		return label.hashCode();
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder("");
		
		sb.append(label + "\t");
		sb.append(labelName + "\t");
		sb.append(labelDescription);
		
		return sb.toString();
	}
}