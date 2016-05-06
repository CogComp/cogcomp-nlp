package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Identifier;

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
public class ParseSibling implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public ParseSibling(){
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
		
		this.base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD, new WordPos()));
	}
	
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#parseSibling#";
	}
}