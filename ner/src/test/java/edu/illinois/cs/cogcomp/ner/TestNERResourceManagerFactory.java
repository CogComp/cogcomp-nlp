/**
 * 
 */
package edu.illinois.cs.cogcomp.ner;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;

/**
 * The resource manager factory can replace user supplied patterns in the resource file
 * the user provided paths. In this way, models and resources can move around without regard for 
 * how they are qualified in the properties file.
 * @author redman
 */
public class TestNERResourceManagerFactory {

	/**
	 * Test the NERResourceManagerFactory.
	 */
	@Test
	public void test() throws IOException {
        ResourceManager rm = NERResourceManagerFactory.get("edu/illinois/cs/cogcomp/ner/reuters.config", "#MODELS_PATH#", 
        		"#RESOURCES_PATH#","","");
        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_MODEL), "models/reuters");
        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_GAZETTEERS), "gazetteers");
        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_TOKEN_NORM_DATA), "brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");
        rm = NERResourceManagerFactory.get("edu/illinois/cs/cogcomp/ner/reuters.config", "#MODELS_PATH#", 
        		"#RESOURCES_PATH#",".",".");
        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_MODEL), "./models/reuters");
        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_GAZETTEERS), "./gazetteers");
        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_TOKEN_NORM_DATA), "./brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");

        try {
	        rm = NERResourceManagerFactory.get("reuters.config", null, null, null, null);
	        fail("This configuration file should not have been found, should have thrown a FileNotFoundException.");
        } catch (FileNotFoundException fnfe) {
        }
        
        try {
	        rm = NERResourceManagerFactory.get("edu/illinois/cs/cogcomp/ner/reuters.config", null, null, null, null);
	        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_MODEL), "#MODELS_PATH#models/reuters");
	        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_GAZETTEERS), "#RESOURCES_PATH#gazetteers");
	        assertEquals(rm.getString(NerBaseConfigurator.PATH_TO_TOKEN_NORM_DATA), "#RESOURCES_PATH#brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");
        } catch (FileNotFoundException fnfe) {
        	throw fnfe;
        }
    }

}
