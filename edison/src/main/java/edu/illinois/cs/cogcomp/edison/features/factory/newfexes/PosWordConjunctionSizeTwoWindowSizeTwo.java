package edu.illinois.cs.cogcomp.edison.features.factory.newfexes;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.features.helpers.SpanLabelsHelper;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Paul Vijayakumar, Mazin Bokhari
 * Extracts the POS Tags as well as the form (text) of tokens 2 before and 2 after from the given token and generates a discrete feature from it.
 *
 */
public class PosWordConjunctionSizeTwoWindowSizeTwo implements FeatureExtractor {

    private final String viewName;
    
    public PosWordConjunctionSizeTwoWindowSizeTwo(String viewName) {
	this.viewName = viewName;
    }

    public String[] getwindowkfrom(View TOKENS, int startspan, int endspan, int k){
	
	String window[] = new String[2*k+1];
	
	int startwin = startspan - k;
	int endwin = endspan + k;
	
	if(endwin > TOKENS.getEndSpan()){
	    endwin = TOKENS.getEndSpan();
	}	
	if(startwin < 0){
	    startwin = 0;
	}
	
	for(int i = startwin; i < endwin; i++){   
	    
	    window[i] = TOKENS.getConstituentsCoveringSpan(i, i+1).get(0).getSurfaceForm();

	}
	return window;
    }

    public String[] getwindowtagskfrom(View TOKENS, View POS, int startspan, int endspan, int k){
	
	String tags[] = new String[2*k+1];
	
	int startwin = startspan - k;
	int endwin = endspan + k;
	
	if(endwin > TOKENS.getEndSpan()){
	    endwin = TOKENS.getEndSpan();
	}	
	if(startwin < 0){
	    startwin = 0;
	}
	
	for(int i = startwin; i < endwin; i++){   
	    
	    tags[i] = POS.getLabelsCoveringSpan(i, i+1).get(0);

	}
	return tags;
    }
    
    @Override
    /**
     * This feature extractor assumes that the TOKEN View, POS View have been
     * generated in the Constituents TextAnnotation. It will use its own POS tag and well 
     * as the form of the word as a forms of the words around the constitent a 
     *
     **/
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
	
	TextAnnotation ta = c.getTextAnnotation();

	View TOKENS=null,POS=null;
	
	try{
	    TOKENS = ta.getView(ViewNames.TOKENS);
	    POS = ta.getView(ViewNames.POS);
	}catch(Exception e){
	    e.printStackTrace();
	}
	
	//We can assume that the constituent in this case is a Word(Token) described by the LBJ chunk definition
	int startspan = c.getStartSpan();
	int endspan = c.getEndSpan();
	
	//All our constituents are words(tokens)
	int k = 2; //words two before & after
	
	String[] forms = getwindowkfrom(TOKENS, startspan, endspan, k);
	String[] tags = getwindowtagskfrom(TOKENS, POS, startspan, endspan, k);
		
	String classifier = "PosWordConjunctionSizeTwoWindowSizeTwo";
	String __id, __value;
	Set<Feature> __result = new LinkedHashSet<Feature>();

	int maxcontext = 2;
	
	for(int j = 1; j < maxcontext; j++){
	    for(int x = 0; x < 2; x++){
		boolean t = true;
		for(int i = 0; i < tags.length; i++){
		    StringBuffer f = new StringBuffer();
		    for(int context = 0; context <= j && i + context < tags.length; context++){
			if(context != 0){
			    f.append("_");
			}
			if (t && x == 0){
			    f.append(tags[i + context]);
			}
			else{
			    f.append(forms[i + context]);
			}
			t = !t;
		    }
		    __id =  classifier+":" + (i + "_" + j);
		    __value = "(" + (f.toString()) + ")";
		    __result.add(new DiscreteFeature(__id+__value));
		}
	    }
	}	
	return __result;
    }
    
    @Override
    public String getName() {
	return "#path#" + viewName;
    }    
}
