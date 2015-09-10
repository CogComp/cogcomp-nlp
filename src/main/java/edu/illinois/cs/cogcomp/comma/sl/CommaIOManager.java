package edu.illinois.cs.cogcomp.comma.sl;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.Sentence;
import edu.illinois.cs.cogcomp.comma.Sentence.CommaLabelSequence;
import edu.illinois.cs.cogcomp.comma.Sentence.CommaSequence;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
public class CommaIOManager {
	public static final String unknownFeature = "unknwonfeature";
	public static SLProblem readProblem(List<Sentence> sentences, Lexiconer lexicon, List<Classifier> lbjExtractors, Classifier lbjLabeler){
		if (lexicon.isAllowNewFeatures())
			lexicon.addFeature(unknownFeature); 
		//TODO lexicon.addLabel("occupy-zero-label-for-some-reason");
		
		SLProblem sp = new SLProblem();
		
		//READ PROBLEM
		for(Sentence sentence: sentences){
			List<CommaSequence> commaSequences = getCommaSequences(sentence, lexicon, lbjExtractors);
			for(CommaSequence commaSequence : commaSequences){
				CommaLabelSequence labelSequence = new CommaLabelSequence(commaSequence, lexicon, lbjLabeler);
				sp.addExample(commaSequence, labelSequence);
			}
		}
		
		return sp;
	}
	
	public static List<CommaSequence> getCommaSequences(Sentence sentence, Lexiconer lexicon, List<Classifier> lbjExtractors){
		LinkedList<Comma> allCommasInSentence = new LinkedList<>(sentence.getCommas());
		List<CommaSequence> commaSequences = new ArrayList<>();
		boolean isCommaStructureFullSentence = CommaProperties.getInstance().isCommaStructureFullSentence();
		if(isCommaStructureFullSentence){
			commaSequences.add(new CommaSequence(allCommasInSentence, lexicon, lbjExtractors));
		}
		else{
			while(!allCommasInSentence.isEmpty()){
				Comma currentComma = allCommasInSentence.pollFirst();
				List<Comma> commasInCurrentStructure = new LinkedList<>();
				commasInCurrentStructure.add(currentComma);
				Iterator<Comma> unusedCommasIt= allCommasInSentence.iterator();
				while(unusedCommasIt.hasNext()){
					Comma otherComma = unusedCommasIt.next();
					if (currentComma.isSibling(otherComma)){
						commasInCurrentStructure.add(otherComma);
						unusedCommasIt.remove();
					}
				}
				commaSequences.add(new CommaSequence(commasInCurrentStructure, lexicon, lbjExtractors));
			}
		}
		return commaSequences;
	}
}
