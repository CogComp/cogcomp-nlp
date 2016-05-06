package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.ParseHeadWordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.ListFeatureFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class HeadWordFeatures implements FeatureExtractor{
	private final FeatureCollection base = new FeatureCollection(this.getName());
	
	public HeadWordFeatures(){
		ArrayList<FeatureCollection> tmp = new ArrayList<FeatureCollection>();
		
		tmp.add(new FeatureCollection(""));
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.word);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.pos);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.numberNormalizer);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);
		tmp.get(0).addFeatureExtractor(ListFeatureFactory.daysOfTheWeek);
		tmp.get(0).addFeatureExtractor(ListFeatureFactory.months);
		tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.dateMarker);
		
		this.base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD, tmp.get(0)));
	}
	
	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		return base.getFeatures(c);
	}
	
	@Override
	public String getName() {
		return "#HeadWordFeatures#";
	}
}
