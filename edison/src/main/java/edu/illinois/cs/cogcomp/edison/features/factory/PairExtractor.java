/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.HashSet;
import java.util.Set;

/** Given two feature extractors, for two
 * this is good when you want to find overlap between two feature representations
 * @author daniel
 */
public abstract class PairExtractor<T1, T2> implements FeatureExtractor<Constituent> {

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        throw new EdisonException("this function shouldn't be used");
    }

    /**
     * @throws EdisonException
     */
    public abstract Set<Feature> getCombinedFeatures(T1 c1, T2 c2) throws EdisonException;

    @Override
    public abstract String getName();
}