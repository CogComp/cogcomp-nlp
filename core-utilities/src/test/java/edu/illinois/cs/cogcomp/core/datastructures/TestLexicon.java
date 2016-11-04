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

import java.util.*;

import static org.junit.Assert.assertEquals;

public class TestLexicon {

    private final Random random = new Random();
    private final String alphabet =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 !@#$%^&*()";

    int threshold = 1;
    int maxLen = 4;
    int N = 50000;

    @Test
    public void testLexicon() throws Exception {
        Lexicon lexicon = new Lexicon(true, true);

        // populate lexicon with features.

        List<String> set = populateLexicon(lexicon, maxLen, N);
        System.out.println(set.size() + " unique features");

        Lexicon prunedLexicon = lexicon.getPrunedLexicon(threshold);

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

        System.out.println(Arrays.toString(feats.getFirst()));

        for (int id : feats.getFirst()) {
            System.out.println(id + "\t" + lexicon.lookupName(id) + "\t"
                    + lexicon.featureCounts.get(id));
        }

        System.out.println(Arrays.toString(f1));
        System.out.println(Arrays.toString(f2));
        assertEquals(true, Arrays.equals(f1, f2));
    }

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
