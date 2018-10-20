/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

/**
 * Prefixes a base feature extractor with the lower-cased surface string of the input constituent.
 * Typically, the input constituent for this feature extractor is a single token.
 *
 * @author Vivek Srikumar
 */
public class TokenPrefixFeatureExtractor implements FeatureExtractor {

    private final FeatureExtractor base;

    public TokenPrefixFeatureExtractor(FeatureExtractor base) {
        this.base = base;

    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return FeatureUtilities.prefix(c.getTokenizedSurfaceForm().toLowerCase().trim(),
                base.getFeatures(c));
    }

    @Override
    public String getName() {
        return base.getName();
    }

}
