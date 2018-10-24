/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.POSBaseLineCounter;

import java.util.*;

/**
 * This feature extractor extracts part of speech based on baseline counting.
 * 
 * @author Xinbo Wu
 */
public class POSBaseLineFeatureExtractor implements FeatureExtractor {
    private final String viewName;
    protected POSBaseLineCounter counter;

    /**
     * Construct the feature extractor given a trained counter.
     * 
     * @param viewName Name of view
     * @param counter trained POS baseline counter
     */
    public POSBaseLineFeatureExtractor(String viewName, POSBaseLineCounter counter) {
        this.viewName = viewName;
        this.counter = counter;
    }

    /**
     * Construct the feature extractor given a training corpus file.
     * 
     * @param viewName Name of view
     * @param corpusName Name of Corpus
     * @param home file name or directory name of the source corpus
     * @throws Exception
     */
    public POSBaseLineFeatureExtractor(String viewName, String corpusName, String home)
            throws Exception {
        this.viewName = viewName;
        this.counter = new POSBaseLineCounter(corpusName);
        counter.buildTable(home);
    }

    /**
     * Construct the feature extractor given a trained counter in JSON format.
     * 
     * @param viewName Name of view
     * @param json JSON format of POSBaseLineCounter instance
     */
    public POSBaseLineFeatureExtractor(String viewName, String json) {
        this.viewName = viewName;
        this.counter = POSBaseLineCounter.read(json);
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        String classifier = "BaseLinePOS";

        TextAnnotation ta = c.getTextAnnotation();

        int start = c.getStartSpan();
        int end = c.getEndSpan();

        Set<Feature> features = new LinkedHashSet<>();

        for (int i = start; i < end; i++) {
            String form = ta.getToken(i);
            String tag = counter.tag(i, ta);
            features.add(new DiscreteFeature(classifier + ":" + tag + "_" + form));
        }
        return features;
    }

    @Override
    public String getName() {
        return "#path#" + this.viewName;
    }
}
