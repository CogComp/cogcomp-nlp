/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.ConstituentFeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.BrownClusterConstituentFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkEmbedding;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetConstituentFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetConstituentFeatureExtractor.WordNetFeatureClass;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts a range fo lexical features relating to form and to WordNet information. Combines
 * {@link WordFeatureExtractorFactory} word, pos, lemma, capitalization, conflatedPOS,gerundMarker,
 * nominalizationMarker, deVerbalSuffix, deNominalNounProducingSuffixes,
 * deAdjectivalAbstractNounSuffixes, and knownPrefixes; {@link WordNetFeatureClass} existsEntry,
 * synsetsFirstSense, synsetsAllSenses, hypernymsFirstSense, hypernymsAllSenses,
 * partHolonymsFirstSense, memberHolonymsFirstSense, and substanceHolonymsFirstSense;
 * {@link ChunkEmbedding} NER; and {@link BrownClusterConstituentFeatureExtractor} instance1000.
 *
 * @keywords lexical, word, WordNet, SRL, Nominalization, nom, nominal, predicate
 * @author Xinbo Wu
 */
public class SrlWordFeatures implements FeatureExtractor<Constituent> {
    private final String name;
    private final ConstituentFeatureCollection base;

    public SrlWordFeatures() throws Exception {
        this("#WordFeatures#");
    }

    public SrlWordFeatures(String name) throws Exception {
        this.name = name;
        this.base = new ConstituentFeatureCollection(this.getName());

        this.base.addFeatureExtractor(WordFeatureExtractorFactory.word);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.lemma);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.capitalization);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.conflatedPOS);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);
        this.base.addFeatureExtractor(ChunkEmbedding.NER);
        this.base.addFeatureExtractor(BrownClusterConstituentFeatureExtractor.instance1000);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.deVerbalSuffix);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.deNominalNounProducingSuffixes);
        this.base
                .addFeatureExtractor(WordFeatureExtractorFactory.deAdjectivalAbstractNounsSuffixes);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.knownPrefixes);

        try {
            WordNetConstituentFeatureExtractor wn = new WordNetConstituentFeatureExtractor();
            wn.addFeatureType(WordNetFeatureClass.existsEntry);
            wn.addFeatureType(WordNetFeatureClass.synsetsFirstSense);
            wn.addFeatureType(WordNetFeatureClass.synsetsAllSenses);
            wn.addFeatureType(WordNetFeatureClass.hypernymsAllSenses);
            wn.addFeatureType(WordNetFeatureClass.hypernymsFirstSense);
            wn.addFeatureType(WordNetFeatureClass.partHolonymsFirstSense);
            wn.addFeatureType(WordNetFeatureClass.memberHolonymsFirstSense);
            wn.addFeatureType(WordNetFeatureClass.substanceHolonymsFirstSense);

            this.base.addFeatureExtractor(wn);
        } catch (Exception e) {
            throw new EdisonException(e);
        }
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
