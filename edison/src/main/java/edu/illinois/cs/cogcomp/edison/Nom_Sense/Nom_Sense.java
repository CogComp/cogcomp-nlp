package edu.illinois.edu.cs.cogcomp.edison.srl.Nom_Sense;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
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
public class Nom_Sense implements FeatureExtractor{
	private final CachedFeatureCollection base = new CachedFeatureCollection("");
	
	public Nom_Sense(){
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
