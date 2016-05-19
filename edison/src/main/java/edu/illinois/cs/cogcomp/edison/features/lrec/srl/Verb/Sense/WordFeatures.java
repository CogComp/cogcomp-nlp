package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Sense;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.BrownClusterFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.ChunkEmbedding;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor.WordNetFeatureClass;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class WordFeatures implements FeatureExtractor{
	private final String name;
	private final FeatureCollection base;

	public WordFeatures()throws Exception{
		this("#WordFeatures#");
	}

	public WordFeatures(String name) throws Exception{
		this.name = name;
		this.base = new FeatureCollection(this.getName());

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

		try {
			WordNetFeatureExtractor wn = new WordNetFeatureExtractor();
			wn.addFeatureType(WordNetFeatureClass.existsEntry);
			wn.addFeatureType(WordNetFeatureClass.synsetsFirstSense);
			wn.addFeatureType(WordNetFeatureClass.synsetsAllSenses);
			wn.addFeatureType(WordNetFeatureClass.hypernymsAllSenses);
			wn.addFeatureType(WordNetFeatureClass.hypernymsFirstSense);
			wn.addFeatureType(WordNetFeatureClass.partHolonymsFirstSense);
			wn.addFeatureType(WordNetFeatureClass.memberHolonymsFirstSense);
			wn.addFeatureType(WordNetFeatureClass.substanceHolonymsFirstSense);

			this.base.addFeatureExtractor(wn);
		} catch (Exception e) {
			throw new EdisonException(e);
		}
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