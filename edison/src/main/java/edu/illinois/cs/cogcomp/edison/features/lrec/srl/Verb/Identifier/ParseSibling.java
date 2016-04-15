package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Identifier;

import java.util.Collections;
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
import edu.illinois.cs.cogcomp.edison.features.ParseHeadWordFeatureExtractor;
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