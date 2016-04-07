package edu.illinois.cs.cogcomp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ACETimeEx implements Serializable {
	public String id;//ID;

	public List<ACETimeExMention> timeExMentionList = new ArrayList<ACETimeExMention>();
}
