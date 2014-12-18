package edu.illinois.cs.cogcomp.quant.standardize;
import java.io.Serializable;
import java.util.*;
import java.util.regex.*;
/**
 * 
 * @author Subhro Roy 
 * Converts simple quantities to standardized form:
 * 20 dollars, five oranges, etc
 */

public class Quantity implements Serializable{

	private static final long serialVersionUID = -996750464077246098L;
	public String bound, units;
	public Double value;
	
	public Quantity( String bound, Double value, String units) {	
		this.bound = bound;
		this.value = value;
		this.units = units; 
	}
	
	public static Quantity extractQuantity(String phrase){
//		System.out.println("Phrase : "+phrase);
		if(phrase == null || phrase.equals("")) {
			return null;
		}
		String[] strArr = Bounds.extractBound(phrase);
		Quantity q = Numbers.extractNumber(strArr[1]);
		if(q == null) {
			q = new Quantity("=", 1.0, phrase.trim());
		}
		q.bound = strArr[0];
		if(q.value==null){
			q.value = 1.0;
		}	
		unitNormalization(q);
		return q;
	}
	
	public static void unitNormalization(Quantity q) {
		if(q.units.toLowerCase().contains("twice") && q.value == 1.0) {
			q.value = 2.0;
			q.units = "times";
		}
		if(q.units.toLowerCase().contains("double") && q.value == 1.0) {
			q.value = 2.0;
			q.units = "times";
		}
		if(q.units.toLowerCase().contains("thrice") && q.value == 1.0) {
			q.value = 3.0;
			q.units = "times";
		}
		if(q.units.toLowerCase().contains("triple") && q.value == 1.0) {
			q.value = 3.0;
			q.units = "times";
		}
		if(q.units.toLowerCase().contains("percent") || 
				q.units.toLowerCase().contains("%") ) {
			q.value *= 0.01;
			q.units = "times";
		}
		if(q.units.toLowerCase().contains("cents")) {
			q.value *= 0.01;
			q.units = "US$";
		}
		if(q.units.toLowerCase().contains("dollar") || 
				q.units.toLowerCase().contains("$") ) {
			q.units = "US$";
		}
	}
	
	public String toString(){
		return "["+this.bound+" "+this.value+"]";
	}
	
	public String toStringWithUnits(){
		return "["+this.bound+" "+this.value+" "+this.units.trim()+"]";
	}
	
	public static void main(String args[]) {
		Bounds.initialize();
		Numbers.initialize();
		System.out.println(Quantity.extractQuantity("70 cents"));
	}
}
	