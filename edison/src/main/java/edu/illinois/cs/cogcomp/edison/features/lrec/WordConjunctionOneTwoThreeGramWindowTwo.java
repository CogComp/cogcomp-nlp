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
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.features.helpers.SpanLabelsHelper;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.*;

/**
 *
 * @author Paul Vijayakumar, Mazin Bokhari Extracts the k Tokens to the left and k Tokens to the
 *         right of the Constituent object. Generates a conjunction of 3-shingles from a window of 2
 *         tokens.
 *
 */
public class WordConjunctionOneTwoThreeGramWindowTwo implements FeatureExtractor {

    // Views required for feature extractor
    public static View TOKENS;

    private final String viewName;

    public WordConjunctionOneTwoThreeGramWindowTwo(String viewName) {
        this.viewName = viewName;
    }

    /**
     *
     * @param TOKENS The Tokens View of the TextAnnotation object
     * @param startspan The span at the beginning of the Constituent object
     * @param endspan The span at the end of the Constituent object
     * @param k The number of Tokens to the left and right of the current Constituent object to get
     * @return Return the window of k Tokens to the left and k Tokens to the right of the current
     *         Constituent object
     */

    public String[] getWindowKFrom(View TOKENS, int startspan, int endspan, int k) {

        String window[] = new String[2 * k + 1];

        int startwin = startspan - k;
        int endwin = endspan + k;

        if (endwin > TOKENS.getEndSpan()) {
            endwin = TOKENS.getEndSpan();
        }
        if (startwin < 0) {
            startwin = 0;
        }

        for (int i = startwin; i < endwin; i++) {

            window[i] = TOKENS.getConstituentsCoveringSpan(i, i + 1).get(0).getSurfaceForm();

        }
        return window;
    }

    @Override
    /**
     * This feature extractor assumes that the TOKEN View has been generated in the Constituents TextAnnotation. 
     * It generate a feature for a window [-2, +2] of Forms (original text) for each constituent.
     *
     **/
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        TextAnnotation ta = c.getTextAnnotation();
        TOKENS = ta.getView(ViewNames.TOKENS);

        // We can assume that the constituent in this case is a Word(Token) described by the LBJ
        // chunk definition

        int startspan = c.getStartSpan();
        int endspan = c.getEndSpan();
        int k = 2;


        // All our constituents are words(tokens)
        String[] forms = getWindowKFrom(TOKENS, startspan, endspan, 2);

        String __id, __value;
        String classifier = "WordConjunctionOneTwoThreeGramWindowTwo";
        Set<Feature> __result = new LinkedHashSet<Feature>();

        for (int j = 0; j < k; j++) {
            // k = 2, j goes from 0 to 1

            for (int i = 0; i < forms.length; i++) {
                // forms.length = 5, So i goes from 0 to 4, for each String
                // in the forms array.

                StringBuffer f = new StringBuffer();

                // Starts with context = 0 and then increments context as long as it is below
                // the current value of j and is not out of index of the forms array.
                // This is basically creating a discrete feature for each combination of one, two
                // and three word combinations within [-2,2] window or words.
                for (int context = 0; context <= j && i + context < forms.length; context++) {

                    // add a '_' between words to conjoin them together
                    if (context != 0) {
                        f.append("_");
                    }
                    f.append(forms[i + context]);
                }

                __id = classifier + ":" + (i + "_" + j);
                __value = "(" + (f.toString()) + ")";
                __result.add(new DiscreteFeature(__id + __value));
            }
        }

        return __result;
    }

    @Override
    public String getName() {
        return "#path#" + viewName;
    }
}
