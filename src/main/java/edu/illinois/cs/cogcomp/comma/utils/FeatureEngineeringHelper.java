package edu.illinois.cs.cogcomp.comma.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.junit.After;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaReader;
import edu.illinois.cs.cogcomp.comma.CommaReader.Ordering;
import edu.illinois.cs.cogcomp.comma.lbj.BayraktarLabelFeature;
import edu.illinois.cs.cogcomp.comma.lbj.BayraktarPatternFeature;
import edu.illinois.cs.cogcomp.comma.lbj.DependencyFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.lbj.POSFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.ParseFeatures;
import edu.illinois.cs.cogcomp.comma.lbj.ParseTreeFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVectorReturner;
import edu.illinois.cs.cogcomp.lbjava.classify.LabelVectorReturner;
import edu.illinois.cs.cogcomp.lbjava.learn.Accuracy;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.TestingMetric;
import edu.illinois.cs.cogcomp.lbjava.parse.ArrayParser;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser.SplitPolicy;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

public class FeatureEngineeringHelper {
	
	public static void main(String[] args){
		featureEngineering();
	}
	public static void featureEngineering(){
		LocalCommaClassifier learner = new LocalCommaClassifier();
		CommaReader cr = new CommaReader("data/comma_resolution_data.txt", "data/CommaFullView.ser", CommaReader.Ordering.ORDERED_SENTENCE);
		List<Comma> trainCommaList = cr.getCommas();
		Comma[] trainCommas = (Comma[]) trainCommaList.toArray(new Comma[trainCommaList.size()]);
		
		ParseFeatures __ParseFeatures = new ParseFeatures();
		ParseTreeFeature __ParseTreeFeature = new ParseTreeFeature();
		POSFeatures __POSFeatures = new POSFeatures();
		DependencyFeatures __DependencyFeatures = new DependencyFeatures();
		BayraktarLabelFeature __BayraktarLabelFeature = new BayraktarLabelFeature();
		BayraktarPatternFeature __BayraktarPatternFeature = new BayraktarPatternFeature();
		
		List<Classifier> features = new ArrayList<Classifier>();
		Classifier labeler = learner.getLabeler();
		features.add( __ParseFeatures);
		features.add( __ParseTreeFeature);
		features.add( __POSFeatures);
		features.add( __DependencyFeatures);
		features.add( __BayraktarLabelFeature);
		features.add( __BayraktarPatternFeature);
		Collection<List<Classifier>> ablatedFeatures = getSubsetsOfSizeAtLeastK(features, 1);
		System.out.println(ablatedFeatures);
		
		
		for(Collection<Classifier> featureSet: ablatedFeatures){
			learner.forget();
			learner.setExtractor(new FeatureVectorReturner());
			learner.setLabeler(new LabelVectorReturner());
			FeatureVector[] featureVectors = new FeatureVector[trainCommas.length];
			Object[] extractedFeatures = new Object[trainCommas.length];
			for(int i=0; i< trainCommas.length; i++){
				FeatureVector result = new FeatureVector();
				for(Classifier classifier: featureSet)
					result.addFeatures(classifier.classify(trainCommas[i]));
				result.addLabels(labeler.classify(trainCommas[i]));
				featureVectors[i] = result;
				extractedFeatures[i] = learner.getExampleArray(result, true);
			}
			Parser featureVectorParser = new ArrayParser(extractedFeatures);
			BatchTrainer trainer = new BatchTrainer(learner, featureVectorParser);
			int[] rounds = {125};
			int k=5;
			SplitPolicy splitPolicy = SplitPolicy.random;
			double alpha=0.05;
			TestingMetric metric = new Accuracy(false);
			boolean statusMessages = false;
			double[][] perfromance = trainer.crossValidation(rounds, k, splitPolicy, alpha, metric, statusMessages);
			//System.out.println(featureSet);
			//System.out.println(100*(1-alpha) + "% confidence interval after " + rounds[0] + "rounds: " + perfromance[0][0] +"% +/-" + perfromance[0][1]+ "%\n\n" );
			System.out.println(featureSet + ";" + perfromance[0][0] + ";" + perfromance[0][1]);
		}
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
