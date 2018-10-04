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
 * Extracts only the label of the phrase. The phrase is defined as the smallest node in the parse
 * tree that contains the candidate. If there is more than one such node, the lowest one is chosen.
 * <p>
 * If possible, use one of the two static objects in this class, for the Charniak and Stanford
 * parses respectively.
 *
 * @author Vivek Srikumar
 */
public class ParsePhraseTypeOnly implements FeatureExtractor {

    public static ParsePhraseTypeOnly CHARNIAK = new ParsePhraseTypeOnly(ViewNames.PARSE_CHARNIAK);
    public static ParsePhraseTypeOnly STANFORD = new ParsePhraseTypeOnly(ViewNames.PARSE_STANFORD);

    private final String parseViewname;

    /**
     * Create a phrase type feature extractor
     *
     * @param parseViewname The name of the parse view that supplies the phrases. This view has to
     *        be a {@link TreeView}.
     */
    public ParsePhraseTypeOnly(String parseViewname) {
        this.parseViewname = parseViewname;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();

        TreeView tree = (TreeView) ta.getView(parseViewname);

        Constituent phrase;
        try {
            phrase = tree.getParsePhrase(c);
        } catch (Exception e) {
            throw new EdisonException(e);
        }

        Set<Feature> features = new LinkedHashSet<>();

        if (phrase != null)
            features.add(DiscreteFeature.create(phrase.getLabel()));

        return features;
    }

    @Override
    public String getName() {
        return "#phrase-only#" + parseViewname;
    }

}
