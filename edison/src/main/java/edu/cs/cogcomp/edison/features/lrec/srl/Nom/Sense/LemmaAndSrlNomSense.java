/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Sense;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.SrlSenseFeatures;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts a conjunction of {@link WordFeatureExtractorFactory} lemma and {@link SrlSenseFeatures}.
 *
 * @keywords lemma, SRL, Nominalization, nominal, nom, sense, conjunction
 * @author Xinbo Wu
 */
public class LemmaAndSrlNomSense implements FeatureExtractor {
    private final FeatureCollection base = new FeatureCollection(this.getName());

    public LemmaAndSrlNomSense() throws Exception {
        this.base.addFeatureExtractor(FeatureUtilities.conjoin(WordFeatureExtractorFactory.lemma,
                new SrlSenseFeatures()));
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return base.getFeatures(c);
    }

    @Override
    public String getName() {
        return "#NomSense#";
    }
}
