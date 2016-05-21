package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
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
public class WordPos implements FeatureExtractor {
	private final String name;
	private final FeatureCollection base;

	public WordPos(){
		this("#wordPos#");
	}

	public WordPos(String name){
		this.name = name;
		this.base = new FeatureCollection(this.getName());

		this.base.addFeatureExtractor(WordFeatureExtractorFactory.word);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
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