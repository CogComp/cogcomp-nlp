package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ACEEvent  implements Serializable {
	
	public String id; //ID;
	public String type;//TYPE;
	public String subtype;//SUBTYPE;
	public String modality;//MODALITY;
	public String polarity;//POLARITY;
	public String genericity;//GENERICITY;
	public String tense;//TENSE;
	
	public List<ACEEventArgument> eventArgumentList = new ArrayList<ACEEventArgument>();
	public List<ACEEventMention> eventMentionList = new ArrayList<ACEEventMention>();

}
