package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.CRC32;

import edu.illinois.cs.cogcomp.comma.CommaReader.Ordering;
import edu.illinois.cs.cogcomp.comma.lbj.BayraktarLabelFeature;
import edu.illinois.cs.cogcomp.comma.lbj.CommaLabel;
import edu.illinois.cs.cogcomp.comma.lbj.DependencyFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.ListCommasConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier$$1;
import edu.illinois.cs.cogcomp.comma.lbj.LocativePairConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.OxfordCommaConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.POSFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.ParseFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.ParseTreeFeature;
import edu.illinois.cs.cogcomp.comma.lbj.SubstitutePairConstrainedCommaClassifier;
import edu.illinois.cs.cogcomp.comma.sl.StructuredCommaClassifier;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
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
		Parser parser = new CommaReader("data/test_commas.txt", "data/test_commas.ser", Ordering.ORIGINAL_SENTENCE);
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
					String filename = "data/errors/" + classifier.name + "/" + textId.replaceAll("\\W+", "_") + "/" + c.commaPosition;
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
		EvaluateDiscrete cvalResult = new EvaluateDiscrete();
		for(int i=0; i<k; foldParser.setPivot(++i)){
			foldParser.setFromPivot(false);
			foldParser.reset();
			SLModel model = StructuredCommaClassifier.trainSequenceCommaModel(foldParser, "config/DCD.config", null);
			foldParser.setFromPivot(true);
			foldParser.reset();
			EvaluateDiscrete evaluator = StructuredCommaClassifier.testSequenceCommaModel(model, foldParser, false);
			cvalResult.reportAll(evaluator);
		}
		
		System.out.println("\nSTRUCTURED CVAL RESULTS");
		cvalResult.printPerformance(System.out);
		cvalResult.printConfusion(System.out);
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
	
	/*public static void featureEngineering(){
		LocalCommaClassifier learner = new LocalCommaClassifier();
		//CommaReader cr = new CommaReader("data/comma_resolution_data.txt", "data/CommaTAGoldFinal.ser", Ordering.ORDERED_SENTENCE);
		CommaReader trainCR = new CommaReader("data/train_commas.txt", "data/train_commas.ser", CommaReader.Ordering.ORDERED_SENTENCE);
		Comma[] trainCommas = (Comma[]) trainCR.getCommas().toArray();
		CommaReader testCR = new CommaReader("data/test_commas.txt", "data/test_commas.ser", Ordering.ORIGINAL_SENTENCE);
		
		ParseFeatures __ParseFeatures = new ParseFeatures();
		ParseTreeFeature __ParseTreeFeature = new ParseTreeFeature();
		POSFeatures __POSFeatures = new POSFeatures();
		DependencyFeatures __DependencyFeatures = new DependencyFeatures();
		BayraktarLabelFeature __BayraktarLabelFeature = new BayraktarLabelFeature();
		
		List<Classifier> features = new ArrayList<Classifier>();
		features.add( __ParseFeatures);
		features.add( __ParseTreeFeature);
		features.add( __POSFeatures);
		features.add( __DependencyFeatures);
		features.add( __BayraktarLabelFeature);
		Collection<Collection<Classifier>> ablatedFeatures = getSubsetsOfSizeAtLeastK(features, 0, 2);
		System.out.println(ablatedFeatures);
		
		
		FeatureVector[] _ParseFeatureVectors = __ParseFeatures.classify(trainCommas);
		FeatureVector[] _ParseTreeFeatureVectors = __ParseTreeFeature.classify(trainCommas);
		FeatureVector[] _POSFeatureVectors = __POSFeatures.classify(trainCommas);
		FeatureVector[] _DependencyFeatureVectors = __DependencyFeatures.classify(trainCommas);
		FeatureVector[] _BayraktarLabelFeatureVectors = __BayraktarLabelFeature.classify(trainCommas);
		
		
		for(Collection<Classifier> featureSet: ablatedFeatures){
			learner.forget();
			FeatureVector[] featureVectors = new FeatureVector[trainCommas.length];
			for(int i=0; i< trainCommas.length; i++){
				FeatureVector result = new FeatureVector();
				for()
				result.addFeatures(v);
				featureVectors[i] = 
			}
				
			learner.learn(trainCommas.toArray());
		}
	}
	
	public static <T> Collection<Collection<T>> getSubsetsOfSizeAtLeastK(List<T> superSet, int idx, int k){
		Collection<Collection<T>> subsetsBiggerThanK = new ArrayList<Collection<T>>();
		int size = superSet.size() - idx;
		if(size<k || size<=0){
			subsetsBiggerThanK.add(new ArrayList<T>());
			return subsetsBiggerThanK;
			
		}
		
		Collection<Collection<T>> subPowerSetOn = getSubsetsOfSizeAtLeastK(superSet, idx+1, Math.max(0, k-1));
		Collection<Collection<T>> subPowerSetOff = new ArrayList<Collection<T>>();
		for(Collection<T> set: subPowerSetOn){
			if(set.size()>=k){
				Collection<T> duplicate = new ArrayList<T>(set);
				subPowerSetOff.add(duplicate);
			}
			set.add(superSet.get(idx));
		}
		subsetsBiggerThanK.addAll(subPowerSetOff);
		subsetsBiggerThanK.addAll(subPowerSetOn);
		return subsetsBiggerThanK;
	}*/
}
