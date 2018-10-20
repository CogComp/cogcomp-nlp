/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;

import java.util.List;

/**
 * join String objects to form features, efficiently
 */
public class FeatureCreatorUtil {

    public static DiscreteFeature createFeatureFromArray(String[] pieces) {
        StringBuilder bldr = new StringBuilder();
        for (String str : pieces)
            bldr.append(str);

        return new DiscreteFeature(bldr.toString());
    }

    /**
     * adjust initial window start/end based on how far word context extends in terms of the
     * underlying text/sentence (depending on ignoreSentenceBoundaries flag). <b>IMPORTANT:</b> this
     * expresses a window relative to the Constituent start and end token indexes, so the end will
     * be different for a 3-token NER Constituent than for a Word Constituent.
     * 
     * @param c Constituent at "center" of window
     * @param windowStart desired relative start offset
     * @param windowEnd desired relative end offset
     * @param ignoreSentenceBoundaries if 'true', allow window to extend past a sentence boundary
     *        iff there are tokens at the relevant indexes.
     * @return a span specifying the actual relative window offsets adjusted as necessary.
     */
    public static IntPair getWindowSpan(Constituent c, int windowStart, int windowEnd,
            boolean ignoreSentenceBoundaries) {

        int startLimit = 0;
        int endLimit = c.getTextAnnotation().size();

        int start = c.getStartSpan();
        int end = c.getEndSpan();

        if (!ignoreSentenceBoundaries) {
            List<Constituent> sentences =
                    c.getTextAnnotation().getView(ViewNames.SENTENCE).getConstituentsCovering(c);
            if (sentences.isEmpty())
                throw new IllegalStateException("Constituent " + c.getSurfaceForm()
                        + " does not fall inside a Sentence for TextAnnotation with id '"
                        + c.getTextAnnotation().getId() + ".");

            startLimit = sentences.get(0).getStartSpan();
            endLimit = sentences.get(0).getEndSpan();
        }

        for (int i = 0; i > windowStart && start >= startLimit; i--)
            start--;

        for (int j = c.getEndSpan(); j < windowEnd && end < endLimit; ++j)
            end++;

        return new IntPair(start, end);
    }
}
