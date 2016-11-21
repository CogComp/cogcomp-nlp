/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.TitleTextNormalizer;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.WordEmbeddings;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.WordEmbeddings.NormalizationMethod;
import edu.illinois.cs.cogcomp.ner.IO.OutFile;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.ner.config.NerOntonotesConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class might more accurately be called 'ParameterLoader'. This is the class that has
 * functionality for populating {@link ParametersForLbjCode}, which actually contains the parameters
 * used throughout the program.
 * <p>
 * Rewritten by Stephen Mayhew, Jan 2013
 */
public class Parameters {
    private static Logger logger = LoggerFactory.getLogger(Parameters.class);

    /* What's the distinction? */
    private static String[] possibleFeatures = {"Forms", "Capitalization", "WordTypeInformation",
            "Affixes", "PreviousTag1", "PreviousTag2", "PreviousTagPatternLevel1",
            "PreviousTagPatternLevel2", "AggregateContext", "AggregateGazetteerMatches",
            "PrevTagsForContext", "PredictionsLevel1", "GazetteersFeatures", "WordEmbeddings",
            "BrownClusterPaths", "Linkability"};


    /**
     * Convenience method. See {@link #readConfigAndLoadExternalData(String, boolean)} for more
     * information.
     * 
     * @param rm a populated <code>ResourceManager</code> passed as argument to
     *        {@link #readAndLoadConfig readAndLoadConfig}
     */
    public static void readConfigAndLoadExternalData(ResourceManager rm) {
        ParametersForLbjCode.currentParameters = readAndLoadConfig(rm, false);
    }


    /**
     * This just calls {@link #readAndLoadConfig readAndLoadConfig}. The main difference is that it
     * assigns the result to {@link ParametersForLbjCode#currentParameters}. This is important:
     * <code>ParametersForLbjCode</code> is changed because of this.
     *
     * @param configFile the path to a config file, to be loaded by a <code>ResourceManager</code>.
     * @param areWeTraining this value determines whether or not this run will involve training a
     *        model. If we are training, then we make sure there exists a folder in which to put the
     *        trained model. If not, then we make sure the model exists.
     * @throws IOException if the <code>ResourceManager</code> doesn't load correctly.
     */
    public static void readConfigAndLoadExternalData(String configFile, boolean areWeTraining)
            throws IOException {
        ResourceManager rm = new ResourceManager(configFile);
        String modelName = rm.getString("modelName");
        String modelDir = rm.getString("pathToModelFile");
        Map<String, String> nonDefaultProps = new HashMap<>();
        nonDefaultProps.put(NerBaseConfigurator.PATH_TO_MODEL, modelDir);
        NerBaseConfigurator baseConfigurator = new NerBaseConfigurator();
        NerOntonotesConfigurator ontonotesConfigurator = new NerOntonotesConfigurator();
        // If this is a known model name just use the path property, otherwise load all non-default
        // settings
        switch (modelName) {
            case ViewNames.NER_CONLL:
                ParametersForLbjCode.currentParameters =
                        readAndLoadConfig(baseConfigurator.getConfig(nonDefaultProps),
                                areWeTraining);
                break;
            case ViewNames.NER_ONTONOTES:
                ParametersForLbjCode.currentParameters =
                        readAndLoadConfig(baseConfigurator.getConfig(ontonotesConfigurator
                                .getConfig(nonDefaultProps)), areWeTraining);
                break;
            default:
                ParametersForLbjCode.currentParameters =
                        readAndLoadConfig(baseConfigurator.getConfig(rm), areWeTraining);
                break;
        }
    }

    /**
     * This is the method that does all the work. This populates and returns a
     * {@link ParametersForLbjCode} object, which is then used throughout the codebase.
     *
     * @param rm a populated <code>ResourceManager</code>.
     * @param areWeTraining this value determines whether or not this run will involve training a
     *        model. If we are training, then we make sure there exists a folder in which to put the
     *        trained model. If not, then we make sure the model exists.
     * @return a {@link ParametersForLbjCode} object populated according to the
     *         <code>ResourceManager</code> argument.
     */
    public static ParametersForLbjCode readAndLoadConfig(ResourceManager rm, boolean areWeTraining) {
        ParametersForLbjCode param = new ParametersForLbjCode();

        try {
            // First check for any empty answers (NOT allowed):
            Enumeration<Object> enumeration = rm.getKeys();
            while (enumeration.hasMoreElements()) {
                String el = (String) enumeration.nextElement();
                if (rm.getString(el).isEmpty()) {
                    throw new IllegalArgumentException("Config File Error: parameter " + el
                            + " has no value. Either comment it out (with #), or remove it.");
                }
            }

            param.debug = rm.getDebug();
            // ParametersForLbjCode.currentParameters.debug = param.debug;

            double randomNoiseLevel = rm.getDouble(NerBaseConfigurator.RANDOM_NOISE_LEVEL);
            double omissionRate = rm.getDouble(NerBaseConfigurator.OMISSION_RATE);

            // Required params
            String cFilename = rm.getString(NerBaseConfigurator.MODEL_NAME);
            if (cFilename == null) {
                throw new IllegalArgumentException(
                        "Config File Error: Expected value for non-optional '"
                                + NerBaseConfigurator.MODEL_NAME + "'");
            }
            param.configFilename = cFilename;

            if (rm.containsKey("labelsToAnonymizeInEvaluation")) {
                String labelsToAnonymizeInEvaluation =
                        rm.getString("labelsToAnonymizeInEvaluation");
                param.labelsToAnonymizeInEvaluation =
                        new Vector<>(Arrays.asList(labelsToAnonymizeInEvaluation.split(" ")));
            }
            if (rm.containsKey("labelsToIgnoreInEvaluation")) {
                String labelsToIgnoreInEvaluation = rm.getString("labelsToIgnoreInEvaluation");
                param.labelsToIgnoreInEvaluation =
                        new Vector<>(Arrays.asList(labelsToIgnoreInEvaluation.split(" ")));
            }
            if (rm.getString("pathToModelFile") == null) {
                throw new IllegalArgumentException(
                        "Config File Error: Expected value for non-optional 'pathToModelFile'");
            }
            param.pathToModelFile =
                    rm.getString("pathToModelFile") + "/" + param.configFilename + ".model";

            String modelFile1 = param.pathToModelFile + ".level1";
            File fullModelFile1 = new File(modelFile1);
            boolean file1Exists =
                    fullModelFile1.exists()
                            || IOUtilities.existsInClasspath(NETaggerLevel1.class, modelFile1);
            String modelFile1Lex = param.pathToModelFile + ".level1.lex";
            File fullModelFile1Lex = new File(modelFile1Lex);
            boolean file1LexExists =
                    fullModelFile1Lex.exists()
                            || IOUtilities.existsInClasspath(NETaggerLevel1.class, modelFile1Lex);
            String modelFile2 = param.pathToModelFile + ".level2";
            File fullModelFile2 = new File(modelFile2);
            boolean file2Exists =
                    fullModelFile2.exists()
                            || IOUtilities.existsInClasspath(NETaggerLevel2.class, modelFile2);
            String modelFile2Lex = param.pathToModelFile + ".level2.lex";
            File fullModelFile2Lex = new File(modelFile2Lex);
            boolean file2LexExists =
                    fullModelFile2Lex.exists()
                            || IOUtilities.existsInClasspath(NETaggerLevel2.class, modelFile2Lex);

            if (!file1Exists
                    || !file1LexExists
                    || (rm.containsKey("PredictionsLevel1")
                            && rm.getString("PredictionsLevel1").equals("1") && (!file2Exists || !file2LexExists))) {
                // if we are not training
                if (!areWeTraining) {
                    throw new IllegalArgumentException("Config File Error: one of "
                            + param.pathToModelFile + ".level{1,2}[.lex] does not exist.");
                } else {
                    // if we are training, we need to have the train directory
                    File trainDir = new File(rm.getString("pathToModelFile"));
                    if (!trainDir.isDirectory())
                        trainDir.mkdirs();
                }
            }

            String taggingEncodingSchemeString = rm.getString("taggingEncodingScheme");
            if (taggingEncodingSchemeString == null) {
                throw new IllegalArgumentException(
                        "Config File Error: Expected value for non-optional 'taggingEncodingScheme'");
            }
            param.taggingEncodingScheme =
                    TextChunkRepresentationManager.EncodingScheme
                            .valueOf(taggingEncodingSchemeString);

            // Optional params
            if (rm.containsKey("auxiliaryModels")) {
                String auxListString = rm.getString("auxiliaryModels");
                String[] auxModels = auxListString.split("\\t"); // should be a list
                // FIXME: add func so that if auxModels.length is odd, then we have a problem...
                for (int i = 0; i < auxModels.length; i += 2) {
                    ResourceManager auxRm = new ResourceManager(auxModels[i]);
                    ParametersForLbjCode aux = readAndLoadConfig(auxRm, false); // loading auxiliary
                                                                                // models, never
                                                                                // training
                    aux.nameAsAuxFeature = auxModels[i + 1];
                    loadClassifierModels(aux);
                    param.auxiliaryModels.addElement(aux);
                }
            }

            if (rm.containsKey("normalizeTitleText")) {
                param.normalizeTitleText = Boolean.parseBoolean(rm.getString("normalizeTitleText"));
            }
            if (rm.containsKey("pathToTokenNormalizationData")) {
                param.pathToTokenNormalizationData = rm.getString("pathToTokenNormalizationData");
                TitleTextNormalizer.pathToBrownClusterForWordFrequencies =
                        param.pathToTokenNormalizationData;
            }
            if (rm.containsKey("forceNewSentenceOnLineBreaks")) {
                param.forceNewSentenceOnLineBreaks =
                        Boolean.parseBoolean(rm.getString("forceNewSentenceOnLineBreaks"));
            }
            if (rm.containsKey("sortLexicallyFilesInFolders")) {
                param.sortLexicallyFilesInFolders =
                        Boolean.parseBoolean(rm.getString("sortLexicallyFilesInFolders"));
            }
            if (rm.containsKey("treatAllFilesInFolderAsOneBigDocument")) {
                param.treatAllFilesInFolderAsOneBigDocument =
                        Boolean.parseBoolean(rm.getString("treatAllFilesInFolderAsOneBigDocument"));
            }

            if (rm.containsKey("minConfidencePredictionsLevel1")) {
                param.minConfidencePredictionsLevel1 =
                        Double.parseDouble(rm.getString("minConfidencePredictionsLevel1"));
            }

            if (rm.containsKey("minConfidencePredictionsLevel2")) {
                param.minConfidencePredictionsLevel2 =
                        Double.parseDouble(rm.getString("minConfidencePredictionsLevel2"));
            }

            if (rm.containsKey("learningRatePredictionsLevel1")) {
                param.learningRatePredictionsLevel1 =
                        Double.parseDouble(rm.getString("learningRatePredictionsLevel1"));
            }

            if (rm.containsKey("learningRatePredictionsLevel2")) {
                param.learningRatePredictionsLevel2 =
                        Double.parseDouble(rm.getString("learningRatePredictionsLevel2"));
            }
            
            if (rm.containsKey("thicknessPredictionsLevel1")) {
                param.thicknessPredictionsLevel1 =
                        Integer.parseInt(rm.getString("thicknessPredictionsLevel1"));
            }

            if (rm.containsKey("thicknessPredictionsLevel2")) {
                param.thicknessPredictionsLevel2 =
                        Integer.parseInt(rm.getString("thicknessPredictionsLevel2"));
            }
            
           // labelTypes is just a String[]
            if (rm.containsKey("labelTypes")) {
                param.labelTypes = rm.getString("labelTypes").split("\\s+"); // split on whitespace
            }

            // Inclusion of all the features
            param.featuresToUse = new HashMap<>();
            for (String feature : possibleFeatures) {
                if (rm.containsKey(feature) && rm.getString(feature).equals("1")) {
                    logger.debug("Adding feature: {}", feature);

                    param.featuresToUse.put(feature, true);
                }
            }

            // Default positive features
            param.featuresToUse.put("TitleNormalization", true);
            param.featuresToUse.put("WordTopicTitleInfo", true);

            // Conditional Features section
            // GazetteersFeatures
            if (rm.containsKey("GazetteersFeatures")
                    && rm.getString("GazetteersFeatures").equals("1")) {
                String pathToGazetteersLists = rm.getString("pathToGazetteersLists");
                if (rm.containsKey("FlatGazetteers")
                        && Boolean.parseBoolean(rm.getString("FlatGazetteers"))) {
                    logger.info("Loading FlatGazetteers");
                    GazetteersFactory.init(5, pathToGazetteersLists, true);
                } else {
                    int maxPhraseLength = 5;
                    if (rm.containsKey(NerBaseConfigurator.PHRASE_LENGTH))
                        maxPhraseLength = rm.getInt(NerBaseConfigurator.PHRASE_LENGTH);
                    GazetteersFactory.init(maxPhraseLength, pathToGazetteersLists, false);
                }
            }

            // WordEmbeddings feature
            String wordEmbeddingDebug = "";
            if (rm.containsKey("WordEmbeddings") && rm.getString("WordEmbeddings").equals("1")) {
                Vector<String> pathsToWordEmbeddings =
                        getStringVector(rm.getString("pathsToWordEmbeddings").split("\\s+")); // list
                Vector<Integer> dimensionality =
                        getIntegerVector(rm.getString("embeddingDimensionalities").split("\\s+")); // list
                Vector<Integer> wordAppThresEmbeddings =
                        getIntegerVector(rm.getString("minWordAppThresholdsForEmbeddings").split(
                                "\\s+")); // list Note: look for minWordAppThresholdsForEmbeddings
                                          // FIXME: check all others for things like this
                Vector<Boolean> isLowercaseWordEmbeddings =
                        getBooleanVector(rm.getString("isLowercaseWordEmbeddings").split("\\s+")); // list
                Vector<Double> normalizationConstantsForEmbeddings =
                        getDoubleVector(rm.getString("normalizationConstantsForEmbeddings").split(
                                "\\s+")); // list
                Vector<NormalizationMethod> normalizationMethodsForEmbeddings =
                        getNMVector(rm.getString("normalizationMethodsForEmbeddings").split("\\s+")); // list

                // Check that all vectors are the same length
                int standard = pathsToWordEmbeddings.size();
                if (dimensionality.size() != standard || wordAppThresEmbeddings.size() != standard
                        || isLowercaseWordEmbeddings.size() != standard
                        || normalizationConstantsForEmbeddings.size() != standard
                        || normalizationMethodsForEmbeddings.size() != standard) {
                    throw new IllegalArgumentException(
                            "Config file error: all resources for WordEmbeddings "
                                    + "(pathsToWordEmbeddings, dimensionality, wordAppThresEmbeddings, "
                                    + "isLowercaseWordEmbeddings, normalizationConstantsForEmbeddings, "
                                    + "normalizationMethodsForEmbeddings) need to have the same number of parameters.");
                }


                WordEmbeddings.init(pathsToWordEmbeddings, dimensionality, wordAppThresEmbeddings,
                        isLowercaseWordEmbeddings, normalizationConstantsForEmbeddings,
                        normalizationMethodsForEmbeddings);

                for (int i = 0; i < pathsToWordEmbeddings.size(); i++) {
                    wordEmbeddingDebug += "Words Embeddings resource: \n";
                    wordEmbeddingDebug += "\t-Path: " + pathsToWordEmbeddings.elementAt(i) + "\n";
                    wordEmbeddingDebug += "\t-Dimensionality=" + dimensionality.elementAt(i) + "\n";
                    wordEmbeddingDebug +=
                            "\t-WordThres=" + wordAppThresEmbeddings.elementAt(i) + "\n";
                    wordEmbeddingDebug +=
                            "\t-IsLowercased=" + isLowercaseWordEmbeddings.elementAt(i) + "\n";
                }

            }

            // BrownClusterPaths feature
            String brownDebug = "";
            if (rm.containsKey("BrownClusterPaths")
                    && rm.getString("BrownClusterPaths").equals("1")) {
                Vector<String> pathsToBrownClusters =
                        getStringVector(rm.getString("pathsToBrownClusters").split("\\s+")); // list
                Vector<Integer> minWordAppThresholdsForBrownClusters =
                        getIntegerVector(rm.getString("minWordAppThresholdsForBrownClusters")
                                .split("\\s+")); // list
                Vector<Boolean> lowercaseBrown =
                        getBooleanVector(rm.getString("isLowercaseBrownClusters").split("\\s+")); // list

                // Check that vectors are all the same length
                int standard = pathsToBrownClusters.size();
                if (minWordAppThresholdsForBrownClusters.size() != standard
                        || lowercaseBrown.size() != standard) {
                    throw new IllegalArgumentException(
                            "Config file error: all resources for BrownClusters "
                                    + "(pathsToBrownClusters, minWordAppThresholdsForBrownClusters, "
                                    + "isLowercaseBrownClusters) need to have the same number of parameters.");
                }

                BrownClusters.init(pathsToBrownClusters, minWordAppThresholdsForBrownClusters,
                        lowercaseBrown);

                // For output later
                for (int i = 0; i < pathsToBrownClusters.size(); i++) {
                    brownDebug += "Brown clusters resource: \n";
                    brownDebug += "\t-Path: " + pathsToBrownClusters.elementAt(i) + "\n";
                    brownDebug +=
                            "\t-WordThres=" + minWordAppThresholdsForBrownClusters.elementAt(i)
                                    + "\n";
                    brownDebug += "\t-IsLowercased=" + lowercaseBrown.elementAt(i) + "\n";
                }

            }

            param.randomNoiseLevel = randomNoiseLevel;
            param.omissionRate = omissionRate;

            // don't forget that these should be initialized only after we know the target labels
            // and the encoding scheme
            param.patternLabelRandomGenerator =
                    new RandomLabelGenerator(param.labelTypes, param.taggingEncodingScheme,
                            randomNoiseLevel);
            param.level1AggregationRandomGenerator =
                    new RandomLabelGenerator(param.labelTypes, param.taggingEncodingScheme,
                            randomNoiseLevel);
            param.prevPredictionsLevel1RandomGenerator =
                    new RandomLabelGenerator(param.labelTypes, param.taggingEncodingScheme,
                            randomNoiseLevel);
            param.prevPredictionsLevel2RandomGenerator =
                    new RandomLabelGenerator(param.labelTypes, param.taggingEncodingScheme,
                            randomNoiseLevel);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return param;
    }



    public static void loadClassifierModels(ParametersForLbjCode config) {
        if (ParametersForLbjCode.currentParameters.debug) {
            logger.debug("Reading the model at: " + config.pathToModelFile + ".level1");
        }
        config.taggerLevel1 =
                new NETaggerLevel1(config.pathToModelFile + ".level1", config.pathToModelFile
                        + ".level1.lex");
        if (ParametersForLbjCode.currentParameters.debug) {
            logger.debug("Reading the model at: " + config.pathToModelFile + ".level2");
        }
        config.taggerLevel2 =
                new NETaggerLevel2(config.pathToModelFile + ".level2", config.pathToModelFile
                        + ".level2.lex");
        logger.debug("## Parameters.loadClassifierModels(): set taggerLevel1 and taggerLevel2 in config passed as argument.");
    }


    // ==================== Methods for converting ===================
    // a string list into a vector
    // this would be so easy in python

    private static Vector<String> getStringVector(String[] arr) {
        return new Vector<>(Arrays.asList(arr));
    }

    private static Vector<Integer> getIntegerVector(String[] arr) {
        Vector<Integer> v = new Vector<>();
        for (String s : arr) {
            v.add(Integer.parseInt(s));
        }
        return v;
    }

    private static Vector<Double> getDoubleVector(String[] arr) {
        Vector<Double> v = new Vector<>();
        for (String s : arr) {
            v.add(Double.parseDouble(s));
        }
        return v;
    }

    private static Vector<Boolean> getBooleanVector(String[] arr) {
        Vector<Boolean> v = new Vector<>();
        for (String s : arr) {
            v.add(Boolean.parseBoolean(s));
        }
        return v;
    }

    private static Vector<NormalizationMethod> getNMVector(String[] arr) {
        Vector<NormalizationMethod> v = new Vector<>();
        for (String s : arr) {
            v.add(NormalizationMethod.valueOf(s));
        }
        return v;
    }
    // ===============================================================
}
