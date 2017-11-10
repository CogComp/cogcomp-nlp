/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.NomLexClassConstituentFeature;
import edu.illinois.cs.cogcomp.edison.features.factory.SubcategorizationFrame;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.Set;


/**
 *
 * @keywords semantic role labeling, srl, nominal, nom, identifier, predicate
 * @author Xinbo Wu
 */
public class SrlNomIdentifierPredicateFeatures implements FeatureExtractor<Constituent> {
    private final String name;
    private final ConstituentFeatureCollection base;

    public SrlNomIdentifierPredicateFeatures() {
        this("#predicateFeatures#");
    }

    public SrlNomIdentifierPredicateFeatures(String name) {
        this.name = name;
        this.base = new ConstituentFeatureCollection(this.getName());

        ArrayList<CachedConstituentFeatureCollection> tmp = new ArrayList<CachedConstituentFeatureCollection>();

        tmp.add(new CachedConstituentFeatureCollection(""));
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.word);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.pos);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.lemma);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.capitalization);

        tmp.get(0).addFeatureExtractor(new AttributeConstituentFeature("predicate"));

        tmp.get(0).addFeatureExtractor(SubcategorizationFrame.STANFORD);
        tmp.get(0).addFeatureExtractor(NomLexClassConstituentFeature.instance);

        tmp.add(new CachedConstituentFeatureCollection("", FeatureInputTransformer.constituentParent, tmp
                .get(0)));

        this.base.addFeatureExtractor(tmp.get(1));
    }


    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return base.getFeatures(c);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
