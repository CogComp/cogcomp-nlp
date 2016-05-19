package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.features.lrec.GetParseLeftSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.GetParseRightSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.PPFeatures;
import edu.illinois.cs.cogcomp.edison.features.lrec.ProjectedPath;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Classifier.ParseSibling;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class ArgumentFeatures implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public ArgumentFeatures(){
		base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.constituentParent,
				new PredicateFeatures("")));
		base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
		base.addFeatureExtractor(LinearPosition.instance);
		base.addFeatureExtractor(ParsePath.STANFORD);
		ContextFeatureExtractor context = new ContextFeatureExtractor(2, true, true);
		context.addFeatureExtractor(new WordPos(""));
		base.addFeatureExtractor(context);
		
		base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.firstWord,
				new WordPos("")));
		
		base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.lastWord,
				new WordPos("")));
		
		base.addFeatureExtractor(new FeatureCollection("", new GetParseLeftSibling(ViewNames.PARSE_STANFORD),
				new ParseSibling("")));
		base.addFeatureExtractor(new FeatureCollection("", new GetParseRightSibling(ViewNames.PARSE_STANFORD),
				new ParseSibling("")));
		base.addFeatureExtractor(new PPFeatures(ViewNames.PARSE_STANFORD));
		
		base.addFeatureExtractor(new ProjectedPath(ViewNames.PARSE_STANFORD));
		
		base.addFeatureExtractor(ChunkEmbedding.NER);
		
		FeatureCollection tmp = new FeatureCollection("");
		tmp.addFeatureExtractor(ParsePath.STANFORD);
		tmp.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
		tmp.addFeatureExtractor(LinearPosition.instance);
		tmp.addFeatureExtractor(new PPFeatures(ViewNames.PARSE_STANFORD));

		base.addFeatureExtractor(FeatureUtilities.conjoin(new PredicateFeatures(""),tmp));
		
		base.addFeatureExtractor(SpanLengthFeature.instance);
		base.addFeatureExtractor(ChunkEmbedding.SHALLOW_PARSE);
		base.addFeatureExtractor(ChunkPathPattern.SHALLOW_PARSE);
		base.addFeatureExtractor(ClauseFeatureExtractor.STANFORD);
		
		base.addFeatureExtractor(SyntacticFrame.STANFORD);
		
		base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD, new WordPos("")));
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