package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class WordContext implements FeatureExtractor {
	private final FeatureCollection base;
	private final String name;

	public WordContext(){
		this("#wordContext#");
	}

	public WordContext(String name){
		//ContextFeatureExtractor context = new ContextFeatureExtractor(2, true, true);
		this.name = name;
		this.base = new FeatureCollection(this.getName());

		ContextFeatureExtractor context = new ContextFeatureExtractor(2, true, true);
		context.addFeatureExtractor(WordFeatureExtractorFactory.word);
		base.addFeatureExtractor(context);
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