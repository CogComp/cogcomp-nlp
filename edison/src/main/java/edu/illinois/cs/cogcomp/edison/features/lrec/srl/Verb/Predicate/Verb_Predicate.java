package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Predicate;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class Verb_Predicate implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public Verb_Predicate(){
		this.base.addFeatureExtractor(new PredicateFeatures());
	}
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#VerbPredicate#";
	}
}