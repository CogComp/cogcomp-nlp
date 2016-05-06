package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.Set;


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
