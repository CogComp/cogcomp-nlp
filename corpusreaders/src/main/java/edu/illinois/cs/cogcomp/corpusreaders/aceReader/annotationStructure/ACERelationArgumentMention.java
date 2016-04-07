package edu.illinois.cs.cogcomp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;

public class ACERelationArgumentMention implements Serializable {

	public String id;
	public String role;
	
	public int start;
	public int end;
	
	public String argStr;
}
