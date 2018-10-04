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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class AttributeFeature implements FeatureExtractor {

    private String attributeName;

    public AttributeFeature(String attributeName) {
        this.attributeName = attributeName;

    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        Set<Feature> set = new LinkedHashSet<>();
        if (c.hasAttribute(attributeName)) {
            set.add(DiscreteFeature.create(c.getAttribute(attributeName)));
        }
        return set;
    }

    @Override
    public String getName() {
        return "#" + attributeName;
    }

}
