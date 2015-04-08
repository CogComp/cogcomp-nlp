package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.lbj.CommaLabel;
import edu.illinois.cs.cogcomp.comma.lbj.ListCommasConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.LocativePairConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.OxfordCommaConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.SubstitutePairConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class ClassifierComparison {
	public static void main(String[] args) {
		Classifier oracle = new CommaLabel();
		List<Classifier> classifiers = new ArrayList<Classifier>();
		classifiers.add(new LocalCommaClassifier());
		classifiers.add(new SubstitutePairConstrainedCommaClassifier());
		classifiers.add(new LocativePairConstrainedCommaClassifier());
		classifiers.add(new ListCommasConstrainedCommaClassifier());
		classifiers.add(new OxfordCommaConstrainedCommaClassifier());

		Parser parser = new CommaReader("data/test_commas.txt", "data/test_commas.ser");

		 for(Classifier classifier : classifiers){
			System.out.println(classifier.name);
		 	TestDiscrete.testDiscrete(classifier, oracle, parser).printPerformance(System.out);
		 	parser.reset();
		 	System.out.println(); 
		 }
	}
}
