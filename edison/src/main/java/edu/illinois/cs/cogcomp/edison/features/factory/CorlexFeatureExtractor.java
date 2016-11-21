/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.*;

public class CorlexFeatureExtractor extends WordFeatureExtractor {
    private static Logger logger = LoggerFactory.getLogger(CorlexFeatureExtractor.class);

    public static final CorlexFeatureExtractor instance = new CorlexFeatureExtractor(true);
    private final static Logger log = LoggerFactory.getLogger(CorlexFeatureExtractor.class);
    private final static String CORLEX_FILE = "CORLEX/corelex_nouns";

    private final static Map<String, String> data = new HashMap<>();

    public CorlexFeatureExtractor(boolean useLastWordOfMultiwordConstituents) {
        super(useLastWordOfMultiwordConstituents);
    }

    public CorlexFeatureExtractor() {
        super();
    }

    private synchronized static void loadData() throws EdisonException {

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
            loadData();

        String lemma = WordHelpers.getLemma(ta, wordPosition);

        Set<Feature> features = new LinkedHashSet<>();
        if (data.containsKey(lemma))
            features.add(DiscreteFeature.create(data.get(lemma)));

        return features;

    }

    @Override
    public String getName() {
        return "corlex";
    }

}
