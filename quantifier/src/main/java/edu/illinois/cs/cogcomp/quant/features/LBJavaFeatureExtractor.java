/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;

import java.util.Set;

/**
 * A simple wrapper for {@code LBJava}-based feature extractors
 */
public abstract class LBJavaFeatureExtractor extends Classifier {

    private static final long serialVersionUID = 1L;

    @Override
    public String getOutputType() {
        return "discrete%";
    }

    @Override
    public FeatureVector classify(Object o) {
        // Make sure the object is a Constituent
        if (!(o instanceof Constituent))
            throw new IllegalArgumentException("Instance must be of type Constituent");
        Constituent instance = (Constituent) o;

        FeatureVector featureVector;
        try {
            featureVector = FeatureUtilities.getLBJFeatures(getFeatures(instance));
        } catch (EdisonException e) {
            throw new RuntimeException(e);
        }
        return featureVector;
    }

    public abstract Set<Feature> getFeatures(Constituent instance) throws EdisonException;
}
