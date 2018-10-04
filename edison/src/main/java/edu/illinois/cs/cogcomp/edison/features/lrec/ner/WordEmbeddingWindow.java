/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.ner;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.FeatureCreatorUtil;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordEmbeddings;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * For a given Constituent, sweeps a window of specified size, optionally ignoring sentence
 * boundaries, and for each relative window position, creates a feature recording that relative
 * position and any word embedding value corresponding to the token at that position.
 *
 * @keywords embedding, window, token
 * @author mssammon
 */
public class WordEmbeddingWindow implements FeatureExtractor {

    private final int windowStart;
    private final int windowEnd;
    private final boolean ignoreSentenceBoundaries;


    public WordEmbeddingWindow(int windowSize, boolean ignoreSentenceBoundaries) throws IOException {
        this.windowStart = 0 - windowSize;
        this.windowEnd = windowSize;
        this.ignoreSentenceBoundaries = ignoreSentenceBoundaries;

        WordEmbeddings.initWithDefaults();
    }


    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        Set<Feature> features = new HashSet<>();

        // get allowable window given position in text

        IntPair relativeWindow =
                FeatureCreatorUtil.getWindowSpan(c, windowStart, windowEnd,
                        ignoreSentenceBoundaries);

        int absStart = c.getStartSpan() - relativeWindow.getFirst();

        View tokens = c.getTextAnnotation().getView(ViewNames.TOKENS);

        for (int i = relativeWindow.getFirst(); i <= relativeWindow.getSecond(); ++i) {
            Constituent word = tokens.getConstituentsCoveringToken(absStart + i).get(0);
            double[] embedding = WordEmbeddings.getEmbedding(word);
            if (embedding != null) {
                for (int dim = 0; dim < embedding.length; dim++) {
                    final String[] pieces =
                            {getName(), ":", "place", Integer.toString(i), "dim",
                                    Integer.toString(dim), ":", Double.toString(embedding[dim])};
                    features.add(FeatureCreatorUtil.createFeatureFromArray(pieces));
                }
            }
            i++;
        }

        return features;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
