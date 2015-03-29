package edu.illinois.cs.cogcomp.comma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


public class Sentence implements Serializable{
	Collection<Comma> commas;
	
	public Sentence(){
		commas = new ArrayList<Comma>();
	}
	
	public void addComma(Comma c){
		commas.add(c);
	}
	
	public Collection<Comma> getCommas(){
		return commas;
	}
	
	public Comma getNextComma(Comma curr_c){
		//System.out.println("curr pos = " + curr_c.commaPosition);
		Comma next_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: commas){
			//System.out.println(c.commaPosition);
			if(c.commaPosition > curr_c.commaPosition && (c.commaPosition - curr_c.commaPosition < curr_diff)){
				next_c = c;
				curr_diff = next_c.commaPosition - curr_c.commaPosition;
			}
		}
		//System.out.println("next = " + (next_c == null? "null" : next_c.commaPosition));
		return next_c;
	}
	
	public Comma getPreviousComma(Comma curr_c){
		//System.out.println("curr pos = " + curr_c.commaPosition);
		Comma prev_c = null;
		int curr_diff = Integer.MAX_VALUE;
		for(Comma c: commas){
			//System.out.println(c.commaPosition);
			if(c.commaPosition < curr_c.commaPosition && (curr_c.commaPosition - c.commaPosition < curr_diff)){
				prev_c = c;
				curr_diff = curr_c.commaPosition - prev_c.commaPosition;
			}
		}
		//System.out.println("prev = " + (prev_c==null ?"null" : prev_c.commaPosition));
		return prev_c;
	}
}
