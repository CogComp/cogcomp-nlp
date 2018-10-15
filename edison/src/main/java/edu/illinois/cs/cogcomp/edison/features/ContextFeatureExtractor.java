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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A ContextFeatureExtractor generates features from the <b>words</b> in the context of the
 * specified constituent. The constructor specifies the context size.
 * <p>
 * To use this class, after creating the object, add other feature extractors using the
 * {@link edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor#addFeatureExtractor(FeatureExtractor)}
 * method. Then, for each neighboring word, the feature extractor will generate features using all
 * the extractors that have been added.
 * <p>
 * Note: This class only generates features from neighboring words. To go beyond words, new code
 * needs to be written as required.
 *
 * @author Vivek Srikumar
 */
public class ContextFeatureExtractor extends FeatureCollection {

    private final boolean specifyIndex;
    private final int contextSize;
    private final boolean ignoreConstituent;

    /**
     * Create a new ContextFeatureExtractor.
     *
     * @param contextSize The number of tokens to the left and right of the constituent from which
     *        the features should be extracted.
     * @param specifyIndex Should the feature mention the index (relative to the constituent)
     * @param ignoreConstituent Should the tokens in the constituent itself be ignored while
     *        generating the features.
     */
    public ContextFeatureExtractor(int contextSize, boolean specifyIndex, boolean ignoreConstituent) {
        super("");
        this.contextSize = contextSize;
        this.specifyIndex = specifyIndex;
        this.ignoreConstituent = ignoreConstituent;
    }

    public ContextFeatureExtractor(int contextSize, boolean specifyIndex,
            boolean ignoreConstituent, WordFeatureExtractor... fex) {
        super("");
        this.contextSize = contextSize;
        this.specifyIndex = specifyIndex;
        this.ignoreConstituent = ignoreConstituent;

        for (WordFeatureExtractor f : fex)
            addFeatureExtractor(f);
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        TextAnnotation ta = c.getTextAnnotation();

        int start = c.getStartSpan() - contextSize;
        int end = c.getEndSpan() + contextSize;

        if (start < 0)
            start = 0;

        if (end >= ta.size())
            end = ta.size();

        Set<Feature> features = new LinkedHashSet<>();
        for (int i = start; i < end; i++) {

            if (ignoreConstituent)
                if (c.getStartSpan() <= i && i < c.getEndSpan())
                    continue;

            for (FeatureExtractor f : this.generators) {

                Constituent neighbor = new Constituent("TMP", "TMP", ta, i, i + 1);

                Set<Feature> feats = f.getFeatures(neighbor);

                for (Feature feat : feats) {
                    String preamble = "context";
                    if (specifyIndex) {
                        String index = "*";
                        if (i < c.getStartSpan())
                            index = (i - c.getStartSpan()) + "";
                        else if (i >= c.getEndSpan())
                            index = (i - c.getEndSpan() + 1) + "";
                        preamble += index;
                    }
                    preamble += ":";

                    features.add(feat.prefixWith(preamble + f.getName()));
                }
            }
        }
        return features;

    }

    @Override
    public String getName() {
        return "#ctxt#";
    }
}
