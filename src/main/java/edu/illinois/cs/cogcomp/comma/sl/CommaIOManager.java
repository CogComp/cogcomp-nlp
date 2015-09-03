package edu.illinois.cs.cogcomp.comma.sl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.Sentence;
import edu.illinois.cs.cogcomp.comma.Sentence.CommaLabelSequence;
import edu.illinois.cs.cogcomp.comma.Sentence.CommaSequence;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
public class CommaIOManager {
	public static final String unknownFeature = "unknwonfeature";
	public static final Learner localCommaClassifier = new LocalCommaClassifier();
	public static final Classifier lbjExtractor = localCommaClassifier.getExtractor();
	public static final Classifier lbjLabeler = localCommaClassifier.getLabeler();
	public static SLProblem readProblem(List<Sentence> sentences, Lexiconer lexicon){
		if (lexicon.isAllowNewFeatures())
			lexicon.addFeature(unknownFeature); 
		//TODO lexicon.addLabel("occupy-zero-label-for-some-reason");
		
		SLProblem sp = new SLProblem();
		
		//READ PROBLEM
		for(Sentence sentence: sentences){
			List<CommaSequence> commaSequences = getCommaSequences(sentence, lexicon);
			for(CommaSequence commaSequence : commaSequences){
				CommaLabelSequence labelSequence = new CommaLabelSequence(commaSequence, lexicon, lbjLabeler);
				sp.addExample(commaSequence, labelSequence);
			}
		}
		
		return sp;
	}
	
	public static List<CommaSequence> getCommaSequences(Sentence sentence, Lexiconer lexicon){
		LinkedList<Comma> allCommasInSentence = new LinkedList<Comma>(sentence.getCommas());
		List<CommaSequence> commaSequences = new ArrayList<Sentence.CommaSequence>();
		boolean isCommaStructureFullSentence = CommaProperties.getInstance().isCommaStructureFullSentence();
		if(isCommaStructureFullSentence){
			commaSequences.add(new CommaSequence(allCommasInSentence, lexicon, lbjExtractor));
		}
		else{
			while(!allCommasInSentence.isEmpty()){
				Comma currentComma = allCommasInSentence.pollFirst();
				List<Comma> commasInCurrentStructure = new LinkedList<Comma>();
				commasInCurrentStructure.add(currentComma);
				Iterator<Comma> unusedCommasIt= allCommasInSentence.iterator();
				while(unusedCommasIt.hasNext()){
					Comma otherComma = unusedCommasIt.next();
					if (currentComma.isSibling(otherComma)){
						commasInCurrentStructure.add(otherComma);
						unusedCommasIt.remove();
					}
				}
				commaSequences.add(new CommaSequence(commasInCurrentStructure, lexicon, lbjExtractor));
			}
		}
		return commaSequences;
	}
}
