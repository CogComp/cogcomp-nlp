package edu.illinois.cs.cogcomp.comma.bayraktar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.collections.map.MultiValueMap;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.comma.utils.PrettyPrint;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class BayraktarEvaluation {
	
	public static void main(String[] args){
		Parser parser = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		printBayraktarBaselinePerformance(parser, true);
	}
	
	/**
	 * checks if the bayraktar label of an annotated pattern is the same as the most frequent label among commas with the same bayraktar pattern
	 * If it is not, then the commas with that pattern are printed out
	 */
	public static void verifyBayraktarLabelEqualsMostFrequentVivekNaveenLabel(){
		VivekAnnotationCommaParser reader = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		List<Comma> vivekCommas = reader.getCommas();
		MultiValueMap patternToVivekCommas = new MultiValueMap();
		for(Comma comma: vivekCommas){
			String pattern = comma.getBayraktarPattern();
			patternToVivekCommas.put(pattern, comma);
		}
		int errorCount = 0;
		List<Entry<String, ArrayList<Comma>>> vivekPatterns = new ArrayList<Entry<String,ArrayList<Comma>>>(patternToVivekCommas.entrySet());
		
		
		//MultiValueMap syntaxPatternToPTBCommas = BayraktarAnnotationGenerationHelper.getBayraktarPatternToPTBCommas();
		for(Entry<String, ArrayList<Comma>> entry: vivekPatterns){
			Counter<String> labelCounts = new Counter<String>();
			String pattern = entry.getKey();
			String bayraktarLabel = BayraktarPatternLabeler.getLabel(pattern);
			if(bayraktarLabel==null) continue;
			for(Comma comma : entry.getValue()){
				String vivekLabel = comma.getVivekNaveenRole();
				labelCounts.incrementCount(vivekLabel);
			}
			String frequentLabel = labelCounts.getMax().getFirst();
			if(!frequentLabel.equals(bayraktarLabel)){
				System.out.println("\n\n\n" + pattern + "\t" + bayraktarLabel);
				for(Comma comma : entry.getValue()){
					System.out.println(comma.getVivekNaveenAnnotatedText());
				}
				/*if(entry.getValue().size()<5){
					System.out.println("PTB examples:");
					List<Comma> ptbCommas = (List<Comma>) syntaxPatternToPTBCommas.get(pattern);
					for(int i=0;i<10 && i< ptbCommas.size();i++){
						System.out.println(ptbCommas.get(i).getTextAnnotation(true).getText());
					}
				}*/
					
				errorCount++;
			}
		}
		System.out.println("\n\n\nERROR COUNT = " + errorCount);
	}
	
	/**
	 * for each annotated pattern, if there is a single comma whose VivekNaveen label differs, print all of them for further analysis 
	 */
	public static void analyseErrors() {
		VivekAnnotationCommaParser reader = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		List<Comma> commas = reader.getCommas();
		MultiValueMap patternToComma = new MultiValueMap();
		for(Comma comma: commas){
			String pattern = comma.getBayraktarPattern();
			patternToComma.put(pattern, comma);
		}
		
		Scanner scanner = new Scanner(System.in);
		MultiValueMap syntaxPatternToCommas = BayraktarAnnotationGenerationHelper.getBayraktarPatternToPTBCommas();
		@SuppressWarnings("unchecked")
		List<Entry<String, ArrayList<Comma>>> patternsToCommaEntries = new ArrayList<Entry<String,ArrayList<Comma>>>(patternToComma.entrySet());
		System.out.println("# patterns in corpus = " + patternsToCommaEntries.size());
		int errorPatternCount = 0;
		for(Entry<String, ArrayList<Comma>> entry : patternsToCommaEntries){
			String pattern = entry.getKey();
			List<Comma> patternOccournces = entry.getValue();
			Collections.sort(patternOccournces, new Comparator<Comma>(){
				@Override
				public int compare(Comma o1, Comma o2) {
					// TODO Auto-generated method stub
					return o1.getVivekBayraktarRole().compareTo(o2.getVivekBayraktarRole());
				}
				
			});
			String bayraktarLabel = BayraktarPatternLabeler.getLabel(pattern);
			if(bayraktarLabel!=null){
				boolean mismatch = false;
				for(Comma occurance : patternOccournces){
					if(!occurance.getVivekNaveenRole().equals(bayraktarLabel)){
						mismatch=true;
						break;
					}
				}
				if(mismatch){
					errorPatternCount++;
					System.out.println(bayraktarLabel+ "\t" + pattern);
					for(Comma c: patternOccournces)
						System.out.println(c.getTextAnnotation(true).getId() + "\t" + c.getVivekAnnotatedText());
					
					List<Comma> tas = (List<Comma>) syntaxPatternToCommas.get(pattern);
					int currText=0;
					int currParse=0;
					int cmd = 0;
					while (cmd != 2) {
						cmd = scanner.nextInt();
						switch (cmd) {
						case 0:
							System.out.println(tas.get(currText++));
							break;
						case 1:
							System.out.println(PrettyPrint.pennString(((TreeView)tas.get(currParse++).getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0)));
							break;
						case 2:
							break;
						}
					}
				}
			}
			System.out.println(errorPatternCount);
		}
		scanner.close();
	}
	
	/**
	 * prints bayraktar baseline performance based on only those commas whose bayraktar patterns have been annotated 
	 */
	public static void printBayraktarBaselinePerformance(Parser parser, boolean testOnGold){
		parser.reset();
		EvaluateDiscrete bayraktarEvalaution = new EvaluateDiscrete();
		Comma comma;
		while((comma = (Comma) parser.next()) != null){
			if(!BayraktarPatternLabeler.isLabelAvailable(comma)) continue;
			Comma.useGoldFeatures(true);
			String goldLabel = comma.getVivekNaveenRole();
			Comma.useGoldFeatures(testOnGold);
			String bayraktarPrediction = comma.getBayraktarLabel();
			bayraktarEvalaution.reportPrediction(bayraktarPrediction, goldLabel);
		}
		System.out.println("Bayraktar baseline performance on only those commas whose patterns have been annotated");
		bayraktarEvalaution.printPerformance(System.out);
		bayraktarEvalaution.printConfusion(System.out);
	}
}
