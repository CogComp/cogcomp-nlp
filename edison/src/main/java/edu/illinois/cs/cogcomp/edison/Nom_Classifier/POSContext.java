package edu.illinois.cs.cogcomp.edison.srl.Nom_Classifier;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor;


/**
 * 
 * @author Xinbo Wu
 */
public class POSContext implements FeatureExtractor{
	private final ContextFeatureExtractor base;
	
	public POSContext(int size, boolean includeIndex, boolean ignoreCenter){
		this.base = new ContextFeatureExtractor(size, includeIndex, ignoreCenter);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
	}
	
	public POSContext(){
		this(2, true, true);
	}
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#posContext#";
	}
}