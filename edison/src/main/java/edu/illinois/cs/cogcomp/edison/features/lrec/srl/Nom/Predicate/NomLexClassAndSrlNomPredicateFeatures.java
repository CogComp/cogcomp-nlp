/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Predicate;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.ConstituentFeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.features.factory.NomLexClassConstituentFeature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Conjoins {@link NomLexClassConstituentFeature} with {@link SrlNomPredicateFeatures}. Sets identifier to
 * indicate it is used for Nominal predicates (as opposed to verb or other predicates).
 *
 * @keywords NomLex, SRL, Nominalization, nominal, nom
 * @author Xinbo Wu
 */
public class NomLexClassAndSrlNomPredicateFeatures implements FeatureExtractor<Constituent> {
    private final ConstituentFeatureCollection base = new ConstituentFeatureCollection(this.getName());

    public NomLexClassAndSrlNomPredicateFeatures() {
        this.base.addFeatureExtractor(FeatureUtilities.conjoin(NomLexClassConstituentFeature.instance,
                new SrlNomPredicateFeatures("")));
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return base.getFeatures(c);
    }

    @Override
    public String getName() {
        return "#NomPredicate#";
    }
}
