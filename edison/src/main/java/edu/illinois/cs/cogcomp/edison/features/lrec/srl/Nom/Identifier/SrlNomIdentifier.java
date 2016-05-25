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
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.features.lrec.HyphenTagFeature;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.POSContextWindowTwo;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.WordContextWindowTwo;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class SrlNomIdentifier implements FeatureExtractor {
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public SrlNomIdentifier(){
		ArrayList<FeatureCollection> tmp = new ArrayList<FeatureCollection>();
		
		tmp.add(new FeatureCollection(""));
		tmp.get(0).addFeatureExtractor(new SrlNomHeadWordFeatures(""));
		tmp.get(0).addFeatureExtractor(new ParsePath(ViewNames.PARSE_STANFORD));
		tmp.get(0).addFeatureExtractor(LinearPosition.instance);
		tmp.get(0).addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));

		this.base.addFeatureExtractor(new SrlNomHeadWordFeatures(""));
		this.base.addFeatureExtractor(LinearPosition.instance);
		this.base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
		this.base.addFeatureExtractor(new ParsePath(ViewNames.PARSE_STANFORD));
		this.base.addFeatureExtractor(new HyphenTagFeature());
		
		this.base.addFeatureExtractor(FeatureUtilities.conjoin(new SrlNomIdentifierPredicateFeatures(""),tmp.get(0)));
		
		this.base.addFeatureExtractor(ChunkEmbedding.SHALLOW_PARSE);
		this.base.addFeatureExtractor(ChunkPathPattern.SHALLOW_PARSE);
		this.base.addFeatureExtractor(ChunkEmbedding.NER);
		this.base.addFeatureExtractor(CurrencyIndicator.instance);
		
		this.base.addFeatureExtractor(new ParseSiblings(ViewNames.PARSE_STANFORD));
		this.base.addFeatureExtractor(new WordContextWindowTwo(""));
		this.base.addFeatureExtractor(new POSContextWindowTwo(""));
	}
	
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#NomIdentifier#";
	}
}
