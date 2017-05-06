/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Divides a corpus into train, dev, and test splits according to source documents, trying to balance
 *    the numbers and types of annotations.
 *
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

    private static final int BEAM_SIZE = 100; //keep this number of candidates for best split

    private Logger logger = LoggerFactory.getLogger(CreateTrainDevTestSplit.class);

    private final double LARGE_DIFF = 1000000000.0;


    private final Set<String> labelsToConsider;
    private Map<Split, Counter<String>> bestRelSplitCounts;
    private boolean useAllLabels;
    private boolean stopEarly;

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

//    private EvictingQueue<QueueElement> bestCandidateSplits;


    public Map<String,Counter<String>> getLabelCounts() {
        return labelCounts;
    }

    public Counter<String> getLabelTotals() {
        return labelTotals;
    }

    public Map<Split,Counter<String>> getBestRelSplitCounts() {
        return bestRelSplitCounts;
    }

    public enum Split {TRAIN, DEV, TEST};

    private Map<String, Counter<String>> labelCounts;
    private Counter<String> labelTotals;


    /**
     * given views identified by source document, and a list of labels to consider, and a train/dev/test ratio,
     *    determine the best division of the corpus according to those constraints.
     * @param annotationViews map from doc id to set of views containing the annotations (constituents, relations)
     *                        that will be modeled.
     * @param labelsToConsider if not null, a subset of relation and constituent names in the view that should be
     */
    public CreateTrainDevTestSplit(Map<String, Set<View>> annotationViews, String[] labelsToConsider) {

        useAllLabels = (labelsToConsider.length == 0) ? true : false;
        labelTotals = new Counter<>();
        labelCounts = new HashMap<>();
        bestRelSplitCounts = new HashMap<>();
        this.labelsToConsider = new HashSet<>();
        this.labelsToConsider.addAll(Arrays.asList(labelsToConsider));

        // allows stop as soon as split does not improve by increasing the number of documents considered for a split.
        this.stopEarly = false;

        populateLabelCounts(annotationViews);

    }


    /**
     * build a matrix from docId to label to count.
     * @param annotationViews
     * @return
     */
    private Map<String, Counter<String>> populateLabelCounts(Map<String, Set<View>> annotationViews) {

        labelTotals = new Counter<>();

        for (String docId : annotationViews.keySet()) {
            Counter<String> docLabelCount = new Counter<>();
            labelCounts.put(docId, docLabelCount);
            for (View v : annotationViews.get(docId)) {
                for (Relation r : v.getRelations()) {
                    String label = r.getRelationName();
                    if (useAllLabels || labelsToConsider.contains(label)) {
                        docLabelCount.incrementCount(label);
                        labelTotals.incrementCount(label);
                    }
                }
                for (Constituent c: v.getConstituents()) {
                    String label = c.getLabel();
                    if (useAllLabels || labelsToConsider.contains(label)) {
                        docLabelCount.incrementCount(label);
                        labelTotals.incrementCount(label);
                    }
                }
            }
        }
        //store labels explicitly
        if (useAllLabels)
            labelsToConsider.addAll(labelTotals.keySet());

        return labelCounts;
    }

    /**
     * expects dev < test < train
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
        availIds.addAll(this.labelCounts.keySet());

        Pair<Set<String>, Counter<String>> dev = getBestSplit(devFrac, availIds);
        splitDocIdSets.put(Split.DEV, dev.getFirst());
        bestRelSplitCounts.put(Split.DEV, dev.getSecond());
        availIds.removeAll(dev.getFirst());

        Pair<Set<String>, Counter<String>> test = getBestSplit(testFrac, availIds);
        splitDocIdSets.put(Split.TEST, test.getFirst());
        bestRelSplitCounts.put(Split.TEST, test.getSecond());
        availIds.removeAll(test.getFirst());

        Counter<String> trainCounts = computeRemainingCounts(availIds);
        splitDocIdSets.put(Split.TRAIN, availIds);
        bestRelSplitCounts.put(Split.TRAIN, trainCounts);
        return splitDocIdSets;
    }



    private Counter<String> computeRemainingCounts(Set<String> availIds) {
        Counter<String> counts = new Counter<>();

        for (String docId : availIds) {
            Counter<String> labelCount = labelCounts.get(docId);
            for (String label : labelCount.keySet())
                counts.incrementCount(label, labelCount.getCount(label));
        }

        return counts;
    }


    /** iterate over candidate sets of documents; find smallest diff of relation counts with target counts */
    private Pair<Set<String>, Counter<String>> getBestSplit(double frac, Set<String> availIds) {
        Set<String> split = new HashSet<>();
        Counter<String> splitCount = null;

        if (frac < 0.01)
            return new Pair(split, splitCount);

        Map<String, Double> targetCounts = findTargetCounts(frac);

        double bestDiff = LARGE_DIFF;

        /*
         * fill in a table of partial counts. Naive, so size is approx 2 * (n choose k)
         * as we keep the last row to save some computation.
         * stop as soon as we have a round where we don't improve the bestRoundDiff, as adding more documents
         * will not reduce the count differences.
         */
        PriorityQueue<QueueElement> oldBestSplitsOfSizeK = new PriorityQueue<>(BEAM_SIZE);
        PriorityQueue<QueueElement> bestSplits = new PriorityQueue<>(BEAM_SIZE);

        // number of documents in the sets considered
        for (int num = 1; num <= availIds.size(); ++num) {
            logger.info("Round {}...", num);
            double bestRoundDiff = LARGE_DIFF;
            // store new combinations generated this round
            boolean isBetterRound = false;

            // each document to that of each previously existing id combination
            // todo: move dcc into olddcc; populate newdcc with dcc counts plus doc counts for each doc
            // make sure to copy counters to avoid shared references across combinations (will corrupt counts)
            Map<Set<String>, Counter<String>> oldCombCounts = initializeCurrentRoundCounts(oldBestSplitsOfSizeK);//new HashMap<>();

            /*
             * compute NUM_DOCS * BEAM_SIZE possible splits.
             */
            Map<Set<String>, Counter<String>> docCombinationCounts = new HashMap<>();
            for (Set<String> keyComb : oldCombCounts.keySet()) {
                Counter<String> keyCount = oldCombCounts.get(keyComb);

                for (String docId : availIds) {
                    Set<String> newComb = new HashSet<>();
                    newComb.addAll(keyComb);
                    newComb.add(docId);
                    // naive implementation does not consider order, so avoid duplication
                    if (!oldCombCounts.containsKey(newComb)) {
                        // the counts for the current docId
                        Counter<String> docLabelCount = labelCounts.get(docId);
                        Counter<String> newCombLabelCount = new Counter<>();

                        // initialize newCombLabelCount with count from base id combination
                        for (String label : keyCount.keySet())
                            newCombLabelCount.incrementCount(label, keyCount.getCount(label));

                        //add current docId label counts
                        for (String label : docLabelCount.items()) {
                            newCombLabelCount.incrementCount(label, docLabelCount.getCount(label));
                        }
                        docCombinationCounts.put(newComb, newCombLabelCount);
                    }
                }
            }

            PriorityQueue<QueueElement> bestSplitsOfSizeK = new PriorityQueue<>();

            // all new combinations for this round have been generated
            // want explicit generation because we will use these as seeds in the next round
            for (Set<String> docidComb : docCombinationCounts.keySet()) {
                double diff = computeCountDiff(docCombinationCounts.get(docidComb), targetCounts);
                bestSplitsOfSizeK.add(new QueueElement(diff, docidComb, docCombinationCounts.get(docidComb)));
                if (diff < bestRoundDiff) {
                    bestRoundDiff = diff;
                    if (bestRoundDiff < bestDiff) {
                        isBetterRound = true;
                        bestDiff = bestRoundDiff;
                    }
                }

            }
            logger.info("current round best diff is {}", bestRoundDiff);
            if (stopEarly && !isBetterRound) {
                logger.warn("Stopping after round {}", num);
                logger.warn("current round best diff is {}", bestRoundDiff);
                break;
            }

            oldBestSplitsOfSizeK = bestSplitsOfSizeK; // store best fixed-size splits
            bestSplits.addAll(bestSplitsOfSizeK); // track best splits overall

            oldBestSplitsOfSizeK = trimQueue(oldBestSplitsOfSizeK);
            bestSplits = trimQueue(bestSplits);
        }

        QueueElement bestSplit = bestSplits.poll();
        return new Pair(bestSplit.docIdSet, bestSplit.labelCounter);
    }


    private PriorityQueue<QueueElement> trimQueue(PriorityQueue<QueueElement> splits) {
        PriorityQueue<QueueElement> trimmedQueue = new PriorityQueue<>();

        for( int i = 0; i < BEAM_SIZE && !splits.isEmpty(); ++i)
            trimmedQueue.add(splits.poll());

        return trimmedQueue;
    }


    private Map<Set<String>, Counter<String>> initializeCurrentRoundCounts(PriorityQueue<QueueElement> oldBestSplitsOfSizeK) {
        if (oldBestSplitsOfSizeK.isEmpty())
            return createEmptyComboAndCount();

        Map<Set<String>, Counter<String>> initialCounts = new HashMap<>();
        for (QueueElement el : oldBestSplitsOfSizeK) {
            initialCounts.put(el.docIdSet, el.labelCounter);
        }

        return initialCounts;
    }


    private Map<Set<String>, Counter<String>> createEmptyComboAndCount() {
        Set<String> ids = new HashSet<>();
        Counter<String> counter = new Counter<>();
        Map<Set<String>, Counter<String>> comboCount = new HashMap<>();
        comboCount.put(ids, counter);

        return comboCount;
    }

    /**
     * compute sum of squared difference of counts
     * @param stringCounter counts from some subset of documents
     * @param targetCounts desired counts based on proportion of data desired
     * @return value of difference
     */
    private double computeCountDiff(Counter<String> stringCounter, Map<String, Double> targetCounts) {
        double accum = 0;
        for (String label : targetCounts.keySet()) {
            double count = 0;
            double targetCount = targetCounts.get(label);
            if ( stringCounter.contains(label) )
                count = stringCounter.getCount(label);

            accum += Math.pow((count - targetCount), 2);
        }
        return accum;
    }

    /**
     * find the target counts for labels of interest, given the target fraction
     * @param frac
     * @return
     */
    private Map<String, Double> findTargetCounts(double frac) {
        Map<String, Double> targetCounts = new HashMap<>();
        for (String label : labelsToConsider) {
            double count = this.labelTotals.getCount(label);
            targetCounts.put(label, Math.ceil(count * frac)); // round up to favor small fractions
        }
        return targetCounts;
    }


    /**
     * read from the cache.
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

        double trainFrac = fullRm.getDouble(CorpusSplitConfigurator.TRAIN_FRACTION);
        double devFrac = fullRm.getDouble(CorpusSplitConfigurator.DEV_FRACTION);
        double testFrac = fullRm.getDouble(CorpusSplitConfigurator.TEST_FRACTION);

//        Path corpusPath = Paths.get(corpusRoot);
//        String corpusName = corpusPath.getName(corpusPath.getNameCount() - 2).toString();

        IOUtils.mkdir(outDir);

        String outFileStem = outDir + "/";
        String[] viewNames = {ViewNames.MENTION_ERE};
        String[] labelsToCount = {};


        EREMentionRelationReader reader = null;
        try {
            reader = new EREMentionRelationReader(ereCorpus, corpusRoot, throwExceptionOnXmlParserFail);
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

        CreateTrainDevTestSplit creator = new CreateTrainDevTestSplit(ereViews, labelsToCount);

        Map<Split, Set<String>> splits = creator.getSplits(trainFrac, devFrac, testFrac);
        Map<Split, Counter<String>> splitCounts = creator.getBestRelSplitCounts();

        Map<String, Counter<String>> counts = creator.getLabelCounts();

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

    private static String printCounts(Counter<String> counter) {
        StringBuilder bldr = new StringBuilder();
        for (String key : counter.keySet()) {
            bldr.append(key).append(": ").append(counter.getCount(key)).append("; ");
        }
        return bldr.toString();
    }
}
