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
public class HyphenTagFeature  implements FeatureExtractor{

	@Override
	public Set<Feature> getFeatures(Constituent c) throws EdisonException {
		Set<Feature> features = new HashSet<>();
		String surfaceString = c.getSurfaceForm();

		if (surfaceString.contains("-") && c.length() == 1) {
			Constituent predicate = c.getIncomingRelations().get(0).getSource();

			String lemma = predicate.getAttribute(CoNLLColumnFormatReader.LemmaIdentifier);

			assert lemma != null;

			if (predicate.getSpan().equals(c.getSpan())) {
				features.add(DiscreteFeature.create("pred-token"));
			}

			String[] parts = surfaceString.split("-");

			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];

				if (part.contains(lemma)) {
					features.add(DiscreteFeature.create(i + ":pred"));
				} else {
					String lowerCase = part.toLowerCase();
					features.add(DiscreteFeature.create(lowerCase));
					features.add(DiscreteFeature.create(i + ":" + lowerCase));
				}
			}
		}

		return features;
	}
	
	@Override
	public String getName() {
		return "#nom-hyp";
	}
}

