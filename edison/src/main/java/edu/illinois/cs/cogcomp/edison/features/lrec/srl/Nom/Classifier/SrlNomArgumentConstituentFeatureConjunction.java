/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Classifier;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.ConstituentFeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts conjunctions of {@link SrlNomArgumentFeatures }.
 *
 * @keywords SRL, Nom, nominalization, nominal, classifier, argument
 * @author Xinbo Wu
 */
public class SrlNomArgumentConstituentFeatureConjunction implements FeatureExtractor<Constituent> {
    private final ConstituentFeatureCollection base = new ConstituentFeatureCollection(this.getName());

    public SrlNomArgumentConstituentFeatureConjunction() throws Exception {
        this.base.addFeatureExtractor(FeatureUtilities.conjoin(new SrlNomArgumentFeatures(),
                new SrlNomArgumentFeatures()));
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return base.getFeatures(c);
    }

    @Override
    public String getName() {
        return "#NomClassifier#";
    }
}
