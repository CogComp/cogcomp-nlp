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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
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

/**
 *
 * @author Paul Vijayakumar, Mazin Bokhari Generates a 3-shingle of POSTags in a window of size 3
 *
 */
public class POSandPositionWindowThree implements FeatureExtractor {
    private static Logger logger = LoggerFactory.getLogger(POSandPositionWindowThree.class);

    public static View POS, TOKENS;

    private final String viewName;

    public POSandPositionWindowThree(String viewName) {
        this.viewName = viewName;
    }

    public List<Constituent> getwordskfrom(View TOKENS, int startspan, int endspan, int k) {

        // This assumes that span is only representing a token
        if (k == 0) {


            return TOKENS.getConstituentsCoveringSpan(startspan, endspan);

        } else if (k < 0) {

            int kprevindex = startspan + k;

            // Checking the token index specified by kprevindex is
            // valid (i.e. non-negative)

            if (kprevindex < 0) {
                kprevindex = 0;
            }

            return TOKENS.getConstituentsCoveringSpan(kprevindex, startspan);

        } else {

            int knextindex = endspan + k;

            // Checking the token index specified by kprevindex is
            // valid (i.e. non-negative)

            if (knextindex > TOKENS.getEndSpan()) {
                knextindex = TOKENS.getEndSpan();
            }

            return TOKENS.getConstituentsCoveringSpan(endspan, knextindex);

        }
    }

    @Override
    /**
     * This feature extractor assumes that the TOKEN View and POS View have been
     * generated in the Constituents TextAnnotation. It will use its own POS tag and well as the POS tag
     * and the SHALLOW_PARSE (Chunk) labels of the previous two tokens and return it as a discrete feature.
     *
     **/
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        String classifier = "POSandPositionWindowThree";

        TextAnnotation ta = c.getTextAnnotation();

        TOKENS = ta.getView(ViewNames.TOKENS);
        POS = ta.getView(ViewNames.POS);

        // We can assume that the constituent in this case is a Word(Token) described by the LBJ
        // chunk definition
        int startspan = c.getStartSpan();
        int endspan = c.getEndSpan();

        int before = 3;
        int after = 3;

        // All our constituents are words(tokens)
        String[] tags = new String[before + after + 1];

        int k = -3; // three words before
        List<Constituent> wordsthreebefore = getwordskfrom(TOKENS, startspan, endspan, k);

        int i = 0;
        for (Constituent token : wordsthreebefore) {

            // Should only be one POS tag for each token
            List<String> POS_tag =
                    POS.getLabelsCoveringSpan(token.getStartSpan(), token.getEndSpan());

            if (POS_tag.size() != 1) {
                logger.warn("Error token has more than one POS tag.");
            }

            tags[i] = POS_tag.get(0);
            i++;
        }

        tags[i] = POS.getLabelsCoveringSpan(c.getStartSpan(), c.getEndSpan()).get(0);
        i++;

        k = 3; // three words after
        List<Constituent> wordsthreeafter = getwordskfrom(TOKENS, startspan, endspan, k);

        for (Constituent token : wordsthreeafter) {

            // Should only be one POS tag for each token
            List<String> POS_tag =
                    POS.getLabelsCoveringSpan(token.getStartSpan(), token.getEndSpan());

            if (POS_tag.size() != 1) {
                logger.info("Error token has more than one POS tag.");
            }

            tags[i] = POS_tag.get(0);
            i++;
        }

        Set<Feature> __result = new LinkedHashSet<Feature>();

        String __id;
        String __value;
        int contextmax = 3;
        for (int j = 0; j < contextmax; j++) {
            for (i = 0; i < tags.length; i++) {
                StringBuffer f = new StringBuffer();
                for (int context = 0; context <= j && i + context < tags.length; context++) {
                    if (context != 0) {
                        f.append("_");
                    }
                    f.append(tags[i + context]);
                }
                __id = "" + (i + "_" + j);
                __value = "" + (f.toString());
                __result.add(new DiscreteFeature(classifier + ":" + __id + "(" + __value + ")"));
            }
        }

        return __result;
    }

    @Override
    public String getName() {
        return "#path#" + viewName;
    }
}
