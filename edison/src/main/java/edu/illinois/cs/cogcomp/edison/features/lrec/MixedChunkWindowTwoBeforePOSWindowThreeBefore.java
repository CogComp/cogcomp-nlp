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
 * Returns conjunctions of previous three POSTags and previous two ChunkLabels
 * 
 * @author Paul Vijayakumar, Mazin Bokhari
 *
 */
public class MixedChunkWindowTwoBeforePOSWindowThreeBefore implements FeatureExtractor {
    private static Logger logger = LoggerFactory.getLogger(MixedChunkWindowTwoBeforePOSWindowThreeBefore.class);

    public static View SHALLOW_PARSE, POS, TOKENS;

    private final String viewName;

    public MixedChunkWindowTwoBeforePOSWindowThreeBefore(String viewName) {
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
     * This feature extractor assumes that the TOKEN View, POS View and the SHALLOW_PARSE View have been
     * generated in the Constituents TextAnnotation. It will use its own POS tag and well as the POS tag
     * and the SHALLOW_PARSE (Chunk) labels of the previous two tokens and return it as a discrete feature. 
     *
     **/
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        TextAnnotation ta = c.getTextAnnotation();

        try {
            TOKENS = ta.getView(ViewNames.TOKENS);
            POS = ta.getView(ViewNames.POS);
            SHALLOW_PARSE = ta.getView(ViewNames.SHALLOW_PARSE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // We can assume that the constituent in this case is a Word(Token) described by the LBJ
        // chunk definition
        int startspan = c.getStartSpan();
        int endspan = c.getEndSpan();

        // All our constituents are words(tokens)
        int k = -2; // words two before
        List<Constituent> wordstwobefore = getwordskfrom(TOKENS, startspan, endspan, k);

        if (wordstwobefore.size() != 2)
            return null;

        String[] tags = new String[3];
        String[] labels = new String[2];

        int i = 0;
        for (Constituent token : wordstwobefore) {

            // Should only be one POS tag for each token
            List<String> POS_tag =
                    POS.getLabelsCoveringSpan(token.getStartSpan(), token.getEndSpan());
            List<String> Chunk_label =
                    SHALLOW_PARSE.getLabelsCoveringSpan(token.getStartSpan(), token.getEndSpan());

            if (POS_tag.size() != 1 || Chunk_label.size() != 1) {
                logger.warn("Error token has more than one POS tag or Chunk Label.");
            }

            labels[i] = Chunk_label.get(0);
            tags[i] = POS_tag.get(0);
            i++;
        }
        tags[i] = POS.getLabelsCoveringSpan(startspan, endspan).get(0);

        Set<Feature> __result = new LinkedHashSet<Feature>();

        String classifier = "MixedChunkWindowTwoBeforePOSWindowThreeBefore";
        String __id = classifier + ":" + "ll";
        String __value = "(" + (labels[0] + "_" + labels[1]) + ")";
        /*
         * BufferedWriter output = null; try { File file = new
         * File("/home/pvijaya2/feat-output.txt");
         * 
         * if(!file.exists()){ file.createNewFile(); }
         * 
         * FileWriter fw = new FileWriter(file,true);
         * 
         * //BufferedWriter writer give better performance BufferedWriter bw = new
         * BufferedWriter(fw);
         */
        logger.info(__id + __value);
        __result.add(new DiscreteFeature(__id + __value));

        __id = classifier + ":" + "lt1";
        __value = "(" + (labels[0] + "_" + tags[1]) + ")";
        logger.info(__id + __value);
        __result.add(new DiscreteFeature(__id + __value));

        __id = classifier + ":" + "lt2";
        __value = "" + (labels[1] + "_" + tags[2]);
        logger.info(__id + __value);
        __result.add(new DiscreteFeature(__id + __value));

        // Closing BufferedWriter Stream
        /*
         * bw.close();
         * 
         * } catch ( IOException e ) { e.printStackTrace(); }
         */

        return __result;
    }

    @Override
    public String getName() {
        return "#path#" + viewName;
    }
}
