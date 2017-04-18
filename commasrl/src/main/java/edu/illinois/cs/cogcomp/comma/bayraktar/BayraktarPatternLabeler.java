package edu.illinois.cs.cogcomp.comma.bayraktar;

import cogcomp.Datastore;
import cogcomp.DatastoreException;
import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.core.io.LineIO;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use this class to get labels for bayraktar-patterns which have been annotated
 *
 */
public class BayraktarPatternLabeler {
    private static final Map<String, String> BAYRAKTAR_PATTERN_TO_COMMA_LABEL;
    static {
    	CommaProperties properties = CommaProperties.getInstance();
    	String[] labels = {"Attribute", "Complementary", "Interrupter", "Introductory", "List", "Quotation", "Substitute", "Locative"};
    	BAYRAKTAR_PATTERN_TO_COMMA_LABEL = new HashMap<>();
    	String ANNOTATION_SOURCE_DIR = properties.getBayraktarAnnotationsDir() + File.separator;
    	if(properties.useDatastoreToReadData()) {
			try {
				Datastore ds = new Datastore("http://smaug.cs.illinois.edu:8080");
				File f = ds.getDirectory("org.cogcomp.comma-srl", "comma-srl-data", 2.2,false);
				ANNOTATION_SOURCE_DIR = f.getAbsolutePath() + File.separator + "comma-srl-data" + File.separator +
						"Bayraktar-SyntaxToLabel" + File.separator + "modified" + File.separator;
			} catch (DatastoreException e) {
				e.printStackTrace();
			}
		}
		for(String label : labels){
			String file = ANNOTATION_SOURCE_DIR + label;
			List<String> lines;
			try {
				lines = LineIO.read(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			for(String line : lines){
				line = line.split("#")[0];
				if(!line.isEmpty())
					BAYRAKTAR_PATTERN_TO_COMMA_LABEL.put(line, label);
			}
		}
    }
	
    /**
     * 
     * @param comma The comma whose Bayraktar label is required
     * @return the Bayraktar-label as specified in the annotation files
     */
	public static String getLabel(Comma comma){
		String bayraktarPattern = comma.getBayraktarPattern();
        return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern);
	}
	
	/**
	 * checks if the given commas bayraktar pattern has been annotated
	 * @param comma 
	 * @return true if the bayraktar pattern has been annotated
	 */
	public static boolean isLabelAvailable(Comma comma){
		String bayraktarPattern = comma.getBayraktarPattern();
		return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern)!=null;
	}

}
