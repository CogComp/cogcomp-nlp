package edu.illinois.cs.cogcomp.comma;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

public class DuplicateComma implements Serializable{
    public String[] sentence;
    public String role;
    public String annotatedSentence;
    public Map<Integer, Pair<String, Set<Integer>>> labeledCommas;
    public int commaPosition;
    TextAnnotation goldTA;
    TextAnnotation TA;
    
    public DuplicateComma(Comma c, String rawText, String annotatedSentence, Map<Integer, Set<Integer>> commaGroups, List<String> commaLabels){
    	TA = c.TA;
    	goldTA = c.goldTA;
    	commaPosition=c.commaPosition;
    	role=c.getRole();
    	sentence = rawText.split("\\s+");
    	this.annotatedSentence = annotatedSentence;
    	
    	labeledCommas = new HashMap<Integer, Pair<String,Set<Integer>>>();
    	Iterator<Entry<Integer, Set<Integer>>> ei = commaGroups.entrySet().iterator();
    	Iterator<String> li = commaLabels.iterator();
    	while(ei.hasNext() && li.hasNext()){
    		Map.Entry<Integer, Set<Integer>> e = ei.next();
    		Pair<String, Set<Integer>> labeledComma = new Pair<String, Set<Integer>>(li.next(), e.getValue());
    		labeledCommas.put(e.getKey(), labeledComma);
    	}
    }
    
    @Override
    public String toString(){
    	System.out.println(annotatedSentence);
    	System.out.println(goldTA);
    	System.out.println();
    }
}
