package edu.illinois.cs.cogcomp.edison.features.factory.newfexes;

import org.apache.commons.lang.ArrayUtils;
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
public class TestPOSandPositionWindowThree extends TestCase {

    private static List<TextAnnotation> tas;
    
    static {
	try {
	    tas = IOUtils.readObjectAsResource(TestPOSandPositionWindowThree.class, "test.ta");
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
    
    protected void setUp() throws Exception {
	super.setUp();
    }
    
    public final void testUsage() throws EdisonException {
	
	// System.out.println("POSWindowpp Feature Extractor");
	//Using the first TA and a constituent between span of 30-40 as a test
	TextAnnotation ta = tas.get(2);
	View TOKENS = ta.getView("TOKENS");
	
	// System.out.println("GOT TOKENS FROM TEXTAnn");
	
	List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(0,20);
	
	for(Constituent c: testlist){
	    // System.out.println(c.getSurfaceForm());
	}
	
	// System.out.println("Testlist size is "+testlist.size());
	
	Constituent test = testlist.get(1);
	
	// System.out.println("The constituent we are extracting features from in this test is: "+test.getSurfaceForm());

		POSandPositionWindowThree POSWpp = new POSandPositionWindowThree("POSandPositionWindowThree");
	
	//Formpp.initViews(test);
	
	System.out.println("Startspan is "+test.getStartSpan()+" and Endspan is "+test.getEndSpan());
	
	Set<Feature> feats = POSWpp.getFeatures(test);
	String[] expected_outputs = {
		"POSandPositionWindowThree:0_0(DT)",
		"POSandPositionWindowThree:1_0(VBZ)",
		"POSandPositionWindowThree:2_0(DT)",
		"POSandPositionWindowThree:3_0(NN)",
		"POSandPositionWindowThree:4_0(.)",
		"POSandPositionWindowThree:5_0(null)",
		"POSandPositionWindowThree:6_0(null)",
		"POSandPositionWindowThree:0_1(DT_VBZ)",
		"POSandPositionWindowThree:1_1(VBZ_DT)",
		"POSandPositionWindowThree:2_1(DT_NN)",
		"POSandPositionWindowThree:3_1(NN_.)",
		"POSandPositionWindowThree:4_1(._null)",
		"POSandPositionWindowThree:5_1(null_null)",
		"POSandPositionWindowThree:6_1(null)",
		"POSandPositionWindowThree:0_2(DT_VBZ_DT)",
		"POSandPositionWindowThree:1_2(VBZ_DT_NN)",
		"POSandPositionWindowThree:2_2(DT_NN_.)",
		"POSandPositionWindowThree:3_2(NN_._null)",
		"POSandPositionWindowThree:4_2(._null_null)",
		"POSandPositionWindowThree:5_2(null_null)",
		"POSandPositionWindowThree:6_2(null)"
	};


	if(feats == null){
	    // System.out.println("Feats are returning NULL.");
	}
	
	// System.out.println("Printing Set of Features");
	for(Feature f: feats){
	    System.out.println(f.getName());
		assert(ArrayUtils.contains( expected_outputs, f.getName()));
	}
	
	// System.out.println("GOT FEATURES YES!");
	
	//System.exit(0);
    }

}
