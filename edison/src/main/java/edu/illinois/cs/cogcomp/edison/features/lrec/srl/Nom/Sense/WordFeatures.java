package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Sense;

import static edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory.deAdjectivalAbstractNounsSuffixes;
import static edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory.deNominalNounProducingSuffixes;

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
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.features.factory.BrownClusterFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkEmbedding;
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
public class WordFeatures implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public WordFeatures(){
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.word);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.lemma);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.capitalization);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.conflatedPOS);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);
		this.base.addFeatureExtractor(ChunkEmbedding.NER);
		this.base.addFeatureExtractor(BrownClusterFeatureExtractor.instance1000);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.deVerbalSuffix);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.deNominalNounProducingSuffixes);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.deAdjectivalAbstractNounsSuffixes);
		this.base.addFeatureExtractor(WordFeatureExtractorFactory.knownPrefixes);
		/*
		 * 
		 wn:exists-entry
		  wn:synsets-first-sense
		  wn:synsets-all
		  wn:hypernyms-all
		  wn:hypernyms-first-sense
		  wn:part-holonyms-first-sense
		  wn:member-holonyms-first-sense
		  wn:substance-holonyms-first-sense
		*/
	}
	
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#wordFeatures#";
	}
}