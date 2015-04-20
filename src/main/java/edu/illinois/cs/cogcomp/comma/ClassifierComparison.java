package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.CommaReader.Ordering;
import edu.illinois.cs.cogcomp.comma.lbj.CommaLabel;
import edu.illinois.cs.cogcomp.comma.lbj.ListCommasConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier$$1;
import edu.illinois.cs.cogcomp.comma.lbj.LocativePairConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.OxfordCommaConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.SubstitutePairConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.sl.StructuredCommaClassifier;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.Accuracy;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser.SplitPolicy;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class ClassifierComparison {
	public static void main(String[] args) throws Exception {
		Classifier oracle = new CommaLabel();
		List<Classifier> classifiers = new ArrayList<Classifier>();
		Classifier featureExtractor = new LocalCommaClassifier$$1();
		classifiers.add(new LocalCommaClassifier());
		classifiers.add(new SubstitutePairConstrainedCommaClassifier());
		classifiers.add(new LocativePairConstrainedCommaClassifier());
		classifiers.add(new ListCommasConstrainedCommaClassifier());
		classifiers.add(new OxfordCommaConstrainedCommaClassifier());

		//Parser parser = new CommaReader("data/comma_resolution_data.txt", "data/CommaTAGoldFinal.ser", Ordering.ORIGINAL_SENTENCE);
		Parser parser = new CommaReader("data/dev_commas.txt", "data/dev_commas.ser", Ordering.ORIGINAL_SENTENCE);
		ErrorAnalysis ea = new ErrorAnalysis("data/comma_resolution_data.txt", parser);
		parser.reset();
		for (Classifier classifier : classifiers) {
			System.out.println(classifier.name);
			EvaluateDiscrete td = new EvaluateDiscrete();
			for(Comma c = (Comma) parser.next(); c!=null; c=(Comma) parser.next()){
				String prediction = classifier.discreteValue(c);
				String gold = oracle.discreteValue(c);
				td.reportPrediction(prediction, gold);
				if(!gold.equals(prediction)){
					String textId = c.getTextAnnotation(true).getId();
					String filename = "data/errors/" + classifier.name + "/" + textId.replaceAll("\\W+", "_") + "_" + c.commaPosition;
					FeatureVector fv = featureExtractor.classify(c);
					ErrorAnalysis.logPredictionError(filename, c.getAnnotatedText(), prediction, gold, ea.getInstanceInfo(textId), fv);
				}
			}
			td.printPerformance(System.out);
			td.printConfusion(System.out);
			parser.reset();
			System.out.println();
		}

		 //structuredCVal();
	}
	
	public static void structuredCVal() throws Exception{
		Parser parser = new CommaReader("data/comma_resolution_data.txt", "data/CommaTAGoldFinal.ser", Ordering.ORDERED_SENTENCE);
		//Parser parser = new CommaReader("data/train_commas.txt", "data/train_commas.ser", Ordering.ORDERED_SENTENCE);
		int k = 20;
		FoldParser foldParser = new FoldParser(parser, k, SplitPolicy.sequential, 0, false);
		TestDiscrete cvalResult = new TestDiscrete();
		for(int i=0; i<k; foldParser.setPivot(++i)){
			foldParser.setFromPivot(false);
			foldParser.reset();
			SLModel model = StructuredCommaClassifier.trainSequenceCommaModel(foldParser, "config/DCD.config", null);
			foldParser.setFromPivot(true);
			foldParser.reset();
			TestDiscrete evaluator = StructuredCommaClassifier.testSequenceCommaModel(model, foldParser, false);
			cvalResult.reportAll(evaluator);
		}
		
		System.out.println("\nSTRUCTURED CVAL RESULTS");
		cvalResult.printPerformance(System.out);
		System.out.println();
	}
	
	public static void localCVal() throws Exception{
		System.out.println("\nLOCAL CVAL RESULTS");
		Parser parser = new CommaReader("data/comma_resolution_data.txt", "data/CommaTAGoldFinal.ser", Ordering.RANDOM_SENTENCE);
		int k = 5;
		Learner localLearner = new LocalCommaClassifier();
		localLearner.forget();
		BatchTrainer bt = new BatchTrainer(new LocalCommaClassifier(), parser);
		int[] rounds = {100};
		bt.crossValidation(rounds, k, SplitPolicy.sequential, 0.05, new Accuracy(true), false);
		
		System.out.println();
	}
}
