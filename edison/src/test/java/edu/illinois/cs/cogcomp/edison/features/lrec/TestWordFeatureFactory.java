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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
/**
 * Test class
 * <p>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 */
public class TestWordFeatureFactory extends TestCase {
    static Logger log = Logger.getLogger(TestAffixes.class.getName());

    private static List<TextAnnotation> tas;

    private static Map<Integer, String> feats;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestWordFeatureFactory.class, "test.ta");
            feats =
                    IOUtils.readObjectAsResource(TestWordFeatureFactory.class, "word.features.test");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public final void testCapitalization() throws EdisonException {
        log.debug("\tTesting capitalization");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.capitalization);
        }
    }

    public final void testConflatedPOS() throws EdisonException {
        log.debug("\tTesting conflated POS");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.conflatedPOS);
        }
    }

    public final void testDeAdjectivalAbstractNounsSuffixes() throws EdisonException {
        log.debug("\tTesting de-adjectival abstract noun suffixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.deAdjectivalAbstractNounsSuffixes);
        }
    }

    public final void testDeNominalNounProducingSuffixes() throws EdisonException {
        log.debug("\tTesting de-nominal noun producing suffixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.deNominalNounProducingSuffixes);
        }
    }

    public final void testDeVerbalSuffixes() throws EdisonException {
        log.debug("\tTesting de-verbal suffixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.deVerbalSuffix);
        }
    }

    public final void testGerundMarker() throws EdisonException {
        log.debug("\tTesting gerund marker");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.gerundMarker);
        }
    }

    public final void testKnownPrefixes() throws EdisonException {
        log.debug("\tTesting known prefixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.knownPrefixes);
        }
    }

    public final void testLemma() throws EdisonException {
        log.debug("\tTesting lemma");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.lemma);
        }
    }

    public final void testNominalizationMarker() throws EdisonException {
        log.debug("\tTesting nominalization marker");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.nominalizationMarker);
        }
    }

    public final void testPOS() throws EdisonException {
        log.debug("\tTesting POS");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.pos);
        }
    }

    public final void testPrefixSuffixes() throws EdisonException {
        log.debug("\tTesting prefix, suffixes");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.prefixSuffixes);
        }
    }

    public final void testWord() throws EdisonException {
        log.debug("\tTesting word");
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.word);
        }
    }

    public final void testBrownFeatures() throws EdisonException {
        log.debug("\tTesting Brown cluster features");
        WordFeatureExtractor brownFeatureGenerator =
                WordFeatureExtractorFactory.getBrownFeatureGenerator("", "brownBllipClusters",
                        new int[] {4, 5});
        for (TextAnnotation ta : tas) {
            runTest(ta, brownFeatureGenerator);
        }

    }

    public final void testWordNet() throws EdisonException {
        log.debug("\tTesting wordNet");
        WordNetManager.loadConfigAsClasspathResource(true);
        for (TextAnnotation ta : tas) {
            runTest(ta, WordFeatureExtractorFactory.getWordNetFeatureExtractor(
                    WordNetFeatureClass.existsEntry, WordNetFeatureClass.synsetsFirstSense,
                    WordNetFeatureClass.lexicographerFileNamesAllSenses));
        }
    }

    public final void testFeatureCollection() throws Exception {
        FeatureCollection f = new FeatureCollection("features");
        f.addFeatureExtractor(WordFeatureExtractorFactory.conflatedPOS);
        f.addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
        f.addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);

        log.debug("\tTesting feature collection");

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

    public final void testGazetteerFeatures() throws Exception {
        log.debug("\tTesting gazetteer features");
        WordFeatureExtractor fex =
                WordFeatureExtractorFactory.getGazetteerFeatureExtractor("gazetteer",
                        new GazetteerViewGenerator("gazetteers", ViewNames.GAZETTEER));

        for (TextAnnotation ta : tas) {
            runTest(ta, fex);
        }
    }

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
