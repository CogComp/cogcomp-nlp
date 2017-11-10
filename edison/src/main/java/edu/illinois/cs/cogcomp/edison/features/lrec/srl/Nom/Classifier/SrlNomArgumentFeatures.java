/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Classifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetConstituentFeatureExtractor.WordNetFeatureClass;
import edu.illinois.cs.cogcomp.edison.features.lrec.HyphenTagConstituentFeature;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.POSContextWindowTwo;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.WordContextWindowTwo;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.Set;


/**
 * Extracts a collection of features used to classify SRL Nominal frame arguments:
 * {@link WordFeatureExtractorFactory} word, pos, numberNormalizer, gerundMarker,
 * nominalizationMarker, and dateMarker; {@link ListFeatureFactory} daysOfTheWeek and months;
 * {@link WordNetConstituentFeatureExtractor} synsetsFirstSense and hypernymsFirstSense;
 * {@link ParseHeadWordConstituentFeatureExtractor}; {@link CurrencyIndicator}; {@link LinearPosition};
 * {@link HyphenTagConstituentFeature}; {@link ParsePhraseType}; {@link ParsePath}; {@link ChunkEmbedding}
 * shallow parse and NER; {@link ChunkPathPattern}; {@link ParseSiblings};
 * {@link WordContextWindowTwo}; {@link POSContextWindowTwo}
 *
 * @keywords SRL, Nom, Nominal, classifier, arguments
 * @author Xinbo Wu
 */
public class SrlNomArgumentFeatures implements FeatureExtractor<Constituent> {
    private final ConstituentFeatureCollection base = new ConstituentFeatureCollection(this.getName());

    public SrlNomArgumentFeatures() throws EdisonException {
        ArrayList<ConstituentFeatureCollection> tmp = new ArrayList<ConstituentFeatureCollection>();

        tmp.add(new ConstituentFeatureCollection("", FeatureInputTransformer.constituentParent,
                new SrlNomClassifierPredicateFeatures("")));

        tmp.add(new ConstituentFeatureCollection(""));
        tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.word);
        tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.pos);
        tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.numberNormalizer);
        tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
        tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);
        tmp.get(1).addFeatureExtractor(ListFeatureFactory.daysOfTheWeek);
        tmp.get(1).addFeatureExtractor(ListFeatureFactory.months);
        tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.dateMarker);

        try {
            WordNetConstituentFeatureExtractor wn = new WordNetConstituentFeatureExtractor();
            wn.addFeatureType(WordNetFeatureClass.synsetsFirstSense);
            wn.addFeatureType(WordNetFeatureClass.hypernymsFirstSense);

            tmp.get(1).addFeatureExtractor(wn);
        } catch (Exception e) {
            throw new EdisonException(e);
        }

        this.base.addFeatureExtractor(tmp.get(0));
        this.base.addFeatureExtractor(new ParseHeadWordConstituentFeatureExtractor(ViewNames.PARSE_STANFORD,
                tmp.get(1)));

        this.base.addFeatureExtractor(CurrencyIndicator.instance);
        this.base.addFeatureExtractor(LinearPosition.instance);

        this.base.addFeatureExtractor(new HyphenTagConstituentFeature());

        this.base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));

        this.base.addFeatureExtractor(new ParsePath(ViewNames.PARSE_STANFORD));
        this.base.addFeatureExtractor(ChunkEmbedding.SHALLOW_PARSE);
        this.base.addFeatureExtractor(ChunkPathPattern.SHALLOW_PARSE);
        this.base.addFeatureExtractor(ChunkEmbedding.NER);

        this.base.addFeatureExtractor(new ParseSiblings(ViewNames.PARSE_STANFORD));
        this.base.addFeatureExtractor(new WordContextWindowTwo(""));
        this.base.addFeatureExtractor(new POSContextWindowTwo(""));
    }


    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return base.getFeatures(c);
    }

    @Override
    public String getName() {
        return "#argumentFeatures#";
    }
}
