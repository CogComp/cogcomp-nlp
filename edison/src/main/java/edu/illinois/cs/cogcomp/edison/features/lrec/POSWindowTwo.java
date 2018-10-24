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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * Generates features for POSTags of window size 2 from given Constituent
 * 
 * @author Paul Vijayakumar, Mazin Bokhari
 *
 */
public class POSWindowTwo implements FeatureExtractor {
    private static Logger logger = LoggerFactory.getLogger(POSWindowTwo.class);

    private final String viewName;

    public POSWindowTwo(String viewName) {
        this.viewName = viewName;
    }

    public String[] getwindowtagskfrom(View TOKENS, View POS, int startspan, int endspan, int k) {

        String tags[] = new String[2 * k + 1];

        int startwin = startspan - k;
        int endwin = endspan + k;

        if (endwin > TOKENS.getEndSpan()) {
            endwin = TOKENS.getEndSpan();
        }
        if (startwin < 0) {
            startwin = 0;
        }

        for (int i = startwin; i < endwin; i++) {

            tags[i] = POS.getLabelsCoveringSpan(i, i + 1).get(0);

        }
        return tags;
    }

    @Override
    /**
     * This feature extractor assumes that the TOKEN View, POS View have been
     * generated in the Constituents TextAnnotation. It will use its own POS tag of the
     * two context words before and after the constituent.
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

        String[] tags = getwindowtagskfrom(TOKENS, POS, startspan, endspan, k);

        String classifier = "POSWindowTwo";
        String __id, __value;
        Set<Feature> __result = new LinkedHashSet<Feature>();

        for (int i = 0; i < tags.length; i++) {

            if (tags[i] == null) {
                continue;
            } else {
                __id = classifier + ":" + i;
                __value = "(" + tags[i] + ")";
                logger.info(__id + __value);
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
