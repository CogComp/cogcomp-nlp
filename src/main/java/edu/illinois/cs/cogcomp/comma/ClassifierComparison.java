package edu.illinois.cs.cogcomp.comma;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.bayraktar.BayraktarEvaluation;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.PrintMetrics;
import edu.illinois.cs.cogcomp.comma.sl.StructuredCommaClassifier;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser.SplitPolicy;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.sl.core.SLModel;

public class ClassifierComparison {
	public static void main(String[] args) throws Exception {
		Parser parser = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		/*System.out.println("GOLD GOLD");
		//localCVal(true, true, parser, 160, 0.014, 0, 3.4);
		localCVal(true, true, parser, 200, 0.024, 0, 3.9);
		System.out.println("GOLD AUTO");
		localCVal(true, false, parser, 200, 0.024, 0, 3.9);
		System.out.println("AUTO AUTO");
		localCVal(false, false, parser, 90, 0.024, 0, 3.6);
		Comma.useGoldFeatures(true);*/
		System.out.println("STRUCTURED GOLD");
		structuredCVal(parser);
		Comma.useGoldFeatures(false);
		System.out.println("STRUCTURED AUTO");
		structuredCVal(parser);
		/*System.out.println("BAYRAKTAR GOLD");
		EvaluateDiscrete bayraktarGold = BayraktarEvaluation.getBayraktarBaselinePerformance(parser, true);
		System.out.println(bayraktarGold.getOverallStats()[2]);
		//bayraktarGold.printPerformance(System.out);
		//bayraktarGold.printConfusion(System.out);
		System.out.println("BAYRAKTAR AUTO");
		EvaluateDiscrete bayraktarAuto = BayraktarEvaluation.getBayraktarBaselinePerformance(parser, false);
		System.out.println(bayraktarAuto.getOverallStats()[2]);*/
		//bayraktarAuto.printPerformance(System.out);
		//bayraktarAuto.printConfusion(System.out);
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
	
	public static void structuredCVal(Parser parser) throws Exception{
		int k = 5;
		parser.reset();
		FoldParser foldParser = new FoldParser(parser, k, SplitPolicy.sequential, 0, false);
		EvaluateDiscrete cvalResult = new EvaluateDiscrete();
		for(int i=0; i<k; foldParser.setPivot(++i)){
			foldParser.setFromPivot(false);
			foldParser.reset();
			LinkedHashSet<Sentence> trainSentences = new LinkedHashSet<Sentence>();
			for(Object comma = foldParser.next(); comma!=null; comma = foldParser.next()){
				trainSentences.add(((Comma)comma).getSentence());
			}
			//System.out.println("NUMBER OF TRAIN " + trainSentences.size());
			SLModel model = StructuredCommaClassifier.trainSequenceCommaModel(new ArrayList<Sentence>(trainSentences), "config/commaDCD.config", null);
			foldParser.setFromPivot(true);
			foldParser.reset();
			LinkedHashSet<Sentence> testSentences = new LinkedHashSet<Sentence>();
			for(Object comma = foldParser.next(); comma!=null; comma = foldParser.next()){
				testSentences.add(((Comma)comma).getSentence());
			}
			//System.out.println("NUMBER OF TEST " + testSentences.size());
			EvaluateDiscrete evaluator = StructuredCommaClassifier.testSequenceCommaModel(model, new ArrayList<Sentence>(testSentences), null);
			cvalResult.reportAll(evaluator);
		}
		System.out.println(cvalResult.getOverallStats()[2]);
		//cvalResult.printPerformance(System.out);
		//cvalResult.printConfusion(System.out);
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
		System.out.println(performanceRecord.getOverallStats()[2]);
		//performanceRecord.printPerformance(System.out);
		//sperformanceRecord.printConfusion(System.out);
		
		/*parser.reset();
		learner.forget();
		BatchTrainer otherTrainer = new BatchTrainer(learner, parser);
		//Lexicon lexicon = otherTrainer.preExtract(null);
		//learner.setLexicon(lexicon);
		otherTrainer.crossValidation(new int[]{learningRounds}, k, SplitPolicy.sequential, 0.05, new PrintMetrics(k), false);*/
	}
}
