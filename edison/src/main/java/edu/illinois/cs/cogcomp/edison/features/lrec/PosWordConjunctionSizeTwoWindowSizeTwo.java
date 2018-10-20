/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Extracts the POS Tags as well as the form (text) of tokens 2 before and 2 after from the given
 * token and generates a discrete feature from it.
 *
 * @keywords pos-tagger, words, tokens, window
 * @author Paul Vijayakumar, Mazin Bokhari, Christos Christodoulopoulos
 */
public class PosWordConjunctionSizeTwoWindowSizeTwo implements FeatureExtractor {

    private final String viewName;

    public PosWordConjunctionSizeTwoWindowSizeTwo(String viewName) {
        this.viewName = viewName;
    }

    /**
     * Extracts an array of tokens from a uniform window of size k
     *
     * @param TOKENS The tokens {@link View} of the {@link TextAnnotation} object
     * @param startspan The span at the beginning of the {@link Constituent} object
     * @param endspan The span at the end of the {@link Constituent} object
     * @param k The number of tokens to the left and right of the current {@link Constituent} object
     *        to get
     * @return The window of k tokens to the left and k tokens to the right of the current
     *         {@link Constituent} object
     */
    private String[] getWindowK(View TOKENS, int startspan, int endspan, int k) {
        String window[] = new String[2 * k + 1];

        int startwin = startspan - k;
        int endwin = endspan + k;

        if (endwin > TOKENS.getEndSpan()) {
            endwin = TOKENS.getEndSpan();
        }
        if (startwin < 0) {
            startwin = 0;
        }

        int index = 0;
        for (int i = startwin; i < endwin; i++) {
            window[index] = TOKENS.getConstituentsCoveringSpan(i, i + 1).get(0).getSurfaceForm();
            index++;
        }
        return window;
    }

    /**
     * Extracts an array of POS-tags from a uniform window of size k
     *
     * @param POS The part-of-speech {@link View} of the {@link TextAnnotation} object
     * @param startspan The span at the beginning of the {@link Constituent} object
     * @param endspan The span at the end of the {@link Constituent} object
     * @param k The number of tokens to the left and right of the current {@link Constituent} object
     *        to get
     * @return The window of k POS-tags to the left and k tokens to the right of the current
     *         {@link Constituent} object
     */
    private String[] getWindowKTags(View POS, int startspan, int endspan, int k) {
        String tags[] = new String[2 * k + 1];

        int startwin = startspan - k;
        int endwin = endspan + k;

        if (endwin > POS.getEndSpan()) {
            endwin = POS.getEndSpan();
        }
        if (startwin < 0) {
            startwin = 0;
        }

        int index = 0;
        for (int i = startwin; i < endwin; i++) {
            tags[index] = POS.getLabelsCoveringSpan(i, i + 1).get(0);
            index++;
        }
        return tags;
    }

    @Override
    /**
     * This feature extractor assumes that the TOKEN View, POS View have been
     * generated in the Constituents TextAnnotation. It will use its own POS tag and well 
     * as the form of the word as a forms of the words around the constitent a 
     *
     **/
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();

        View TOKENS = null, POS = null;

        try {
            TOKENS = ta.getView(ViewNames.TOKENS);
            POS = ta.getView(ViewNames.POS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // We can assume that the constituent in this case is a Word(Token) described by the LBJ
        // chunk definition
        int startspan = c.getStartSpan();
        int endspan = c.getEndSpan();

        // All our constituents are words(tokens)
        int k = 2; // words two before & after
        int window = 2;

        String[] forms = getWindowK(TOKENS, startspan, endspan, k);
        String[] tags = getWindowKTags(POS, startspan, endspan, k);

        String classifier = "PosWordConjunctionSizeTwoWindowSizeTwo";
        String id, value;
        Set<Feature> result = new LinkedHashSet<>();

        for (int j = 0; j < k; j++) {
            for (int i = 0; i < tags.length; i++) {
                StringBuilder f = new StringBuilder();
                for (int context = 0; context <= j && i + context < tags.length; context++) {
                    if (context != 0) {
                        f.append("_");
                    }
                    f.append(tags[i + context]);
                    f.append("-");
                    f.append(forms[i + context]);
                }
                // 2 is the center object in the array so i should go from -2 to +2 (with 0 being
                // the center)
                // j is the size of the n-gram so it goes 1 to 2
                id = classifier + ":" + ((i - window) + "_" + (j + 1));
                value = "(" + (f.toString()) + ")";
                result.add(new DiscreteFeature(id + value));
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "#path#" + viewName;
    }
}
