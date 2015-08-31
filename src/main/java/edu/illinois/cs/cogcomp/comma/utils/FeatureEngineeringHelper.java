package edu.illinois.cs.cogcomp.comma.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser;
import edu.illinois.cs.cogcomp.comma.lbj.BayraktarLabelFeature;
import edu.illinois.cs.cogcomp.comma.lbj.BayraktarPatternFeature;
import edu.illinois.cs.cogcomp.comma.lbj.ChunkFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.DependencyFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.POSFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.ParseFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.ParseTreeFeature;
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

public class FeatureEngineeringHelper {
	static double learningRate = 0.024;
	static double threshold = 0;
	static double thickness = 3.6;
	
	public static void main(String[] args){
		featureEngineering();
	}
	
	public static void featureEngineering(){
		LocalCommaClassifier learner = new LocalCommaClassifier();
		VivekAnnotationCommaParser cr = new VivekAnnotationCommaParser("data/comma_resolution_data.txt", CommaProperties.getInstance().getAllCommasSerialized(), VivekAnnotationCommaParser.Ordering.ORDERED_SENTENCE);
		List<Comma> commaList = cr.getCommas();
		Comma[] commas = (Comma[]) commaList.toArray(new Comma[commaList.size()]);
		
		ParseFeatures __ParseFeatures = new ParseFeatures();
		ParseTreeFeature __ParseTreeFeature = new ParseTreeFeature();
		POSFeatures __POSFeatures = new POSFeatures();
		ChunkFeatures __ChunkFeatures = new ChunkFeatures();
		DependencyFeatures __DependencyFeatures = new DependencyFeatures();
		BayraktarLabelFeature __BayraktarLabelFeature = new BayraktarLabelFeature();
		BayraktarPatternFeature __BayraktarPatternFeature = new BayraktarPatternFeature();
		
		List<Classifier> features = new ArrayList<Classifier>();
		Classifier labeler = learner.getLabeler();
		features.add(__ParseFeatures);//Sibling and Parent
		features.add(__ParseTreeFeature);
		features.add(__POSFeatures);
		features.add(__ChunkFeatures);
		features.add(__DependencyFeatures);
		features.add( __BayraktarLabelFeature);
		features.add( __BayraktarPatternFeature);
		Collection<List<Classifier>> ablatedFeatures = getSubsetsOfSizeAtLeastK(features, 3);
		System.out.println(ablatedFeatures);
		
		Comma.useGoldFeatures(false);
		@SuppressWarnings("unused")
		Classifier extractor = learner.getExtractor();
		List<Pair<Double, String>> performanceFeaturePairs = new ArrayList<Pair<Double,String>>();
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
			int[] rounds = {90};
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
				return (int) Math.signum(o1.getFirst().doubleValue() - o2.getFirst().doubleValue());
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
		List<List<T>> subsetsBiggerThanK = new ArrayList<List<T>>();
		int size = superSet.size() - idx;
		if(size<k || size<=0){
			subsetsBiggerThanK.add(new ArrayList<T>());
			return subsetsBiggerThanK;
			
		}
		
		List<List<T>> subPowerSetOn = getSubsetsOfSizeAtLeastK(superSet, Math.max(0, k-1), idx+1);
		List<List<T>> subPowerSetOff = new ArrayList<List<T>>();
		for(List<T> set: subPowerSetOn){
			if(set.size()>=k){
				List<T> duplicate = new ArrayList<T>(set);
				subPowerSetOff.add(duplicate);
			}
			set.add(superSet.get(idx));
		}
		subsetsBiggerThanK.addAll(subPowerSetOff);
		subsetsBiggerThanK.addAll(subPowerSetOn);
		return subsetsBiggerThanK;
	}
}
