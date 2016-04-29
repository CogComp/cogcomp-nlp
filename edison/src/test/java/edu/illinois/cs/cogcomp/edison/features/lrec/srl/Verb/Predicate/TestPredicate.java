package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Predicate;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Predicate.PredicateFeatures;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.lrec.FeatureGenerators;
import edu.illinois.cs.cogcomp.edison.features.lrec.ProjectedPath;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Predicate.WordContext;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.Constant;
import edu.illinois.cs.cogcomp.edison.features.manifest.FeatureManifest;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestPredicate extends TestCase {
	
	private static List<TextAnnotation> tas;

	static {
		try {
			tas = IOUtils.readObjectAsResource(PredicateFeatures.class, "test.ta");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public final void test() throws Exception {
		System.out.println("PredicateFeatures Feature Extractor");
		// Using the first TA and a constituent between span of 30-40 as a test
//		TextAnnotation ta = tas.get(2);
//		View TOKENS = ta.getView("TOKENS");
//
//		System.out.println("GOT TOKENS FROM TEXTAnn");
//
//		List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 20);
//
//		for (Constituent c : testlist) {
//			System.out.println(c.getSurfaceForm());
//		}
//
//		System.out.println("Testlist size is " + testlist.size());

		String[] viewsToAdd = {ViewNames.POS, ViewNames.LEMMA,ViewNames.SHALLOW_PARSE, ViewNames.PARSE_GOLD,
				ViewNames.SRL_VERB,ViewNames.PARSE_STANFORD, ViewNames.NER_CONLL};
		TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd,true);
		int i = 0;

		System.out.println("This textannoation annotates the text: " + ta.getText());

		View SRL_VERB = ta.getView("SRL_VERB");

		System.out.println("GOT SRL_VERB FROM TEXTAnn");

		List<Constituent> testlist = SRL_VERB.getConstituentsCoveringSpan(0, 5);

		System.out.println("SRL output");
		int SRLFexCount = 0;
	
		FeatureManifest featureManifest;
		FeatureExtractor fex;
		String fileName = Constant.prefix + "\\Verb\\Predicate\\predicate-features.fex";
		
		featureManifest = new FeatureManifest(new FileInputStream(fileName));
		FeatureManifest.setFeatureExtractor("hyphen-argument-feature", FeatureGenerators.hyphenTagFeature);
		FeatureManifest.setTransformer("parse-left-sibling", FeatureGenerators.getParseLeftSibling(ViewNames.PARSE_STANFORD));
		FeatureManifest.setTransformer("parse-right-sibling", FeatureGenerators.getParseLeftSibling(ViewNames.PARSE_STANFORD));
		FeatureManifest.setFeatureExtractor("pp-features", FeatureGenerators.ppFeatures(ViewNames.PARSE_STANFORD));

		FeatureManifest.setFeatureExtractor("projected-path", new ProjectedPath(ViewNames.PARSE_STANFORD));
		
		featureManifest.useCompressedName();
		featureManifest.setVariable("*default-parser*", ViewNames.PARSE_STANFORD);
		
		fex = featureManifest.createFex();
		
		
//		ArrayList<Set<Feature>> featslist = new ArrayList<Set<Feature>>();
//
//		for (Constituent test : testlist)
//			featslist.add(fex.getFeatures(test));
//
//		if (featslist.isEmpty()) {
//			System.out.println("Feats list is returning NULL.");
//		}
//
//		System.out.println("Printing list of Feature set");
//
//		for (Set<Feature> feats : featslist) {
//			for (Feature f : feats){
//				System.out.println(f.getName());
//				SRLFexCount += f.getName().split("/n").length;
//			}
//			System.out.println();
//		}

		for (Constituent test : testlist){
			System.out.println("The constituent for testing is " + test.toString());
			Set<Feature> feats = fex.getFeatures(test);
			for (Feature f : feats){
				System.out.println(f.getName());
				SRLFexCount += f.getName().split("/n").length;
			}
			System.out.println();
		}

		System.out.println("GOT FEATURES YES!");
		
		System.out.println("--------------------------------------------------------------------");
		
		System.out.println("Edison output");
		int EdisonFexCount = 0;
		PredicateFeatures pf = new PredicateFeatures(); 
	
//		featslist.clear();
//
//		for (Constituent test : testlist)
//			featslist.add(pf.getFeatures(test));
//
//		if (featslist.isEmpty()) {
//			System.out.println("Feats list is returning NULL.");
//		}
//
//		System.out.println("Printing list of Feature set");
//
//		for (Set<Feature> feats : featslist) {
//			for (Feature f : feats){
//				System.out.println(f.getName());
//				EdisonFexCount += f.getName().split("/n").length;
//			}
//		}

		for (Constituent test : testlist){
			System.out.println("The constituent for testing is " + test.toString());
			Set<Feature> feats = pf.getFeatures(test);
			for (Feature f : feats){
				System.out.println(f.getName());
				EdisonFexCount += f.getName().split("/n").length;
			}
			System.out.println();
		}

		System.out.println("GOT FEATURES YES!");
		assertEquals(SRLFexCount,EdisonFexCount);
	}

	private void testFex(FeatureExtractor fex, boolean printBoth, String... viewNames) throws EdisonException {

		for (TextAnnotation ta : tas) {
			for (String viewName : viewNames)
				if (ta.hasView(viewName))
					System.out.println(ta.getView(viewName));
		}
	}
}


