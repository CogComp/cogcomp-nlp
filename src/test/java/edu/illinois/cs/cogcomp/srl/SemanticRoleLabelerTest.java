package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.srl.experiment.TextPreProcessor;
import junit.framework.TestCase;

public class SemanticRoleLabelerTest extends TestCase {
    private static final String CONFIG = "src/test/resources/srl-config.properties";

    private static String[] requiredViews = new String[] {ViewNames.POS, ViewNames.LEMMA,
            ViewNames.SHALLOW_PARSE, ViewNames.PARSE_STANFORD, ViewNames.NER_CONLL};

    private ResourceManager rm;

    public void setUp() throws Exception {
		super.setUp();
        rm = new ResourceManager( CONFIG );
	}

	public void testVerbSRL() throws Exception {
        SemanticRoleLabeler verbSRL = new SemanticRoleLabeler(rm, "Verb");
		TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(requiredViews, false);
		PredicateArgumentView srl = verbSRL.getSRL(ta);
		assertEquals("finish:02\n    A1: The construction of the library\n    AM-TMP: on time\n", srl.toString());
	}

	public void testNomSRL() throws Exception {
        SemanticRoleLabeler nomSRL = new SemanticRoleLabeler(rm, "Nom");
		TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(requiredViews, false);
		PredicateArgumentView srl = nomSRL.getSRL(ta);
		assertEquals("construction:01\n    A1: of the library\n", srl.toString());
	}
}