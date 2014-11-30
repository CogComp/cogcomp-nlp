package edu.illinois.cs.cogcomp.comma;

import java.util.Comparator;
import java.util.Iterator;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Queries;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

/**
 * A data structure containing all the information related to a comma.
 */
public class Comma {
    private String[] sentence;
    private String role;
    public int commaPosition;
    TextAnnotation ta;

    public Comma(int commaPosition, String role, String sentence, TextAnnotation ta) {
        this.commaPosition = commaPosition;
        if (role.equals("Entity attribute")) this.role = "Attribute";
        else if (role.equals("Entity substitute")) this.role = "Substitute";
        else this.role = role;
        this.sentence = sentence.split("\\s+");
        this.ta = ta;
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
    
    public String getPOSToLeft(int distance){
    	TokenLabelView posView = (TokenLabelView) ta.getView(ViewNames.POS);
    	return posView.getLabel(commaPosition - distance);
    }
    
    public String getPOSToRight(int distance){
    	TokenLabelView posView = (TokenLabelView) ta.getView(ViewNames.POS);
    	return posView.getLabel(commaPosition + distance);
    }
    
    public String getPhraseToLeftOfComma(int distance, boolean lexicalise){
		Constituent comma = getCommaConstituent();
		
		Constituent phraseToLeft = getSibilingToLeft(distance, comma);
		if(phraseToLeft != null){
			String notation = phraseToLeft.getLabel();
			if (lexicalise) {
				notation += " -";
				IntPair span = phraseToLeft.getSpan();
				for (int tokenId = span.getFirst(); tokenId < span.getSecond(); tokenId++)
					notation += " " + WordHelpers.getPOS(ta, tokenId);
			}
			return notation;
		}
		else 
			return "NULL";
    }
    
    public String getPhraseToRightOfComma(int distance, boolean lexicalise){
		Constituent comma = getCommaConstituent();
		
		Constituent phraseToRight= getSibilingToRight(distance, comma);
		if(phraseToRight != null){
			String notation = phraseToRight.getLabel();
			if (lexicalise) {
				notation += " -";
				IntPair span = phraseToRight.getSpan();
				for (int tokenId = span.getFirst(); tokenId < span.getSecond(); tokenId++)
					notation += " " + WordHelpers.getPOS(ta, tokenId);
			}
			return notation;
		}
		else 
			return "NULL";
    }
    
    public String getPhraseToLeftOfParent(int distance){
		Constituent comma = getCommaConstituent();
		Constituent parent = TreeView.getParent(comma);
		Constituent phraseToLeft = getSibilingToLeft(distance, parent);
		if(phraseToLeft != null)
			return phraseToLeft.getLabel();
		else 
			return "NULL";
    }
    
    public String getPhraseToRightOfParent(int distance){
		Constituent comma = getCommaConstituent();
		Constituent parent = TreeView.getParent(comma);
		Constituent phraseToRight= getSibilingToRight(distance, parent);
		if(phraseToRight != null)
			return phraseToRight.getLabel();
		else 
			return "NULL";
    }
    
    public String getNodesAtCommaLevel(){
    	TreeView parseView= (TreeView) ta.getView(ViewNames.PARSE_GOLD);
		Constituent comma = getCommaConstituent();
		IQueryable<Constituent> nodesAtSameLevel = parseView.where(Queries.isSiblingOf(comma)).orderBy(new Comparator<Constituent>() {
			@Override
			public int compare(Constituent o1, Constituent o2) {
				return o1.getStartSpan() - o2.getStartSpan();
			}
		});
		
		Iterator<Constituent> it = nodesAtSameLevel.iterator();
		Constituent curr = it.next();
		String feature = curr.getLabel() + "(" + curr + ")";
		while (it.hasNext()){
			curr = it.next();
			feature += " " + curr.getLabel() + "(" + curr + ")";
		} 
		return feature;
    }
    
    public String getNodesAtParentLevel(){
    	TreeView parseView= (TreeView) ta.getView(ViewNames.PARSE_GOLD);
		Constituent comma = getCommaConstituent();
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
    
    public Constituent getCommaConstituent(){
    	TreeView parseView= (TreeView) ta.getView(ViewNames.PARSE_GOLD);
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
    
    public Constituent getSibilingToLeft(int distance, Constituent c){
    	TreeView parseView= (TreeView) ta.getView(ViewNames.PARSE_GOLD);
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
    
    public Constituent getSibilingToRight(int distance, Constituent c){
    	TreeView parseView= (TreeView) ta.getView(ViewNames.PARSE_GOLD);
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
}


