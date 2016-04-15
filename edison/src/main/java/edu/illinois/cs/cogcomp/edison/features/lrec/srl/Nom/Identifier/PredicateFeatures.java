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
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.features.factory.NomLexClassFeature;
import edu.illinois.cs.cogcomp.edison.features.factory.SubcategorizationFrame;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.AttributeFeature;
import edu.illinois.cs.cogcomp.edison.features.CachedFeatureCollection;


/**
 * 
 * @author Xinbo Wu
 */
public class PredicateFeatures implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public PredicateFeatures(){
		ArrayList<CachedFeatureCollection> tmp = new ArrayList<CachedFeatureCollection>();
		
		tmp.add(new CachedFeatureCollection(""));
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.word);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.pos);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.lemma);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.capitalization);
		
		tmp.get(0).addFeatureExtractor(new AttributeFeature("predicate"));
		
		tmp.get(0).addFeatureExtractor(SubcategorizationFrame.STANFORD);
		tmp.get(0).addFeatureExtractor(NomLexClassFeature.instance);
		
		tmp.add(new CachedFeatureCollection("", FeatureInputTransformer.constituentParent,
				tmp.get(0)));
		
		this.base.addFeatureExtractor(tmp.get(1));
	}
	
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#predicateFeatures#";
	}
}