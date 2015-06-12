package edu.illinois.cs.cogcomp.comma.sl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.comma.CommaReader;
import edu.illinois.cs.cogcomp.comma.CommaReader.Ordering;
import edu.illinois.cs.cogcomp.comma.ErrorAnalysis;
import edu.illinois.cs.cogcomp.comma.utils.EvaluateDiscrete;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.sl.applications.sequence.SequenceLabel;
import edu.illinois.cs.cogcomp.sl.core.SLModel;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.Learner;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

public class StructuredCommaClassifier {
	public static SLModel trainSequenceCommaModel(Parser train,
			String configFilePath, String modelPath) throws Exception {
		SLModel model = new SLModel();
		model.lm = new Lexiconer();
		
		SLProblem sp = CommaIOManager.readProblem(train, model.lm);
		
		// Disallow the creation of new features
		model.lm.setAllowNewFeatures(false);
		

		// initialize the inference solver
		//model.infSolver = new SequenceInferenceSolver();
		model.infSolver = new CommaSequenceInferenceSolver();
		
		SLParameters para = new SLParameters();
		para.loadConfigFile(configFilePath);
		CommaSequenceFeatureGenerator fg = new CommaSequenceFeatureGenerator();
		para.TOTAL_NUMBER_FEATURE = CommaIOManager.numFeatures * CommaIOManager.numLabels + CommaIOManager.numLabels +
				CommaIOManager.numLabels *CommaIOManager.numLabels;
		
		
		// numLabels*numLabels for transition features
		// numWordsInVocab*numLabels for emission features
		// numLabels for prior on labels
		Learner learner = LearnerFactory.getLearner(model.infSolver, fg, para);
		
		
		
		model.wv = learner.train(sp);
		/*long start_time = System.currentTimeMillis();
		System.out.println("Training structured classifier took "
				+ (System.currentTimeMillis() - start_time) / 1000.0
				+ " secs");*/
		
		
		model.config =  new HashMap<String, String>();
		model.config.put("numFeatures", String.valueOf(CommaIOManager.numFeatures));
		model.config.put("numLabels", String.valueOf(CommaIOManager.numLabels));
		
		WeightVector.printSparsity(model.wv);
		//if(learner instanceof L2LossSSVMLearner)
		//	System.out.println("Primal objective:" + ((L2LossSSVMLearner)learner).getPrimalObjective(sp, model.wv, model.infSolver, para.C_FOR_STRUCTURE));
		
		//save the model
		if(modelPath!=null)
			model.saveModel(modelPath);
		return model;
	}
	
	public static EvaluateDiscrete testSequenceCommaModel(SLModel model, Parser test, boolean errorAnalysis) throws Exception{
		return testSequenceCommaModel(model, test, null, errorAnalysis);
	}
	
	public static EvaluateDiscrete testSequenceCommaModel(String modelPath, Parser test, boolean errorAnalysis) throws Exception{
		SLModel model = SLModel.loadModel(modelPath);
		return testSequenceCommaModel(model, test, null, errorAnalysis);
	}
	
	public static EvaluateDiscrete testSequenceCommaModel(SLModel model, Parser test, String predictionFileName, boolean errorAnalysis)
			throws Exception {
		model.lm.setAllowNewFeatures(false);
		CommaIOManager.numFeatures = Integer.valueOf(model.config.get("numFeatures"));
		CommaIOManager.numLabels = Integer.valueOf(model.config.get("numLabels"));
		SLProblem sp = CommaIOManager.readProblem(test, model.lm);
		long start_time = System.currentTimeMillis();

		
		EvaluateDiscrete SLEvaluator = new EvaluateDiscrete();
		BufferedWriter writer = null;
		if(predictionFileName!=null){
			writer = new BufferedWriter(new FileWriter(predictionFileName));
		}
		ErrorAnalysis ea = null;
		if(errorAnalysis)
			ea = new ErrorAnalysis("data/comma_resolution_data.txt", test);
		for (int i = 0; i < sp.instanceList.size(); i++) {

			SequenceLabel gold = (SequenceLabel) sp.goldStructureList
					.get(i);
			SequenceLabel prediction = (SequenceLabel) model.infSolver
					.getBestStructure(model.wv, sp.instanceList.get(i));
			if(predictionFileName!=null){
				for(int j=0; j< prediction.tags.length; j++){
					writer.write(String.valueOf(prediction.tags[j]+1)+"\n");
				}
			}
			if(errorAnalysis && !gold.equals(prediction)){
				//String textId = c.getTextAnnotation(true).getId();
				String textId = String.valueOf(sp.instanceList.get(i).hashCode());
				String filename = "data/errors/StructuredClassifier/" + textId.replaceAll("\\W+", "_");
				//ErrorAnalysis.logPredictionError(filename, "",prediction.toString(), gold.toString(), ea.getInstanceInfo(textId));
			}
			
            
			for (int j = 0; j < prediction.tags.length; j++) {
				String predictedTag = model.lm.getLabelString(prediction.tags[j]);
				String goldTag = model.lm.getLabelString(gold.tags[j]);
				SLEvaluator.reportPrediction(predictedTag, goldTag);
			}
		}
		if(predictionFileName!=null){
			writer.close();
		}
		
		System.out.println("Evaluating structured classifier took "
				+ (System.currentTimeMillis() - start_time) / 1000.0
				+ " secs");
		
		return SLEvaluator;
	}

	public static void main(String args[]) throws Exception{
		Parser train = new CommaReader("data/train_commas.txt", "data/train_commas.ser", Ordering.ORDERED_SENTENCE);
		Parser test = new CommaReader("data/dev_commas.txt", "data/dev_commas.ser", Ordering.ORIGINAL_SENTENCE);
		SLModel model = trainSequenceCommaModel(train, "config/DCD.config", null);
		train.reset();
		//testSequenceCommaModel("data/output/seqCommaModel", test).printPerformance(System.out);
		EvaluateDiscrete ed = testSequenceCommaModel(model, test, false);
		ed.printConfusion(System.out);
		ed.printPerformance(System.out);
	}
}
