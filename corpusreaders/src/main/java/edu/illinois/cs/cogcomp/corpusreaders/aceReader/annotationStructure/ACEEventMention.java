package edu.illinois.cs.cogcomp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ACEEventMention implements Serializable {

	private static final long serialVersionUID = 1L;

	public String id;

	public int extentStart;
	public int extentEnd;
	public String extent;
	
	public int scopeStart;
	public int scopeEnd;
	public String scope;

	public int anchorStart;
	public int anchorEnd;
	public String anchor;

	public List<ACEEventArgumentMention> eventArgumentMentionList = new ArrayList<ACEEventArgumentMention>();

}
