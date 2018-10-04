/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.edison.features.ChunkPropertyIndicator;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;

/**
 * A collection of features that extract properties from chunks.
 *
 * @author Vivek Srikumar
 */
@SuppressWarnings("serial")
public class ChunkPropertyFeatureFactory {

    private static Predicate<Constituent> isNegatedPredicate = new Predicate<Constituent>() {

        @Override
        public Boolean transform(Constituent input) {

            TextAnnotation ta = input.getTextAnnotation();
            boolean found = false;
            for (int i = input.getStartSpan(); i < input.getEndSpan(); i++) {
                if (ta.getTokens()[i].equals("not") || (ta.getTokens()[i].equals("n't"))) {
                    found = true;
                    break;
                }
            }
            return found;

        }
    };
    public static ChunkPropertyIndicator isNegated = new ChunkPropertyIndicator(
            ViewNames.SHALLOW_PARSE, "neg?", isNegatedPredicate);
    private static Predicate<Constituent> hasModalVerbPredicate = new Predicate<Constituent>() {

        @Override
        public Boolean transform(Constituent input) {
            TextAnnotation ta = input.getTextAnnotation();

            boolean found = false;
            for (int i = input.getStartSpan(); i < input.getEndSpan(); i++) {
                if (WordHelpers.getPOS(ta, i).equals("MD")) {
                    found = true;
                    break;
                }
            }
            return found;
        }
    };
    public static ChunkPropertyIndicator hasModalVerb = new ChunkPropertyIndicator(
            ViewNames.SHALLOW_PARSE, "modal?", hasModalVerbPredicate);
}
