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
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Sense;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkPropertyFeatureFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.Set;


/**
 *
 * @keywords  semantic role labeling, srl, nominal, nom, sense, my
 * @author Xinbo Wu
 */
public class MyFeatures implements FeatureExtractor {
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public MyFeatures()throws Exception{
		ArrayList<ContextFeatureExtractor> tmp = new ArrayList<ContextFeatureExtractor>();
		
		tmp.add(new ContextFeatureExtractor(3, true, true));
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.word);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.pos);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.lemma);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.word);
		tmp.get(0).addFeatureExtractor(ChunkPropertyFeatureFactory.hasModalVerb);
		tmp.get(0).addFeatureExtractor(ChunkPropertyFeatureFactory.isNegated);
		
		tmp.add(new ContextFeatureExtractor(1, true, true));
		tmp.get(1).addFeatureExtractor(NgramFeatureExtractor.bigrams(WordFeatureExtractorFactory.word));
		tmp.get(1).addFeatureExtractor(NgramFeatureExtractor.trigrams(WordFeatureExtractorFactory.word));
		
		
		
		this.base.addFeatureExtractor(tmp.get(0));
		this.base.addFeatureExtractor(tmp.get(1));


		this.base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.stanfordGovernor, new WordFeatures("")));
		this.base.addFeatureExtractor(new FeatureCollection("", FeatureInputTransformer.stanfordObject, new WordFeatures("")));
	}
	
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#myFeatures#";
	}
}