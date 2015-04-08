package edu.illinois.cs.cogcomp.comma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class Sentence implements Serializable{
	Collection<Comma> commas;
	private static final long serialVersionUID = 2522617554768671153l;
	public Sentence(){
		commas = new ArrayList<Comma>();
	}
	
	public void addComma(Comma c){
		commas.add(c);
	}
	
	public Collection<Comma> getCommas(){
		return commas;
	}
	
	public Collection<Comma> getFirstCommasWhichAreNotLast(){
		Collection<Comma> firstCommasWhichAreNotLast = new ArrayList<Comma>();
		for (Comma c : commas)
			if(getNextComma(c)!=null && getPreviousComma(c)==null)
				firstCommasWhichAreNotLast.add(c);
		return firstCommasWhichAreNotLast;
	}
	
	public Collection<Comma> getMiddleCommas(){
		Collection<Comma> middleCommas = new ArrayList<Comma>();
		for (Comma c : commas)
			if(getNextComma(c)!=null && getPreviousComma(c)!=null)
				middleCommas.add(c);
		return middleCommas;
	}
	
	public Comma getNextComma(Comma curr_c){
		Comma next_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: commas){
			if(c.commaPosition > curr_c.commaPosition && (c.commaPosition - curr_c.commaPosition < curr_diff)){
				next_c = c;
				curr_diff = next_c.commaPosition - curr_c.commaPosition;
			}
		}
		return next_c;
	}
	
	public Comma getPreviousComma(Comma curr_c){
		Comma prev_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: commas){
			if(c.commaPosition < curr_c.commaPosition && (curr_c.commaPosition - c.commaPosition < curr_diff)){
				prev_c = c;
				curr_diff = curr_c.commaPosition - prev_c.commaPosition;
			}
		}
		return prev_c;
	}

	public Collection<Comma> getFirstSiblingCommasWhichAreNotLast(){
		Collection<Comma> firstCommasWhichAreNotLast = new ArrayList<Comma>();
		for (Comma c : commas)
			if(getNextSiblingComma(c)!=null && getPreviousSiblingComma(c)==null)
				firstCommasWhichAreNotLast.add(c);

		return firstCommasWhichAreNotLast;
	}
	
	public Collection<Comma> getMiddleSiblingCommas(){
		Collection<Comma> middleCommas = new ArrayList<Comma>();
		for (Comma c : commas)
			if(getNextSiblingComma(c)!=null && getPreviousSiblingComma(c)!=null)
				middleCommas.add(c);
		return middleCommas;
	}
	
	public Comma getNextSiblingComma(Comma curr_c){
		Comma next_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: curr_c.getSiblingCommas()){
			if(c.commaPosition > curr_c.commaPosition && (c.commaPosition - curr_c.commaPosition < curr_diff)){
				next_c = c;
				curr_diff = next_c.commaPosition - curr_c.commaPosition;
			}
		}
		return next_c;
	}
	
	public Comma getPreviousSiblingComma(Comma curr_c){
		Comma prev_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: curr_c.getSiblingCommas()){
			if(c.commaPosition < curr_c.commaPosition && (curr_c.commaPosition - c.commaPosition < curr_diff)){
				prev_c = c;
				curr_diff = curr_c.commaPosition - prev_c.commaPosition;
			}
		}
		return prev_c;
	}

}
