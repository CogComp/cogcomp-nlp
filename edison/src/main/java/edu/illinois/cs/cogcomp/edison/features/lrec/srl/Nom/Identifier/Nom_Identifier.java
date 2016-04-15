package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

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
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
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
public class Nom_Identifier implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public Nom_Identifier(){
		ArrayList<CachedFeatureCollection> tmp = new ArrayList<CachedFeatureCollection>();
		
		tmp.add(new CachedFeatureCollection(""));
		tmp.get(0).addFeatureExtractor(new HeadWordFeatures());
		tmp.get(0).addFeatureExtractor(new ParsePath(ViewNames.PARSE_STANFORD));
		tmp.get(0).addFeatureExtractor(LinearPosition.instance);
		tmp.get(0).addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
		
		this.base.addFeatureExtractor(LinearPosition.instance);
		this.base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
		this.base.addFeatureExtractor(new ParsePath(ViewNames.PARSE_STANFORD));
		
		this.base.addFeatureExtractor(FeatureUtilities.conjoin(new PredicateFeatures(),tmp.get(0)));
		
		this.base.addFeatureExtractor(ChunkEmbedding.SHALLOW_PARSE);
		this.base.addFeatureExtractor(ChunkPathPattern.SHALLOW_PARSE);
		this.base.addFeatureExtractor(ChunkEmbedding.NER);
		this.base.addFeatureExtractor(CurrencyIndicator.instance);
		
		this.base.addFeatureExtractor(new ParseSiblings(ViewNames.PARSE_STANFORD));
		this.base.addFeatureExtractor(new WordContext());
		this.base.addFeatureExtractor(new POSContext());
		//hyphen-argument-feature
	}
	
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#NomIdentifier#";
	}
}
