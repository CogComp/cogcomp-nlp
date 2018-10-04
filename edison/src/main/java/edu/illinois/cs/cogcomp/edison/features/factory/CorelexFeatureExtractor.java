/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Returns a set of features that are CoreLex's basic types of lemmas. This feature extractor generates features by
 * looking up the lemma in the corlex file, and using the type as the feature.
 * This resource (~150MB) is a relatively big collection of nouns connected to their semantic categories
 * More details can be found in this paper:
 * Buitelaar, Paul. "CORELEX: An ontology of systematic polysemous classes." (1998): 221-235.
 */

public class CorelexFeatureExtractor extends WordFeatureExtractor {
    private static Logger logger = LoggerFactory.getLogger(CorelexFeatureExtractor.class);

    public static final CorelexFeatureExtractor instance = new CorelexFeatureExtractor(true);
    private final static Logger log = LoggerFactory.getLogger(CorelexFeatureExtractor.class);
    private static String CORLEX_FILE;
    static {
        try {
            Datastore ds = null;
            try {
                ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            } catch (InvalidPortException | InvalidEndpointException e) {
                e.printStackTrace();
            }
            File f = ds.getDirectory("org.cogcomp.corelex", "corelex_nouns", 1.3, false);
            System.out.println(f);
            CORLEX_FILE = f.getAbsolutePath() + File.separator  + "CORLEX" +  File.separator + "corelex_nouns";
        } catch (DatastoreException e) {
            e.printStackTrace();
        }
    }

    private final static Map<String, String> data = new HashMap<>();

    public CorelexFeatureExtractor(boolean useLastWordOfMultiwordConstituents) {
        super(useLastWordOfMultiwordConstituents);
    }

    public CorelexFeatureExtractor() {
        super();
    }

    private synchronized static void loadDataFromClassPath() throws EdisonException {

        if (data.size() > 0)
            return;

        List<String> lines;
        try {
            lines = LineIO.readFromClasspath(CORLEX_FILE);
            logger.info("\n");
        } catch (FileNotFoundException e) {
            throw new EdisonException("CORLEX not found in class path at " + CORLEX_FILE);
        }

        log.info("Loading CORLEX from {}", CORLEX_FILE);

        for (String line : lines) {
            if (line.length() == 0)
                continue;

            if (line.startsWith("#"))
                continue;

            String[] parts = line.split("\t");
            if (parts.length == 2) {
                String lemma = parts[0].trim();
                String type = parts[1].trim().intern();

                data.put(lemma, type);
            }
        }

        log.info("Finished loading CORLEX. Found {} nouns", data.size());
    }

    @Override
    public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {

        if (data.size() == 0)
            loadDataFromClassPath();

        String lemma = WordHelpers.getLemma(ta, wordPosition);

        Set<Feature> features = new LinkedHashSet<>();

        if (data.containsKey(lemma)) {
            features.add(DiscreteFeature.create(data.get(lemma)));
        }
        return features;
    }

    @Override
    public String getName() {
        return "corlex";
    }

}
