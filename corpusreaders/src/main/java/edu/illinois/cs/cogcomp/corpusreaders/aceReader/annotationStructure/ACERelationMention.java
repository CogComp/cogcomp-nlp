package edu.illinois.cs.cogcomp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ACERelationMention implements Serializable {

	private static final long serialVersionUID = 1L;

	public String id;
	public String lexicalCondition;

	public int extentStart;
	public int extentEnd;
	public String extent;
	
	public List<ACERelationArgumentMention> relationArgumentMentionList = new ArrayList<ACERelationArgumentMention>();

}
