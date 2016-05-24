/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Classifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class ParseSibling implements FeatureExtractor {
	private final String name;
	private final FeatureCollection base;

	public ParseSibling(){
		this("#parseSibling#");
	}

	public ParseSibling(String name){
		this.name = name;
		this.base = new FeatureCollection(this.getName());

		this.base.addFeatureExtractor(new FeatureExtractor() {

			@Override
			public String getName() {
				return "";
			}

			@Override
			public Set<Feature> getFeatures(Constituent c) throws EdisonException {
				return new LinkedHashSet<Feature>(Collections.singletonList(DiscreteFeature.create(c.getLabel())));
			}
		});
		
		this.base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD, new WordPos("")));
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