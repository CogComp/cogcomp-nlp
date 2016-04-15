package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Classifier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureInputTransformer;
import edu.illinois.cs.cogcomp.edison.features.GetParseLeftSibling;
import edu.illinois.cs.cogcomp.edison.features.ParseHeadWordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkEmbedding;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkPathPattern;
import edu.illinois.cs.cogcomp.edison.features.factory.ClauseFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.LinearPosition;
import edu.illinois.cs.cogcomp.edison.features.factory.ListFeatureFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.NomLexClassFeature;
import edu.illinois.cs.cogcomp.edison.features.factory.ParsePath;
import edu.illinois.cs.cogcomp.edison.features.factory.ParsePhraseType;
import edu.illinois.cs.cogcomp.edison.features.factory.ParseSiblings;
import edu.illinois.cs.cogcomp.edison.features.factory.SpanLengthFeature;
import edu.illinois.cs.cogcomp.edison.features.factory.SubcategorizationFrame;
import edu.illinois.cs.cogcomp.edison.features.factory.SyntacticFrame;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor.WordNetFeatureClass;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.lrec.GetParseRightSibling;
import edu.illinois.cs.cogcomp.edison.features.lrec.PPFeatures;
import edu.illinois.cs.cogcomp.edison.features.lrec.ProjectedPath;
import edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.CurrencyIndicator;
import edu.illinois.cs.cogcomp.edison.features.AttributeFeature;
import edu.illinois.cs.cogcomp.edison.features.CachedFeatureCollection;


/**
 * 
 * @author Xinbo Wu
 */
public class ArgumentFeatures implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public ArgumentFeatures(){
		base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.constituentParent,
				new PredicateFeatures()));
		base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
		base.addFeatureExtractor(LinearPosition.instance);
		base.addFeatureExtractor(ParsePath.STANFORD);
		ContextFeatureExtractor context = new ContextFeatureExtractor(2, true, true);
		context.addFeatureExtractor(new WordPos());
		base.addFeatureExtractor(context);
		
		base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.firstWord,
				new WordPos()));
		
		base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.lastWord,
				new WordPos()));
		
		base.addFeatureExtractor(new FeatureCollection("", new GetParseLeftSibling(ViewNames.PARSE_STANFORD),
				new ParseSibling()));
		base.addFeatureExtractor(new FeatureCollection("", new GetParseRightSibling(ViewNames.PARSE_STANFORD),
				new ParseSibling()));
		base.addFeatureExtractor(new PPFeatures(ViewNames.PARSE_STANFORD));
		
		base.addFeatureExtractor(new ProjectedPath(ViewNames.PARSE_STANFORD));
		
		base.addFeatureExtractor(ChunkEmbedding.NER);
		
		base.addFeatureExtractor(SpanLengthFeature.instance);
		base.addFeatureExtractor(ChunkEmbedding.SHALLOW_PARSE);
		base.addFeatureExtractor(ChunkPathPattern.SHALLOW_PARSE);
		base.addFeatureExtractor(ClauseFeatureExtractor.STANFORD);
		
		base.addFeatureExtractor(SyntacticFrame.STANFORD);
		
		base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD, new WordPos()));
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