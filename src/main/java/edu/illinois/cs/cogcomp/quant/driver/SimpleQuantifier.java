package edu.illinois.cs.cogcomp.quant.driver;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.quant.standardize.Normalizer;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleQuantifier {
	
	public static String decimalRegex = "(?:\\d+\\,\\d+,\\d+\\.\\d+|\\d+\\,\\d+,\\d+|"
			+ "\\d+\\,\\d+\\.\\d+|\\d+\\,\\d+|\\d*\\.\\d+|\\d+)";
	public static Map<String, Double> numberWords = new HashMap<String, Double>();
	 
	public SimpleQuantifier() {
		new Normalizer();
		numberWords.put( "zero",       0.0);
		numberWords.put( "one", 	   1.0);
		numberWords.put( "two", 	   2.0);
		numberWords.put( "three",      3.0);
		numberWords.put( "four",       4.0);
		numberWords.put( "five",       5.0);
		numberWords.put( "six",        6.0);
		numberWords.put( "seven",      7.0);
		numberWords.put( "eight",      8.0);
		numberWords.put( "nine",       9.0);
		numberWords.put( "ten",        10.0);
	    // the teens
		numberWords.put( "eleven",     11.0);
		numberWords.put( "twelve",     12.0);
		numberWords.put( "thirteen",   13.0);
		numberWords.put( "fourteen",   14.0);
		numberWords.put( "fifteen",    15.0);
		numberWords.put( "sixteen",    16.0);
		numberWords.put( "seventeen",  17.0);
		numberWords.put( "eighteen",   18.0);
		numberWords.put( "nineteen",   19.0);
	    // multiples of ten
		numberWords.put( "twenty",     20.0);
		numberWords.put( "thirty",     30.0);
		numberWords.put( "forty",      40.0);
		numberWords.put( "fourty",     40.0);
		numberWords.put( "fifty",      50.0);
		numberWords.put( "sixty",      60.0);
		numberWords.put( "seventy",    70.0);
		numberWords.put( "eighty",     80.0);
		numberWords.put( "ninety",     90.0);
		

		numberWords.put( "twice",      2.0);
		numberWords.put( "double",     2.0);
		numberWords.put( "thrice",     3.0);
		numberWords.put( "triple",     3.0);
		numberWords.put( "half",       0.5);
		
	}
	
	public List<QuantSpan> getSpans(String text) throws Exception {
		Matcher matcher = Pattern.compile(decimalRegex).matcher(text);
		List<QuantSpan> qsList = new ArrayList<QuantSpan>();
		while(matcher.find()) {
			QuantSpan qs = new QuantSpan(
					new Quantity("=", Double.parseDouble(matcher.group().replace(",", "")), ""), 
					matcher.start(), matcher.end());
			qsList.add(qs);
		}
		TextAnnotation ta = Quantifier.pipeline.createBasicTextAnnotation("", "", text);
		for(int i=0; i<ta.size(); ++i) {
			if(numberWords.containsKey(ta.getToken(i).toLowerCase())) {
				QuantSpan qs = new QuantSpan(
						new Quantity("=", 1.0*numberWords.get(ta.getToken(i).toLowerCase()), ""), 
						ta.getTokenCharacterOffset(i).getFirst(), ta.getTokenCharacterOffset(i).getSecond());
				qsList.add(qs);
			}
		}
		Collections.sort(qsList, new Comparator<QuantSpan>() {
			@Override
			public int compare(QuantSpan o1, QuantSpan o2) {
				return (int)Math.signum(o1.start-o2.start);
			}
		});
		return qsList;
	}
	
}
