package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Classifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class PredicateFeatures implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public PredicateFeatures(){
		this.base.addFeatureExtractor(new AttributeFeature("predicate"));
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
		this.base.addFeatureExtractor(VerbVoiceIndicator.STANFORD);
		this.base.addFeatureExtractor(SubcategorizationFrame.STANFORD);
		this.base.addFeatureExtractor(ChunkPropertyFeatureFactory.hasModalVerb);
		this.base.addFeatureExtractor(ChunkPropertyFeatureFactory.isNegated);
		this.base.addFeatureExtractor(new ParsePhraseType(ViewNames.PARSE_STANFORD));
		
		ContextFeatureExtractor context = new ContextFeatureExtractor(1, true, false);
		FeatureCollection tmp = new FeatureCollection("");
		tmp.addFeatureExtractor(WordFeatureExtractorFactory.word);
		tmp.addFeatureExtractor(WordFeatureExtractorFactory.pos);
		tmp.addFeatureExtractor(FeatureUtilities.conjoin(WordFeatureExtractorFactory.word ,WordFeatureExtractorFactory.pos));
		context.addFeatureExtractor(tmp);
		this.base.addFeatureExtractor(context);
		
		this.base.addFeatureExtractor(LevinVerbClassFeature.instance);
	}
	
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#predicateFeatures#";
	}
}