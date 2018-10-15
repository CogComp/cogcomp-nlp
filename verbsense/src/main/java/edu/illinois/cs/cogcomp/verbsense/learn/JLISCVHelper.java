/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.learn;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.StructuredProblem;
import edu.illinois.cs.cogcomp.sl.inference.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.verbsense.learn.CrossValidationHelper.DatasetSplitter;
import edu.illinois.cs.cogcomp.verbsense.learn.CrossValidationHelper.PerformanceMeasureAverager;
import edu.illinois.cs.cogcomp.verbsense.learn.CrossValidationHelper.Tester;
import edu.illinois.cs.cogcomp.verbsense.learn.CrossValidationHelper.Trainer;
import edu.illinois.cs.cogcomp.verbsense.learn.JLISCVHelper.RealMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class JLISCVHelper {

    private final static Logger log = LoggerFactory.getLogger(JLISCVHelper.class);

    public static class RealMeasure implements PerformanceMeasure {
        public final Double d;

        public RealMeasure(Double d) {
            this.d = d;

        }

        @Override
        public int compareTo(PerformanceMeasure o) {

            if (o == null)
                return 1;

            return d.compareTo(((RealMeasure) o).d);
        }

        @Override
        public String summarize() {
            return d.toString();
        }

        @Override
        public String toString() {
            return summarize();
        }

    }

    private static int[] getSplitLocations(int numFolds, int numExamples) {
        int foldSize = numExamples / numFolds;

        int[] splitIdStarts = new int[numFolds + 1];
        splitIdStarts[0] = 0;
        for (int i = 1; i < numFolds; i++) {
            splitIdStarts[i] = splitIdStarts[i - 1] + foldSize;
        }
        splitIdStarts[numFolds] = numExamples;
        return splitIdStarts;
    }

    public static LearnerParameters cvSSVMSerial(AbstractInferenceSolver[] inference,
            StructuredProblem sp, Tester<StructuredProblem> evaluator, int nFolds) throws Exception {

        log.info("Cross validation for struct SVM");
        int[] structSplits = getSplitLocations(nFolds, sp.size());

        StructureProblemSplitter splitter = new StructureProblemSplitter(structSplits);

        CrossValidationHelper<StructuredProblem> cvHelper =
                new CrossValidationHelper<StructuredProblem>(nFolds, inference,
                        new RealMeasureAverager(), splitter, new SSVMTrainer(), evaluator);

        List<LearnerParameters> params = new ArrayList<LearnerParameters>();

        for (int i = -8; i < 0; i++) {
            params.add(LearnerParameters.getSSVMParams(Math.pow(2d, i)));
        }

        LearnerParameters learnerParameters = cvHelper.doCV(sp, params, false);

        return learnerParameters;
    }

    public static LearnerParameters cvSSVM(AbstractInferenceSolver[] inference,
            StructuredProblem sp, Tester<StructuredProblem> evaluator, int nThreads, int nFolds)
            throws Exception {

        log.info("Cross validation for struct SVM");
        int[] structSplits = getSplitLocations(nFolds, sp.size());

        StructureProblemSplitter splitter = new StructureProblemSplitter(structSplits);

        CrossValidationHelper<StructuredProblem> cvHelper =
                new CrossValidationHelper<StructuredProblem>(nFolds, inference,
                        new RealMeasureAverager(), splitter, new SSVMTrainer(), evaluator);

        List<LearnerParameters> params = new ArrayList<LearnerParameters>();

        for (int i = -8; i < 0; i++) {
            params.add(LearnerParameters.getSSVMParams(Math.pow(2d, i)));
        }

        LearnerParameters learnerParameters = cvHelper.doCV(sp, params);

        return learnerParameters;
    }

}


class SSVMTrainer implements Trainer<StructuredProblem> {

    @Override
    public WeightVector train(StructuredProblem dataset, LearnerParameters params,
            AbstractInferenceSolver[] inference) throws Exception {
        return JLISLearner.trainStructSVM(inference, dataset, params.getcStruct());
    }

}


abstract class SingleListDatasetSplitter<DatasetType> implements DatasetSplitter<DatasetType> {
    protected final int[] splitIds;

    public SingleListDatasetSplitter(int[] splitIds) {
        this.splitIds = splitIds;
    }
}


class StructureProblemSplitter extends SingleListDatasetSplitter<StructuredProblem> {

    public StructureProblemSplitter(int[] splitIds) {
        super(splitIds);
    }

    public Pair<StructuredProblem, StructuredProblem> getFoldData(StructuredProblem problem,
            int foldId) {

        int testStart = splitIds[foldId];
        int testEnd = splitIds[foldId + 1];

        StructuredProblem train = new StructuredProblem();
        StructuredProblem test = new StructuredProblem();

        train.input_list = new ArrayList<IInstance>();
        test.input_list = new ArrayList<IInstance>();

        train.output_list = new ArrayList<IStructure>();
        test.output_list = new ArrayList<IStructure>();

        for (int i = 0; i < problem.input_list.size(); i++) {

            IInstance x = problem.input_list.get(i);
            IStructure y = problem.output_list.get(i);

            if (i < testStart || i >= testEnd) {
                train.input_list.add(x);
                train.output_list.add(y);
            } else {
                test.input_list.add(x);
                test.output_list.add(y);
            }

        }

        return new Pair<StructuredProblem, StructuredProblem>(train, test);
    }

}


class RealMeasureAverager implements PerformanceMeasureAverager<RealMeasure> {

    @Override
    public RealMeasure average(List<? extends PerformanceMeasure> m) {
        double sum = 0;
        for (PerformanceMeasure r : m) {
            sum += ((RealMeasure) r).d;
        }

        return new RealMeasure(sum / m.size());
    }
}
