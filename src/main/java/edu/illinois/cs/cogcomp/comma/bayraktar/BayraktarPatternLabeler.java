package edu.illinois.cs.cogcomp.comma.bayraktar;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import edu.illinois.cs.cogcomp.comma.Comma;
import edu.illinois.cs.cogcomp.comma.CommaProperties;

/**
 * Use this class to get labels for bayraktar-patterns which have been annotated
 *
 */
public class BayraktarPatternLabeler {
    private static final Map<String, String> BAYRAKTAR_PATTERN_TO_COMMA_LABEL;
    private static final boolean USE_NEW_LABEL_SET;
    static {
    	CommaProperties properties = CommaProperties.getInstance();
    	USE_NEW_LABEL_SET = properties.useNewLabelSet();
    	String[] labels = {"Attribute", "Complementary", "Interrupter", "Introductory", "List", "Quotation", "Substitute", "Locative"};
    	BAYRAKTAR_PATTERN_TO_COMMA_LABEL = new HashMap<>();
    	String ANNOTATION_SOURCE_DIR = properties.getBayraktarAnnotationsDir();
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
		String label = BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern);
		if(USE_NEW_LABEL_SET)
			return label;
		else
			return newToOldLabel(label);
	}
	
	/**
     * 
     * @param bayraktarPattern The bayraktar pattern whose label is required
     * @return the Bayraktar-label as specified in the annotation files
     */
	public static String getLabel(String bayraktarPattern){
		String label = BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern);
		if(USE_NEW_LABEL_SET)
			return label;
		else
			return newToOldLabel(label);
	}
	
	public static boolean isLabelAvailable(String bayraktarPattern){
		return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern)!=null;
	}
	
	public static boolean isLabelAvailable(Comma comma){
		String bayraktarPattern = comma.getBayraktarPattern();
		return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.get(bayraktarPattern)!=null;
	}
	
	/**
	 * checks whether the given label belongs to the set of labels included in the Bayraktar annotations that were not previously included in Vivek's annotations 
	 * @param label: the label that is to be checked
	 * @return true of the label does belong to the set of labels included in the Bayraktar annotations that were not previously included in Vivek's annotations. Else false
	 */
	public static boolean isNewLabel(String label){
		String[] newLabels = {"Complementary", "Interrupter", "Introductory", "Quotation"};
		return ArrayUtils.contains(newLabels, label);
	}
	
	/**
	 * Transforms the given label belonging to the new label set to Vivek's old label set
	 * Complementary, Quotation, Introductory and Interrupter are transofrmed to Other. The rest remain the same.
	 * @param newLabel the label to be transformed
	 * @return the transformed label
	 */
	public static String newToOldLabel(String newLabel){
		if(BayraktarPatternLabeler.isNewLabel(newLabel))
			return "Other";
		else
			return newLabel;
	}
	
	public static int deleteGetTotalPatternsAnnotated(){
		return BAYRAKTAR_PATTERN_TO_COMMA_LABEL.size();
	}
}
