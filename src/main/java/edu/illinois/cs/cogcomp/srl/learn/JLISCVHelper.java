package edu.illinois.cs.cogcomp.srl.learn;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.core.StructuredProblem;
import edu.illinois.cs.cogcomp.sl.inference.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;
import edu.illinois.cs.cogcomp.srl.learn.CrossValidationHelper.DatasetSplitter;
import edu.illinois.cs.cogcomp.srl.learn.CrossValidationHelper.PerformanceMeasureAverager;
import edu.illinois.cs.cogcomp.srl.learn.CrossValidationHelper.Tester;
import edu.illinois.cs.cogcomp.srl.learn.CrossValidationHelper.Trainer;
import edu.illinois.cs.cogcomp.srl.learn.JLISCVHelper.RealMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class JLISCVHelper {

	private final static Logger log = LoggerFactory
			.getLogger(JLISCVHelper.class);

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

	public static LearnerParameters cvSSVMSerial(
			AbstractInferenceSolver[] inference,
			SLProblem sp, Tester<SLProblem> evaluator,
			int nFolds) throws Exception {

		log.info("Cross validation for struct SVM");
		int[] structSplits = getSplitLocations(nFolds, sp.size());

		StructureProblemSplitter splitter = new StructureProblemSplitter(
				structSplits);

		CrossValidationHelper<SLProblem> cvHelper = new CrossValidationHelper<SLProblem>(
				nFolds, inference, new RealMeasureAverager(), splitter,
				new SSVMTrainer(), evaluator);

		List<LearnerParameters> params = new ArrayList<LearnerParameters>();

		for (int i = -8; i < 0; i++) {
			params.add(LearnerParameters.getSSVMParams(Math.pow(2d, i)));
		}

		LearnerParameters learnerParameters = cvHelper.doCV(sp, params, false);

		return learnerParameters;
	}

	public static LearnerParameters cvSSVM(
			AbstractInferenceSolver[] inference,
			SLProblem sp, Tester<SLProblem> evaluator,
			int nThreads, int nFolds) throws Exception {

		log.info("Cross validation for struct SVM");
		int[] structSplits = getSplitLocations(nFolds, sp.size());

		StructureProblemSplitter splitter = new StructureProblemSplitter(
				structSplits);

		CrossValidationHelper<SLProblem> cvHelper = new CrossValidationHelper<SLProblem>(
				nFolds, inference, new RealMeasureAverager(), splitter,
				new SSVMTrainer(), evaluator);

		List<LearnerParameters> params = new ArrayList<LearnerParameters>();

		for (int i = -8; i < 0; i++) {
			params.add(LearnerParameters.getSSVMParams(Math.pow(2d, i)));
		}

		LearnerParameters learnerParameters = cvHelper.doCV(sp, params);

		return learnerParameters;
	}

}

class SSVMTrainer implements Trainer<SLProblem> {

	@Override
	public WeightVector train(SLProblem dataset,
			LearnerParameters params,
			AbstractInferenceSolver[] inference) throws Exception {
		return JLISLearner.trainStructSVM(inference, dataset,
				params.getcStruct());
	}

}

abstract class SingleListDatasetSplitter<DatasetType> implements
		DatasetSplitter<DatasetType> {
	protected final int[] splitIds;

	public SingleListDatasetSplitter(int[] splitIds) {
		this.splitIds = splitIds;
	}
}

class StructureProblemSplitter extends
		SingleListDatasetSplitter<SLProblem> {

	public StructureProblemSplitter(int[] splitIds) {
		super(splitIds);
	}

	public Pair<SLProblem, SLProblem> getFoldData(
			SLProblem problem, int foldId) {

		int testStart = splitIds[foldId];
		int testEnd = splitIds[foldId + 1];

		SLProblem train = new SLProblem();
		SLProblem test = new SLProblem();

		train.instanceList = new ArrayList<IInstance>();
		test.instanceList = new ArrayList<IInstance>();

		train.goldStructureList = new ArrayList<IStructure>();
		test.goldStructureList = new ArrayList<IStructure>();

		for (int i = 0; i < problem.instanceList.size(); i++) {

			IInstance x = problem.instanceList.get(i);
			IStructure y = problem.goldStructureList.get(i);

			if (i < testStart || i >= testEnd) {
				train.instanceList.add(x);
				train.goldStructureList.add(y);
			} else {
				test.instanceList.add(x);
				test.goldStructureList.add(y);
			}

		}

		return new Pair<SLProblem, SLProblem>(train, test);
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
