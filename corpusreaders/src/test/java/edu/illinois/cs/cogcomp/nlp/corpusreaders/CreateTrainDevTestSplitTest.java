/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders;

import edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils.CreateTrainDevTestSplit;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils.ListExampleLabelCounter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * use a synthetic data set to test corpus split creator.
 *
 * @author mssammon
 */
public class CreateTrainDevTestSplitTest {

    private static final double TRAIN_FRAC = 0.7;
    private static final double DEV_FRAC = 0.1;
    private static final double TEST_FRAC = 0.2;
    private static final int NUM_EX = 128;

    private String[] categories;
    private double[] proportions;

    @Before
    public void init() {

        categories = new String[] {"DUMMY", "Red", "Green", "Yellow", "Blue"};
        proportions = new double[categories.length];
        proportions[0] = 0.0;
        double sum = 0.0;


        // set proportions to be 1/(2^n), except make last two proportions equal for convenient fractions
        for (int i = 1; i < categories.length - 1; ++i) {
            double prop = 1.0/(double) Math.pow(2, i+1);
            proportions[i] = prop;
            sum += prop;
        }
        proportions[categories.length - 1] = proportions[categories.length - 2];
        sum += proportions[categories.length - 1];

        double norm = 1.0 / sum;

        for (int i = 0; i < categories.length; ++i) {
            proportions[i] *= norm;
        }
    }


    /**
     * when data set is small, balancing different characteristics is important.
     */
    @Test
    public void testSmallDataset() {

        Map<String, Map<String, Integer>> smallExampleList = generateListExamples(categories, NUM_EX);
        ListExampleLabelCounter listCounter = new ListExampleLabelCounter(smallExampleList);
        CreateTrainDevTestSplit createTrainDevTestSplit = new CreateTrainDevTestSplit(listCounter);
        Map<CreateTrainDevTestSplit.Split, Set<String>> splits =
                createTrainDevTestSplit.getSplits(TRAIN_FRAC, DEV_FRAC, TEST_FRAC);

        assertTrue(checkCount(NUM_EX, TRAIN_FRAC, splits.get(CreateTrainDevTestSplit.Split.TRAIN)));
        assertTrue(checkCount(NUM_EX, TEST_FRAC, splits.get(CreateTrainDevTestSplit.Split.TEST)));
        assertTrue(checkCount(NUM_EX, DEV_FRAC, splits.get(CreateTrainDevTestSplit.Split.DEV)));

        double[] featureCounts = getFeatureCounts(splits.get(CreateTrainDevTestSplit.Split.DEV), smallExampleList);

        assertTrue(compareProportions(proportions, featureCounts));
    }

    private boolean checkCount(int numEx, double frac, Set<String> ids) {
        double targetSize = (double) NUM_EX * frac;
        double sizeDiff = Math.abs(targetSize - (double)ids.size());
        // for small fraction -- e.g. 10% -- of small dataset, relax the target difference
        double targetDiff = Math.max(0.1 * (double) numEx, 0.2 * frac * (double) numEx);

        return sizeDiff < targetDiff;
    }


    private boolean compareProportions(double[] proportions, double[] featureCounts) {

        boolean areProportionsCorrect = true;

        double total = 0;
        for (int i = 0; i < featureCounts.length; ++i) {
            total += featureCounts[i];
        }

        double[] sampleProportions = new double[proportions.length];

        for (int i = 0; i < featureCounts.length; ++i) {
            sampleProportions[i] = featureCounts[i] / total;
            if (Math.abs(proportions[i] - sampleProportions[i]) > proportions[i]*0.2) {
                System.err.println("Split feature " + categories[i] + " has poor proportion: " +
                "goal is " + String.format("%.3f", proportions[i]) + "; actual proportion: " +
                String.format("%.3f", sampleProportions[i]));
                areProportionsCorrect = false;
            }
        }
        return areProportionsCorrect;
    }

    private double[] getFeatureCounts(Set<String> ids, Map<String, Map<String, Integer>> exampleList) {

        Map<String, Integer> rawCounts = new HashMap<>();
        for (String feat : categories)
            rawCounts.put(feat, 0);

        for (String id : ids) {
            Map<String, Integer> ex = exampleList.get(id);
            for (String feat : ex.keySet())
                if (!"DUMMY".equals(feat))
                    rawCounts.put(feat, rawCounts.get(feat) + ex.get(feat));
        }

        double[] featureCounts = new double[categories.length]; // allow for dummy feature
        for (int i = 0; i < categories.length; ++i)
            featureCounts[i] = (double) rawCounts.get(categories[i]);

        return featureCounts;
    }

    /**
     * generate a set of examples with the categories named, with each having a fixed proportion.
     * numExamples should be a power of 2.
     *
     * @param categories
     * @param numExamples
     * @return
     */
    private Map<String, Map<String, Integer>> generateListExamples(String[] categories, int numExamples) {

        Map<String, Map<String, Integer>> examples = new HashMap<>();
        int[] numActivePerCat = new int[categories.length];
        for (int i = 0; i < categories.length; ++i)
            numActivePerCat[i] = Math.round((float) proportions[i] * numExamples);

        StringBuilder bldr = new StringBuilder();
        for (int i = 0; i < numActivePerCat.length; ++i) {
            bldr.append(numActivePerCat[i]).append(",");
        }
        System.err.println("## numActive counts: " + bldr.toString());

        for (int i = 0; i < numExamples; ++i) {
            Map<String, Integer> example = new HashMap<>();
            example.put("DUMMY", 1); // add this so at least one count is active in every example
            // to avoid half examples being completely empty
            if (i < numExamples * proportions[0])
                example.put(categories[0], 1);

            for (int j = 1; j < proportions.length; ++j)
                if(0 == i % (numExamples / numActivePerCat[j]))
                    example.put(categories[j], 1);

            examples.put(String.format("%06d", i), example);
        }

        return examples;
    }
}
