/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
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
 * @author Paul Vijayakumar, Mazin Bokhari
 *
 */
public class ChunkWindowThreeBefore implements FeatureExtractor {
    private static Logger logger = LoggerFactory.getLogger(ChunkWindowThreeBefore.class);

    public static View SHALLOW_PARSE, TOKENS;

    private final String viewName;

    public ChunkWindowThreeBefore(String viewName) {
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
     * This feature extractor assumes that the TOKEN View and the SHALLOW_PARSE View have been
     * generated in the Constituents TextAnnotation. It will generate discrete features from
     * the chunk labels of the previous two tokens.
     *
     **/
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        String classifier = "ChunkWindowThreeBefore";

        TextAnnotation ta = c.getTextAnnotation();

        TOKENS = ta.getView(ViewNames.TOKENS);
        SHALLOW_PARSE = ta.getView(ViewNames.SHALLOW_PARSE);

        // We can assume that the constituent in this case is a Word(Token) described by the LBJ
        // chunk definition
        int startspan = c.getStartSpan();
        int endspan = c.getEndSpan();

        // All our constituents are words(tokens)
        int k = -2; // two words before
        List<Constituent> wordstwobefore = getwordskfrom(TOKENS, startspan, endspan, k);

        String[] labels = new String[2];

        Set<Feature> result = new LinkedHashSet<Feature>();

        int i = 0;

        if (wordstwobefore.size() == 0) {
            return result;
        }

        for (Constituent token : wordstwobefore) {

            // Should only be one POS tag for each token
            List<String> Chunk_label =
                    SHALLOW_PARSE.getLabelsCoveringSpan(token.getStartSpan(), token.getEndSpan());

            if (Chunk_label.size() != 1) {
                logger.warn("Error token has more than one POS tag or Chunk Label.");
            }

            labels[i] = Chunk_label.get(0);
            String __value = "(" + labels[i] + ")";
            String __id = classifier + ":" + (i++);
            result.add(new DiscreteFeature(__id + __value));
        }

        return result;
    }

    @Override
    public String getName() {
        return "#path#" + viewName;
    }
}
