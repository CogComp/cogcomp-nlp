/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts a collection of features for SRL Verb classifier and identifier predicates.
 *
 * @author Xinbo Wu
 */

public class SrlVerbPredicateFeatures implements FeatureExtractor {
    private final String name;
    private final FeatureCollection base;

    public SrlVerbPredicateFeatures() {
        this("#predicateFeatures#");
    }

    public SrlVerbPredicateFeatures(String name) {
        this.name = name;
        this.base = new FeatureCollection(this.getName());

        this.base.addFeatureExtractor(new AttributeFeature("predicate"));
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
        this.base.addFeatureExtractor(VerbVoiceIndicator.STANFORD);
        this.base.addFeatureExtractor(SubcategorizationFrame.STANFORD);
        this.base.addFeatureExtractor(ChunkPropertyFeatureFactory.hasModalVerb);
        this.base.addFeatureExtractor(ChunkPropertyFeatureFactory.isNegated);
        this.base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));

        ContextFeatureExtractor context = new ContextFeatureExtractor(1, true, false);
        FeatureCollection tmp = new FeatureCollection("");
        tmp.addFeatureExtractor(WordFeatureExtractorFactory.word);
        tmp.addFeatureExtractor(WordFeatureExtractorFactory.pos);
        tmp.addFeatureExtractor(FeatureUtilities.conjoin(WordFeatureExtractorFactory.word,
                WordFeatureExtractorFactory.pos));
        context.addFeatureExtractor(tmp);
        this.base.addFeatureExtractor(context);

        this.base.addFeatureExtractor(LevinVerbClassFeature.instance);
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
