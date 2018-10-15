/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.config;

import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.util.Properties;

/**
 * Configuration for NER using CoNLL model with "standard" parameters (i.e., those we found to
 * perform best on the labeled data we have). Note that this includes a default ViewName that
 * conforms to expected CCG software behavior.
 * <p>
 * This class avoids the need for a configuration file. Non-default config values can be set
 * individually, and the client will use the inherited {#getConfig( ResourceManager rm )} method to
 * override only those values that conflict with defaults. Created by mssammon on 10/14/15.
 */
public class NerBaseConfigurator extends AnnotatorConfigurator {

    /**
     * default: do NOT treat as sentence-level annotator as NER has some features that are based on a large context
     *     window.
     */
    public final static Property IS_SENTENCE_LEVEL = new Property(AnnotatorConfigurator.IS_SENTENCE_LEVEL.key, FALSE);

    public final static String PATH_TO_MODEL = "pathToModelFile";
    public final static String VIEW_NAME = "viewName";

    public final static String TRAINED_ON = "all-data";
    public final static String TRAINED_ON_ALL_DATA = "all-data";
    public final static String TRAINED_ON_TRAINING_DATA = "training-data";

    public final static String MODEL_NAME = "modelName";
    public final static String TAG_SCHEME = "taggingEncodingScheme";
    // public final static String TOKENIZATION_SCHEME = "tokenizationScheme";
    public final static String FORCE_NEW_SENTENCE_ON_LINE_BREAKS = "forceNewSentenceOnLineBreaks";
    public final static String LABEL_TYPES = "labelTypes";

    public final static String NORMALIZE_TITLE_TEXT = "normalizeTitleText";
    public final static String PATH_TO_TOKEN_NORM_DATA = "pathToTokenNormalizationData";
    public final static String SORT_FILES_LEXICALLY = "sortLexicallyFilesInFolders";
    public final static String MIN_CONFIDENCE_PREDICTIONS_1 = "minConfidencePredictionsLevel1";
    public final static String MIN_CONFIDENCE_PREDICTIONS_2 = "minConfidencePredictionsLevel2";
    public final static String TREAT_ALL_FILES_AS_ONE = "treatAllFilesInFolderAsOneBigDocument";
    public final static String DEBUG = "debug";
    public final static String LANGUAGE = "language";


    public final static String FORMS = "Forms";
    public final static String PHRASE_LENGTH = "PhraseLength";
    public final static String CAPITALIZATION = "Capitalization";
    public final static String WORD_TYPE_INFORMATION = "WordTypeInformation";
    public final static String AFFIXES = "Affixes";
    public final static String PREV_TAG_1 = "PreviousTag1";
    public final static String PREV_TAG_2 = "PreviousTag2";
    public final static String PREV_TAG_PATTERN_1 = "PreviousTagPatternLevel1";
    public final static String PREV_TAG_PATTERN_2 = "PreviousTagPatternLevel2";
    public final static String AGGREGATE_CONTEXT = "AggregateContext";
    public final static String AGGREGATE_GAZETTEER = "AggregateGazetteerMatches";
    public final static String PREV_TAGS_FOR_CONTEXT = "PrevTagsForContext";
    public final static String PREDICTIONS_1 = "PredictionsLevel1";
    public final static String FEATUREPRUNINGTHRESHOLD = "FeaturePruningThreshold";

    public final static String BROWN_CLUSTER_PATHS = "BrownClusterPaths";
    public final static String USE_LOCAL_BROWN_CLUSTER = "UseLocalBrownCluster";
    public final static String IS_LOWERCASE_BROWN_CLUSTERS = "isLowercaseBrownClusters";
    public final static String PATHS_TO_BROWN_CLUSTERS = "pathsToBrownClusters";
    public final static String MIN_WORD_APP_THRESHOLDS_FOR_BROWN_CLUSTERS =
            "minWordAppThresholdsForBrownClusters";

    public final static String GAZETTEER_FEATURES = "GazetteersFeatures";
    public final static String PATH_TO_GAZETTEERS = "pathToGazetteersLists";

    public final static String WORD_EMBEDDINGS = "WordEmbeddings";

    public final static String RANDOM_NOISE_LEVEL = "randomNoiseLevel";
    public final static String OMISSION_RATE = "omissionRate";
    public final static String DEFAULT_MIN_CONFIDENCE_PREDICTIONS_1 = "0.0";
    public final static String DEFAULT_MIN_CONFIDENCE_PREDICTIONS_2 = "0.0";
    private final static String DEFAULT_BROWN_CLUSTER_PATHS = "1";
    private final static String DEFAULT_USE_LOCAL_BROWN_CLUSTER = "false";
    private final static String DEFAULT_IS_LOWERCASE_BROWN_CLUSTERS = "false false false";
    private final static String DEFAULT_PATHS_TO_BROWN_CLUSTERS =
            "brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt    brown-clusters/brownBllipClusters    brown-clusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt";
    private final static String DEFAULT_MIN_WORD_APP_THRESHOLDS_FOR_BROWN_CLUSTERS = "5 5 5";
    private final static String DEFAULT_GAZETTEER_FEATURES = "1";
    private final static String DEFAULT_PATHS_TO_GAZETTEERS = "gazetteers";
    private final static String DEFAULT_WORD_EMBEDDINGS = "0";
    private final static String DEFAULT_MODEL_PATH = "ner/models";
    private final static String DEFAULT_FORMS = "1";
    private final static String DEFAULT_PHRASE_LENGTH = "5";
    private final static String DEFAULT_CAPITALIZATION = "1";
    private final static String DEFAULT_WORD_TYPE_INFORMATION = "1";
    private final static String DEFAULT_AFFIXES = "1";
    private final static String DEFAULT_PREV_TAG_1 = "1";
    private final static String DEFAULT_PREV_TAG_2 = "1";
    private final static String DEFAULT_PREV_TAG_PATTERN_1 = "1";
    private final static String DEFAULT_PREV_TAG_PATTERN_2 = "1";
    private final static String DEFAULT_AGGREGATE_CONTENT = "0";
    private final static String DEFAULT_AGGREGATE_GAZETTEER = "0";
    private final static String DEFAULT_PREV_TAGS_FOR_CONTEXT = "1";
    private final static String DEFAULT_PREDICTIONS_1 = "1";
    private final static String DEFAULT_FEATUREPRUNINGTHRESHOLD = "0.000001";
    
    // private final static String DEFAULT_BEAM_SIZE = "5";
    private final static String DEFAULT_FORCE_LINE_BREAKS = TRUE;
    private final static String DEFAULT_LABELS = "PER ORG LOC MISC";
    private final static String DEFAULT_TAG_SCHEME = "BILOU";
    // private final static String DEFAULT_TOKENIZATION_SCHEME = "DualTokenizationScheme";
    private final static String DEFAULT_NORMALIZE_TITLE = FALSE;
    private final static String DEFAULT_PATH_TO_TOKEN_NORM_DATA =
            "brown-clusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt";
    private final static String DEFAULT_SORT_FILES_LEXICALLY = TRUE;
    private final static String DEFAULT_TREAT_ALL_FILES_AS_ONE = TRUE;
    private final static String DEFAULT_DEBUG = FALSE;
    private final static String DEFAULT_MODEL_NAME = "CoNLL_enron";
    private final static String DEFAULT_LANGUAGE = Language.English.getCode();

    private static final String DEFAULT_RANDOM_NOISE_LEVEL = "0.1";
    private static final String DEFAULT_OMISSION_RATE = "0.2";


    @Override
    public ResourceManager getDefaultConfig() {
        Properties props = new Properties();

        props.setProperty(VIEW_NAME, ViewNames.NER_CONLL);
        props.setProperty(TRAINED_ON, TRAINED_ON_ALL_DATA);
        props.setProperty(PHRASE_LENGTH, DEFAULT_PHRASE_LENGTH);
        props.setProperty(PATH_TO_MODEL, DEFAULT_MODEL_PATH);
        props.setProperty(MODEL_NAME, DEFAULT_MODEL_NAME);
        props.setProperty(AFFIXES, DEFAULT_AFFIXES);
        props.setProperty(AGGREGATE_CONTEXT, DEFAULT_AGGREGATE_CONTENT);
        props.setProperty(AGGREGATE_GAZETTEER, DEFAULT_AGGREGATE_GAZETTEER);
        props.setProperty(DEBUG, DEFAULT_DEBUG);
        props.setProperty(BROWN_CLUSTER_PATHS, DEFAULT_BROWN_CLUSTER_PATHS);
        props.setProperty(USE_LOCAL_BROWN_CLUSTER, DEFAULT_USE_LOCAL_BROWN_CLUSTER);

        props.setProperty(CAPITALIZATION, DEFAULT_CAPITALIZATION);
        props.setProperty(FORCE_NEW_SENTENCE_ON_LINE_BREAKS, DEFAULT_FORCE_LINE_BREAKS);
        props.setProperty(FORMS, DEFAULT_FORMS);
        props.setProperty(GAZETTEER_FEATURES, DEFAULT_GAZETTEER_FEATURES);

        props.setProperty(IS_LOWERCASE_BROWN_CLUSTERS, DEFAULT_IS_LOWERCASE_BROWN_CLUSTERS);
        props.setProperty(LABEL_TYPES, DEFAULT_LABELS);
        props.setProperty(NORMALIZE_TITLE_TEXT, DEFAULT_NORMALIZE_TITLE);
        props.setProperty(TAG_SCHEME, DEFAULT_TAG_SCHEME);
        props.setProperty(TREAT_ALL_FILES_AS_ONE, DEFAULT_TREAT_ALL_FILES_AS_ONE);
        props.setProperty(PATH_TO_TOKEN_NORM_DATA, DEFAULT_PATH_TO_TOKEN_NORM_DATA);

        props.setProperty(MIN_CONFIDENCE_PREDICTIONS_1, DEFAULT_MIN_CONFIDENCE_PREDICTIONS_1);
        props.setProperty(MIN_CONFIDENCE_PREDICTIONS_2, DEFAULT_MIN_CONFIDENCE_PREDICTIONS_2);
        props.setProperty(SORT_FILES_LEXICALLY, DEFAULT_SORT_FILES_LEXICALLY);
        props.setProperty(PREV_TAG_1, DEFAULT_PREV_TAG_1);
        props.setProperty(PREV_TAG_2, DEFAULT_PREV_TAG_2);
        props.setProperty(PREV_TAG_PATTERN_1, DEFAULT_PREV_TAG_PATTERN_1);
        props.setProperty(PREV_TAG_PATTERN_2, DEFAULT_PREV_TAG_PATTERN_2);
        props.setProperty(PREV_TAGS_FOR_CONTEXT, DEFAULT_PREV_TAGS_FOR_CONTEXT);
        props.setProperty(WORD_TYPE_INFORMATION, DEFAULT_WORD_TYPE_INFORMATION);
        props.setProperty(PREDICTIONS_1, DEFAULT_PREDICTIONS_1);
        props.setProperty(FEATUREPRUNINGTHRESHOLD, DEFAULT_FEATUREPRUNINGTHRESHOLD);
        props.setProperty(PATHS_TO_BROWN_CLUSTERS, DEFAULT_PATHS_TO_BROWN_CLUSTERS);
        props.setProperty(WORD_EMBEDDINGS, DEFAULT_WORD_EMBEDDINGS);
        props.setProperty(PATH_TO_GAZETTEERS, DEFAULT_PATHS_TO_GAZETTEERS);
        props.setProperty(MIN_WORD_APP_THRESHOLDS_FOR_BROWN_CLUSTERS,
                DEFAULT_MIN_WORD_APP_THRESHOLDS_FOR_BROWN_CLUSTERS);

        props.setProperty(RANDOM_NOISE_LEVEL, DEFAULT_RANDOM_NOISE_LEVEL);
        props.setProperty(OMISSION_RATE, DEFAULT_OMISSION_RATE);
        props.setProperty(IS_LAZILY_INITIALIZED.key, TRUE);
        props.setProperty(LANGUAGE, DEFAULT_LANGUAGE);
        props.setProperty(IS_SENTENCE_LEVEL.key, IS_SENTENCE_LEVEL.value);

        return new ResourceManager(props);
    }
}
