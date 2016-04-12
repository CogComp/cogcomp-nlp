package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;

public class ACEValueMention implements Serializable {

	public String id;

	public int extentStart;
	public int extentEnd;
	public String extent;

}
