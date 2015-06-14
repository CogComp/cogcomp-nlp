package edu.illinois.cs.cogcomp.comma.bayraktar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.map.MultiValueMap;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.CommaReader;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.comma.utils.Prac;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.TreeView;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class BayraktarEvaluation {
	
	public static void main(String[] args){
		//printBayraktarBaselinePerformance();
		verify();
	}
	
	
	public static void verify(){
		CommaReader reader = new CommaReader("data/comma_resolution_data.txt", "data/CommaFullView.ser", CommaReader.Ordering.ORDERED_SENTENCE);
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
			ConcurrentMap<String, AtomicInteger> labelToCounts = new ConcurrentHashMap<String, AtomicInteger>();
			String pattern = entry.getKey();
			String bayraktarLabel = BayraktarPatternLabeler.getLabel(pattern);
			if(bayraktarLabel==null) continue;
			for(Comma comma : entry.getValue()){
				String vivekLabel = comma.getVivekNaveenRole();
				labelToCounts.putIfAbsent(vivekLabel, new AtomicInteger(0));
				labelToCounts.get(vivekLabel).incrementAndGet();
			}
			String frequentLabel = null;
			int frequency = 0;
			for(String vivekLabel: labelToCounts.keySet()){
				int currFrequency = labelToCounts.get(vivekLabel).get();
				if(currFrequency>frequency){
					frequentLabel = vivekLabel;
					frequency = currFrequency;
				}
			}
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
	
	public static void errorAnalysis() {
		CommaReader reader = new CommaReader("data/comma_resolution_data.txt", "data/CommaFullView.ser", CommaReader.Ordering.ORDERED_SENTENCE);
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
					return o1.getRole().compareTo(o2.getRole());
				}
				
			});
			String bayraktarLabel = BayraktarPatternLabeler.getLabel(pattern);
			if(bayraktarLabel!=null){
				boolean mismatch = false;
				for(Comma occurance : patternOccournces){
					if(!occurance.getRole().equals(newToOldLabel(bayraktarLabel))){
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
							System.out.println(Prac.pennString(((TreeView)tas.get(currParse++).getTextAnnotation(true).getView(ViewNames.PARSE_GOLD)).getTree(0)));
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
	
	public static void printBayraktarBaselinePerformance(){
		CommaReader reader = new CommaReader("data/comma_resolution_data.txt", "data/CommaFullView.ser", CommaReader.Ordering.ORDERED_SENTENCE);
		List<Comma> commas = reader.getCommas();
		EvaluateDiscrete bayraktarEvalaution = new EvaluateDiscrete();
		CommaProperties properties = CommaProperties.getInstance();
        boolean USE_NEW_LABEL_SET = properties.useNewLabelSet();
		for(Comma comma: commas){
			if(!BayraktarPatternLabeler.isLabelAvailable(comma)) continue;
			String goldLabel;
			String bayraktarPrediction = comma.getBayraktarLabel();
			if(USE_NEW_LABEL_SET)
				goldLabel = comma.getVivekNaveenRole();
			else{
				goldLabel = comma.getRole();
				bayraktarPrediction = newToOldLabel(bayraktarPrediction);
			}
			bayraktarEvalaution.reportPrediction(bayraktarPrediction, goldLabel);
		}
		System.out.println("Bayraktar");
		bayraktarEvalaution.printPerformance(System.out);
		bayraktarEvalaution.printConfusion(System.out);
	}
	
	public static void printTreeforId(String id){
		List<TextAnnotation> tas = BayraktarAnnotationGenerationHelper.getPTBTAList();
		for(TextAnnotation ta : tas){
			if(ta.getId().equals(id)){
				System.out.println(ta);
				System.out.println(Prac.pennString(((TreeView)ta.getView(ViewNames.PARSE_GOLD)).getTree(0)));
			}
		}
	}
	
	public static String newToOldLabel(String newLabel){
		if(BayraktarPatternLabeler.isNewLabel(newLabel))
			return "Other";
		else
			return newLabel;
	}
	
}
