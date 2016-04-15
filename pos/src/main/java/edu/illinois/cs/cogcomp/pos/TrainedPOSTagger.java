package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.pos.lbjava.*;

/**
 * This POS Tagger uses a pre-trained model. The model files will be found by checking two locations
 * in order:
 * <ul>
 * <li>First, the directory specified in the constant Constants.modelPath
 * <li>If the files are not found in this directory, the classpath will be checked (this will result
 * in loading the files from the maven repository
 * </ul>
 *
 * This class acts as a wrapper around the POS Tagging classes defined in the LBJava code.
 * 
 * @author Colin Graber
 */
public class TrainedPOSTagger {

    private POSTaggerKnown known;
    private POSTaggerUnknown unknown;
    private BaselineTarget baseline;
    private WordForm wordForm;

    /**
     * Initializes a tagger from either a pre-specified directory or the classpath
     */
    public TrainedPOSTagger() {
        ResourceManager rm = new POSConfigurator().getDefaultConfig();
        String knownModelFile = rm.getString("knownModelPath");
        String knownLexFile = rm.getString("knownLexPath");
        String unknownModelFile = rm.getString("unknownModelPath");
        String unknownLexFile = rm.getString("unknownLexPath");
        String baselineModelFile = rm.getString("baselineModelPath");
        String baselineLexFile = rm.getString("baselineLexPath");
        String mikheevModelFile = rm.getString("mikheevModelPath");
        String mikheevLexFile = rm.getString("mikheevLexPath");

        baseline = new BaselineTarget(baselineModelFile, baselineLexFile);
        MikheevTable mikheevTable = new MikheevTable(mikheevModelFile, mikheevLexFile);
        known = new POSTaggerKnown(knownModelFile, knownLexFile, baseline);
        unknown = new POSTaggerUnknown(unknownModelFile, unknownLexFile, mikheevTable);
        wordForm = new WordForm();
    }

    /**
     * Finds the correct POS tag for the provided token
     *
     * @param w The Token whose POS tag is being sought
     * @return A string representing the POS tag for the token
     */
    public String discreteValue(Token w) {
        if (baseline.observed(wordForm.discreteValue(w))) {
            return known.discreteValue(w);
        }
        return unknown.discreteValue(w);
    }
}
