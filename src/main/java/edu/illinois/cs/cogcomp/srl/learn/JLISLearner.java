package edu.illinois.cs.cogcomp.srl.learn;

import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMLearner;
import edu.illinois.cs.cogcomp.sl.learner.l2_loss_svm.L2LossSSVMParalleDCDSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassLabel;
import edu.illinois.cs.cogcomp.srl.learn.CrossValidationHelper.Tester;
import edu.illinois.cs.cogcomp.srl.utilities.WeightVectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;


public class JLISLearner {

	private final static Logger log = LoggerFactory
			.getLogger(JLISLearner.class);

	public static void saveWeightVector(WeightVector weightVector,
			String modelName) throws FileNotFoundException, IOException {
		log.info("Saving weight vector to " + modelName);
		WeightVectorUtils.save(modelName, weightVector);
	}

	public static WeightVector loadWeightVector(String modelName)
			throws Exception {
		return WeightVectorUtils.load(modelName);
	}

	public static WeightVector trainStructSVM(
			AbstractInferenceSolver[] inference,
			SLProblem SLProblem, float c) throws Exception {


		//L2LossSSVMParalleDCDSolver learner = new L2LossSSVMParalleDCDSolver();

		return learner.
		return learner.parallelTrainStructuredSVM(inference, SLProblem, params);
	}

	public static LearnerParameters cvStructSVMSRL(SLProblem problem,
			AbstractInferenceSolver[] inference, int nFolds)
			throws Exception {
		Tester<SLProblem> evaluator = new Tester<SLProblem>() {

			@Override
			public PerformanceMeasure evaluate(SLProblem testSet,
					WeightVector weight,
					AbstractInferenceSolver inference)
					throws Exception {

				double p = JLISLearner.evaluateSRLLabel(inference, testSet,
						weight);

				return new JLISCVHelper.RealMeasure(p);

			}
		};

		LearnerParameters bestParams = JLISCVHelper.cvSSVM(inference, problem,
				evaluator, inference.length, nFolds);

		return bestParams;
	}

	public static LearnerParameters cvStructSVM(SLProblem problem,
			AbstractInferenceSolver[] inference, int nFolds,
			Tester<SLProblem> evaluator) throws Exception {
		LearnerParameters bestParams = JLISCVHelper.cvSSVM(inference, problem,
				evaluator, inference.length, nFolds);

		return bestParams;
	}

	public static double evaluateSRLLabel(
			AbstractInferenceSolver inference,
			SLProblem testSet, WeightVector weights) throws Exception {
		EvaluationRecord evalRecord = new EvaluationRecord();
		for (int i = 0; i < testSet.input_list.size(); i++) {
			IInstance x = testSet.input_list.get(i);

			SRLMulticlassLabel gold = (SRLMulticlassLabel) testSet.output_list
					.get(i);

			SRLMulticlassLabel bestStructure = (SRLMulticlassLabel) inference
					.getBestStructure(weights, x);

			if (gold.getLabel() == bestStructure.getLabel())
				evalRecord.incrementCorrect();

			evalRecord.incrementGold();
			evalRecord.incrementPredicted();

		}

		log.info("Predicted = " + evalRecord.getPredictedCount() + ", Gold = "
				+ evalRecord.getGoldCount() + " Correct = "
				+ evalRecord.getCorrectCount());

		return evalRecord.getF1();
	}

	private static void initializeSolver(SLParameters params) {

		// how precisely should the dual be solved
		params.BINARY_DUAL_GAP = 0.1;
		params.DUAL_GAP = 0.5;

		params.TRAINMINI = true;
		params.TRAINMINI_SIZE = 5000;

		params.verbose_level = SLParameters.VLEVEL_MID;

		params.MAX_SVM_ITER = 500;
		// params.CLEAN_CACHE = false;
		params.MAX_OUT_ITER = 25;

		params.CALCULATE_REAL_OBJ = true;

	}

}
