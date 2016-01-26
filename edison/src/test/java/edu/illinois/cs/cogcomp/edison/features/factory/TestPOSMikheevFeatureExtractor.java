package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
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
import java.util.*;


public class TestPOSMikheevFeatureExtractor extends TestCase {

	private static List<TextAnnotation> tas;

	static {
		try {
			tas = IOUtils.readObjectAsResource(TestPOSMikheevFeatureExtractor.class, "test.ta");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public final void test() throws Exception {
		String fileName = Constant.prefix + Constant.POSCorpus;
		
		POSMikheevFeatureExtractor posMikheev = new POSMikheevFeatureExtractor("posMikheev", "test_corpus", fileName);
		
		System.out.println("POSMikheev Feature Extractor");
		System.out.println("Only print the features with known tags");
		// Using the first TA and a constituent between span of 30-40 as a test
		int i = 0;
		for(TextAnnotation ta : tas){
			ArrayList<String> outFeatures = new ArrayList<String>();
			View TOKENS = ta.getView("TOKENS");
			
			Iterator<Constituent> iter = TOKENS.iterator();
			ArrayList<Set<Feature>> featslist = new ArrayList<Set<Feature>>();
			
			while(iter.hasNext()){
				Set<Feature> feats = posMikheev.getFeatures(iter.next());
				if (feats.isEmpty()) {
					System.out.println("Feats list is returning NULL.");
				}
					for (Feature f : feats)
						if(!f.getName().contains("UNKNOWN")){
						outFeatures.add(f.getName());
						}
			}
			
			if (!outFeatures.isEmpty()) {
				System.out.println("-------------------------------------------------------");
				System.out.println("Text Annotation: " + i);
				System.out.println("Text Features: ");

				for (String out : outFeatures)
					System.out.println(out);

				System.out.println("-------------------------------------------------------");
			}
			
			i++;
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
