/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;

import java.util.HashSet;
import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class HasVerb implements FeatureExtractor {

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        boolean hasVerb = false;
        TextAnnotation ta = c.getTextAnnotation();

        for (int i = c.getStartSpan(); i < c.getEndSpan(); i++) {

            if (POSUtils.isPOSVerb(WordHelpers.getPOS(ta, i))) {
                hasVerb = true;
                break;
            }
        }
        Set<Feature> feats = new HashSet<>();

        if (hasVerb) {
            feats.add(DiscreteFeature.create(getName()));
        }
        return feats;
    }

    @Override
    public String getName() {
        return "#has-verb";
    }
}
