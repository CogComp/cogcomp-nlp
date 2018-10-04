/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.annotators.GazetteerViewGenerator;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor.WordNetFeatureClass;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
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
public class TestWordFeatureFactory {

    private static List<TextAnnotation> tas;

    private static Map<Integer, String> feats;
    private static Logger logger = LoggerFactory.getLogger(TestWordFeatureFactory.class);

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestWordFeatureFactory.class, "test.ta");
            feats =
                    IOUtils.readObjectAsResource(TestWordFeatureFactory.class, "word.features.test");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//    }

    @Test
    public final void testCapitalization() throws EdisonException {
        logger.info("\tTesting capitalization");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.capitalization);
        }
    }

    @Test
    public final void testConflatedPOS() throws EdisonException {
        logger.info("\tTesting conflated POS");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.conflatedPOS);
        }
    }

    @Test
    public final void testDeAdjectivalAbstractNounsSuffixes() throws EdisonException {
        logger.info("\tTesting de-adjectival abstract noun suffixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.deAdjectivalAbstractNounsSuffixes);
        }
    }

    @Test
    public final void testDeNominalNounProducingSuffixes() throws EdisonException {
        logger.info("\tTesting de-nominal noun producing suffixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.deNominalNounProducingSuffixes);
        }
    }

    @Test
    public final void testDeVerbalSuffixes() throws EdisonException {
        logger.info("\tTesting de-verbal suffixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.deVerbalSuffix);
        }
    }

    @Test
    public final void testGerundMarker() throws EdisonException {
        logger.info("\tTesting gerund marker");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.gerundMarker);
        }
    }

    @Test
    public final void testKnownPrefixes() throws EdisonException {
        logger.info("\tTesting known prefixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.knownPrefixes);
        }
    }

    @Test
    public final void testLemma() throws EdisonException {
        logger.info("\tTesting lemma");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.lemma);
        }
    }

    @Test
    public final void testNominalizationMarker() throws EdisonException {
        logger.info("\tTesting nominalization marker");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.nominalizationMarker);
        }
    }

    @Test
    public final void testPOS() throws EdisonException {
        logger.info("\tTesting POS");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.pos);
        }
    }

    @Test
    public final void testPrefixSuffixes() throws EdisonException {
        logger.info("\tTesting prefix, suffixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.prefixSuffixes);
        }
    }

    @Test
    public final void testWord() throws EdisonException {
        logger.info("\tTesting word");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.word);
        }
    }

    @Test
    public final void testBrownFeatures() throws EdisonException {
        logger.info("\tTesting Brown cluster features");
        WordFeatureExtractor brownFeatureGenerator =
                WordFeatureExtractorFactory.getBrownFeatureGenerator("", "brownBllipClusters",
                        new int[] {4, 5});
        for (TextAnnotation ta : tas) {
            runTest(ta, brownFeatureGenerator);
        }
    }

    @Test
    public final void testWordNet() throws EdisonException {
        logger.info("\tTesting wordNet");
        WordNetManager.loadConfigAsClasspathResource(true);
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.getWordNetFeatureExtractor(
                    WordNetFeatureClass.existsEntry, WordNetFeatureClass.synsetsFirstSense,
                    WordNetFeatureClass.lexicographerFileNamesAllSenses));
        }
    }

    @Test
    public final void testFeatureCollection() throws Exception {
        FeatureCollection f = new FeatureCollection("features");
        f.addFeatureExtractor(WordFeatureExtractorFactory.conflatedPOS);
        f.addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
        f.addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);

        logger.info("\tTesting feature collection");

        Map<Integer, String> map =
                IOUtils.readObjectAsResource(TestWordFeatureFactory.class,
                        "feature.collection.test");

        for (TextAnnotation ta : tas) {
            for (int tokenId = 0; tokenId < ta.size(); tokenId++) {

                Constituent c = new Constituent("", "", ta, tokenId, tokenId + 1);
                Set<Feature> features = f.getFeatures(c);
                if (features.size() > 0) {
                    String id = ta.getTokenizedText() + ":" + tokenId;
                    assertEquals(map.get(id.hashCode()), features.toString());
                }
            }
        }
    }
// Not needed anymore, as test of SimpleGazetteerAnnotator in SimpleGazetteerAnnotatorTest.java suffice.
//    public final void testGazetteerFeatures() throws Exception {
//        System.out.println("\tTesting gazetteer features");
//        WordFeatureExtractor fex =
//                WordFeatureExtractorFactory.getGazetteerFeatureExtractor("gazetteer",
//                        new GazetteerViewGenerator("gazetteers", ViewNames.GAZETTEER));
//
//        for (TextAnnotation ta : tas) {
//            runTest(ta, fex);
//        }
//    }

    private void runTest(TextAnnotation ta, WordFeatureExtractor fex) throws EdisonException {

        for (int tokenId = 0; tokenId < ta.size(); tokenId++) {
            Set<Feature> features = fex.getWordFeatures(ta, tokenId);
            if (features.size() > 0) {
                String f = features.toString();
                String id = fex.getName() + ":" + ta.getTokenizedText() + ":" + tokenId;

                assertEquals(feats.get(id.hashCode()), f);
            }
        }
    }
}
