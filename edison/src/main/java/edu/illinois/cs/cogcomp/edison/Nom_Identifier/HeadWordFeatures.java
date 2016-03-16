package edu.illinois.cs.cogcomp.edison.srl.Nom_Identifier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
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
import edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.CurrencyIndicator;
import edu.illinois.cs.cogcomp.edison.features.AttributeFeature;
import edu.illinois.cs.cogcomp.edison.features.CachedFeatureCollection;


/**
 * 
 * @author Xinbo Wu
 */
public class HeadWordFeatures implements FeatureExtractor{
	private final CachedFeatureCollection base = new CachedFeatureCollection("");
	
	public HeadWordFeatures(){
		ArrayList<CachedFeatureCollection> tmp = new ArrayList<CachedFeatureCollection>();
		
		tmp.add(new CachedFeatureCollection(""));
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.word);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.pos);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.numberNormalizer);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);
		tmp.get(0).addFeatureExtractor(ListFeatureFactory.daysOfTheWeek);
		tmp.get(0).addFeatureExtractor(ListFeatureFactory.months);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.dateMarker);
		
		this.base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD, tmp.get(0)));
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
