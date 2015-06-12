package edu.illinois.cs.cogcomp.comma.bayraktar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

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
		printBayraktarBaselinePerformance();
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
		MultiValueMap syntaxPatternToCommas = BayraktarAnnotationGenerationHelper.getBayraktarPatternToCommas();
		@SuppressWarnings("unchecked")
		List<Entry<String, ArrayList<Comma>>> patternsToCommaEntries = new ArrayList<Entry<String,ArrayList<Comma>>>(patternToComma.entrySet());
		System.out.println("# patterns in corpus = " + patternsToCommaEntries.size());
		int errorPatternCount = 0;
		for(Entry<String, ArrayList<Comma>> entry : patternsToCommaEntries){
			String pattern = entry.getKey();
			List<Comma> patternOccournces = entry.getValue();
			MultiValueMap goldLabelToComma = new MultiValueMap();
			Collections.sort(patternOccournces, new Comparator<Comma>(){
				@Override
				public int compare(Comma o1, Comma o2) {
					// TODO Auto-generated method stub
					return o1.getRole().compareTo(o2.getRole());
				}
				
			});
			String bayraktarLabel = BayraktarPatternLabeler.getBayraktarLabel(pattern);
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
						System.out.println(c.getTextAnnotation(true).getId() + "\t" + c.getAnnotatedText());
					
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
							System.out.println(Prac.pennString(((TreeView)tas.get(currParse++).getView(ViewNames.PARSE_GOLD)).getTree(0)));
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
			String goldLabel = comma.getRole();
			String bayraktarPrediction = comma.getBayraktarLabel();
			
			if(USE_NEW_LABEL_SET){
				if(goldLabel.equals("Other") && BayraktarPatternLabeler.isNewLabel(bayraktarPrediction))
					goldLabel = bayraktarPrediction;
			}
			else
				bayraktarPrediction = newToOldLabel(bayraktarPrediction);
				
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
