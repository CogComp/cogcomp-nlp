package edu.illinois.cs.cogcomp.comma.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.lbj.BayraktarLabelFeature;
import edu.illinois.cs.cogcomp.comma.lbj.ChunkFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.POSFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.ParseFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.WordFeatures;
import edu.illinois.cs.cogcomp.comma.readers.CommaParser;
import edu.illinois.cs.cogcomp.comma.readers.CommaParser.Ordering;
import edu.illinois.cs.cogcomp.comma.readers.PrettyCorpusReader;
import edu.illinois.cs.cogcomp.comma.readers.TestReader;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVectorReturner;
import edu.illinois.cs.cogcomp.lbjava.classify.LabelVectorReturner;
import edu.illinois.cs.cogcomp.lbjava.learn.Accuracy;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.learn.TestingMetric;
import edu.illinois.cs.cogcomp.lbjava.parse.ArrayParser;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser.SplitPolicy;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Used to get performance of the classifier over different subsets of features 
 *
 */
public class FeatureEngineeringHelper {
	static double learningRate = 0.003;
	static double threshold = 0;
	static double thickness = 3.5;
	
	public static void main(String[] args) throws Exception{
		featureEngineering();
	}
	
	public static void featureEngineering() throws Exception{
		LocalCommaClassifier learner = new LocalCommaClassifier();
		PrettyCorpusReader pcr = TestReader.deserialize();
		CommaParser parser = new CommaParser(pcr.getSentences(), Ordering.ORDERED, true);
		List<Comma> commaList = pcr.getCommas();
		Comma[] commas = commaList.toArray(new Comma[commaList.size()]);
		
		ParseFeatures __ParseFeatures = new ParseFeatures();
		WordFeatures __WordFeatures = new WordFeatures();
		POSFeatures __POSFeatures = new POSFeatures();
		ChunkFeatures __ChunkFeatures = new ChunkFeatures();
		BayraktarLabelFeature __BayraktarLabelFeature = new BayraktarLabelFeature();
		
		List<Classifier> features = new ArrayList<>();
		Classifier labeler = learner.getLabeler();
		features.add(__ParseFeatures);
		features.add(__WordFeatures);
		features.add(__POSFeatures);
		features.add(__ChunkFeatures);
		features.add( __BayraktarLabelFeature);
		Collection<List<Classifier>> ablatedFeatures = getSubsetsOfSizeAtLeastK(features, 4);
		System.out.println(ablatedFeatures);
		
		@SuppressWarnings("unused")
		Classifier extractor = learner.getExtractor();
		List<Pair<Double, String>> performanceFeaturePairs = new ArrayList<>();
		
		/*for(List<Classifier> featureSet: ablatedFeatures){
			StructuredCommaClassifier structured = new StructuredCommaClassifier(featureSet, labeler);
			EvaluateDiscrete structuredPerformance = ClassifierComparison.structuredCVal(structured, parser, false, false);
			System.out.println(structuredPerformance.getOverallStats()[2] + "\t" + featureSet + "\n");
			
			Pair<Double, String> performanceFeaturePair = new Pair<>(structuredPerformance.getOverallStats()[2], featureSet.toString());
			performanceFeaturePairs.add(performanceFeaturePair);
		}*/
		
		for(Collection<Classifier> featureSet: ablatedFeatures){
			learner.forget();
			learner.setLTU(new SparseAveragedPerceptron(learningRate, threshold, thickness));
			learner.setExtractor(new FeatureVectorReturner());
			learner.setLabeler(new LabelVectorReturner());
			FeatureVector[] featureVectors = new FeatureVector[commas.length];
			Object[] extractedFeatures = new Object[commas.length];
			for(int i=0; i< commas.length; i++){
				FeatureVector result = new FeatureVector();
				for(Classifier classifier: featureSet)
					result.addFeatures(classifier.classify(commas[i]));
				result.addLabels(labeler.classify(commas[i]));
				featureVectors[i] = result;
				extractedFeatures[i] = learner.getExampleArray(result, true);
			}
			Parser featureVectorParser = new ArrayParser(extractedFeatures);
			BatchTrainer trainer = new BatchTrainer(learner, featureVectorParser);
			int[] rounds = {250};
			int k=5;
			SplitPolicy splitPolicy = SplitPolicy.sequential;
			double alpha=0.05;
			TestingMetric metric = new Accuracy();
			boolean statusMessages = false;
			double[][] perfromance = trainer.crossValidation(rounds, k, splitPolicy, alpha, metric, statusMessages);
			System.out.println(featureSet);
			System.out.println("\t\t" + 100*(1-alpha) + "% confidence interval after " + rounds[0] + "rounds: " + perfromance[0][0] +"% +/-" + perfromance[0][1]+ "%\n\n" );
			Pair<Double, String> performanceFeaturePair = new Pair<Double, String>(perfromance[0][0], featureSet.toString());
			performanceFeaturePairs.add(performanceFeaturePair);
			//System.out.println(featureSet + ";" + perfromance[0][0] + ";" + perfromance[0][1]);
		}
		
		Collections.sort(performanceFeaturePairs, new Comparator<Pair<Double, String>>() {
			@Override
			public int compare(Pair<Double, String> o1, Pair<Double, String> o2) {
				return (int) Math.signum(o1.getFirst() - o2.getFirst());
			}
		});
		
		System.out.println("\n\n\n\n\n---------------------------------------SORTED--------------------------------------------");
		for(Pair<Double, String> performanceFeaturePair : performanceFeaturePairs)
			System.out.println(performanceFeaturePair.getFirst() + "\t" + performanceFeaturePair.getSecond());
	}
	
	public static <T> List<List<T>> getSubsetsOfSizeAtLeastK(List<T> superSet, int k){
		List<List<T>> subsets = getSubsetsOfSizeAtLeastK(superSet, k, 0);
		Collections.sort(subsets, new Comparator<List<T>>(){

			@Override
			public int compare(List<T> o1, List<T> o2) {
				return o2.size()-o1.size();
			}
			
		});
		return subsets;
	}
	
	public static <T> List<List<T>> getSubsetsOfSizeAtLeastK(List<T> superSet, int k, int idx){
		List<List<T>> subsetsBiggerThanK = new ArrayList<>();
		int size = superSet.size() - idx;
		if(size<k || size<=0){
			subsetsBiggerThanK.add(new ArrayList<T>());
			return subsetsBiggerThanK;
			
		}
		
		List<List<T>> subPowerSetOn = getSubsetsOfSizeAtLeastK(superSet, Math.max(0, k-1), idx+1);
		List<List<T>> subPowerSetOff = new ArrayList<>();
		for(List<T> set: subPowerSetOn){
			if(set.size()>=k){
				List<T> duplicate = new ArrayList<>(set);
				subPowerSetOff.add(duplicate);
			}
			set.add(superSet.get(idx));
		}
		subsetsBiggerThanK.addAll(subPowerSetOff);
		subsetsBiggerThanK.addAll(subPowerSetOn);
		return subsetsBiggerThanK;
	}
}
