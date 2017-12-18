/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.HashSet;
import java.util.Set;

/**
 * Extracts features for a full TextAnnotation
 * @author Daniel Khashabi
 */
public class TextAnnotationFeatureExtractor implements FeatureExtractor<TextAnnotation> {

    String viewName;
    FeatureExtractor<Constituent> fe = null;

    static TextAnnotationFeatureExtractor LEMMA = new TextAnnotationFeatureExtractor(ViewNames.LEMMA, WordFeatureExtractorFactory.lemma);

    /**
     * @param viewName the view from which to extract the features
     * @param fe the base features extractor, on constituent level
     */
    public TextAnnotationFeatureExtractor(String viewName, FeatureExtractor<Constituent> fe) {
        this.viewName = viewName;
        this.fe = fe;
    }

    /**
     * @param ta TextAnnotation on which to do the extraction
     * @return the combined features
     * @throws EdisonException
     */
    @Override
    public Set<Feature> getFeatures(TextAnnotation ta) throws EdisonException {
        Set<Feature> features = new HashSet<>();
        for (Constituent c : ta.getView(viewName).getConstituents()) {
            features.addAll(FeatureUtilities.prefix(this.getName(), fe.getFeatures(c)));
        }
        return features;
    }

    @Override
    public String getName() {
        return "#ta-feat#";
    }

}
