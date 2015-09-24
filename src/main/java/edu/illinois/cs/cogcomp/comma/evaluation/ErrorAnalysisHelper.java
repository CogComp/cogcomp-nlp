package edu.illinois.cs.cogcomp.comma.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.comma.utils.VivekAnnotationExtractor;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;


public class ErrorAnalysisHelper{
    
    public static void logPredictionError(String filename, String sentenceText, String prediction, String gold, String info, FeatureVector fv){
    	File file = new File(filename);
    	if(file.exists())
    		file = new File(filename+".2");
    	file.getParentFile().mkdirs();
    	PrintWriter writer;
		try {
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
    	writer.println("Gold: " + gold);
    	writer.println("Prediction: " + prediction);
    	writer.println("\n" + sentenceText);
    	writer.println("\n\n----------------------------------------FEATURES--------------------------------------------");
    	for(int i = 0; i < fv.featuresSize(); i++)
    		writer.println(fv.getFeature(i).toStringNoPackage() + "\n");
    	writer.println("\n\n---------------------------------------ADDITIONAL INFORMATION------------------------------------------");
		writer.println(info);
		writer.close();
    }
    
    public static void logClassifierErrors(Learner learner, Parser parser){
    	String directoryName = "data/errors/" + learner.name + "/";
    	VivekAnnotationExtractor vivekAnnotationHelper = new VivekAnnotationExtractor();
		EvaluateDiscrete ed = new EvaluateDiscrete();
		Classifier oracle = learner.getLabeler();
		parser.reset();
		for (Comma c = (Comma) parser.next(); c != null; c = (Comma) parser
				.next()) {
			String prediction = learner.discreteValue(c);
			String gold = oracle.discreteValue(c);
			ed.reportPrediction(prediction, gold);
			if (!gold.equals(prediction)) {
				String textId = c.getTextAnnotation(true).getId();
				String filename = directoryName + c.getCommaID();
				FeatureVector fv = learner.getExtractor().classify(c);
				String instanceInfo = vivekAnnotationHelper.getAnnotation(textId) + c.getAllViews();
				ErrorAnalysisHelper.logPredictionError(filename,
						c.getVivekNaveenAnnotatedText(), prediction, gold,
						instanceInfo, fv);
			}
		}
		ed.printPerformance(System.out);
		ed.printConfusion(System.out);
    }
}