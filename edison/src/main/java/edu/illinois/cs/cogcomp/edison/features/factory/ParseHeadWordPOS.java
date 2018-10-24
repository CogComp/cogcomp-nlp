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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Extracts the head word and POS tag of the constituent. The head is defined using the Collins'
 * head percolation table applied to the phrase in the parse tree.
 * <p>
 * If possible, use one of the two static objects in this class, corresponding to the Charniak and
 * Stanford parses respectively.
 *
 * @author Vivek Srikumar
 */
public class ParseHeadWordPOS implements FeatureExtractor {

    public static ParseHeadWordPOS CHARNIAK = new ParseHeadWordPOS(ViewNames.PARSE_CHARNIAK);
    public static ParseHeadWordPOS STANFORD = new ParseHeadWordPOS(ViewNames.PARSE_STANFORD);

    private final String parseViewName;

    public ParseHeadWordPOS(String parseViewName) {
        this.parseViewName = parseViewName;
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

        features.add(DiscreteFeature.create("hw:" + ta.getToken(head).toLowerCase().trim()));
        features.add(DiscreteFeature.create("h-pos:" + WordHelpers.getPOS(ta, head)));

        return features;

    }

    @Override
    public String getName() {
        return "#head-pos#" + parseViewName;
    }

}
