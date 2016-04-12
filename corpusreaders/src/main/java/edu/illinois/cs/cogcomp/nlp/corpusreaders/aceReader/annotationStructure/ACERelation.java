package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader.annotationStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ACERelation  implements Serializable {
	
	public String id; //ID;
	public String type;//TYPE;
	public String subtype;//SUBTYPE;
	public String modality;//MODALITY;
	public String tense;//TENSE;
	
	public List<ACERelationArgument> relationArgumentList = new ArrayList<ACERelationArgument>();
	public List<ACERelationMention> relationMentionList = new ArrayList<ACERelationMention>();

}
