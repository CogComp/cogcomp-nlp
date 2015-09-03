package edu.illinois.cs.cogcomp.comma;

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
    
    public boolean isCommaStructureFullSentence() {
    	return getBoolean("IS_COMMA_STRUCUTRE_FULL_SENTENCE");
    }
    
    public boolean useNewLabelSet() {
        return getBoolean("USE_NEW_LABEL_SET");
    }

    public String getOtherRelabeledFile() {
        return getString("OTHER_RELABELED_FILE");
    }
    
    public String getAllCommasSerialized() {
        return getString("SERIALIZED_DIR") + "/" + getString("SERIALIZED_ALL_COMMAS_FILE");
    }
    
    public String getTrainCommasSerialized() {
    	return getString("SERIALIZED_DIR") + "/" +  getString("SERIALIZED_TRAIN_COMMAS_FILE");
    }
    
    public String getDevCommasSerialized() {
    	return getString("SERIALIZED_DIR") + "/" +  getString("SERIALIZED_DEV_COMMAS_FILE");
    }
    
    public String getTestCommasSerialized() {
    	return getString("SERIALIZED_DIR") + "/" +  getString("SERIALIZED_TEST_COMMAS_FILE");
    }
    
    public String getOriginalVivekAnnotationFile(){
    	return getString("ORIGINAL_VIVEK_ANNOTATIONS_FILE");
    }
    
    public String getOTHERRefinedFile(){
    	return getString("OTHER_REFINED_FILE");
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
}
