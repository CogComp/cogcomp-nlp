/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CorlexFeatureExtractor extends WordFeatureExtractor {

    public static final CorlexFeatureExtractor instance = new CorlexFeatureExtractor(true);
    private final static Logger log = LoggerFactory.getLogger(CorlexFeatureExtractor.class);
    private final static String CORLEX_FILE = "resources/CORLEX/corelex_nouns";

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

        List<URL> list;
        try {
            list = IOUtils.lsResources(CorlexFeatureExtractor.class, CORLEX_FILE);
        } catch (Exception e) {
            throw new EdisonException(e);
        }
        if (list.size() == 0) {
            throw new EdisonException("CORLEX not found in class path at " + CORLEX_FILE);
        }

        log.info("Loading CORLEX from {}", CORLEX_FILE);

        Scanner scanner;
        try {
            scanner = new Scanner(list.get(0).openStream());
        } catch (IOException e) {
            throw new EdisonException(e);
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

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

        scanner.close();

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
