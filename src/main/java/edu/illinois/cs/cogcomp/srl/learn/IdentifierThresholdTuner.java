package edu.illinois.cs.cogcomp.srl.learn;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.math.Permutations;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.sl.core.StructuredProblem;
import edu.illinois.cs.cogcomp.srl.core.ArgumentIdentifier;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassInstance;
import edu.illinois.cs.cogcomp.srl.jlis.SRLMulticlassLabel;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class IdentifierThresholdTuner {

	protected final double n_F;
	private final int nThreads;
	private final SRLManager manager;
	private final StructuredProblem problem;

	public IdentifierThresholdTuner(SRLManager manager, double N_F,
			StructuredProblem problem) {
		this.manager = manager;

		this.n_F = N_F;
		this.problem = problem;

		this.nThreads = Math.min(8, Runtime.getRuntime().availableProcessors());

	}

	protected double fN(double precision, double recall, double n) {
		double denom = n * n * precision + recall;
		double num = (n * n + 1) * precision * recall;

		if (denom == 0)
			return 0;

		return num / denom;

	}

	public Pair<Double, Double> tuneIdentifierScale(List<Double> A,
			List<Double> B) throws Exception {

		List<Pair<Double, Boolean>> scores = new ArrayList<Pair<Double, Boolean>>();

		int totalGold = 0;
		int numExamples = 0;
		ArgumentIdentifier rawScorer = new ArgumentIdentifier(1.0, 0.0, manager);

		for (int i = 0; i < this.problem.size(); i++) {

			SRLMulticlassInstance x = (SRLMulticlassInstance) this.problem.input_list
					.get(i);
			SRLMulticlassLabel y = (SRLMulticlassLabel) this.problem.output_list
					.get(i);

			assert y.getLabel() == 0 || y.getLabel() == 1;

			boolean goldLabel = y.getLabel() == 1;

			if (goldLabel)
				totalGold++;

			double rawScore = rawScorer.getIdentifierRawScore(x);

			scores.add(new Pair<Double, Boolean>(rawScore, goldLabel));
			numExamples++;
			if (numExamples % 10000 == 0) {
				System.out.println(numExamples + " scores cached");
			}
		}

		Map<Pair<Double, Double>, IntPair> perf = getPerformance(A, B, scores);

		List<Pair<String, Double>> list = new ArrayList<Pair<String, Double>>();

		double maxF = Double.NEGATIVE_INFINITY;
		Pair<Double, Double> maxer = null;

		// System.out
		// .println("(A, B)\ttotalGold\ttotalPredicted\tcorrect\tP\tR\tF"
		// + n_F);

		for (Entry<Pair<Double, Double>, IntPair> entry : perf.entrySet()) {
			Pair<Double, Double> key = entry.getKey();
			IntPair value = entry.getValue();

			double totalPredicted = value.getFirst();
			double correct = value.getSecond();

			double precision = 0, recall = 0, f = 0;

			if (totalPredicted > 0)
				precision = correct / totalPredicted;

			if (totalGold > 0)
				recall = correct / totalGold;

			f = fN(precision, recall, n_F);

			String output = key.toString();
			output += "\t" + (int) (totalGold);
			output += "\t" + (int) (totalPredicted);
			output += "\t" + (int) (correct);

			output += "\t"
					+ StringUtils.getFormattedTwoDecimal(precision * 100);
			output += "\t" + StringUtils.getFormattedTwoDecimal(recall * 100);
			output += "\t" + StringUtils.getFormattedTwoDecimal(f * 100);
			// System.out.println(output);

			list.add(new Pair<String, Double>(output, f));

			if (f > maxF) {
				maxF = f;
				maxer = key;
			}
		}

		System.out.println();
		System.out.println("Based on F" + n_F
				+ " measure, recommended (A, B) = " + maxer);

		System.out.println("Top 100 values");

		System.out
				.println("(A, B)\ttotalGold\ttotalPredicted\tcorrect\tP\tR\tF"
						+ n_F);

		Collections.sort(list, new Comparator<Pair<String, Double>>() {

			@Override
			public int compare(Pair<String, Double> arg0,
					Pair<String, Double> arg1) {
				return -arg0.getSecond().compareTo(arg1.getSecond());
			}
		});

		for (int i = 0; i < 100; i++) {
			System.out.println(list.get(i).getFirst());
		}

		return maxer;

	}

	@SuppressWarnings("unchecked")
	public Map<Pair<Double, Double>, IntPair> getPerformance(List<Double> A,
			List<Double> B, final List<Pair<Double, Boolean>> scores)
			throws InterruptedException, ExecutionException {

		ExecutorService executor = Executors.newFixedThreadPool(nThreads);

		List<FutureTask<Pair<Pair<Double, Double>, IntPair>>> tasks = new ArrayList<FutureTask<Pair<Pair<Double, Double>, IntPair>>>();

		for (List<Double> element : Permutations.crossProduct(Arrays.asList(A,
				B))) {

			final double a = element.get(0);
			final double b = element.get(1);
			FutureTask<Pair<Pair<Double, Double>, IntPair>> task = new FutureTask<Pair<Pair<Double, Double>, IntPair>>(
					new Callable<Pair<Pair<Double, Double>, IntPair>>() {

						@Override
						public Pair<Pair<Double, Double>, IntPair> call()
								throws Exception {
							return getPerformance(a, b, scores);
						}
					});

			tasks.add(task);
			executor.execute(task);

		}

		executor.shutdown();

		Map<Pair<Double, Double>, IntPair> map = new HashMap<Pair<Double, Double>, IntPair>();
		for (FutureTask<Pair<Pair<Double, Double>, IntPair>> task : tasks) {
			Pair<Pair<Double, Double>, IntPair> out = task.get();
			map.put(out.getFirst(), out.getSecond());
		}

		return map;
	}

	private Pair<Pair<Double, Double>, IntPair> getPerformance(double A,
			double B, List<Pair<Double, Boolean>> scores) {

		ArgumentIdentifier identifier = new ArgumentIdentifier(A, B, manager);

		int totalPredicted = 0;
		int totalCorrectTrue = 0;
		for (Pair<Double, Boolean> entry : scores) {
			double rawScore = entry.getFirst();
			boolean goldLabel = entry.getSecond();

			double scaledScore = identifier.scaleIdentifierScore(rawScore);

			boolean prediction = scaledScore >= 0;

			if (prediction) {
				totalPredicted++;
				if (goldLabel)
					totalCorrectTrue++;
			}

		}

		IntPair perf = new IntPair(totalPredicted, totalCorrectTrue);

		return new Pair<Pair<Double, Double>, IntPair>(
				new Pair<Double, Double>(A, B), perf);
	}
}