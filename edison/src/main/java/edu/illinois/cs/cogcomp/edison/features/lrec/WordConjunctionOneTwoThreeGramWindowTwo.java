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
 * Extracts the k tokens to the left and k tokens to the right of the {@link Constituent} object.
 * Generates a conjunction of 3-shingles from a window of 2 tokens.
 *
 * @keywords chunker, shallow-parser, pos-tagger, words, tokens, window, trigram
 * @author Paul Vijayakumar, Mazin Bokhari, Christos Christodoulopoulos
 */
public class WordConjunctionOneTwoThreeGramWindowTwo implements FeatureExtractor {

    // Views required for feature extractor
    public static View TOKENS;

    private final String viewName;

    public WordConjunctionOneTwoThreeGramWindowTwo(String viewName) {
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

    @Override
    /**
     * This feature extractor assumes that the TOKEN View has been generated in the Constituents TextAnnotation. 
     * It generate a feature for a window [-2, +2] of Forms (original text) for each constituent.
     *
     **/
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();
        TOKENS = ta.getView(ViewNames.TOKENS);

        // We can assume that the constituent in this case is a Word(Token)
        int startspan = c.getStartSpan();
        int endspan = c.getEndSpan();
        // k is 3 since we need up to 3-grams
        int k = 3;
        int window = 2;

        // All our constituents are words(tokens)
        String[] forms = getWindowK(TOKENS, startspan, endspan, window);

        String id, value;
        String classifier = "WordConjunctionOneTwoThreeGramWindowTwo";
        Set<Feature> result = new LinkedHashSet<>();

        for (int j = 0; j < k; j++) {
            // k = 3, j goes from 0 to 2

            for (int i = 0; i < forms.length; i++) {
                // forms.length = 5, So i goes from 0 to 4, for each String in the forms array.

                StringBuilder f = new StringBuilder();

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

                // 2 is the center object in the array so i should go from -2 to +2 (with 0 being
                // the center)
                // j is the size of the n-gram so it goes 1 to 3
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
