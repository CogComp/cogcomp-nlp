package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.io.Serializable;

import edu.illinois.cs.cogcomp.datalessclassification.hierarchy.TreeNode;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class ConceptTreeNode<T extends Serializable> extends TreeNode {
	
	private static final long serialVersionUID = 1L;
	
	private String labelDescription;
	private SparseVector<T> conceptVector;
	
	public static<T extends Serializable> ConceptTreeNode<T> makeBasicTypedNode (String label) {
		ConceptTreeNode<T> node = new ConceptTreeNode<T>("", label, null);
		return node;
	}
	
	public static<T extends Serializable> ConceptTreeNode<T> makeNode (String labelDesc, String label, SparseVector<T> conceptVector) {
		ConceptTreeNode<T> node = new ConceptTreeNode<>(labelDesc, label, conceptVector);
		return node;
	}
	
	public ConceptTreeNode (String label) {
		this("", label);
	}
	
	public ConceptTreeNode (String labelDesc, String label) {
		this(labelDesc, label, null);
	}
	
	public ConceptTreeNode (String labelDesc, String label, SparseVector<T> conceptVector) {
		super(label);
		setLabelDescription(labelDesc);
		setConceptVector(conceptVector);
	}
	
	public String getLabelDescription () {
		return this.labelDescription;
	}
	
	public void setLabelDescription (String labelDesc) {
		this.labelDescription = labelDesc;
	}
	
	public SparseVector<T> getConceptVector () {
		return this.conceptVector;
	}
	
	public void setConceptVector (SparseVector<T> vector) {
		if (vector == null)
			vector = new SparseVector<>();
		
		this.conceptVector = vector;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals (Object o) {
		if ((o instanceof ConceptTreeNode<?>) == false)
			return false;
		
		ConceptTreeNode<T> other = (ConceptTreeNode<T>) o;
		
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
		sb.append(conceptVector);
		
		return sb.toString();
	}
	
	
//	public void writeObject (ObjectOutputStream out) throws IOException {
//		out.writeObject(label);
//		out.writeObject(labelDescription);
//		out.writeObject(conceptVector);
//	}
//	
//	@SuppressWarnings("unchecked")
//	public void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
//		label = (String) in.readObject();
//		labelDescription = (String) in.readObject();
//		conceptVector = (SparseVector<T>) in.readObject();
//	}
}