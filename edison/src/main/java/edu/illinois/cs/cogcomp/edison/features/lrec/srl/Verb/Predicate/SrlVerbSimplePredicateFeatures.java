/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Predicate;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.AttributeFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkEmbedding;
import edu.illinois.cs.cogcomp.edison.features.factory.SubcategorizationFrame;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.POSContextWindowTwo;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.WordContextWindowTwo;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts features for SRL Verb predicates. Combines {@link WordContextWindowTwo};
 * {@link POSContextWindowTwo}; {@link WordFeatureExtractorFactory} word, lemma, pos,
 * capitalization, and numberNormalizer; {@link SubcategorizationFrame}; and {@link ChunkEmbedding}
 * for NER and Shallow Parse.
 *
 *
 * @author Xinbo Wu
 */
public class SrlVerbSimplePredicateFeatures implements FeatureExtractor {
    private final String name;
    private final FeatureCollection base;

    public SrlVerbSimplePredicateFeatures() {
        this("#predicateFeatures#");
    }

    public SrlVerbSimplePredicateFeatures(String name) {
        this.name = name;
        this.base = new FeatureCollection(this.getName());

        this.base.addFeatureExtractor(new WordContextWindowTwo(""));
        this.base.addFeatureExtractor(new POSContextWindowTwo(""));
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.word);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.lemma);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.capitalization);
        this.base.addFeatureExtractor(new AttributeFeature("predicate"));
        this.base.addFeatureExtractor(SubcategorizationFrame.STANFORD);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.numberNormalizer);
        this.base.addFeatureExtractor(ChunkEmbedding.SHALLOW_PARSE);
        this.base.addFeatureExtractor(ChunkEmbedding.NER);
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
