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

    private SemanticRoleLabeler verbSRL, nomSRL;

	public void setUp() throws Exception {
		super.setUp();

        /**
         * the commented code below helps debug problems when gurobi libraries are not found.
         * the first section dumps the classpath, to check for Gurobi jar.
         * The second checks LD_LIBRARY_PATH, which is where the JVM looks for the JNI library.
         */
//        System.err.println( "## CLASSPATH: " );
//        ClassLoader cl = ClassLoader.getSystemClassLoader();
//        URL[] urls = ((URLClassLoader)cl).getURLs();
//
//        for(URL url: urls){
//            System.out.println(url.getFile());
//        }
//
//        String pathToAdd = "/home/mssammon/lib/gurobi650/linux64/lib";
//        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
//        usrPathsField.setAccessible(true);
//
//        //get array of paths
//        final String[] paths = (String[])usrPathsField.get(null);
//
//        //check if the path to add is already present
//        for(String path : paths) {
//            System.err.println( "## found usr path: " + path );
//            if(path.equals(pathToAdd)) {
//                break;
//            }
//        }
//
//        //add the new path
//        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
//        newPaths[newPaths.length-1] = pathToAdd;
//        usrPathsField.set(null, newPaths);

        ResourceManager rm = new ResourceManager( CONFIG );

		verbSRL = new SemanticRoleLabeler(rm, "Verb");
		nomSRL = new SemanticRoleLabeler(rm, "Nom");
	}

	public void testVerbSRL() throws Exception {
		TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(requiredViews, false);
		PredicateArgumentView srl = verbSRL.getSRL(ta);
		assertEquals("finish:02\n    A1: The construction of the library\n    AM-TMP: on time\n", srl.toString());
	}

	public void testNomSRL() throws Exception {
		TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(requiredViews, false);
		PredicateArgumentView srl = nomSRL.getSRL(ta);
		assertEquals("construction:01\n    A1: of the library\n", srl.toString());
	}
}