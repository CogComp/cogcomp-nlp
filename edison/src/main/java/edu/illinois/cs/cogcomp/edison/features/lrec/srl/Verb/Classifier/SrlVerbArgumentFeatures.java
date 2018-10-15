/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Classifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.features.lrec.GetParseLeftSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.GetParseRightSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.PPFeatures;
import edu.illinois.cs.cogcomp.edison.features.lrec.ProjectedPath;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.ParseSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.SrlVerbPredicateFeatures;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.WordAndPos;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts features to classify SRL Verb arguments. Combines {@link SrlVerbPredicateFeatures}
 * tranformed using {@link FeatureInputTransformer} constituentParent; {@link ParsePhraseType};
 * {@link LinearPosition}; {@link ParsePath}; {@link WordAndPos } in a context window of size 2;
 * parse siblings; {@link PPFeatures}; {@link ProjectedPath}; {@link ChunkPathPattern} for shallow
 * parse; {@link ChunkEmbedding} for NER and shallow parse; {@link ClauseFeatureExtractor};
 * {@link SpanLengthFeature}; {@link SyntacticFrame}; and {@link ParseHeadWordFeatureExtractor}.
 *
 * @keywords SRL, verb, arguments, mixed
 * @author Xinbo Wu
 */
public class SrlVerbArgumentFeatures implements FeatureExtractor {
    private final FeatureCollection base = new FeatureCollection(this.getName());

    public SrlVerbArgumentFeatures() {
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
