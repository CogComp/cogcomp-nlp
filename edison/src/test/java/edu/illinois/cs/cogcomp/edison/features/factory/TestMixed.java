package edu.illinois.cs.cogcomp.edison.features.factory.newfexes;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestTAResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;

import java.util.List;
import java.util.Set;
import java.util.Random;
import java.io.Writer;


/**
 * Test class for SHALLOW PARSER Formpp Feature Extractor
 *
 * @author Paul Vijayakumar, Mazin Bokhari
 */
public class TestMixed extends TestCase {

    private static List<TextAnnotation> tas;
    
    static {
	try {
	    tas = IOUtils.readObjectAsResource(TestSOPrevious.class, "test.ta");
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
    
    protected void setUp() throws Exception {
	super.setUp();
    }
    
    public final void testMixed() throws EdisonException {
	
	System.out.println("Mixed Feature Extractor");
	//Using the first TA and a constituent between span of 30-40 as a test
	TextAnnotation ta = tas.get(1);
	View TOKENS = ta.getView("TOKENS");
	
	System.out.println("GOT TOKENS FROM TEXTAnn");
	
	List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0,20);
	
	for(Constituent c: testlist){
	    System.out.println(c.getSurfaceForm());
	}

	System.out.println("Testlist size is "+testlist.size());

	Constituent test = testlist.get(1);
	
	System.out.println("The constituent we are extracting features from in this test is: "+test.getSurfaceForm());
	
       	Mixed M = new Mixed("Mixed");
	
	System.out.println("Init Views");
	
	//Formpp.initViews(test);
	
	//System.out.println("Startspan is "+test.getStartSpan()+" and Endspan is "+test.getEndSpan());
	
	//List<Constituent> words2b4 = SOP.getstuff(test);
	System.out.println("About to print out words from text");
	//for(Constituent word: words2b4){
	    
	//   System.out.println(word.getSurfaceForm());
	//}
	
	Set<Feature> feats = M.getFeatures(test);
	
	if(feats == null){
	    System.out.println("Feats are returning NULL.");
	}
	
	System.out.println("Printing Set of Features");
	for(Feature f: feats){
	    System.out.println(f.getName());
	}
	
	System.out.println("GOT FEATURES YES!");
	
	//System.exit(0);
    }

    private void testFex(FeatureExtractor fex, boolean printBoth, String... viewNames) throws EdisonException {
	
	for (TextAnnotation ta : tas) {
	    for (String viewName : viewNames)
		if (ta.hasView(viewName)) System.out.println(ta.getView(viewName));
	}
    }
}
