package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.features.helpers.SpanLabelsHelper;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.*;

/**
 *
 * @author Paul Vijayakumar, Mazin Bokhari
 *
 */
public class Affixes implements FeatureExtractor {

    public static View TOKENS;
    
    private final String viewName;
    
    public Affixes(String viewName) {
	this.viewName = viewName;
    }
    
    @Override
    /**
     * This feature extractor assumes that the TOKEN View have been generated in the 
     * Constituents TextAnnotation. It will use its own POS tag and well as the POS tag
     * and the SHALLOW_PARSE (Chunk) labels of the previous two tokens and return it as 
     * a discrete feature. 
     *
     **/
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
	
	String classifier = "Affixes";
	
	TextAnnotation ta = c.getTextAnnotation();
	
	TOKENS = ta.getView(ViewNames.TOKENS);
	
	Set<Feature> __result = new LinkedHashSet<Feature>();

	String __id;
	String __value;
	String word = c.getSurfaceForm();
	
	for (int i = 3; i <= 4; ++i) {
	    if (word.length() > i) {
		__id = "p|";
		__value = "" + (word.substring(0, i));
		__result.add(new DiscreteFeature(classifier+":"+__id+"("+__value+")"));
	
	    }
	}
	for (int i = 1; i <= 4; ++i) {
	    if (word.length() > i) {
		__id = "s|";
		__value = "" + (word.substring(word.length() - i));
		__result.add(new DiscreteFeature(classifier+":"+__id+"("+__value+")"));
		
	    }
	}
	
	return __result;
    }
    
    @Override
    public String getName() {
	return "#path#" + viewName;
    }    
}
