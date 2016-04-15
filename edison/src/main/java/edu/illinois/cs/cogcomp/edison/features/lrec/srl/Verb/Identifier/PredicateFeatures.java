package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkPropertyFeatureFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.LevinVerbClassFeature;
import edu.illinois.cs.cogcomp.edison.features.factory.NomLexClassFeature;
import edu.illinois.cs.cogcomp.edison.features.factory.ParsePhraseType;
import edu.illinois.cs.cogcomp.edison.features.factory.SubcategorizationFrame;
import edu.illinois.cs.cogcomp.edison.features.factory.VerbVoiceIndicator;
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