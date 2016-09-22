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

import java.util.Properties;

/**
 * Attempt to determine whether the lazy initialization Annotator API from i-c-u creates memory use
 * problems
 * 
 * @author mssammon
 */
public class NonJUnitMemoryUseTest {

    private static final String CONFIG = "src/test/resources/srl-config.properties";

    private static String[] requiredViews = new String[] {ViewNames.POS, ViewNames.LEMMA,
            ViewNames.SHALLOW_PARSE, ViewNames.PARSE_STANFORD, ViewNames.NER_CONLL};

    private ResourceManager rm;

    static SemanticRoleLabeler srlStatic;


    public static void main(String[] args) {

        NonJUnitMemoryUseTest test = new NonJUnitMemoryUseTest();

        try {
            test.setUp();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        boolean isStatic = false;
        System.out.println("Test method-scope SRL:");

        try {
            test.testVerbSRL(isStatic);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Finished first test. Running again.");

        try {
            test.testVerbSRL(isStatic);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Running test again, this time with static field.");

        isStatic = true;

        try {
            test.testVerbSRL(isStatic);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("setting static field to null, and rerunning...");
        srlStatic = null;
        showMemoryUsage();
        try {
            test.testVerbSRL(isStatic);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }


    }


    public void setUp() throws Exception {
        ResourceManager tempRm = new ResourceManager(CONFIG);
        Properties props = new Properties();
        props.setProperty(SrlConfigurator.INSTANTIATE_PREPROCESSOR.key, SrlConfigurator.TRUE);
        rm = new ResourceManager(props);
        rm = SrlConfigurator.mergeProperties(tempRm, rm);
    }

    public void testVerbSRL(boolean isStatic) throws Exception {

        TextAnnotation ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(requiredViews, false,
                        1);
        // if (!ta.hasView(ViewNames.CLAUSES_STANFORD)) // an additional "invisible" dependency
        // ta.addView(ClauseViewGenerator.STANFORD);
        SemanticRoleLabeler verbSRL = getSrl(isStatic);
        PredicateArgumentView srl = (PredicateArgumentView) verbSRL.getView(ta);

        System.out.println("SRL output: " + srl.toString());
        System.out.println("memory use" + (isStatic ? "before srl goes out of scope" : "") + ":");
        showMemoryUsage();
    }

    private SemanticRoleLabeler getSrl(boolean isStatic) throws Exception {
        Properties props = new Properties();
        props.setProperty(SrlConfigurator.SRL_TYPE.key, SRLType.Verb.name());

        rm = SrlConfigurator.mergeProperties(rm, new ResourceManager(props));

        SemanticRoleLabeler verbSRL = null;

        if (!isStatic || null == srlStatic)
            verbSRL = new SemanticRoleLabeler(rm, false);

        if (isStatic && null == srlStatic)
            srlStatic = verbSRL;

        return verbSRL;
    }


    public static void showMemoryUsage() {
        int mb = 1024 * 1024;

        // Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        System.out.println("##### Heap utilization statistics [MB] #####");

        // Print used memory
        System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        // Print free memory
        System.out.println("Free Memory:" + runtime.freeMemory() / mb);

        // Print total available memory
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);

        // Print Maximum available memory
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
    }

}
