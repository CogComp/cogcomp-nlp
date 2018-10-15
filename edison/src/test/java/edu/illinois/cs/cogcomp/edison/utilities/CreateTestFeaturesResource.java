/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.annotators.SimpleGazetteerAnnotator;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prepares the word.features.test and feature.collection.text files used for the Maven Tests NB:
 * File test.ta needs to exist
 *
 * TODO: 2/15/16 Needs to be switched to use DummyTextAnnotationGenerator
 *
 * @author Christos Christodoulopoulos
 */
public class CreateTestFeaturesResource {
    public static final String FEATURE_COLLECTION_FILE =
            "src/test/resources/feature.collection.test";
    public static final String WORD_FEATURES_FILE = "src/test/resources/word.features.test";
    private static List<TextAnnotation> tas;
    private static Map<Integer, String> feats;
    private static Logger logger = LoggerFactory.getLogger(CreateTestFeaturesResource.class);

    public CreateTestFeaturesResource() throws Exception {
        feats = new HashMap<>();
        tas = IOUtils.readObjectAsResource(CreateTestFeaturesResource.class, "test.ta");

        // Add all the features
        addFeatCollection();
        addCapitalization();
        addConflatedPOS();
        addDeAdjectivalAbstractNounsSuffixes();
        addDeNominalNounProducingSuffixes();
        addDeVerbalSuffixes();
        addGerundMarker();
        addKnownPrefixes();
        addLemma();
        addNominalizationMarker();
        addPOS();
        addPrefixSuffixes();
        addWord();
        addBrownFeatures();
        addWordNet();
        addGazetteerFeatures();

        IOUtils.writeObject(feats, WORD_FEATURES_FILE);
    }

    public static void main(String[] args) {
        try {
            new CreateTestFeaturesResource();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCapitalization() throws EdisonException {
        logger.info("\tadding capitalization");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.capitalization);
        }
    }

    private void addConflatedPOS() throws EdisonException {
        logger.info("\tadding conflated POS");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.conflatedPOS);
        }
    }

    private void addDeAdjectivalAbstractNounsSuffixes() throws EdisonException {
        logger.info("\tadding de-adjectival abstract noun suffixes");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.deAdjectivalAbstractNounsSuffixes);
        }
    }

    private void addDeNominalNounProducingSuffixes() throws EdisonException {
        logger.info("\tadding de-nominal noun producing suffixes");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.deNominalNounProducingSuffixes);
        }
    }

    private void addDeVerbalSuffixes() throws EdisonException {
        logger.info("\tadding de-verbal suffixes");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.deVerbalSuffix);
        }
    }

    private void addGerundMarker() throws EdisonException {
        logger.info("\tadding gerund marker");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.gerundMarker);
        }
    }

    private void addKnownPrefixes() throws EdisonException {
        logger.info("\tadding known prefixes");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.knownPrefixes);
        }
    }

    private void addLemma() throws EdisonException {
        logger.info("\tadding lemma");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.lemma);
        }
    }

    private void addNominalizationMarker() throws EdisonException {
        logger.info("\tadding nominalization marker");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.nominalizationMarker);
        }
    }

    private void addPOS() throws EdisonException {
        logger.info("\tadding POS");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.pos);
        }
    }

    private void addPrefixSuffixes() throws EdisonException {
        logger.info("\tadding prefix, suffixes");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.prefixSuffixes);
        }
    }

    private void addWord() throws EdisonException {
        logger.info("\tadding word");
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.word);
        }
    }

    private void addBrownFeatures() throws EdisonException {
        logger.info("\tadding Brown cluster features");
        WordFeatureExtractor brownFeatureGenerator =
                WordFeatureExtractorFactory.getBrownFeatureGenerator("", "brownBllipClusters",
                        new int[] {4, 5});
        for (TextAnnotation ta : tas) {
            addFeatures(ta, brownFeatureGenerator);
        }
    }

    private void addWordNet() throws EdisonException {
        logger.info("\tadding wordNet");
        WordNetManager.loadConfigAsClasspathResource(true);
        for (TextAnnotation ta : tas) {
            addFeatures(ta, WordFeatureExtractorFactory.getWordNetFeatureExtractor(
                    WordNetFeatureExtractor.WordNetFeatureClass.existsEntry,
                    WordNetFeatureExtractor.WordNetFeatureClass.synsetsFirstSense,
                    WordNetFeatureExtractor.WordNetFeatureClass.lexicographerFileNamesAllSenses));
        }
    }

    private void addGazetteerFeatures() throws Exception {
        logger.info("\tadding gazetteer features");
        WordFeatureExtractor fex =
                WordFeatureExtractorFactory.getGazetteerFeatureExtractor("gazetteer",
                        new SimpleGazetteerAnnotator());

        for (TextAnnotation ta : tas) {
            addFeatures(ta, fex);
        }
    }

    private void addFeatures(TextAnnotation ta, WordFeatureExtractor fex) throws EdisonException {
        for (int tokenId = 0; tokenId < ta.size(); tokenId++) {
            Set<Feature> features = fex.getWordFeatures(ta, tokenId);
            if (features.size() > 0) {
                String id = fex.getName() + ":" + ta.getTokenizedText() + ":" + tokenId;
                feats.put(id.hashCode(), features.toString());
            }
        }
    }

    private void addFeatCollection() throws EdisonException, IOException {
        Map<Integer, String> map = new HashMap<>();
        FeatureCollection featureCollection = new FeatureCollection("features");
        featureCollection.addFeatureExtractor(WordFeatureExtractorFactory.conflatedPOS);
        featureCollection.addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
        featureCollection.addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);
        for (TextAnnotation ta : tas) {
            for (int tokenId = 0; tokenId < ta.size(); tokenId++) {
                Constituent c = new Constituent("", "", ta, tokenId, tokenId + 1);
                Set<Feature> features = featureCollection.getFeatures(c);
                if (features.size() > 0) {
                    String id = ta.getTokenizedText() + ":" + tokenId;
                    map.put(id.hashCode(), features.toString());
                }
            }
        }

        IOUtils.writeObject(map, FEATURE_COLLECTION_FILE);
    }
}
