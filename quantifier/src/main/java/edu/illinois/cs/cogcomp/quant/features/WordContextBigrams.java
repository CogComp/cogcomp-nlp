/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

public class WordContextBigrams extends LBJavaFeatureExtractor {

    private static final long serialVersionUID = 1L;

    @Override
    public Set<Feature> getFeatures(Constituent instance) throws EdisonException {
        ContextFeatureExtractor f = new ContextFeatureExtractor(2, true, true);
        f.addFeatureExtractor(WordFeatureExtractorFactory.word);
        return f.getFeatures(instance);
    }
}
