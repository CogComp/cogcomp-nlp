/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test class
 * <p>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 */
public class TestNGramFeatures {
    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestNGramFeatures.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void testBigramsWord() throws Exception {
        testBigrams(WordFeatureExtractorFactory.word);
    }

    @Test
    public final void testTrigramsWord() throws Exception {
        testTrigrams(WordFeatureExtractorFactory.word);
    }

    @Test
    public final void testBigramsPOS() throws Exception {
        testBigrams(WordFeatureExtractorFactory.pos);
    }

    @Test
    public final void testTrigramsPOS() throws Exception {
        testTrigrams(WordFeatureExtractorFactory.pos);
    }


    private void testBigrams(WordFeatureExtractor f) throws EdisonException {
        final NgramFeatureExtractor bigrams = NgramFeatureExtractor.bigrams(f);

        for (final TextAnnotation ta : tas) {

            for (int i = 0; i < ta.size(); i++) {
                final Set<Feature> b = bigrams.getWordFeatures(ta, i);

                Set<Feature> wordFeatures0 = f.getWordFeatures(ta, i - 1);
                Set<Feature> wordFeatures1 = f.getWordFeatures(ta, i);

                assertEquals(wordFeatures0.size() * wordFeatures1.size(), b.size());

                for (Feature w0 : wordFeatures0) {
                    for (Feature w1 : wordFeatures1) {
                        assertEquals(true, b.contains(w0.conjoinWith(w1)));
                    }
                }

            }
        }
    }

    private void testTrigrams(WordFeatureExtractor f) throws EdisonException {
        NgramFeatureExtractor trigrams = NgramFeatureExtractor.trigrams(f);

        for (TextAnnotation ta : tas) {

            for (int i = 0; i < ta.size(); i++) {
                Set<Feature> b = trigrams.getWordFeatures(ta, i);

                Set<Feature> wordFeatures0 = f.getWordFeatures(ta, i - 2);

                Set<Feature> wordFeatures1 = f.getWordFeatures(ta, i - 1);

                Set<Feature> wordFeatures2 = f.getWordFeatures(ta, i);

                assertEquals(wordFeatures0.size() * wordFeatures1.size() * wordFeatures2.size(),
                        b.size());

                for (Feature w0 : wordFeatures0) {
                    for (Feature w1 : wordFeatures1) {
                        for (Feature w2 : wordFeatures2) {
                            assertEquals(true, b.contains(w0.conjoinWith(w1).conjoinWith(w2)));
                        }
                    }
                }
            }
        }
    }
}
