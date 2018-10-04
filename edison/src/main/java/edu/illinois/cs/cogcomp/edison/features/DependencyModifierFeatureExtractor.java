/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds modifiers from a specified dependency view.
 *
 * @author Vivek Srikumar
 */
public class DependencyModifierFeatureExtractor implements FeatureExtractor {

    private final FeatureExtractor baseFex;
    private final FeatureInputTransformer dependencyHeadIdentifier;

    public DependencyModifierFeatureExtractor(String dependencyViewName, FeatureExtractor baseFex)
            throws EdisonException {
        switch (dependencyViewName) {
            case ViewNames.DEPENDENCY:
                this.dependencyHeadIdentifier = FeatureInputTransformer.easyFirstDependencyHead;
                break;
            case ViewNames.DEPENDENCY_STANFORD:
                this.dependencyHeadIdentifier = FeatureInputTransformer.stanfordDependencyHead;
                break;
            default:
                this.dependencyHeadIdentifier = null;
                break;
        }
        this.baseFex = baseFex;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        List<Constituent> parents = dependencyHeadIdentifier.transform(c);
        Set<Feature> features = new LinkedHashSet<>();
        if (parents.size() == 0) {

            Constituent parent = parents.get(0);
            for (Relation out : parent.getOutgoingRelations()) {
                String label = out.getRelationName();

                if (label.contains("det") || label.contains("mod") || label.contains("number")) {

                    features.addAll(FeatureUtilities.prefix(label,
                            baseFex.getFeatures(out.getTarget())));

                }
            }
        }
        return features;
    }

    @Override
    public String getName() {
        return "#dep-mod";
    }

}
