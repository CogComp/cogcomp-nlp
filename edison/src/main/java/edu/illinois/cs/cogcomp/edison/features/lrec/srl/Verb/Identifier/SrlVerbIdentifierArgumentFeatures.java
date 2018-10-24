/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.features.lrec.GetParseLeftSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.GetParseRightSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.PPFeatures;
import edu.illinois.cs.cogcomp.edison.features.lrec.ProjectedPath;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.SrlVerbPredicateFeatures;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.ParseSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.WordAndPos;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 *
 * @keywords semantic role labeling, srl, verbal, verb, identifier, argument
 * @author Xinbo Wu
 */
public class SrlVerbIdentifierArgumentFeatures implements FeatureExtractor {
    private final FeatureCollection base = new FeatureCollection(this.getName());

    public SrlVerbIdentifierArgumentFeatures() {
        base.addFeatureExtractor(new FeatureCollection("",
                FeatureInputTransformer.constituentParent, new SrlVerbPredicateFeatures("")));
        base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
        base.addFeatureExtractor(LinearPosition.instance);
        base.addFeatureExtractor(ParsePath.STANFORD);
        ContextFeatureExtractor context = new ContextFeatureExtractor(2, true, true);
        context.addFeatureExtractor(new WordAndPos(""));
        base.addFeatureExtractor(context);

        base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.firstWord,
                new WordAndPos("")));

        base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.lastWord,
                new WordAndPos("")));

        base.addFeatureExtractor(new FeatureCollection("", new GetParseLeftSibling(
                ViewNames.PARSE_STANFORD), new ParseSibling("")));
        base.addFeatureExtractor(new FeatureCollection("", new GetParseRightSibling(
                ViewNames.PARSE_STANFORD), new ParseSibling("")));
        base.addFeatureExtractor(new PPFeatures(ViewNames.PARSE_STANFORD));

        base.addFeatureExtractor(new ProjectedPath(ViewNames.PARSE_STANFORD));

        base.addFeatureExtractor(ChunkEmbedding.NER);

        FeatureCollection tmp = new FeatureCollection("");
        tmp.addFeatureExtractor(ParsePath.STANFORD);
        tmp.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
        tmp.addFeatureExtractor(LinearPosition.instance);
        tmp.addFeatureExtractor(new PPFeatures(ViewNames.PARSE_STANFORD));

        base.addFeatureExtractor(FeatureUtilities.conjoin(new SrlVerbPredicateFeatures(""), tmp));

        base.addFeatureExtractor(SpanLengthFeature.instance);
        base.addFeatureExtractor(ChunkEmbedding.SHALLOW_PARSE);
        base.addFeatureExtractor(ChunkPathPattern.SHALLOW_PARSE);
        base.addFeatureExtractor(ClauseFeatureExtractor.STANFORD);

        base.addFeatureExtractor(SyntacticFrame.STANFORD);

        base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD,
                new WordAndPos("")));
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
