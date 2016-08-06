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
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.PseudoParse;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.lrec.FeatureGenerators;
import edu.illinois.cs.cogcomp.edison.features.lrec.ProjectedPath;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.Constant;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.SRLFeaturesComparator;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.SrlVerbPredicateFeatures;
import edu.illinois.cs.cogcomp.edison.features.manifest.FeatureManifest;
import junit.framework.TestCase;

import java.io.FileInputStream;
import java.util.List;

/**
 *
 * @author Xinbo Wu
 */
public class TestSrlVerbPredicateFeatures extends TestCase {

	public final void test() throws Exception {
		System.out.println("PredicateFeatures Feature Extractor");

		String[] viewsToAdd = {ViewNames.POS, ViewNames.LEMMA, ViewNames.SHALLOW_PARSE, ViewNames.PARSE_GOLD,
				ViewNames.SRL_VERB, ViewNames.PARSE_STANFORD, ViewNames.NER_CONLL};
		TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,true, 3);
		int i = 0;
		ta.addView(ClauseViewGenerator.STANFORD);
		ta.addView(PseudoParse.STANFORD);

		System.out.println("This textannotation annotates the text: \n" + ta.getText());

		View TOKENS = ta.getView("TOKENS");

		List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(10,13);
		testlist.addAll(TOKENS.getConstituentsCoveringSpan(26,27));

		FeatureManifest featureManifest;
		FeatureExtractor fex;
		String fileName = Constant.prefix + "/Verb/Identifier/predicate-features.fex";

		featureManifest = new FeatureManifest(new FileInputStream(fileName));
		FeatureManifest.setFeatureExtractor("hyphen-argument-feature", FeatureGenerators.hyphenTagFeature);
		FeatureManifest.setTransformer("parse-left-sibling", FeatureGenerators.getParseLeftSibling(ViewNames.PARSE_STANFORD));
		FeatureManifest.setTransformer("parse-right-sibling", FeatureGenerators.getParseRightSibling(ViewNames.PARSE_STANFORD));
		FeatureManifest.setFeatureExtractor("pp-features", FeatureGenerators.ppFeatures(ViewNames.PARSE_STANFORD));
		FeatureManifest.setFeatureExtractor("projected-path", new ProjectedPath(ViewNames.PARSE_STANFORD));

		featureManifest.useCompressedName();
		featureManifest.setVariable("*default-parser*", ViewNames.PARSE_STANFORD);

		fex = featureManifest.createFex();


		SrlVerbPredicateFeatures af = new SrlVerbPredicateFeatures();


		for (Constituent test : testlist){
			assertTrue(SRLFeaturesComparator.isEqual(test, fex, af));
		}
	}

}


