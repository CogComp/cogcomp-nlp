package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Sense;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class Nom_Sense implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public Nom_Sense() throws Exception{
		this.base.addFeatureExtractor(FeatureUtilities.conjoin(WordFeatureExtractorFactory.lemma,new MyFeatures()));
	}
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#NomSense#";
	}
}
