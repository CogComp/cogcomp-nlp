package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Classifier;

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
import edu.illinois.cs.cogcomp.edison.features.ParseHeadWordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkEmbedding;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkPathPattern;
import edu.illinois.cs.cogcomp.edison.features.factory.LinearPosition;
import edu.illinois.cs.cogcomp.edison.features.factory.ListFeatureFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.NomLexClassFeature;
import edu.illinois.cs.cogcomp.edison.features.factory.ParsePath;
import edu.illinois.cs.cogcomp.edison.features.factory.ParsePhraseType;
import edu.illinois.cs.cogcomp.edison.features.factory.ParseSiblings;
import edu.illinois.cs.cogcomp.edison.features.factory.SubcategorizationFrame;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor.WordNetFeatureClass;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.lrec.HyphenTagFeature;
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
		ArrayList<FeatureCollection> tmp = new ArrayList<FeatureCollection>();
		
		tmp.add(new FeatureCollection("", FeatureInputTransformer.constituentParent,
				new PredicateFeatures()));
		
		tmp.add(new FeatureCollection(""));
		tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.word);
		tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.pos);
		tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.numberNormalizer);
		tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
		tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);
		tmp.get(1).addFeatureExtractor(ListFeatureFactory.daysOfTheWeek);
		tmp.get(1).addFeatureExtractor(ListFeatureFactory.months);
		tmp.get(1).addFeatureExtractor(WordFeatureExtractorFactory.dateMarker);
		//tmp.get(1).addFeatureExtractor(WordNetFeatureClass.synsetsFirstSense);
		//tmp.get(1).addFeatureExtractor(WordNetFeatureClass.hypernymsFirstSense);

		this.base.addFeatureExtractor(tmp.get(0));
		this.base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD, tmp.get(1)));
		
		this.base.addFeatureExtractor(CurrencyIndicator.instance);
		this.base.addFeatureExtractor(LinearPosition.instance);
		
		this.base.addFeatureExtractor(new HyphenTagFeature());
		
		this.base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
		
		this.base.addFeatureExtractor(new ParsePath(ViewNames.PARSE_STANFORD));
		this.base.addFeatureExtractor(ChunkEmbedding.SHALLOW_PARSE);
		this.base.addFeatureExtractor(ChunkPathPattern.SHALLOW_PARSE);
		this.base.addFeatureExtractor(ChunkEmbedding.NER);
		
		this.base.addFeatureExtractor(new ParseSiblings(ViewNames.PARSE_STANFORD));
		this.base.addFeatureExtractor(new WordContext());
		this.base.addFeatureExtractor(new POSContext());
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