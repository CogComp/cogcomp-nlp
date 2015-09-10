package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser.Ordering;
import edu.illinois.cs.cogcomp.comma.bayraktar.BayraktarEvaluation;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.sl.StructuredCommaClassifier;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser.SplitPolicy;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class ClassifierComparison {
	public static void main(String[] args) throws Exception {
		Parser parser = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		System.out.println("GOLD GOLD");
		localCVal(true, true, parser, 160, 0.014, 0, 3.4);
		localCVal(true, true, parser, 200, 0.024, 0, 3.9);
		System.out.println("GOLD AUTO");
		localCVal(true, false, parser, 200, 0.024, 0, 3.9);
		System.out.println("AUTO AUTO");
		localCVal(false, false, parser, 90, 0.024, 0, 3.6);

		List<Classifier> lbjExtractors = new ArrayList<>();
		lbjExtractors.add(new LocalCommaClassifier().getExtractor());
		Classifier lbjLabeler = new LocalCommaClassifier().getLabeler();
		System.out.println("STRUCTURED GOLD");
		StructuredCommaClassifier goldStructured = new StructuredCommaClassifier(lbjExtractors, lbjLabeler, "config/DCD.config");
		structuredCVal(goldStructured, parser, true);
		System.out.println("STRUCTURED AUTO");
		StructuredCommaClassifier autoStructured = new StructuredCommaClassifier(lbjExtractors, lbjLabeler, "config/DCD.config");
		structuredCVal(autoStructured, parser, false);
		
		System.out.println("BAYRAKTAR GOLD");
		EvaluateDiscrete bayraktarGold = BayraktarEvaluation.getBayraktarBaselinePerformance(parser, true);
		System.out.println(bayraktarGold.getOverallStats()[2]);
		bayraktarGold.printPerformance(System.out);
		//bayraktarGold.printConfusion(System.out);
		System.out.println("BAYRAKTAR AUTO");
		EvaluateDiscrete bayraktarAuto = BayraktarEvaluation.getBayraktarBaselinePerformance(parser, false);
		System.out.println(bayraktarAuto.getOverallStats()[2]);
		bayraktarAuto.printPerformance(System.out);
		//bayraktarAuto.printConfusion(System.out);
		
		reasonForBelievingThatStructuredIsPerformingWorseDueToOverfitting();
	}
	
	public static void errorAnalysis(){
		/*Classifier oracle = new CommaLabel();
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
		}*/
	}
	
	public static EvaluateDiscrete structuredCVal(StructuredCommaClassifier model, Parser parser, boolean useGoldFeatures) throws Exception{
		Comma.useGoldFeatures(useGoldFeatures);
		int k = 5;
		parser.reset();
		FoldParser foldParser = new FoldParser(parser, k, SplitPolicy.sequential, 0, false);
		EvaluateDiscrete cvalResult = new EvaluateDiscrete();
		for(int i=0; i<k; foldParser.setPivot(++i)){
			foldParser.setFromPivot(false);
			foldParser.reset();
			LinkedHashSet<Sentence> trainSentences = new LinkedHashSet<>();
			for(Object comma = foldParser.next(); comma!=null; comma = foldParser.next()){
				trainSentences.add(((Comma)comma).getSentence());
			}
			//System.out.println("NUMBER OF TRAIN " + trainSentences.size());
			model.train(new ArrayList<>(trainSentences), null);
			foldParser.setFromPivot(true);
			foldParser.reset();
			LinkedHashSet<Sentence> testSentences = new LinkedHashSet<>();
			for(Object comma = foldParser.next(); comma!=null; comma = foldParser.next()){
				testSentences.add(((Comma)comma).getSentence());
			}
			//System.out.println("NUMBER OF TEST " + testSentences.size());
			EvaluateDiscrete evaluator = model.test(new ArrayList<>(testSentences), null);
			cvalResult.reportAll(evaluator);
		}
		//System.out.println(cvalResult.getOverallStats()[2]);
		cvalResult.printPerformance(System.out);
		//cvalResult.printConfusion(System.out);
		
		return cvalResult;
	}
	
	public static void localCVal(boolean trainOnGold, boolean testOnGold, Parser parser,int learningRounds, double learningRate, double threshold, double thickness){
		int k = 5;
		LocalCommaClassifier learner = new LocalCommaClassifier();
		learner.setLTU(new SparseAveragedPerceptron(learningRate, threshold, thickness));
		parser.reset();
		final FoldParser foldParser = new FoldParser(parser, k, SplitPolicy.sequential, 0, false);
		EvaluateDiscrete performanceRecord = new EvaluateDiscrete();
		for(int i=0; i<k; foldParser.setPivot(++i)){
			foldParser.setFromPivot(false);
			foldParser.reset();
			learner.forget();
			BatchTrainer bt = new BatchTrainer(learner, foldParser);
			Comma.useGoldFeatures(trainOnGold);
			Lexicon lexicon = bt.preExtract(null);
			learner.setLexicon(lexicon);
			bt.train(learningRounds);
			foldParser.setFromPivot(true);
			foldParser.reset();
			Comma.useGoldFeatures(testOnGold);
			EvaluateDiscrete currentPerformance = EvaluateDiscrete.evaluateDiscrete(learner, learner.getLabeler(), foldParser);
			performanceRecord.reportAll(currentPerformance);
		}
		//System.out.println(performanceRecord.getOverallStats()[2]);
		performanceRecord.printPerformance(System.out);
		//performanceRecord.printConfusion(System.out);
	}
	
	/**
	 * Structured's higher performance on the train set and lower performance on test set is indicative of overfitting
	 */
	public static void reasonForBelievingThatStructuredIsPerformingWorseDueToOverfitting() throws Exception{
		VivekAnnotationCommaParser train = new VivekAnnotationCommaParser("data/train_commas.txt", CommaProperties.getInstance().getTrainCommasSerialized(), Ordering.ORDERED_SENTENCE);
		VivekAnnotationCommaParser test = new VivekAnnotationCommaParser("data/test_commas.txt", CommaProperties.getInstance().getTestCommasSerialized(), Ordering.ORDERED_SENTENCE);
		List<Classifier> lbjExtractors = new ArrayList<>();
		lbjExtractors.add(new LocalCommaClassifier().getExtractor());
		Classifier lbjLabeler = new LocalCommaClassifier().getLabeler();
		StructuredCommaClassifier model = new StructuredCommaClassifier(lbjExtractors, lbjLabeler, "config/DCD.config");
		model.train(train.getSentences(), null);
		train.reset();
		test.reset();
		EvaluateDiscrete structuredPerformanceOnTrainSet = model.test(train.getSentences(), null);
		EvaluateDiscrete structuredPerformanceOnTestSet = model.test(test.getSentences(), null);
		
		int learningRounds = 200;
		double learningRate = 0.024;
		double threshold = 0;
		double thickness = 3.6;
		LocalCommaClassifier learner = new LocalCommaClassifier();
		learner.setLTU(new SparseAveragedPerceptron(learningRate, threshold, thickness));
		
		train.reset();
		BatchTrainer bt = new BatchTrainer(learner, train);
		Lexicon lexicon = bt.preExtract(null);
		learner.setLexicon(lexicon);
		bt.train(learningRounds);
		train.reset();
		test.reset();
		EvaluateDiscrete localPerformanceOnTrainSet = EvaluateDiscrete.evaluateDiscrete(learner, learner.getLabeler(), train);
		EvaluateDiscrete localPerformanceOnTestSet = EvaluateDiscrete.evaluateDiscrete(learner, learner.getLabeler(), test);
		
		System.out.println("Structured performance on train set " + structuredPerformanceOnTrainSet.getOverallStats()[2]);
		System.out.println("Structured performance on test set " + structuredPerformanceOnTestSet.getOverallStats()[2]);
		System.out.println("Local performance on train set " + localPerformanceOnTrainSet.getOverallStats()[2]);
		System.out.println("Localperformance on test set " + localPerformanceOnTestSet.getOverallStats()[2]);
		
	}
}
