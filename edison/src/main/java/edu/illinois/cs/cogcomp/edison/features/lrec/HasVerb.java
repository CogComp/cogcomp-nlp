package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureInputTransformer;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;

import java.util.*;


/**
 * 
 * @author Xinbo Wu
 */
public class HasVerb implements FeatureExtractor{

	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		boolean hasVerb = false;
		TextAnnotation ta = c.getTextAnnotation();

		for (int i = c.getStartSpan(); i < c.getEndSpan(); i++) {

			if (POSUtils.isPOSVerb(WordHelpers.getPOS(ta, i))) {
				hasVerb = true;
				break;
			}
		}
		Set<Feature> feats = new HashSet<>();

		if (hasVerb) {
			feats.add(DiscreteFeature.create(getName()));
		}
		return feats;
	}
	
	@Override
	public String getName() {
		return "#has-verb";
	}
}

