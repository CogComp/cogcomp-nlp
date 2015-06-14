package edu.illinois.cs.cogcomp.comma.bayraktar;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import edu.illinois.cs.cogcomp.comma.Comma;

public class BayraktarPatternLabeler {
	private static final String ANNOTATION_SOURCE_DIR = "data/Us+Bayraktar-SyntaxToLabel/";
    private static final Map<String, String> BAYRAKTAR_PATTERN_TO_COMMA_LABEL;
    
    static {
    	String[] labels = {"Attribute", "Complementary", "Interrupter", "Introductory", "List", "Quotation", "Substitute", "Locative"};
    	BAYRAKTAR_PATTERN_TO_COMMA_LABEL = new HashMap<String, String>();
		for(String label : labels){
			File file = new File(ANNOTATION_SOURCE_DIR + label);
			List<String> lines = null;
			try {
				lines = FileUtils.readLines(file, null);
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
     * 
     * @param comma The bayraktar pattern whose label is required
     * @return the Bayraktar-label as specified in the annotation files
     */
	public static String getLabel(String bayraktarPattern){
		return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern);
	}
	
	public static boolean isLabelAvailable(String bayraktarPattern){
		return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern)!=null;
	}
	
	public static boolean isLabelAvailable(Comma comma){
		String bayraktarPattern = comma.getBayraktarPattern();
		return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern)!=null;
	}
	
	public static boolean isNewLabel(String label){
		String[] newLabels = {"Complementary", "Interrupter", "Introductory", "Quotation"};
		return ArrayUtils.contains(newLabels, label);
	}
	
	public static int deleteGetTotalPatternsAnnotated(){
		return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.size();
	}
}
