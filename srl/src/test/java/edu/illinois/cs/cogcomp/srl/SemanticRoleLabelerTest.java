/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import junit.framework.TestCase;

import java.util.Properties;

public class SemanticRoleLabelerTest extends TestCase {
    private static final String CONFIG = "src/test/resources/srl-config.properties";

    private static String[] requiredViews = new String[] {ViewNames.POS, ViewNames.LEMMA,
            ViewNames.SHALLOW_PARSE, ViewNames.PARSE_STANFORD, ViewNames.NER_CONLL};

    private ResourceManager rm;

    public void setUp() throws Exception {
		super.setUp();
        ResourceManager tempRm = new ResourceManager( CONFIG );
        Properties props = new Properties();
        props.setProperty( SrlConfigurator.INSTANTIATE_PREPROCESSOR.key, SrlConfigurator.TRUE );
        rm = new ResourceManager( props );
        rm = SrlConfigurator.mergeProperties( tempRm, rm );
    }

	public void testVerbSRL() throws Exception {
        Properties props = new Properties();
        props.setProperty( SrlConfigurator.SRL_TYPE.key, SRLType.Verb.name() );

        rm = SrlConfigurator.mergeProperties( rm, new ResourceManager(props));

        SemanticRoleLabeler verbSRL = new SemanticRoleLabeler(rm);
		TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(requiredViews, false, 1);
        if (!ta.hasView(ViewNames.CLAUSES_STANFORD)) // an additional "invisible" dependency
            ta.addView(ClauseViewGenerator.STANFORD);
		PredicateArgumentView srl = (PredicateArgumentView) verbSRL.getView(ta);

        String expected = "finish:02\n    A1: The construction of the John Smith library\n    AM-MNR: on time\n" +
                "design:01\n    A1: The $10M building\n    AM-MNR: designed\n    AM-TMP: in 2016\n" +
                "paving:01\ncommence:01\n    A0: The paving\n    A1: Monday\n" +
                "finish:02\n    A1: finish\n    AM-MOD: will\n    AM-TMP: in June\n";
//"finish:02\n    A1: The construction of the library\n    AM-TMP: on time\n"
		assertEquals(expected, srl.toString());
	}

    /**
     * TODO: why does nom only annotate the first sentence? no predicates in other sentences?
     * @throws Exception
     */
	public void testNomSRL() throws Exception {
        Properties props = new Properties();
        props.setProperty( SrlConfigurator.SRL_TYPE.key, SRLType.Nom.name() );
        rm = SrlConfigurator.mergeProperties( rm, new ResourceManager(props));

        SemanticRoleLabeler nomSRL = new SemanticRoleLabeler(rm);
		TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(requiredViews, false, 1);
		PredicateArgumentView srl = (PredicateArgumentView) nomSRL.getView(ta);

        String expected = "construction:01\n    A1: of the John Smith library\nlibrary:01\n";
        assertEquals(expected, srl.toString());
	}
}