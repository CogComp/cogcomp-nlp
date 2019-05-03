/**
 * 
 */
package edu.illinois.cs.cogcomp.ner;

import java.io.File;

import org.cogcomp.Datastore;
import org.cogcomp.DatastoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

/**
 * This class will load the level one and level two NER models. It will look first in the local file system
 * in case the user has their own custom model. Next it will look in a jar file for the same reason. In 
 * most cases, they are not found there, we will get them from the Minio datastore. The resulting models
 * are stored in the ParametersForLbjCode singleton.
 * @author redman
 */
public class ModelLoader {
    
    /** logger for the model loader. */
    private static Logger logger = LoggerFactory.getLogger(ModelLoader.class);

    /**
     * Load the models wherever they are found. Check file system first, then classpath, and finally get it 
     * from Minio datastore.
     * @param rm the resource manager.
     * @param training if we are training.
     * @param viewName the name of the view identifies the model.
     * @param cp the parameters for the calling model.
     */
    static public void load(ResourceManager rm, String viewName, boolean training, ParametersForLbjCode cp) {
        
        // the loaded built into the model will check the local file system and the jar files in the classpath.
        String modelPath = cp.pathToModelFile;
        String modelFilePath = modelPath + ".level1";
        java.io.File modelFile = new File(modelFilePath);
        NETaggerLevel1 tagger1 = null;
        NETaggerLevel2 tagger2 = null;
        if (modelFile.exists()) {
            tagger1 = new NETaggerLevel1(modelPath + ".level1", modelPath + ".level1.lex");
            logger.info("Reading L1 model from file : " + modelPath + ".level2");
            if (cp.featuresToUse.containsKey("PredictionsLevel1")) {
                tagger2 = new NETaggerLevel2(modelPath + ".level2", modelPath + ".level2.lex");
                logger.info("Reading L2 model from file : " + modelPath + ".level2");
            } else {
                logger.info("L2 model not required.");
            }
        } else if (IOUtilities.existsInClasspath(NETaggerLevel1.class, modelFilePath)) {
            tagger1 = new NETaggerLevel1(modelPath + ".level1", modelPath + ".level1.lex");
            logger.info("Reading L1 model from classpath : " + modelPath + ".level2");
            if (cp.featuresToUse.containsKey("PredictionsLevel1")) {
                tagger2 = new NETaggerLevel2(modelPath + ".level2", modelPath + ".level2.lex");
                logger.info("Reading L2 model from classpath : " + modelPath + ".level2");
            } else {
                logger.info("L2 model not required.");
            }
        } else if (training) {
            
            // we are training a new model, so it it doesn't exist, we don't care, just create a
            // container.
            tagger1 = new NETaggerLevel1(modelPath + ".level1", modelPath + ".level1.lex");
            logger.info("Reading L1 model from file : " + modelPath + ".level2");
            if (cp.featuresToUse.containsKey("PredictionsLevel1")) {
                tagger2 = new NETaggerLevel2(modelPath + ".level2", modelPath + ".level2.lex");
                logger.info("Reading L2 model from file : " + modelPath + ".level2");
            } else {
                logger.info("L2 model not required.");
            }
        } else {

            // all else has filed, load from the datastore, create artifact ids based on the view
            // name and training data designation.
            String dataset;
            String lowercaseViewName = viewName.toLowerCase();
            if (lowercaseViewName.contains(ViewNames.NER_CONLL.toLowerCase())) {
                dataset = "enron-conll";
            } else if (lowercaseViewName.contains(ViewNames.NER_ONTONOTES.toLowerCase())) {
                dataset = "ontonotes";
            } else {
                // not a standard model, and we can't find it on the command line.
                throw new IllegalArgumentException("The NER models could not be found at \""+modelPath+"\", and no default with view name "+viewName);
            }
            String data_split;
            if (!rm.containsKey(NerBaseConfigurator.TRAINED_ON))
                data_split = NerBaseConfigurator.TRAINED_ON_ALL_DATA;
            else
                data_split= rm.getString(NerBaseConfigurator.TRAINED_ON);
            try {
                Datastore ds = new Datastore(new ResourceConfigurator().getConfig(rm));
                String artifact_id = "ner-model-" + dataset + "-" + data_split;
                File modelDir = ds.getDirectory("edu.illinois.cs.cogcomp.ner", artifact_id, 4.0, false);
                String model = "";
                if(modelDir.getPath().contains("conll")) {
                    model = modelDir.getPath() + "/model/EnronCoNLL.model";
                }
                else {
                    model = modelDir.getPath() + "/model/OntoNotes.model";
                }
                tagger1 = new NETaggerLevel1(model + ".level1", model + ".level1.lex");
                if (cp.featuresToUse.containsKey("PredictionsLevel1")) {
                    tagger2 = new NETaggerLevel2(model + ".level2", model + ".level2.lex");
                }
            } catch (InvalidPortException | DatastoreException | InvalidEndpointException e) {
                e.printStackTrace();
            }
        }
        cp.taggerLevel1 = tagger1;
        cp.taggerLevel2 = tagger2;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
    }
}
