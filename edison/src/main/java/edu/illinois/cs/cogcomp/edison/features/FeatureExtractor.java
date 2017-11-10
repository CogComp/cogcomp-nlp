package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

/**
 * An interface that specifies what a feature extractor should do.
 * <p>
 * In general, a feature extractor looks at a {@code Constituent} of a {@code TextAnnotation} and
 * generates a set of strings as features for that constituent.
 *
 * @author Vivek Srikumar
 * @author Daniel Khashabi
 *
 */
public interface FeatureExtractor<T> {
    Set<Feature> getFeatures(T c) throws EdisonException;
    String getName();
}
