/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.common;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

import java.util.List;

/**
 * Created by mssammon on 2/1/16.
 */
public class HandlerUtils {
    /**
     * check whether sentences in TextAnnotation respect a sentence length limit. If not, return the
     * constituent corresponding to the first such sentence, otherwise return null.
     * 
     * @param ta TextAnnotation to check
     * @return Constituent corresponding to first illegal sentence, null if none found.
     */
    public static Constituent checkTextAnnotationRespectsSentenceLengthLimit(TextAnnotation ta,
            int maxSentenceLength) {
        List<Constituent> sentences = ta.getView(ViewNames.SENTENCE).getConstituents();

        Constituent illegalSentence = null;

        for (Constituent c : sentences) {
            if ((maxSentenceLength > 0) && (c.getEndSpan() - c.getStartSpan() > maxSentenceLength)) {
                illegalSentence = c;
                break;
            }

        }
        return illegalSentence;
    }


    /**
     * generate an error message for sentence length error.
     * 
     * @param taId Id of text from which sentence came
     * @param sentence sentence text
     * @param maxSentenceLength length limit
     * @return
     */
    public static String getSentenceLengthError(String taId, String sentence, int maxSentenceLength) {
        String msg =
                "Unable to parse TextAnnotation " + taId
                        + " since it is larger than the maximum sentence length of the parser ("
                        + maxSentenceLength + "). Sentence is: '" + sentence + "'.";

        return msg;
    }
}
