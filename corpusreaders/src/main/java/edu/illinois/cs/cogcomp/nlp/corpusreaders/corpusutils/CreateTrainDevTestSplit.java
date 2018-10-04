/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.XmlTextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREEventReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Divides a corpus into train, dev, and test splits according to source documents, trying to balance
 *    the numbers and types of annotations.
 *
 * compute target counts for specified split proportions.
 * split by two relevant lower-frequency characteristics, sample from the resulting sets randomly.
 * Try n subsets, compute diff, pick best.

 * The main() method takes as argument an EreCorpus value (enumeration), which correspond to different ERE
 *    releases -- each of which has differences in annotation standards and/or directory structure.
 *    Here are the relevant values:
 *
 * LDC2015E29_DEFT_Rich_ERE_English_Training_Annotation_V2: ENR1
 * LDC2015E68_DEFT_Rich_ERE_English_Training_Annotation_R2_V2: ENR2
 * LDC2016E31_DEFT_Rich_ERE_English_Training_Annotation_R3: ENR3
 *
 * LDC2015E107_DEFT_Rich_ERE_Spanish_Annotation_V1: ESR1 (ENR2)
 * LDC2016E34_DEFT_Rich_ERE_Spanish_Annotation_R2: ESR2 (ENR3)
 *
 * LDC2015E105_DEFT_Rich_ERE_Chinese_Training_Annotation: ZHR1 (ENR2)
 * LDC2015E112_DEFT_Rich_ERE_Chinese_Training_Annotation_R2: ZHR2 (ENR3)
 */

public class CreateTrainDevTestSplit {
    private static final String NAME = CreateTrainDevTestSplit.class.getCanonicalName();

    public enum Split {TRAIN, DEV, TEST}

    private class QueueElement implements Comparable<QueueElement> {
        final double score;
        final Set<String> docIdSet;
        final Counter<String> labelCounter;

        public QueueElement(double score, Set<String> ids, Counter<String> counts) {
            this.score = score;
            this.docIdSet = ids;
            this.labelCounter = counts;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         * <p>
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
         * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
         * <tt>y.compareTo(x)</tt> throws an exception.)
         * <p>
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         * <p>
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
         * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         * <p>
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         * <p>
         * <p>In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(QueueElement o) {
            return Double.compare(this.score, o.score);
        }
    }

    private class PairQueueElement implements Comparable<PairQueueElement> {
        final double score;
        final String label;

        public PairQueueElement(double score, String label) {
            this.score = score;
            this.label = label;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         * <p>
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
         * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
         * <tt>y.compareTo(x)</tt> throws an exception.)
         * <p>
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         * <p>
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
         * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         * <p>
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         * <p>
         * <p>In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(PairQueueElement o) {
            return Double.compare(this.score, o.score);
        }
    }

    /** keep this number of candidates for best split */
    private final int NUM_TRIALS;

//    private Random random;
    private final double LARGE_DIFF = 1000000000.0;
    private Logger logger = LoggerFactory.getLogger(CreateTrainDevTestSplit.class);
    private Map<Split, Counter<String>> bestRelSplitCounts;

/** if 'true', randomize selection of examples at each step. set 'false' for debugging/testing. */
    private boolean doRandomize;

    private LabelCountExtractor labelCountExtractor;

    /** a map from data example ID to the counts of target characteristics of that example */
    private Map<String, Counter<String>> exampleLabelCounts;

//    private EvictingQueue<QueueElement> bestCandidateSplits;
    /** total counts for each target characteristic of examples in data set */
    private Counter<String> labelTotals;

    public CreateTrainDevTestSplit(LabelCountExtractor labelExtractor) {
        this(labelExtractor, 10, true);
    }


    public CreateTrainDevTestSplit(LabelCountExtractor labelExtractor, int numTrials, boolean doRandomize) {
        this.labelCountExtractor = labelExtractor;
        this.NUM_TRIALS = numTrials;
        this.doRandomize = doRandomize;
//        this.random = new Random();
        labelTotals = new Counter<>();
        exampleLabelCounts = new HashMap<>();
        bestRelSplitCounts = new HashMap<>();

        exampleLabelCounts = labelExtractor.getLabelCounts();
        labelTotals = labelExtractor.getLabelTotals();
    }


    private static String printCounts(Counter<String> counter) {
        StringBuilder bldr = new StringBuilder();
        for (String key : counter.keySet()) {
            bldr.append(key).append(": ").append(counter.getCount(key)).append("; ");
        }
        return bldr.toString();
    };

    public Map<String,Counter<String>> getExampleLabelCounts() {
        return exampleLabelCounts;
    }

    public Counter<String> getLabelTotals() {
        return labelTotals;
    }

    public Map<Split,Counter<String>> getBestRelSplitCounts() {
        return bestRelSplitCounts;
    }

    /**
     * expects dev < test < train
     * find non-overlapping subsets of devFrac, testFrac, and trainFrac documents that each approximate the
     *      target distribution, generate sets of corresponding ids
     * the three parameter values should sum to 1.0. All three are specified for clarity.
     * @param trainFrac
     * @param devFrac
     * @param testFrac
     * @return
     */
    public Map<Split, Set<String>> getSplits(double trainFrac, double devFrac, double testFrac) {
        Map<Split, Set<String>> splitDocIdSets = new HashMap<>();

        if (Math.abs(1.0 - trainFrac - devFrac - testFrac) > 0.001)
            throw new IllegalArgumentException("trainFrac, devFrac, and testFrac must sum to 1.0.");

        Set<String> availIds = new HashSet<>();
        availIds.addAll(this.exampleLabelCounts.keySet());

        Pair<Set<String>, Counter<String>> dev = getBestSplit(devFrac, availIds);
        splitDocIdSets.put(Split.DEV, dev.getFirst());
        bestRelSplitCounts.put(Split.DEV, dev.getSecond());
        availIds.removeAll(dev.getFirst());

        Pair<Set<String>, Counter<String>> test = getBestSplit(testFrac, availIds);
        splitDocIdSets.put(Split.TEST, test.getFirst());
        bestRelSplitCounts.put(Split.TEST, test.getSecond());
        availIds.removeAll(test.getFirst());

        Counter<String> trainCounts = computeLabelCounts(availIds);
        splitDocIdSets.put(Split.TRAIN, availIds);
        bestRelSplitCounts.put(Split.TRAIN, trainCounts);
        return splitDocIdSets;
    }



    private Counter<String> computeLabelCounts(Set<String> availIds) {
        Counter<String> counts = new Counter<>();

        for (String docId : availIds) {
            Counter<String> labelCount = exampleLabelCounts.get(docId);
            for (String label : labelCount.keySet())
                counts.incrementCount(label, labelCount.getCount(label));
        }

        return counts;
    }


    /**
     * sample without replacement the available ids a set of  (frac, 1-frac), trying to match the
     *    proportions of labels indicated.
     *
     * @param availIds ids to apportion
     * iterate over candidate sets of documents; find smallest diff of relation counts with target counts.
     * for larger data sets, splits in to blocks of specified size and performs the split in each,
     *    then concatenate the results.
     */
    private Pair<Set<String>, Counter<String>> getBestSplit(double frac, Set<String> availIds) {

        Set<String> bestSplit = new HashSet<>();
        Counter<String> splitCount = null;

        if (frac < 0.01)
            return new Pair(bestSplit, splitCount);

        Counter<String> targetCounts = labelCountExtractor.findTargetCounts(frac);

        double bestDiff = LARGE_DIFF;

        // Pick a dimension to split on -- say, the one with the lowest count (and therefore the most
        //   likely to be proportionally imbalanced in random split)

        List<String> targetSplitOrder = getTargetSplitOrder(targetCounts);

        //TODO: have cost weight infrequent labels more highly.
        Map<String, Double> weights = setTargetWeights(targetSplitOrder, targetCounts);

        // then try sampling randomly a few times, keeping the best split in terms of distance
        //   from target count for the highest priority target feature(s)

        for (int i = 0; i < NUM_TRIALS && bestDiff > 0 ; ++i) {

            Pair<Set<String>, Counter<String>> splitAndCount = getRandomSplit(availIds, targetCounts, targetSplitOrder);
            Set<String> splitIds = splitAndCount.getFirst();
            Counter<String> labelCount = splitAndCount.getSecond(); //
            double cost = computeCountDiff(labelCount, targetCounts, weights);
            logger.debug("best prior diff: {}; current diff: {}", bestDiff, cost);

            if (cost < bestDiff) {
                bestSplit = splitIds;
                splitCount = labelCount;
                bestDiff = cost;
            }
        }

        return new Pair(bestSplit, splitCount);
    }

    private Map<String, Double> setTargetWeights(List<String> targetSplitOrder, Counter<String> targetCounts) {
        double[] fractions = new double[targetSplitOrder.size()];
        double total = targetCounts.getTotal();
        double inverseTotal = 0;

        for (int i = 0; i < fractions.length; ++i) {

            double count = targetCounts.getCount(targetSplitOrder.get(i));
            fractions[i] = total / ((0 == count) ? 1 : count);
            inverseTotal += fractions[i];
        }

        Map<String, Double> weights = new HashMap<>();

        for (int i = 0; i < fractions.length; ++i) {
            fractions[i] /= inverseTotal;
            weights.put(targetSplitOrder.get(i), fractions[i]);
        }

        return weights;
    }

    /**
     * return a subset of availIds of size frac * availIds.size(), trying to balance the proportion of
     *    labels in priority order indicated by targetSplitOrder. First cut: just the first entry in
     *    targetSplitOrder.
     *    Second cut: first, select examples that have only the current target active and split those.
     *      the remaining examples can't affect these counts; so compute the counts of the next target in the first selected set,
     *      and select examples to augment the first set based on adjusting the overall proportion and the second target simultaneously
     *
     * @param availIds a set of example IDs to be divided
     * @param targetCounts desired counts for labels in focus of split (the set of ids returned)
     * @param targetSplitOrder ordered list of target labels to balance in the split
     * @return set of ids in proposed split, plus corresponding label counts
     */
    private Pair<Set<String>, Counter<String>> getRandomSplit(Set<String> availIds, Counter<String> targetCounts,
                                                              List<String> targetSplitOrder) {

        String firstTarget = targetSplitOrder.get(0);

        Set<String> splitIds = new HashSet<>();
        List<String> relevantIdList = getRelevantExamples(availIds, firstTarget);

        double targetCount = targetCounts.getCount(firstTarget);
        double currentCount = 0;

        for (int index = 0; index < relevantIdList.size(); ++index) {

            String id = relevantIdList.get(index);
            currentCount += this.exampleLabelCounts.get(id).getCount(firstTarget);
            splitIds.add(id);

            if (currentCount >= targetCount)
                break;
        }

        Counter<String> splitCounts = computeLabelCounts(splitIds);
        Pair<Set<String>, Counter<String>> splitInfo = new Pair(splitIds, splitCounts);

        // recursive step:
        if (targetSplitOrder.size() > 1) {
            Set<String> newAvailIds = new HashSet<>(availIds);
            // don't touch any examples that had relevant target
            newAvailIds.removeAll(relevantIdList);

            if (!newAvailIds.isEmpty()) {

                List<String> newTargetSplitOrder = targetSplitOrder.subList(1, targetSplitOrder.size());
                Counter<String> adjustedTargetCounts = targetCounts.copy();
                decrementCounts(adjustedTargetCounts, splitCounts);

                Pair<Set<String>, Counter<String>> recurseSplitInfo =
                        getRandomSplit(newAvailIds, adjustedTargetCounts, newTargetSplitOrder);

                splitIds.addAll(recurseSplitInfo.getFirst());
                incrementCounts(splitCounts, recurseSplitInfo.getSecond());
            }
            // else base case: all examples are covered by the set of labels we considered.
        }
        // base case: targetSplitOrder had only one entry and we split with it.
        return splitInfo;
    }

    private void incrementCounts(Counter<String> origCounts, Counter<String> addCounts) {
        updateCounter(origCounts, addCounts, true);
    }

    private void decrementCounts(Counter<String> origCounts, Counter<String> subtractCounts) {
        updateCounter(origCounts, subtractCounts, false);
    }

    private List<String> getRelevantExamples(Set<String> availIds, String targetLabel) {

        List<String> relevantIdList = new ArrayList<>();

        for (String availId: availIds)
            if (this.exampleLabelCounts.get(availId).getCount(targetLabel) > 0)
                relevantIdList.add(availId);

        if (doRandomize)
            Collections.shuffle(relevantIdList);

        return relevantIdList;
    }

    private List<String> getTargetSplitOrder(Counter<String> targetCounts) {
        PriorityQueue<PairQueueElement> queue = new PriorityQueue();
        for (String label : targetCounts.keySet()) {
            queue.add(new PairQueueElement(targetCounts.getCount(label), label));
        }
        List<String> orderedTargets = new ArrayList<>(targetCounts.size());
        PairQueueElement pqe = null;
        while (null != (pqe = queue.poll())) {
            orderedTargets.add(pqe.label);
        }

        return orderedTargets;
    }

    private void updateCounter(Counter<String> origCount, Counter<String> changeCounts, boolean isIncrement) {
        for (String key : changeCounts.keySet()) {
            if (isIncrement)
                origCount.incrementCount(key, changeCounts.getCount(key));
            else
                origCount.decrementCount(key, changeCounts.getCount(key));
        }
    }

//    private PriorityQueue<QueueElement> trimQueue(PriorityQueue<QueueElement> splits) {
//        PriorityQueue<QueueElement> trimmedQueue = new PriorityQueue<>(new TargetComparator);
//
//        for( int i = 0; i < BEAM_SIZE && !splits.isEmpty(); ++i)
//            trimmedQueue.add(splits.poll());
//
//        return trimmedQueue;
//    }


    /**
     * compute weighted sum of absolute difference of counts
     *
     * @param stringCounter counts from some subset of documents
     * @param targetCounts desired counts based on proportion of data desired
     * @param weights weights for targets for cost computation
     * @return value of difference
     */
    private double computeCountDiff(Counter<String> stringCounter, Counter<String> targetCounts, Map<String, Double> weights) {
        double accum = 0;
        for (String label : targetCounts.keySet()) {
            double count = 0;
            double weight = 0;
            double targetCount = targetCounts.getCount(label);
            if ( stringCounter.contains(label) ) {
                count = stringCounter.getCount(label);
                weight = weights.get(label);
            }
            accum += weight * Math.abs(count - targetCount);
        }
        return accum;
    }





    /**
     * split an ERE corpus with 0.7/0.1/0.2 train/dev/test proportions, trying to balance
     *    all (or at least, lowest frequency) type count.
     *
     * @param args
     */
    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: " + NAME + " EreCorpusType corpusDir splitDir");
            System.exit(-1);
        }

        EREDocumentReader.EreCorpus ereCorpus = EREDocumentReader.EreCorpus.valueOf(args[0]);
        String corpusRoot = args[1];
        String outDir = args[2];

        ResourceManager fullRm = new CorpusSplitConfigurator().getDefaultConfig();

        boolean throwExceptionOnXmlParserFail = false;

        double trainFrac = fullRm.getDouble(CorpusSplitConfigurator.TRAIN_FRACTION.key);
        double devFrac = fullRm.getDouble(CorpusSplitConfigurator.DEV_FRACTION.key);
        double testFrac = fullRm.getDouble(CorpusSplitConfigurator.TEST_FRACTION.key);

//        Path corpusPath = Paths.get(corpusRoot);
//        String corpusName = corpusPath.getName(corpusPath.getNameCount() - 2).toString();

        IOUtils.mkdir(outDir);

        String outFileStem = outDir + "/";
        String[] viewNames = fullRm.getCommaSeparatedValues(CorpusSplitConfigurator.VIEWS_TO_CONSIDER.key);//{ViewNames.EVENT_ERE};
        String[] labelsToCount = {};


        EREMentionRelationReader reader = null;
        try {
            reader = new EREEventReader(ereCorpus, corpusRoot, throwExceptionOnXmlParserFail);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Map<String, XmlTextAnnotation> ereTas = new HashMap<>();
        Map<String, Set<View>> ereViews = new HashMap<>();

        while (reader.hasNext()) {
            XmlTextAnnotation xmlTextAnnotation = reader.next();
            ereTas.put(xmlTextAnnotation.getTextAnnotation().getId(), xmlTextAnnotation);
            Set<View> views = new HashSet<>();
            TextAnnotation ta = xmlTextAnnotation.getTextAnnotation();

            for (String viewName : viewNames)
                if (ta.hasView(viewName))
                    views.add(ta.getView(viewName));

            ereViews.put(ta.getId(), views);
        }
        TextAnnotationLabelCounter lce = new TextAnnotationLabelCounter(labelsToCount.length == 0, labelsToCount, ereViews);

        CreateTrainDevTestSplit creator = new CreateTrainDevTestSplit(lce);

        Map<Split, Set<String>> splits = creator.getSplits(trainFrac, devFrac, testFrac);
        Map<Split, Counter<String>> splitCounts = creator.getBestRelSplitCounts();

        Map<String, Counter<String>> counts = creator.getExampleLabelCounts();

        List<String> outLines = new ArrayList<>(splitCounts.size() + 2);
        for (String docId : counts.keySet()) {
            outLines.add(docId + ": " + printCounts(counts.get(docId)));
        }

        for (Split s : splitCounts.keySet()) {
            outLines.add(s.name() + ": " + printCounts(splitCounts.get(s)));
        }

        Counter<String> totalLabelCounts = creator.getLabelTotals();
        outLines.add("TOTALS: " + printCounts(totalLabelCounts));

        try {
            LineIO.write(outFileStem + "countInfo.txt", outLines);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for (Split s : splits.keySet()) {
            List<String> ids = new ArrayList<>(splits.get(s));
            try {
                LineIO.write(outFileStem + s.name() + ".txt", ids);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

}
