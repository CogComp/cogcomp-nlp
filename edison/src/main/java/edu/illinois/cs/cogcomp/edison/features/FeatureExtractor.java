package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

/**
 * An interface that specifies what a feature extractor should do.
 * <p>
 * In general, a feature extractor looks at a {@code Constituent} of a {@code
 * TextAnnotation} and generates a set of strings as features for that
 * constituent.
 * 
 * @author Vivek Srikumar
 * 
 */
public interface FeatureExtractor {
    Set<Feature> getFeatures(Constituent c) throws EdisonException;

    String getName();
}
