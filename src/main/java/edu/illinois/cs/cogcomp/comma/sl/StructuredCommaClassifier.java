package edu.illinois.cs.cogcomp.comma.sl;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.Sentence;
import edu.illinois.cs.cogcomp.comma.Sentence.CommaLabelSequence;
import edu.illinois.cs.cogcomp.comma.Sentence.CommaSequence;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser.Ordering;
import edu.illinois.cs.cogcomp.comma.lbj.LocalCommaClassifier;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class StructuredCommaClassifier extends SLModel{
	private static final long serialVersionUID = 1L;
	private final List<Classifier> lbjExtractors;
	private final Classifier lbjLabeler;
	
	/**
	 * 
	 * @param lbjLabeler the LBJava learner whose extractor and labeler we can use to build instances
	 * @param configFilePath path to config file for the structured learner
	 * @throws Exception
	 */
	public StructuredCommaClassifier(List<Classifier> lbjExtractors, Classifier lbjLabeler, String configFilePath) throws Exception {
		this.lbjExtractors = lbjExtractors;
		this.lbjLabeler = lbjLabeler;
		lm = new Lexiconer();
		infSolver = new CommaSequenceInferenceSolver(lm);
		para = new SLParameters();
		para.loadConfigFile(configFilePath);
		featureGenerator = new CommaSequenceFeatureGenerator(lm);
	}
	
	/**
	 * 
	 * @param sentences the training set
	 * @param modelPath the location to save the learnt model. If it is null, it is not saved
	 * @throws Exception
	 */
	public void train(List<Sentence> sentences, String modelPath) throws Exception {
		lm.setAllowNewFeatures(true);
		SLProblem sp = CommaIOManager.readProblem(sentences, lm, lbjExtractors, lbjLabeler);
		
		// numLabels*numLabels for transition features
		// numWordsInVocab*numLabels for emission features
		// numLabels for prior on labels
		int numFeatures= lm.getNumOfFeature();
		int numLabels = lm.getNumOfLabels();
		para.TOTAL_NUMBER_FEATURE = numFeatures * numLabels + numLabels + numLabels * numLabels;
		
		Learner learner = LearnerFactory.getLearner(infSolver, featureGenerator, para);
		wv = learner.train(sp);
		
		//save the model
		if(modelPath!=null)
			saveModel(modelPath);
	}
	
	/**
	 * 
	 * @param sentences the test set
	 * @param predictionFileName location to which to save the predictions of the model. If it is null, predictions are not saved
	 * @return and EvaluateDiscrete object which can provide the performance statistics
	 * @throws Exception
	 */
	public EvaluateDiscrete test(List<Sentence> sentences, String predictionFileName)
			throws Exception {
		lm.setAllowNewFeatures(false);
		SLProblem sp = CommaIOManager.readProblem(sentences, lm, lbjExtractors, lbjLabeler);
		
		EvaluateDiscrete SLEvaluator = new EvaluateDiscrete();
		BufferedWriter writer = null;
		if(predictionFileName!=null){
			writer = new BufferedWriter(new FileWriter(predictionFileName));
		}
		
		for (int i = 0; i < sp.instanceList.size(); i++) {

			CommaLabelSequence gold = (CommaLabelSequence) sp.goldStructureList
					.get(i);
			CommaLabelSequence prediction = (CommaLabelSequence) infSolver
					.getBestStructure(wv, sp.instanceList.get(i));
			
			for (int j = 0; j < prediction.labels.size(); j++) {
				String predictedTag = prediction.labels.get(j);
				String goldTag = gold.labels.get(j);
				SLEvaluator.reportPrediction(predictedTag, goldTag);
			}
			
			if(predictionFileName!=null){
				CommaSequence instance = ((CommaSequence)sp.instanceList.get(i));
				instance.sortedCommas.get(i).getSentence().getAnnotatedText();
				for(int j=0; j< prediction.labels.size(); j++){
					int commaPosition = instance.sortedCommas.get(j).commaPosition;
					String predictedLabel = lm.getLabelString(Integer.parseInt((prediction.labels.get(j))));
					writer.write(commaPosition + "\t" + predictedLabel +"\n");
				}
				writer.write("\n");
			}
		}
		if(predictionFileName!=null){
			writer.close();
		}
		
		return SLEvaluator;
	}

	public static void main(String args[]) throws Exception{
		VivekAnnotationCommaParser train = new VivekAnnotationCommaParser("data/train_commas.txt", CommaProperties.getInstance().getTrainCommasSerialized(), Ordering.ORDERED_SENTENCE);
		VivekAnnotationCommaParser test = new VivekAnnotationCommaParser("data/test_commas.txt", CommaProperties.getInstance().getTestCommasSerialized(), Ordering.ORDERED_SENTENCE);
		List<Classifier> lbjExtractors = new ArrayList<>();
		lbjExtractors.add(new LocalCommaClassifier().getExtractor());
		Classifier lbjLabeler = new LocalCommaClassifier().getLabeler();
		StructuredCommaClassifier model = new StructuredCommaClassifier(lbjExtractors, lbjLabeler, "config/DCD.config");
		model.train(train.getSentences(), null);
		EvaluateDiscrete ed = model.test(train.getSentences(), null);
		ed.printConfusion(System.out);
		ed.printPerformance(System.out);
	}
}
