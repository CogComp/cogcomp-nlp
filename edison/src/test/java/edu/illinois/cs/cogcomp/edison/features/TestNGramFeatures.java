package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestTAResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;

import java.util.List;
import java.util.Set;

/**
 * Test class NB: If needed, please re-create the {@code test.ta} and
 * {@code feature.collection.text} files using {@link CreateTestTAResource} and
 * {@link CreateTestFeaturesResource}
 *
 * @author Vivek Srikumar
 */
public class TestNGramFeatures extends TestCase {
    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestNGramFeatures.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final void testBigramsWord() throws Exception {
        testBigrams(WordFeatureExtractorFactory.word);
    }

    public final void testTrigramsWord() throws Exception {
        testTrigrams(WordFeatureExtractorFactory.word);
    }

    public final void testBigramsPOS() throws Exception {
        testBigrams(WordFeatureExtractorFactory.pos);
    }

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
