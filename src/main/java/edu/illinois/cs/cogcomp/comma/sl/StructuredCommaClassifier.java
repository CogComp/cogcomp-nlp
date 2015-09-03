package edu.illinois.cs.cogcomp.comma.sl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.CommaProperties;
import edu.illinois.cs.cogcomp.comma.Sentence;
import edu.illinois.cs.cogcomp.comma.Sentence.CommaLabelSequence;
import edu.illinois.cs.cogcomp.comma.Sentence.CommaSequence;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser;
import edu.illinois.cs.cogcomp.comma.VivekAnnotationCommaParser.Ordering;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class StructuredCommaClassifier {
	public static SLModel trainSequenceCommaModel(List<Sentence> sentences,
			String configFilePath, String modelPath) throws Exception {
		SLModel model = new SLModel();
		model.lm = new Lexiconer();
		
		SLProblem sp = CommaIOManager.readProblem(sentences, model.lm);
		
		// Disallow the creation of new features
		model.lm.setAllowNewFeatures(false);
		
		// initialize the inference solver
		model.infSolver = new CommaSequenceInferenceSolver(model.lm);
		
		SLParameters para = new SLParameters();
		para.loadConfigFile(configFilePath);
		CommaSequenceFeatureGenerator fg = new CommaSequenceFeatureGenerator(model.lm);
		
		// numLabels*numLabels for transition features
		// numWordsInVocab*numLabels for emission features
		// numLabels for prior on labels
		int numFeatures= model.lm.getNumOfFeature();
		int numLabels = model.lm.getNumOfLabels();
		para.TOTAL_NUMBER_FEATURE = numFeatures * numLabels + numLabels + numLabels * numLabels;
		
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		
		model.wv = learner.train(sp);
		
		//WeightVector.printSparsity(model.wv);
		//if(learner instanceof L2LossSSVMLearner)
		//	System.out.println("Primal objective:" + ((L2LossSSVMLearner)learner).getPrimalObjective(sp, model.wv, model.infSolver, para.C_FOR_STRUCTURE));
		
		//save the model
		if(modelPath!=null)
			model.saveModel(modelPath);
		return model;
	}
	
	public static EvaluateDiscrete testSequenceCommaModel(SLModel model, List<Sentence> sentences, String predictionFileName)
			throws Exception {
		model.lm.setAllowNewFeatures(false);
		SLProblem sp = CommaIOManager.readProblem(sentences, model.lm);
		
		EvaluateDiscrete SLEvaluator = new EvaluateDiscrete();
		BufferedWriter writer = null;
		if(predictionFileName!=null){
			writer = new BufferedWriter(new FileWriter(predictionFileName));
		}
		
		for (int i = 0; i < sp.instanceList.size(); i++) {

			CommaLabelSequence gold = (CommaLabelSequence) sp.goldStructureList
					.get(i);
			CommaLabelSequence prediction = (CommaLabelSequence) model.infSolver
					.getBestStructure(model.wv, sp.instanceList.get(i));
			
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
					String predictedLabel = model.lm.getLabelString(Integer.parseInt((prediction.labels.get(j))));
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
		SLModel model = trainSequenceCommaModel(train.getSentences(), "config/commaDCD.config", null);
		EvaluateDiscrete ed = testSequenceCommaModel(model, test.getSentences(), null);
		ed.printConfusion(System.out);
		ed.printPerformance(System.out);
	}
}
