package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class TestPOSBaseLineFeatureExtractor extends TestCase {

	private static List<TextAnnotation> tas;

	static {
		try {
			tas = IOUtils.readObjectAsResource(TestPOSBaseLineFeatureExtractor.class, "test.ta");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	public final void test() throws Exception {
		System.out.println("POSBaseLine Feature Extractor");
		// Using the first TA and a constituent between span of 30-40 as a test
		TextAnnotation ta = tas.get(2);
		View TOKENS = ta.getView("TOKENS");

		System.out.println("GOT TOKENS FROM TEXTAnn");

		List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0, 20);

		for (Constituent c : testlist) {
			System.out.println(c.getSurfaceForm());
		}

		System.out.println("Testlist size is " + testlist.size());
		
		//use training corpus from xinbowu_edison-pos\pos
		String fileName = "path of training corpus";
		POSBaseLineFeatureExtractor posBaseLine = new POSBaseLineFeatureExtractor("posBaseLine", "test_corpus", fileName);
		
		ArrayList<Set<Feature>> featslist = new ArrayList<Set<Feature>>();
		
		for (Constituent test : testlist)
			featslist.add(posBaseLine.getFeatures(test));

		if (featslist.isEmpty()) {
			System.out.println("Feats list is returning NULL.");
		}
		
		System.out.println("Printing list of Feature set");
		
		for (Set<Feature> feats : featslist) {
			for (Feature f : feats)
				System.out.println(f.getName());
		}
		
		
		System.out.println("GOT FEATURES YES!");
	}

	private void testFex(FeatureExtractor fex, boolean printBoth, String... viewNames) throws EdisonException {

		for (TextAnnotation ta : tas) {
			for (String viewName : viewNames)
				if (ta.hasView(viewName))
					System.out.println(ta.getView(viewName));
		}
	} 
}
