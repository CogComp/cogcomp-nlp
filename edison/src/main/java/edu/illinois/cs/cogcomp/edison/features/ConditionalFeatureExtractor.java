/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

public class ConditionalFeatureExtractor implements FeatureExtractor {

    private Predicate<Constituent> condition;
    private FeatureExtractor ifTrue;
    private FeatureExtractor ifFalse;

    public ConditionalFeatureExtractor(Predicate<Constituent> condition, FeatureExtractor ifTrue,
            FeatureExtractor ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        if (condition.transform(c))
            return ifTrue.getFeatures(c);
        else
            return ifFalse.getFeatures(c);
    }

    @Override
    public String getName() {
        return "";
    }

}
