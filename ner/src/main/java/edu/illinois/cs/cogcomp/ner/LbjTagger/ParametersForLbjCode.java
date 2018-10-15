/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseNetworkLearner;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;

import java.util.HashMap;
import java.util.Vector;

public class ParametersForLbjCode {

    /** Enums
    TODO: fix or remove DualTokenizationScheme*/
    public enum TokenizationScheme {
        LbjTokenizationScheme, DualTokenizationScheme
    }

    /** this is the gazetteers if we are using them, or null if not. */
    public Gazetteers gazetteers = null;
    
    /** the brown clusters, or null if disabled. */
    public BrownClusters brownClusters = null;
    
    /** Optional / predefined features
    // This is necessary for brackets file reader
    // will be initialized to something like {"PER","ORG","LOC","MISC"}; */
    public String[] labelTypes = {"PER", "ORG", "LOC", "MISC"};

    /** Labels to ignore when evaluating model performance, e.g. "MISC" for the MUC7 dataset. */
    public Vector<String> labelsToIgnoreInEvaluation = null;

    // Labels to evaluate only for having found an NE regardless of the label found.
    public Vector<String> labelsToAnonymizeInEvaluation = null;

    // if we use this set of parameters as auxiliary feature- this tells us what is the name of the
    // feature to generate.
    public String nameAsAuxFeature = null;

    public SparseNetworkLearner taggerLevel1;
    public SparseNetworkLearner taggerLevel2;

    // predictions with lower confidence will be pruned
    public double minConfidencePredictionsLevel1 = 0;
    // predictions with lower confidence will be pruned
    public double minConfidencePredictionsLevel2 = 0;

    // predictions with lower confidence will be pruned
    public double learningRatePredictionsLevel1 = 0.06;
    
    // predictions with lower confidence will be pruned
    public double learningRatePredictionsLevel2 = 0.05;

    // predictions with lower confidence will be pruned
    public int thicknessPredictionsLevel1 = 50;
    
    // predictions with lower confidence will be pruned
    public int thicknessPredictionsLevel2 = 30;

    // the predictions of these models will be the input to the classifier
    public Vector<ParametersForLbjCode> auxiliaryModels = new Vector<>();

    public boolean debug = false;

    public boolean sortLexicallyFilesInFolders = true;
    public boolean treatAllFilesInFolderAsOneBigDocument = false;
    public boolean forceNewSentenceOnLineBreaks = false;
    // this will selectively lowercase the text in the first sentence if it's all-capitalized
    public boolean normalizeTitleText = false;

    public Language language = null;

    // can be:
    // ".data/ner-ext/BrownHierarchicalWordClusters/brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt";
    public String pathToTokenNormalizationData = null;

    // this will not normalize the text in any way
    public boolean keepOriginalFileTokenizationAndSentenceSplitting = false;

    public double featurePruningThreshold = 0.000001;
    public double randomNoiseLevel = 0;
    public double omissionRate = 0;
    public RandomLabelGenerator patternLabelRandomGenerator = null;
    public RandomLabelGenerator level1AggregationRandomGenerator = null;
    public RandomLabelGenerator prevPredictionsLevel1RandomGenerator = null;
    public RandomLabelGenerator prevPredictionsLevel2RandomGenerator = null;

    public HashMap<String, Boolean> featuresToUse = null;

    // Required features
    public String pathToModelFile = null;
    // should be either LbjTokenizationScheme or DualTokenizationScheme
    public TextChunkRepresentationManager.EncodingScheme taggingEncodingScheme = null;
    // should be BIO / BILOU/ IOB1/ IOE1/ IOE2
    public String configFilename = null;
    // so that we can automatically save the models at the right place
    // this name must be unique for each config file, and it will be appended to the model file
    // names,
    public TokenizationScheme tokenizationScheme = TokenizationScheme.DualTokenizationScheme;

}
