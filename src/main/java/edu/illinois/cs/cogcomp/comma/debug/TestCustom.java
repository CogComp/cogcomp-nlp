package edu.illinois.cs.cogcomp.comma.debug;

import edu.illinois.cs.cogcomp.comma.Annotator;
import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.Sentence;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;

public class TestCustom {
	public static void main(String[] args) throws Exception{
		Annotator annotator = new Annotator(true, true);
		//String tokenizedText = "John Cannell , an Albuquerque , N.M. , psychiatrist and founder of an educational research organization , Friends for Education , which has studied standardized testing";
		String tokenizedText = "at the elderly , the demographic segment with the highest savings";
		TextAnnotation TA = annotator.preProcess("Custom", "Custom", tokenizedText);
		
		String[] tokens = tokenizedText.split("\\s+");
		Sentence sentence = new Sentence();
		for(int i = 0; i< tokens.length; i++){
			if(tokens[i].equals(",")){
				Comma comma = new Comma(i, tokenizedText, TA, sentence);
				sentence.addComma(comma);
			}
		}
		
		Classifier  localClassifier = new LocalCommaClassifier();
		for(Comma comma : sentence.getCommas()){
			int commaPosition = comma.commaPosition;
			String prediction = localClassifier.discreteValue(comma);
			System.out.println(commaPosition + ": " + prediction);
		}
	}
}
