/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class TestLexicon {
    private static Logger logger = LoggerFactory.getLogger(TestLexicon.class);

    private final Random random = new Random();
    private final String alphabet =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 !@#$%^&*()";

    @Test
    public void testLexicon() throws Exception {
        Lexicon lexicon = new Lexicon(false, true);

        // populate lexicon with features.
        int threshold = 1;
        int maxLen = 4;
        int N = 50000;
        List<String> set = populateLexicon(lexicon, maxLen, N);
        logger.info(set.size() + " unique features");

        Lexicon prunedLexicon = lexicon.getPrunedLexicon(threshold, true, false, false, true);

        // verify that all the pruned features in fact have a count that is more
        // than the threshold in the original lexicon

        for (String s : set) {
            if (prunedLexicon.contains(s)) {
                // the ids should be the same in both places
                int originalId = lexicon.lookupId(s);
                assertEquals(prunedLexicon.lookupId(s), originalId);

                // the count should be more than the threshold in the original
                // lexicon
                assertEquals(true, lexicon.featureCounts.get(originalId) > threshold);
            }
        }

        // build a random feature vector with 10 features
        Map<String, Float> features = new HashMap<>();
        for (int i = 0; i < 10; i++)
            features.put(set.get(random.nextInt(set.size())), 1f);

        Pair<int[], float[]> feats = lexicon.getFeatureVector(features);

        int[] f1 =
                lexicon.pruneFeaturesByCount(feats.getFirst(), feats.getSecond(), threshold)
                        .getFirst();

        int[] f2 = prunedLexicon.getFeatureVector(features).getFirst();

        logger.info(Arrays.toString(feats.getFirst()));

        for (int id : feats.getFirst()) {
            logger.info(id + "\t" + lexicon.lookupName(id) + "\t"
                    + lexicon.featureCounts.get(id));
        }

        logger.info(Arrays.toString(f1));
        logger.info(Arrays.toString(f2));
        assertEquals(true, Arrays.equals(f1, f2));
    }


    // test the effect of pruning on keeping the names in the lexicon
    @Test
    public void testLexicon2() throws Exception {
        Lexicon lexicon = new Lexicon(false, true);

        // populate lexicon with features.
        int threshold = 1;
        int N = 50000;
        for(int i = 0; i < N; i++) {
            if (!lexicon.contains("a"))
                lexicon.previewFeature("a");
            else {
                int id = lexicon.lookupId("a");
                lexicon.countFeature(id);
            }
        }

        Lexicon prunedLexicon = lexicon.getPrunedLexicon(threshold, true, true, false, true);

        assertEquals(prunedLexicon.getFeatureNames().size(), 1);
        assertEquals(prunedLexicon.size(), 1);
    }

    // test whether you can save and read back your lexicon
    @Test
    public void testLexicon3() throws Exception {
        Lexicon lexicon = new Lexicon(true, true);

        // populate lexicon with features.
        int threshold = 1;
        int maxLen = 4;
        int N = 50000;
        List<String> set = populateLexicon(lexicon, maxLen, N);
        logger.info(set.size() + " unique features");

        lexicon.save("output.lex");

        File lcFile = new File("output.lex");
        Lexicon lexiconFromDisk = new Lexicon(lcFile, true);

        assertEquals(lexicon.size(), lexiconFromDisk.size());
        assertEquals(lexicon.getFeatureNames().size(), lexiconFromDisk.getFeatureNames().size());
    }

    @Test
    public void testLexicon4() throws Exception {
        Lexicon lexicon = new Lexicon(false, true);

        // populate lexicon with features.
        int threshold = 1;
        int maxLen = 4;
        int N = 50000;
        List<String> set = populateLexicon(lexicon, maxLen, N);
        logger.info(set.size() + " unique features");

        for(int i = 0; i < 5; i++) {
            int featureNamesSize = lexicon.getFeatureNames().size();
            int featureCounts = lexicon.featureCounts.size();
            int featureMapSize = lexicon.getFeatureMap().size();
            assertEquals(featureNamesSize, featureCounts);
            assertEquals(featureMapSize, featureCounts);
            lexicon = lexicon.getPrunedLexicon(i, true, true, false, true);
            populateLexicon(lexicon, maxLen, N);
        }

        lexicon.save("output.lex");

        File lcFile = new File("output.lex");
        Lexicon lexiconFromDisk = new Lexicon(lcFile, true);

        assertEquals(lexicon.size(), lexiconFromDisk.size());
        assertEquals(lexicon.getFeatureNames().size(), lexiconFromDisk.getFeatureNames().size());
    }

    /**
     *
     * @param lexicon
     * @param maxLen max length of the randomly-generated feature names
     * @param N the number of features
     * @return feature names
     */
    private List<String> populateLexicon(Lexicon lexicon, int maxLen, int N) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < N; i++) {
            int len = random.nextInt(maxLen) + 1;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            String feature = sb.toString();
            set.add(feature);

            if (!lexicon.contains(feature))
                lexicon.previewFeature(feature);

            lexicon.countFeature(lexicon.lookupId(feature));
        }
        return new ArrayList<>(set);
    }
}
