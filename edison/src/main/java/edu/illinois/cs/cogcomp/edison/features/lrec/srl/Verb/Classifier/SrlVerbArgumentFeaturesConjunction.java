/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Classifier;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts conjoined {@link SrlVerbArgumentFeatures}.
 *
 * @keywords SRL, Verb, conjunction, argument, classifier
 * @author Xinbo Wu
 */
public class SrlVerbArgumentFeaturesConjunction implements FeatureExtractor {
    private final FeatureCollection base = new FeatureCollection(this.getName());

    public SrlVerbArgumentFeaturesConjunction() {
        this.base.addFeatureExtractor(FeatureUtilities.conjoin(new SrlVerbArgumentFeatures(),
                new SrlVerbArgumentFeatures()));
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return base.getFeatures(c);
    }

    @Override
    public String getName() {
        return "#VerbClassifier#";
    }
}
