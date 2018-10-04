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
 * @author Paul Vijayakumar, Mazin Bokhari
 *
 */
public class WordTypeInformation implements FeatureExtractor {

    // Views required for feature extractor
    public static View TOKENS;

    private final String viewName;

    public WordTypeInformation(String viewName) {
        this.viewName = viewName;
    }

    public String[] getwindowkfrom(View TOKENS, int startspan, int endspan, int k) {

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

            window[i - startwin] =
                    TOKENS.getConstituentsCoveringSpan(i, i + 1).get(0).getSurfaceForm();

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
        String[] forms = getwindowkfrom(TOKENS, startspan, endspan, 2);

        String __id, __value;
        String classifier = "WordTypeInformation";
        Set<Feature> __result = new LinkedHashSet<Feature>();

        for (int i = 0; i < forms.length; i++) {

            if (forms[i] != null) {

                boolean allCapitalized = true, allDigits = true, allNonLetters = true;

                for (int j = 0; j < forms[i].length(); ++j) {

                    allCapitalized &= Character.isUpperCase(forms[i].charAt(j));
                    allDigits &= Character.isDigit(forms[i].charAt(j));
                    allNonLetters &= !Character.isLetter(forms[i].charAt(j));

                }
                __id = classifier + ":" + ("c" + i);
                __value = "(" + (allCapitalized) + ")";
                __result.add(new DiscreteFeature(__id + __value));
                __id = classifier + ":" + ("d" + i);
                __value = "(" + (allDigits) + ")";
                __result.add(new DiscreteFeature(__id + __value));
                __id = classifier + ":" + ("c" + i);
                __value = "(" + (allNonLetters) + ")";
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
