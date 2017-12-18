/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.HashSet;
import java.util.Set;

/**
 * Extracts features for a full TextAnnotation
 * @author Daniel Khashabi
 */
//public class SimpleLabelExtractor implements FeatureExtractor<Constituent> {
//
//    @Override
//    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
//        new HashSet();
//        new DiscreteFeature(c.getLabel());
//        return ;
//    }
//
//    @Override
//    public String getName() {
//        return "#simple-lavek-feat#";
//    }
//
//}
