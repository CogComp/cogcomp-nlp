package edu.illinois.cs.cogcomp.quant.driver;

import edu.illinois.cs.cogcomp.quant.standardize.Normalizer;
import edu.illinois.cs.cogcomp.quant.standardize.Numbers;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;

import java.util.*;
import java.util.regex.Matcher;

public class SimpleQuantifier {
	
	public SimpleQuantifier() {
		new Normalizer();
	}
	
	public List<QuantSpan> getSpans(String text) {
		Matcher matcher = Numbers.decimalPat.matcher(text);
		List<QuantSpan> qsList = new ArrayList<QuantSpan>();
		while(matcher.find()) {
			QuantSpan qs = new QuantSpan(
					new Quantity("=", Double.parseDouble(matcher.group()), ""), 
					matcher.start(), matcher.end());
			qsList.add(qs);
		}
		return qsList;
	}
	
	public static void main(String args[])throws Throwable {
		SimpleQuantifier quantifier = new SimpleQuantifier();
		List<QuantSpan> quantities = 
				quantifier.getSpans("John has 5 apples and 3 oranges");
		System.out.println(Arrays.asList(quantities));
	}
	
}
