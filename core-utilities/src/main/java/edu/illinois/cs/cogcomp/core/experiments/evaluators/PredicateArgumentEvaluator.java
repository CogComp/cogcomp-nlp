/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments.evaluators;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Evaluator for the {@link PredicateArgumentView}. Compares two views and returns the results using
 * two {@link ClassificationTester}s: one for the sense of the predicate
 *
 * @author Vivek Srikumar
 */
public class PredicateArgumentEvaluator extends Evaluator {
    private static Logger logger = LoggerFactory.getLogger(PredicateArgumentEvaluator.class);

    PredicateArgumentView gold, prediction;
    Map<Constituent, Constituent> goldToPredictionPredicateMapping;

    public void evaluateSense(ClassificationTester senseTester, View goldView, View predictionView) {
        gold = (PredicateArgumentView) goldView;
        prediction = (PredicateArgumentView) predictionView;
        goldToPredictionPredicateMapping = getGoldToPredictionPredicateMapping();
        for (Constituent gp : gold.getPredicates()) {
            if (goldToPredictionPredicateMapping.containsKey(gp)) {
                Constituent pp = goldToPredictionPredicateMapping.get(gp);
                String goldSense = gp.getAttribute(PredicateArgumentView.SenseIdentifer);

                // XXX: As in training, all predicates that are labeled as XX are marked as 01
                if (goldSense.equals("XX"))
                    goldSense = "01";

                String predSense = pp.getAttribute(PredicateArgumentView.SenseIdentifer);

                assert predSense != null;
                senseTester.record(goldSense, predSense);
            }
        }
    }

    /**
     * This function emulates the standard SRL evaluation script. The treatment of C-Args in the
     * original script is non-intuitive, but has been replicated here.
     *
     * @param tester The multi-class {@link ClassificationTester} for the argument labels
     */
    public void evaluate(ClassificationTester tester, View goldView, View predictionView) {
        gold = (PredicateArgumentView) goldView;
        prediction = (PredicateArgumentView) predictionView;
        goldToPredictionPredicateMapping = getGoldToPredictionPredicateMapping();
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

            Map<IntPair, Record> goldLabels = getArgumentMap(gold, gp);
            Map<IntPair, Record> predictedLabels = getArgumentMap(prediction, pp);

            Set<IntPair> goldDone = new HashSet<>();

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
                    // this is a strange thing about the standard evaluation
                    // script. If the gold label contains a C-arg and the
                    // predicted label doesn't, then the script counts ONE
                    // over-prediction (Even if the C-args and the arg of the
                    // gold label together form the same span as the prediction.)
                    tester.recordPredictionOnly(p.baseLabel);
                } else if (gComponents.size() == 1 && pComponents.size() > 1) {
                    // same as above!
                    tester.recordPredictionOnly(p.baseLabel);
                } else {
                    if (p.baseLabel.startsWith("AM")) {
                        Set<IntPair> set = new HashSet<>();
                        set.addAll(gComponents.keySet());
                        set.addAll(pComponents.keySet());

                        for (IntPair s : set) {
                            String gLabel = gComponents.get(s);
                            String pLabel = pComponents.get(s);

                            if (gLabel != null && pLabel != null)
                                tester.record(gLabel, pLabel);
                            else if (gLabel == null)
                                tester.recordPredictionOnly(pLabel);
                            else
                                tester.recordGoldOnly(gLabel);
                        }
                        goldDone.add(predictedSpan);
                    } else {
                        // all spans should be correct!
                        boolean allOK = p.baseLabel.equals(g.baseLabel);
                        Set<IntPair> goldSpansLeft = new HashSet<>(gComponents.keySet());
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

        Map<IntPair, String> components = new HashMap<>();

        Record(int start, int end, String base) {
            this.start = start;
            this.end = end;
            baseLabel = base;

            components.put(new IntPair(start, end), baseLabel);
        }

        @Override
        public String toString() {
            return "Record [start=" + start + ", end=" + end + ", baseLabel=" + baseLabel
                    + ", components=" + components + "]";
        }
    }

    /**
     * This is an annoying function to write. It is probably VERY inefficient too...
     */
    private Map<IntPair, Record> getArgumentMap(PredicateArgumentView view, Constituent predicate) {
        Set<IntPair> spans = new HashSet<>();

        List<Pair<String, Constituent>> output = new ArrayList<>();
        for (Relation r : view.getArguments(predicate)) {
            Constituent target = r.getTarget();
            output.add(new Pair<>(r.getRelationName(), target));

            if (spans.contains(target.getSpan()))
                logger.error("Error! Overlapping spans in " + view.getViewName() + "\n"
                        + view.getTextAnnotation() + "\n" + view);

            spans.add(target.getSpan());
        }

        Collections.sort(output, new Comparator<Pair<String, Constituent>>() {

            public int compare(Pair<String, Constituent> arg0, Pair<String, Constituent> arg1) {
                return TextAnnotationUtilities.constituentStartComparator.compare(arg0.getSecond(),
                        arg1.getSecond());
            }
        });

        List<Record> records = new ArrayList<>();
        // add a label for the verb first
        Record vRecord = new Record(predicate.getStartSpan(), predicate.getEndSpan(), "V");
        records.add(vRecord);

        Map<String, Record> recordsSoFar = new HashMap<>();
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
                    // a dangling C-arg. This should never happen, but one never knows.
                    // Simply treat this C-arg as arg.
                    Record record = new Record(c.getStartSpan(), c.getEndSpan(), baseLabel);
                    recordsSoFar.put(baseLabel, record);
                    records.add(record);
                }
            } else {
                Record record = new Record(c.getStartSpan(), c.getEndSpan(), label);
                recordsSoFar.put(label, record);
                records.add(record);
            }
        }

        Map<IntPair, Record> map = new HashMap<>();

        for (Record rec : records) {
            map.put(new IntPair(rec.start, rec.end), rec);
        }
        return map;
    }

    private Map<Constituent, Constituent> getGoldToPredictionPredicateMapping() {
        Map<Constituent, Constituent> goldToPredictionPredicateMapping = new HashMap<>();

        for (Constituent gp : gold.getPredicates()) {
            for (Constituent pp : prediction.getPredicates()) {
                if (gp.getSpan().equals(pp.getSpan())) {
                    goldToPredictionPredicateMapping.put(gp, pp);
                    break;
                }
            }
        }
        return goldToPredictionPredicateMapping;
    }
}
