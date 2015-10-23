package edu.illinois.cs.cogcomp.comma.datastructures;


import edu.illinois.cs.cogcomp.core.utilities.ResourceManager;

import java.io.IOException;

/**
 * A property manager for the CommaSRL.
 */
public class CommaProperties extends ResourceManager {

    private static CommaProperties instance;

    public CommaProperties(String configFile) throws IOException {
        super(configFile);
    }

    public static CommaProperties getInstance() {
        if (instance == null)
            try {
                instance = new CommaProperties("config/comma.properties");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Unable to read the configuration file");
                System.exit(-1);
            }
        return instance;
    }

    public boolean useGold() {
        return getBoolean("USE_GOLD");
    }
    
    public boolean allowMultiLabelCommas() {
        return getBoolean("ALLOW_MULTI_LABEL_COMMAS");
    }
    
    public boolean includeNullLabelCommas() {
        return getBoolean("INCLUDE_NULL_LABEL_COMMAS");
    }
    
    public boolean isCommaStructureFullSentence() {
    	return getBoolean("IS_COMMA_STRUCTURE_FULL_SENTENCE");
    }
    
    public boolean useNewLabelSet() {
        return getBoolean("USE_NEW_LABEL_SET");
    }

    public String getOtherRelabeledFile() {
        return getString("OTHER_RELABELED_FILE");
    }
    
    public String getOriginalSrikumarAnnotationFile(){
    	return getString("ORIGINAL_SRIKUMAR_ANNOTATIONS_FILE");
    }
    
    public String getBayraktarAnnotationsDir(){
    	return getString("BAYRAKTAR_ANNOTATIONS_DIR");
    }
    
    public String getPTBHDir() {
        return getString("PTB_DIR");
    }

    public String getPropbankDir() {
        return getString("PROPBANK_DIR");
    }

    public String getNombankDir() {
        return getString("NOMBANK_DIR");
    }
    
    public String getConstituentParser() {
        return getString("CONSTITUENT_PARSER");
    }

    public boolean lexicaliseNER() {
        return getBoolean("LEX_NER");
    }

    public boolean lexicalisePOS() {
        return getBoolean("LEX_POS");
    }

    public boolean useCurator() {
        return getBoolean("USE_CURATOR");
    }

    public String getLearnerConfig() {
        return getString("LEARNER_CONFIG");
    }
}
