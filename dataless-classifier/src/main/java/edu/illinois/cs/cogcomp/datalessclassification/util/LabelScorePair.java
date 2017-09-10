package edu.illinois.cs.cogcomp.datalessclassification.util;

/**
 * @author yqsong@illinois.edu
 * @author shashank
 */

public class LabelScorePair implements Comparable<LabelScorePair> {
	public String labelName;
	double labelScore;
	
	public LabelScorePair (String name, double score) {
		labelName = name;
		labelScore = score;
	}
	
	public String getLabel(){
		return labelName;
	}
	
	public double getScore() {
		return labelScore;
	}
	
	public void setLabel (String label) {
		labelName = label;
	}
	
	public void setScore (double score) {
		labelScore = score;
	}
	
	@Override
	public int compareTo(LabelScorePair kvp) {
		if (this.labelScore > kvp.labelScore) {
			return 1;
		} else if (this.labelScore < kvp.labelScore) {
			return -1;
		} else {
			return 0;
		}
	}
}