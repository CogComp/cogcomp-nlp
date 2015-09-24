package edu.illinois.cs.cogcomp.comma.bayraktar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.readers.VivekAnnotationReader;
import edu.illinois.cs.cogcomp.comma.utils.PrettyPrintParseTree;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.stats.Counter;

public class BayraktarErrorAnalysis {
	
	/**
	 * checks if the bayraktar label of an annotated pattern is the same as the most frequent label among commas with the same bayraktar pattern
	 * If it is not, then the commas with that pattern are printed out
	 */
	public static void verifyBayraktarLabelEqualsMostFrequentVivekNaveenLabel(){
		VivekAnnotationReader reader = new VivekAnnotationReader(CommaProperties.getInstance().getOriginalVivekAnnotationFile());
		List<Comma> vivekCommas = reader.getCommas();
		Multimap<String, Comma> patternToVivekCommas = HashMultimap.create();
		for(Comma comma: vivekCommas){
			String pattern = comma.getBayraktarPattern();
			patternToVivekCommas.put(pattern, comma);
		}

		int errorCount = 0;
		for(String pattern: patternToVivekCommas.keySet()){
			Counter<String> labelCounts = new Counter<>();
			String bayraktarLabel = BayraktarPatternLabeler.getLabel(pattern);
			if(bayraktarLabel==null) continue;
			for(Comma comma : patternToVivekCommas.get(pattern)){
				String manualLabel = comma.getLabel();
				labelCounts.incrementCount(manualLabel);
			}
			String frequentLabel = labelCounts.getMax().getFirst();
			if(!frequentLabel.equals(bayraktarLabel)){
				System.out.println("\n\n\n" + pattern + "\t" + bayraktarLabel);
				for(Comma comma : patternToVivekCommas.get(pattern)){
					System.out.println(comma.getVivekNaveenAnnotatedText());
				}
				errorCount++;
			}
		}
		System.out.println("\n\n\nERROR COUNT = " + errorCount);
	}
	
	/**
	 * for each annotated pattern, if there is a single comma whose VivekNaveen label differs, print all of them for further analysis 
	 */
	public static void analyseErrors() {
		VivekAnnotationReader reader = new VivekAnnotationReader(CommaProperties.getInstance().getOriginalVivekAnnotationFile());
		List<Comma> commas = reader.getCommas();
		Multimap<String, Comma> patternToCommas = HashMultimap.create();
		for(Comma comma: commas){
			String pattern = comma.getBayraktarPattern();
			patternToCommas.put(pattern, comma);
		}
		
		Scanner scanner = new Scanner(System.in);
		Multimap<String, Comma> syntaxPatternToCommas = BayraktarAnnotationGenerationHelper.getBayraktarPatternToPTBCommas();
		System.out.println("# patterns in corpus = " + syntaxPatternToCommas.keySet().size());
		int errorPatternCount = 0;
		for(String pattern : syntaxPatternToCommas.keySet()){
			List<Comma> patternOccournces = new ArrayList<>(syntaxPatternToCommas.get(pattern));
			Collections.sort(patternOccournces, new Comparator<Comma>(){
				@Override
				public int compare(Comma o1, Comma o2) {
					// TODO Auto-generated method stub
					return o1.getBayraktarRefinedLabel().compareTo(o2.getBayraktarRefinedLabel());
				}
				
			});
			String bayraktarLabel = BayraktarPatternLabeler.getLabel(pattern);
			if(bayraktarLabel!=null){
				boolean mismatch = false;
				for(Comma occurance : patternOccournces){
					if(!occurance.getLabel().equals(bayraktarLabel)){
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
							System.out.println(PrettyPrintParseTree.pennString(((TreeView)tas.get(currParse++).getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0)));
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
	
}
