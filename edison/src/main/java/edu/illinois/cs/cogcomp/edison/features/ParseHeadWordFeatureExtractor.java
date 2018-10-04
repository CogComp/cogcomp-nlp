/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Extracts features from the the head word of the constituent. The head is defined using the
 * Collins' head percolation table applied to the phrase in the parse tree.
 *
 * @author Vivek Srikumar
 */
public class ParseHeadWordFeatureExtractor implements FeatureExtractor {

    private final String parseViewName;
    private FeatureExtractor fex;

    public ParseHeadWordFeatureExtractor(String parseViewName, FeatureExtractor featureExtractor) {
        this.parseViewName = parseViewName;
        this.fex = featureExtractor;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        TextAnnotation ta = c.getTextAnnotation();

        TreeView tree = (TreeView) ta.getView(parseViewName);

        Constituent phrase;
        try {
            phrase = tree.getParsePhrase(c);
        } catch (Exception e) {
            throw new EdisonException(e);
        }
        Set<Feature> features = new LinkedHashSet<>();

        int head = CollinsHeadFinder.getInstance().getHeadWordPosition(phrase);
        Constituent c1 = new Constituent("", "", ta, head, head + 1);

        features.addAll(fex.getFeatures(c1));

        return features;

    }

    @Override
    public String getName() {
        return "#head#" + parseViewName + ":" + fex.getName();
    }

}
