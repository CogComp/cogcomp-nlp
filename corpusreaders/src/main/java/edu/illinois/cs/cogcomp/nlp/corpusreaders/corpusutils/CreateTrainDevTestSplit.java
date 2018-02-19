/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
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
 * Divides a SMALL corpus into train, dev, and test splits according to source documents, trying to balance
 *    the numbers and types of annotations.
 *
 * Designed for small corpora where examples are grouped into e.g. documents, and where it is desirable to keep
 *    examples from a single document within one of the dev/test/train splits.
 *
 * Computationally inefficient -- brute force exploration of sample of space of subsets.
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

    /** keep this number of candidates for best split */
    private final int BEAM_SIZE;
    /** for larger data sets, the size of blocks to use to speed up computation */
    private int BLOCK_SIZE = 1000;
    private final double LARGE_DIFF = 1000000000.0;
    private Logger logger = LoggerFactory.getLogger(CreateTrainDevTestSplit.class);
    private Map<Split, Counter<String>> bestRelSplitCounts;
    private boolean stopEarly;
    private Map<String, Double> defaultWeights;

    private LabelCountExtractor labelCountExtractor;

    /** a map from data example ID to the counts of target characteristics of that example */
    private Map<String, Counter<String>> labelCounts;

//    private EvictingQueue<QueueElement> bestCandidateSplits;
    /** total counts for each target characteristic of examples in data set */
    private Counter<String> labelTotals;


    public CreateTrainDevTestSplit(LabelCountExtractor labelExtractor) {
        this(labelExtractor, 100);
    }
        /**
         * given views identified by source document, and a list of labels to consider, and a train/dev/test ratio,
         *    determine the best division of the corpus according to those constraints.
         */
    public CreateTrainDevTestSplit(LabelCountExtractor labelExtractor, int beamSize) {

        this.BEAM_SIZE = beamSize;
        this.labelCountExtractor = labelExtractor;
        labelTotals = new Counter<>();
        labelCounts = new HashMap<>();
        bestRelSplitCounts = new HashMap<>();

        // allows stop as soon as split does not improve by increasing the number of documents considered for a split.
        this.stopEarly = false;
        labelCounts = labelExtractor.getLabelCounts();
        labelTotals = labelExtractor.getLabelTotals();

        this.defaultWeights = new HashMap<>();
        for (String label : labelTotals.items())
            defaultWeights.put(label, 1.0);
    }


    private static String printCounts(Counter<String> counter) {
        StringBuilder bldr = new StringBuilder();
        for (String key : counter.keySet()) {
            bldr.append(key).append(": ").append(counter.getCount(key)).append("; ");
        }
        return bldr.toString();
    };

    public Map<String,Counter<String>> getLabelCounts() {
        return labelCounts;
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


    /**
     * iterate over candidate sets of documents; find smallest diff of relation counts with target counts.
     * for larger data sets, splits in to blocks of specified size and performs the split in each,
     *    then concatenate the results.
     * Naive implementation: consider individual documents first, then incrementally add documents
     *   in different combinations. Each time, compare count distribution to target distribution,
     *   and keep a k-best list of the closest document combinations.
     */
    private Pair<Set<String>, Counter<String>> getBestSplit(double frac, Set<String> availIds) {

        Set<String> split = new HashSet<>();
        Counter<String> splitCount = null;

        if (frac < 0.01)
            return new Pair(split, splitCount);

        int numBlocks = (int) Math.ceil(((double) availIds.size()) * frac / BLOCK_SIZE);

        logger.info("Processing data in {} blocks of size {} (# of examples)...", numBlocks, BLOCK_SIZE);

        Counter<String> targetCounts = labelCountExtractor.findTargetCounts(frac);

        for (String label : targetCounts.keySet()) {
            targetCounts.incrementCount(label, targetCounts.getCount(label)/(double) numBlocks);
        }

        List<List<String>> idBlocks = generateIdBlocks(availIds, BLOCK_SIZE);

        // get a split for each block, add to returnset
        for (int i = 0; i < numBlocks; ++i) {

            Pair<Set<String>, Counter<String>> blockSplit = getBlockSplit(idBlocks.get(i), targetCounts);
            split.addAll(blockSplit.getFirst());
            updateCounter(splitCount, blockSplit.getSecond());
        }

        return new Pair(split, splitCount);
    }

    /**
     * generate a list of lists of ids of size blockSize, no overlapping ids, exhaust availIds
     * @param availIds
     * @param blockSize
     * @return
     */
    private List<List<String>> generateIdBlocks(Set<String> availIds, int blockSize) {
        List<List<String>> blocks = new ArrayList<>();

        List<String> allIds = new ArrayList<>(availIds);
        Collections.shuffle(allIds);
        int num = 0;
        List<String> blockIds = new ArrayList<>(blockSize);
        for (String id : allIds) {
            blockIds.add(id);
            if (++num % blockSize == 0) {
                blocks.add(blockIds);
                blockIds = new ArrayList<>(blockSize);
            }
        }

        if (!blockIds.isEmpty())
            blocks.add(blockIds);

        return blocks;
    }


    /**
     * sample without replacement the available ids a set of  (frac, 1-frac), trying to match the
     *    proportions of labels indicated.
     *
     * @param availIds ids to apportion
     * @param targetCounts desired counts of example fields in the sampled data
     * @return the set of
     */
    private Pair<Set<String>,Counter<String>> getBlockSplit(List<String> availIds, Counter<String> targetCounts) {

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
            logger.info("Round {} of maximum {}...", num, availIds.size());
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
                double diff = computeCountDiff(docCombinationCounts.get(docidComb), targetCounts, defaultWeights);
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

    private void updateCounter(Counter<String> splitCount, Counter<String> addCounts) {
        for (String key : addCounts.keySet())
            splitCount.incrementCount(key, addCounts.getCount(key));
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
     * compute weighted sum of absolute difference of counts
     * @param stringCounter counts from some subset of documents
     * @param targetCounts desired counts based on proportion of data desired
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

}
