package edu.illinois.cs.cogcomp.comma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.sentences.Queries;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.SpanLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

/**
 * A data structure containing all the information related to a comma.
 */
public class Comma implements Serializable{
    private String[] sentence;
    private String role;
    public int commaPosition;
    TextAnnotation goldTA;
    TextAnnotation TA;

    public Comma(int commaPosition, String role, String sentence, TextAnnotation TA) {
        this.commaPosition = commaPosition;
        if (role.equals("Entity attribute")) this.role = "Attribute";
        else if (role.equals("Entity substitute")) this.role = "Substitute";
        else this.role = role;
        this.sentence = sentence.split("\\s+");
        this.TA = TA;
    }
    
    public Comma(int commaPosition, String role, String sentence, TextAnnotation TA, TextAnnotation goldTA) {
        this(commaPosition, role, sentence, TA);
    	this.goldTA = goldTA;
    }

    public String getRole() {
        return role;
    }

    public String getWordToRight(int distance) {
        // Dummy symbol for sentence end (in case comma is the second to last word in the sentence)
        if (commaPosition + distance >= sentence.length)
            return "###";
        return sentence[commaPosition + distance];
    }

    public String getWordToLeft(int distance) {
        // Dummy symbol for sentence start (in case comma is the second word in the sentence)
        if (commaPosition - distance < 0)
            return "$$$";
        return sentence[commaPosition - distance];
    }
    
    public String getPOSToLeft(int distance, boolean gold){
		TokenLabelView posView;
		if (gold)
			posView = (TokenLabelView) goldTA.getView(ViewNames.POS);
		else
			posView = (TokenLabelView) TA.getView(ViewNames.POS);
		return posView.getLabel(commaPosition - distance);
	}
    
    public String getPOSToRight(int distance, boolean gold){
    	TokenLabelView posView;
		if (gold)
			posView = (TokenLabelView) goldTA.getView(ViewNames.POS);
		else
			posView = (TokenLabelView) TA.getView(ViewNames.POS);
    	return posView.getLabel(commaPosition + distance);
    }

    public Constituent getChunkToRightOfComma(int distance, boolean gold){
    	if(!this.TA.hasView(ViewNames.SHALLOW_PARSE)){
    		CuratorClient client = new CuratorClient("trollope.cs.illinois.edu", 9010);
    		try {
				client.addChunkView(this.TA, false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
    	}
    	
    	SpanLabelView chunkView;
    	if(gold)
    		chunkView = (SpanLabelView) goldTA.getView(ViewNames.SHALLOW_PARSE);
    	else
    		chunkView = (SpanLabelView) TA.getView(ViewNames.SHALLOW_PARSE);
    	
		List<Constituent> chunksToRight= chunkView.getSpanLabels(commaPosition+1, TA.getTokens().length);
		Collections.sort(chunksToRight, new Comparator<Constituent>() {
			@Override
			public int compare(Constituent o1, Constituent o2) {
				return o1.getStartSpan() - o2.getStartSpan();
			}
		});
		
		Constituent chunk;
		if(distance<=0 || distance>chunksToRight.size())
			chunk = null;
		else 
			chunk = chunksToRight.get(distance-1);
		return chunk;
    }
    
    public Constituent getChunkToLeftOfComma(int distance, boolean gold){
    	if(!this.TA.hasView(ViewNames.SHALLOW_PARSE)){
    		CuratorClient client = new CuratorClient("trollope.cs.illinois.edu", 9010);
    		try {
				client.addChunkView(this.TA, false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
    	}
    	
    	SpanLabelView chunkView;
    	if(gold)
    		chunkView = (SpanLabelView) goldTA.getView(ViewNames.SHALLOW_PARSE);
    	else
    		chunkView = (SpanLabelView) TA.getView(ViewNames.SHALLOW_PARSE);;
    	
		//Constituent comma = covers.get(0);
		
		List<Constituent> chunksToLeft = chunkView.getSpanLabels(0, commaPosition+1);
		System.out.println(chunksToLeft);
		Collections.sort(chunksToLeft, new Comparator<Constituent>() {
			@Override
			public int compare(Constituent o1, Constituent o2) {
				return o2.getStartSpan() - o1.getStartSpan();
			}
		});
		
		Constituent chunk;
		if(distance<=0 || distance>chunksToLeft.size())
			chunk = null;
		else 
			chunk = chunksToLeft.get(distance-1);
		return chunk;
    }

    public Constituent getPhraseToLeftOfComma(int distance, boolean gold){
    	TreeView parseView;
    	if(gold)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
		Constituent comma = getCommaConstituentFromTree(parseView);
		
		Constituent phraseToLeft = getSibilingToLeft(distance, comma, parseView);
		return phraseToLeft;
    }
    
    public Constituent getPhraseToRightOfComma(int distance, boolean gold){
    	TreeView parseView;
    	if(gold)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
		Constituent comma = getCommaConstituentFromTree(parseView);
		
		Constituent phraseToRight= getSibilingToRight(distance, comma, parseView);
		return phraseToRight;
    }
    
    public Constituent getPhraseToLeftOfParent(int distance, boolean gold){
    	TreeView parseView;
    	if(gold)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
		Constituent comma = getCommaConstituentFromTree(parseView);
		Constituent parent = TreeView.getParent(comma);
		Constituent phraseToLeft = getSibilingToLeft(distance, parent, parseView);
		return phraseToLeft;
    }

    public Constituent getPhraseToRightOfParent(int distance, boolean gold){
    	TreeView parseView;
    	if(gold)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
		Constituent comma = getCommaConstituentFromTree(parseView);
		Constituent parent = TreeView.getParent(comma);
		Constituent phraseToRight= getSibilingToRight(distance, parent, parseView);
		return phraseToRight;
    }
    
    public String getNodesAtCommaLevel(boolean gold){
    	TreeView parseView;
    	if(gold)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
		Constituent comma = getCommaConstituentFromTree(parseView);
		IQueryable<Constituent> nodesAtSameLevel = parseView.where(Queries.isSiblingOf(comma)).orderBy(new Comparator<Constituent>() {
			@Override
			public int compare(Constituent o1, Constituent o2) {
				return o1.getStartSpan() - o2.getStartSpan();
			}
		});
		
		Iterator<Constituent> it = nodesAtSameLevel.iterator();
		Constituent curr = it.next();
		String feature = curr.getLabel();
		while (it.hasNext()){
			curr = it.next();
			feature += " " + curr.getLabel();
		} 
		return feature;
    }
    
    public String getNodesAtParentLevel(boolean gold){
    	TreeView parseView;
    	if(gold)
    		parseView = (TreeView) goldTA.getView(ViewNames.PARSE_GOLD);
    	else
    		parseView = (TreeView) TA.getView(ViewNames.PARSE_STANFORD);
		Constituent comma = getCommaConstituentFromTree(parseView);
		Constituent commaParent = TreeView.getParent(comma);
		IQueryable<Constituent> nodesAtParentLevel = parseView.where(Queries.isSiblingOf(commaParent)).orderBy(new Comparator<Constituent>() {
			@Override
			public int compare(Constituent o1, Constituent o2) {
				return o1.getStartSpan() - o2.getStartSpan();
			}
		});
		
		Iterator<Constituent> it = nodesAtParentLevel.iterator();
		String feature = it.next().getLabel();
		while (it.hasNext()){
			feature += " " + it.next().getLabel();
		}
		return feature;
    }
    
    public Constituent getCommaConstituentFromTree(TreeView parseView){
		Constituent comma = null;
		for(Constituent c: parseView.getConstituents()){
			if(c.isConsituentInRange(commaPosition, commaPosition+1)){
				try {
					comma = parseView.getParsePhrase(c);
				} catch (EdisonException e) {
					e.printStackTrace();
					System.exit(1);
				}
				break;
			}
		}
		return comma;
    }
    
    public Constituent getSibilingToLeft(int distance, Constituent c, TreeView parseView){
    	Constituent leftSibiling = c;
    	IQueryable<Constituent> sibilings = parseView.where(Queries.isSiblingOf(c)); 
    	while(distance-- > 0){
    		Iterator<Constituent> leftSibilingIt = sibilings.where(Queries.adjacentToBefore(leftSibiling)).iterator();
    		if(leftSibilingIt.hasNext())
    			leftSibiling = leftSibilingIt.next();
    		else
    			return null;
    	}
    	return leftSibiling;
    }
    
    public Constituent getSibilingToRight(int distance, Constituent c, TreeView parseView){
    	Constituent rightSibiling = c;
    	IQueryable<Constituent> sibilings = parseView.where(Queries.isSiblingOf(c)); 
    	while(distance-- > 0){
    		Iterator<Constituent> rightSibilingIt = sibilings.where(Queries.adjacentToAfter(rightSibiling)).iterator();
    		if(rightSibilingIt.hasNext())
    			rightSibiling = rightSibilingIt.next();
    		else
    			return null;
    	}
    	return rightSibiling;
    }
    
    public static String getNotation(Constituent c, boolean POSlexicalise, boolean NERlexicalise){
    	
    	if(c == null)
    		return "NULL";
    	String notation = c.getLabel();
    	
    	if(NERlexicalise)
    		notation += " -" + getNamedEntityTag(c);
    	
    	if(POSlexicalise){
			notation += " -";
			IntPair span = c.getSpan();
			TextAnnotation TA = c.getTextAnnotation();
			for (int tokenId = span.getFirst(); tokenId < span.getSecond(); tokenId++)
					notation += " " + WordHelpers.getPOS(TA, tokenId);
	    }
    	
    	
    	
		return notation;
    }
    
    /*public Relation getSRL(){
    	PredicateArgumentView pav = (PredicateArgumentView)TA.getView(ViewNames.SRL_VERB);
		List<Relation> rels = new ArrayList<Relation>();
		for(Constituent pred : pav.getPredicates()){
			rels.addAll(pav.getArguments(pred));
		}
		for(Relation rel : rels){
			if(rel.getTarget().getEndSpan()>commaPosition && rel.getTarget().getStartSpan()>=commaPosition)
				return rel;
		}
		return null;
    }*/

    public static String getNamedEntityTag(Constituent c){
    	TextAnnotation TA = c.getTextAnnotation();
    	List<String> NETags = TA.getView(ViewNames.NER).getLabelsCovering(c);
    	String result = NETags.size()==0? "NULL" : NETags.get(0);
    	for(int i = 1; i<NETags.size(); i++)
    		result += " " + NETags.get(i);
    	return result;
    }
}



