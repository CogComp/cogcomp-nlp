/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Classifier;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.AttributeFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.NomLexClassFeature;
import edu.illinois.cs.cogcomp.edison.features.factory.SubcategorizationFrame;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.POSContextWindowTwo;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.WordContextWindowTwo;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts a collection of lexical and parse structure features to classify Nominal SRL arguments.
 * Uses {@link WordFeatureExtractorFactory} word, pos, lemma, and capitalization;
 * {@link WordContextWindowTwo}; {@link POSContextWindowTwo}; {@link AttributeFeature};
 * {@link SubcategorizationFrame}; {@link NomLexClassFeature};
 *
 * @keywords SRL, Nom, nominal, predicate, classifier
 * @author Xinbo Wu
 */
public class SrlNomClassifierPredicateFeatures implements FeatureExtractor {
    private final String name;
    private final FeatureCollection base;

    public SrlNomClassifierPredicateFeatures() {
        this("#predicateFeatures#");
    }

    public SrlNomClassifierPredicateFeatures(String name) {
        this.name = name;
        this.base = new FeatureCollection(this.getName());

        this.base.addFeatureExtractor(WordFeatureExtractorFactory.word);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.lemma);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.capitalization);

        this.base.addFeatureExtractor(new WordContextWindowTwo(""));
        this.base.addFeatureExtractor(new POSContextWindowTwo(""));

        this.base.addFeatureExtractor(new AttributeFeature("predicate"));

        this.base.addFeatureExtractor(SubcategorizationFrame.STANFORD);
        this.base.addFeatureExtractor(NomLexClassFeature.instance);
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
