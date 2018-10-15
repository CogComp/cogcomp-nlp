/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

/**
 * Features of a word.
 * <p>
 * If the input constituent is not a word, then the feature extractor can do one of two things: If a
 * flag is set in the constructor, then it will generate features from the last word of the
 * constituent. If the flag is not set, then it will throw a {@link EdisonException}.
 *
 * @author Vivek Srikumar
 */
public abstract class WordFeatureExtractor implements FeatureExtractor {

    private final boolean useLastWordOfMultiwordConstituents;

    /**
     * Creates a new WordFeatureExtractor. If parameter {@code useLastWordOfMultiwordConstituents}
     * is {@code true}, then the feature extractor will generate features from the last word of
     * multi-word constituents. If it is not true, then the feature extractor will throw an
     * exception on seeing a multi-word constituent.
     * <p>
     * It is probably safest to the parameter to {@code true}. This will provide a check to ensure
     * that the WordFeatureExtractor only sees words.
     */
    public WordFeatureExtractor(boolean useLastWordOfMultiwordConstituents) {
        this.useLastWordOfMultiwordConstituents = useLastWordOfMultiwordConstituents;
    }

    /**
     * Creates a new {@link edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor}. This
     * constructor is equivalent to calling {@code new WordFeatureExtractor(true)}.
     *
     * @see edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor#WordFeatureExtractor(boolean)
     */
    public WordFeatureExtractor() {
        this(true);
    }

    public static WordFeatureExtractor convertToWordFeatureExtractor(final FeatureExtractor fex) {
        return new WordFeatureExtractor() {

            @Override
            public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition)
                    throws EdisonException {
                return fex.getFeatures(new Constituent("", "", ta, wordPosition, wordPosition + 1));
            }
        };
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        final int wordPosition;
        if (c.size() != 1) {
            if (useLastWordOfMultiwordConstituents)
                wordPosition = c.getEndSpan() - 1;
            else
                throw new EdisonException("Input \"" + c.getTokenizedSurfaceForm()
                        + "\" to WordFeatureExtractor is not a single word. ");
        } else
            wordPosition = c.getStartSpan();

        return getWordFeatures(c.getTextAnnotation(), wordPosition);

    }

    public abstract Set<Feature> getWordFeatures(TextAnnotation ta, final int wordPosition)
            throws EdisonException;

    public String getName() {
        return "#word#";
    }
}
