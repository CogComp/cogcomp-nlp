package edu.illinois.cs.cogcomp.quant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import edu.illinois.cs.cogcomp.quant.driver.QuantSpan;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;

public class AI2Test {
	
	public Quantifier quantifier;
	public static String wikiScienceFile = "/Users/subhroroy/AI2Data/"
			+ "simplewiki-science-sentences.txt";
	
	public AI2Test() {
		quantifier = new Quantifier();
	}
	
	public void extractQuantitiesFromWikiScienceSentences() 
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				new File(wikiScienceFile)));
		String str = null;
		while((str = br.readLine()) != null) {
			for(int i=0; i<str.length(); ++i) {
				if(str.charAt(i) == '\t') {
					System.out.println("Sentence : "+str.substring(i+1));
					List<QuantSpan> qs = quantifier.getSpans(
							str.substring(i+1), true);
					System.out.println("Quantities : "+qs);
					break;
				}
			}
		}
		br.close();
	}
	
	public static void main(String args[]) throws IOException {
		AI2Test ai2 = new AI2Test();
		ai2.extractQuantitiesFromWikiScienceSentences();
	}

}
