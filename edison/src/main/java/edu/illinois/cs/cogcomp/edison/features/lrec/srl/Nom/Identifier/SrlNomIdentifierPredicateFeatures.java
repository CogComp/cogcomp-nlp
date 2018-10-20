/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.NomLexClassFeature;
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
public class SrlNomIdentifierPredicateFeatures implements FeatureExtractor {
    private final String name;
    private final FeatureCollection base;

    public SrlNomIdentifierPredicateFeatures() {
        this("#predicateFeatures#");
    }

    public SrlNomIdentifierPredicateFeatures(String name) {
        this.name = name;
        this.base = new FeatureCollection(this.getName());

        ArrayList<CachedFeatureCollection> tmp = new ArrayList<CachedFeatureCollection>();

        tmp.add(new CachedFeatureCollection(""));
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.word);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.pos);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.lemma);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.capitalization);

        tmp.get(0).addFeatureExtractor(new AttributeFeature("predicate"));

        tmp.get(0).addFeatureExtractor(SubcategorizationFrame.STANFORD);
        tmp.get(0).addFeatureExtractor(NomLexClassFeature.instance);

        tmp.add(new CachedFeatureCollection("", FeatureInputTransformer.constituentParent, tmp
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
