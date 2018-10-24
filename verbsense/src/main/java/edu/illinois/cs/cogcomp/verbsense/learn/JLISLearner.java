/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.learn;

import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IParameters;
import edu.illinois.cs.cogcomp.sl.core.StructuredProblem;
import edu.illinois.cs.cogcomp.sl.inference.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.learner.L2LossSVM.L2LossSSVMParalleLearner;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.verbsense.jlis.SenseStructure;
import edu.illinois.cs.cogcomp.verbsense.learn.CrossValidationHelper.Tester;
import edu.illinois.cs.cogcomp.verbsense.utilities.WeightVectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class JLISLearner {

    private final static Logger log = LoggerFactory.getLogger(JLISLearner.class);

    public static void saveWeightVector(WeightVector weightVector, String modelName)
            throws IOException {
        log.info("Saving weight vector to " + modelName);
        WeightVectorUtils.save(modelName, weightVector);
    }

    public static WeightVector trainStructSVM(AbstractInferenceSolver[] inference,
            StructuredProblem structuredProblem, double c) throws Exception {
        IParameters params = new IParameters();
        params.c_struct = c;
        initializeSolver(params);

        L2LossSSVMParalleLearner learner = new L2LossSSVMParalleLearner();
        return learner.parallelTrainStructuredSVM(inference, structuredProblem, params);
    }

    public static LearnerParameters crossvalStructSVMSense(StructuredProblem problem,
            AbstractInferenceSolver[] inference, int nFolds) throws Exception {
        Tester<StructuredProblem> evaluator = new Tester<StructuredProblem>() {

            @Override
            public PerformanceMeasure evaluate(StructuredProblem testSet, WeightVector weight,
                    AbstractInferenceSolver inference) throws Exception {
                double p = JLISLearner.evaluateSenseStructure(inference, testSet, weight);
                return new JLISCVHelper.RealMeasure(p);
            }
        };

        return JLISCVHelper.cvSSVM(inference, problem, evaluator, inference.length, nFolds);
    }

    public static double evaluateSenseStructure(AbstractInferenceSolver inference,
            StructuredProblem testSet, WeightVector weights) throws Exception {
        EvaluationRecord evalRecord = new EvaluationRecord();
        for (int i = 0; i < testSet.input_list.size(); i++) {
            IInstance x = testSet.input_list.get(i);

            SenseStructure gold = (SenseStructure) testSet.output_list.get(i);
            SenseStructure bestStructure = (SenseStructure) inference.getBestStructure(weights, x);

            if (gold.getLabel() == bestStructure.getLabel())
                evalRecord.incrementCorrect();

            evalRecord.incrementGold();
            evalRecord.incrementPredicted();
        }

        log.info("Predicted = " + evalRecord.getPredictedCount() + ", Gold = "
                + evalRecord.getGoldCount() + " Correct = " + evalRecord.getCorrectCount());

        return evalRecord.getF1();
    }

    private static void initializeSolver(IParameters params) {
        // how precisely should the dual be solved
        params.BINARY_DUAL_GAP = 0.1;
        params.DUAL_GAP = 0.5;

        params.TRAINMINI = true;
        params.TRAINMINI_SIZE = 5000;

        params.verbose_level = IParameters.VLEVEL_MID;

        params.MAX_SVM_ITER = 500;
        // params.CLEAN_CACHE = false;
        params.MAX_OUT_ITER = 25;

        params.CALCULATE_REAL_OBJ = true;

    }

}
