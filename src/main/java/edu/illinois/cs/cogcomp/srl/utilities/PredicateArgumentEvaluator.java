package edu.illinois.cs.cogcomp.srl.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import edu.illinois.cs.cogcomp.core.experiments.EvaluationRecord;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.edison.data.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.edison.sentences.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class PredicateArgumentEvaluator {

	public static void evaluateSense(PredicateArgumentView gold,
			PredicateArgumentView prediction, ClassificationTester tester) {
		Map<Constituent, Constituent> goldToPredictionPredicateMapping = getGoldToPredictionPredicateMapping(
				gold, prediction);

		for (Constituent gp : gold.getPredicates()) {

			if (goldToPredictionPredicateMapping.containsKey(gp)) {

				Constituent pp = goldToPredictionPredicateMapping.get(gp);

				String goldSense = gp
						.getAttribute(CoNLLColumnFormatReader.SenseIdentifer);

				// XXX: As in training, all predicates that are labeled as XX
				// are
				// marked as 01
				if (goldSense.equals("XX"))
					goldSense = "01";

				String predSense = pp
						.getAttribute(CoNLLColumnFormatReader.SenseIdentifer);
				
				assert predSense != null;

				tester.record(goldSense, predSense);
			}

		}

	}

	/**
	 * This function emulates the standard SRL evaluation script. The treatment
	 * of C-Args in the original script is non-intuitive, but has been
	 * replicated here.
	 */
	public static void evaluate(PredicateArgumentView gold,
			PredicateArgumentView prediction, ClassificationTester tester) {

		Map<Constituent, Constituent> goldToPredictionPredicateMapping = getGoldToPredictionPredicateMapping(
				gold, prediction);

		for (Constituent gp : gold.getPredicates()) {
			if (!goldToPredictionPredicateMapping.containsKey(gp)) {

				// if there is no matching prediction, then, we have a recall
				// problem for the label "V".
				tester.recordGoldOnly("V");

				// Should the argument classifier be penalized for this? I would
				// say no, because the argument classifier is not even allowed
				// to decide on the arguments of this predicate.

				continue;
			}

			Constituent pp = goldToPredictionPredicateMapping.get(gp);

			// Map<IntPair, String> goldLabels = getArgumentLabels(gold, gp);
			// Map<IntPair, String> predictedLabels = getArgumentLabels(
			// prediction, pp);

			Map<IntPair, Record> goldLabels = getArgumentMap(gold, gp);

			Map<IntPair, Record> predictedLabels = getArgumentMap(prediction,
					pp);

			Set<IntPair> goldDone = new HashSet<IntPair>();

			for (IntPair predictedSpan : predictedLabels.keySet()) {

				Record p = predictedLabels.get(predictedSpan);
				Record g = goldLabels.get(predictedSpan);

				if (g == null) {
					tester.recordPredictionOnly(p.baseLabel);
					continue;
				}

				Map<IntPair, String> gComponents = g.components;
				Map<IntPair, String> pComponents = p.components;

				assert gComponents != null;
				assert pComponents != null;

				if (gComponents.size() == 1 && pComponents.size() == 1) {
					tester.record(g.baseLabel, p.baseLabel);
					goldDone.add(predictedSpan);
				} else if (gComponents.size() > 1 && pComponents.size() == 1) {
					// this is a strange thing abotu the standard evaluation
					// script. If the gold label contains a C-arg and the
					// predicted label doesn't, then the script counts ONE
					// overprediction (Even if the C-args and the arg of hte
					// gold label together form the same span as the
					// prediction.)
					tester.recordPredictionOnly(p.baseLabel);
				} else if (gComponents.size() == 1 && pComponents.size() > 1) {
					// same as above!
					tester.recordPredictionOnly(p.baseLabel);
				} else {

					if (p.baseLabel.startsWith("AM")) {

						Set<IntPair> set = new HashSet<IntPair>();
						set.addAll(gComponents.keySet());
						set.addAll(pComponents.keySet());

						for (IntPair s : set) {
							String gLabel = gComponents.get(s);
							String pLabel = pComponents.get(s);

							if (gLabel != null && pLabel != null)
								tester.record(gLabel, pLabel);
							else if (gLabel == null)
								tester.recordPredictionOnly(pLabel);
							else if (pLabel == null)
								tester.recordGoldOnly(gLabel);
						}

						goldDone.add(predictedSpan);

					} else {

						// all spans should be correct!
						boolean allOK = p.baseLabel.equals(g.baseLabel);
						Set<IntPair> goldSpansLeft = new HashSet<IntPair>(
								gComponents.keySet());
						for (IntPair pSpan : pComponents.keySet()) {
							if (gComponents.containsKey(pSpan))
								goldSpansLeft.remove(pSpan);
							else {
								allOK = false;
								break;
							}
						}

						if (allOK) {
							tester.record(g.baseLabel, p.baseLabel);
							goldDone.add(predictedSpan);
						} else {
							tester.recordPredictionOnly(p.baseLabel);
						}
					}
				}
			}

			for (IntPair gSpan : goldLabels.keySet()) {
				if (!goldDone.contains(gSpan))
					tester.recordGoldOnly(goldLabels.get(gSpan).baseLabel);
			}

		}

	}

	private static class Record {
		int start, end;

		String baseLabel;

		Map<IntPair, String> components = new HashMap<IntPair, String>();

		Record(int start, int end, String base) {
			this.start = start;
			this.end = end;
			baseLabel = base;

			components.put(new IntPair(start, end), baseLabel);
		}

		@Override
		public String toString() {
			return "Record [start=" + start + ", end=" + end + ", baseLabel="
					+ baseLabel + ", components=" + components + "]";
		}
	}

	/**
	 * This is an annoying function to write. It is probably VERY inefficient
	 * too...
	 */
	public static Map<IntPair, Record> getArgumentMap(
			PredicateArgumentView view, Constituent predicate) {

		// Map<String, Record> records = new HashMap<String,
		// PredicateArgumentEvaluator.Record>();

		Set<IntPair> spans = new HashSet<IntPair>();

		List<Pair<String, Constituent>> output = new ArrayList<Pair<String, Constituent>>();
		for (Relation r : view.getArguments(predicate)) {
			Constituent target = r.getTarget();
			output.add(new Pair<String, Constituent>(r.getRelationName(),
					target));

			if (spans.contains(target.getSpan()))
				System.out.println("Error! Overlapping spans in "
						+ view.getViewName() + "\n" + view.getTextAnnotation()
						+ "\n" + view);

			spans.add(target.getSpan());
		}

		Collections.sort(output, new Comparator<Pair<String, Constituent>>() {

			public int compare(Pair<String, Constituent> arg0,
					Pair<String, Constituent> arg1) {
				return TextAnnotationUtilities.constituentStartComparator
						.compare(arg0.getSecond(), arg1.getSecond());
			}
		});

		List<Record> records = new ArrayList<PredicateArgumentEvaluator.Record>();
		// add a label for the verb first
		Record vRecord = new Record(predicate.getStartSpan(),
				predicate.getEndSpan(), "V");
		records.add(vRecord);

		Map<String, Record> recordsSoFar = new HashMap<String, PredicateArgumentEvaluator.Record>();
		recordsSoFar.put("V", vRecord);

		for (Pair<String, Constituent> pair : output) {

			Constituent c = pair.getSecond();

			String label = pair.getFirst().replaceAll("Support", "SUP");

			if (label.startsWith("C-")) {
				String baseLabel = label.replaceAll("C-", "");
				if (recordsSoFar.containsKey(baseLabel)) {
					Record record = recordsSoFar.get(baseLabel);

					record.start = Math.min(c.getStartSpan(), record.start);
					record.end = Math.max(c.getEndSpan(), record.end);
					assert record.baseLabel.equals(baseLabel);
					record.components.put(c.getSpan(), label);

				} else {
					// a dangling C-arg. This should never happen, but one never
					// knows. Simply treat this C-arg as arg.

					Record record = new Record(c.getStartSpan(),
							c.getEndSpan(), baseLabel);
					recordsSoFar.put(baseLabel, record);
					records.add(record);
				}
			} else {

				Record record = new Record(c.getStartSpan(), c.getEndSpan(),
						label);
				recordsSoFar.put(label, record);
				records.add(record);
			}

		}

		Map<IntPair, Record> map = new HashMap<IntPair, PredicateArgumentEvaluator.Record>();

		for (Record rec : records) {
			map.put(new IntPair(rec.start, rec.end), rec);
		}

		// System.out.println("PredicateArgumentEvaluator.getArgumentMap()");
		// System.out.println(predicate);
		//
		// for (IntPair pair : map.keySet()) {
		// Record m = map.get(pair);
		//
		// System.out.println(pair + ": " + m.baseLabel + "\t" + m.components);
		// }

		return map;
	}

	private static Map<Constituent, Constituent> getGoldToPredictionPredicateMapping(
			PredicateArgumentView gold, PredicateArgumentView prediction) {
		Map<Constituent, Constituent> goldToPredictionPredicateMapping = new HashMap<Constituent, Constituent>();

		for (Constituent gp : gold.getPredicates()) {

			boolean found = false;
			for (Constituent pp : prediction.getPredicates()) {
				if (gp.getSpan().equals(pp.getSpan())) {
					goldToPredictionPredicateMapping.put(gp, pp);
					found = true;
					break;
				}
			}

			if (!found) {
				// System.out.println("Predicate " + gp + " not found");
				// System.out.println("Gold: " + gold);
				// System.out.println("Pred: " + prediction);
				// assert false;
			}

		}
		return goldToPredictionPredicateMapping;
	}

	public static void addToResultCache(String file, TextAnnotation ta,
			ClassificationTester tester) throws IOException {
		int hash = ta.getTokenizedText().hashCode();

		EvaluationRecord record = tester.getEvaluationRecord();

		List<String> l = new ArrayList<String>();
		l.add(hash + "\tAll\t" + record.getGoldCount() + "\t"
				+ record.getPredictedCount() + "\t" + record.getCorrectCount());

		List<String> labels = new ArrayList<String>(tester.getLabels());
		Collections.sort(labels);

		for (String label : labels) {
			record = tester.getEvaluationRecord(label);

			l.add(hash + "\t" + label + "\t" + record.getGoldCount() + "\t"
					+ record.getPredictedCount() + "\t"
					+ record.getCorrectCount());

		}

		LineIO.append(file, l);
	}

	public static Map<Integer, Map<String, EvaluationRecord>> loadResultCache(
			String file) throws FileNotFoundException {
		Map<Integer, Map<String, EvaluationRecord>> records = new HashMap<Integer, Map<String, EvaluationRecord>>();
		Scanner scanner = new Scanner(new File(file));

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			String[] parts = line.split("\t");
			assert parts.length == 5;

			int taId = Integer.parseInt(parts[0]);
			String label = parts[1];
			int goldCount = Integer.parseInt(parts[2]);
			int predCount = Integer.parseInt(parts[3]);
			int correctCount = Integer.parseInt(parts[4]);

			Map<String, EvaluationRecord> map;

			if (records.containsKey(taId))
				map = records.get(taId);
			else {
				map = new HashMap<String, EvaluationRecord>();
				records.put(taId, map);
			}
			EvaluationRecord record = new EvaluationRecord();
			map.put(label, record);

			record.incrementCorrect(correctCount);
			record.incrementGold(goldCount);
			record.incrementPredicted(predCount);

		}

		scanner.close();

		return records;
	}

}
