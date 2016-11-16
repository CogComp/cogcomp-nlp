/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.annotation.AnnotatorConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel1;
import edu.illinois.cs.cogcomp.ner.LbjFeatures.NETaggerLevel2;
import edu.illinois.cs.cogcomp.ner.LbjTagger.*;
import edu.illinois.cs.cogcomp.ner.config.NerBaseConfigurator;
import edu.illinois.cs.cogcomp.ner.config.NerOntonotesConfigurator;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Generate NER annotations using the Annotator API.
 * 
 * @author redman
 */
public class NERAnnotator extends Annotator {

    /** POS, shallow parsing are NOT required. */
//    private static final String[] REQUIRED_VIEWS = {};
    /** our specific logger. */
    private final Logger logger = LoggerFactory.getLogger(NERAnnotator.class);
    /** the level one tagger. */
    private NETaggerLevel1 t1;
    /** the level two tagger. */
    private NETaggerLevel2 t2;


    /**
     * @param nonDefaultConfigValues a configuration file specifying non-default parameters for the
     *        NER model to use
     * @param viewName indicates the view name, and hence the model, that you wish to use. If you
     *        specify {@link ViewNames#NER_CONLL} or {@link ViewNames#NER_ONTONOTES}, This name will
     *        be used when creating Views in TextAnnotation objects.
     */
    public NERAnnotator(String nonDefaultConfigValues, String viewName) throws IOException {
        this(new ResourceManager(nonDefaultConfigValues), viewName);

    }

    /**
     * default constructor -- NER_CONLL models will be loaded. Lazily initialized by default.
     * 
     * @param viewName name of view this annotator will add.
     * @throws IOException
     */
    public NERAnnotator(String viewName) throws IOException {
        this(new ResourceManager(new Properties()), viewName);
    }

    /**
     * Default behavior is to use lazy initialization; override with ResourceManager entry per the
     * Configurator property {@link AnnotatorConfigurator#IS_LAZILY_INITIALIZED}
     *
     * @param nonDefaultRm specify properties to override defaults, including lazy initialization
     * @param viewName name of the view to add to the TextAnnotation (and for client to request)
     */
    public NERAnnotator(ResourceManager nonDefaultRm, String viewName) {
        super(viewName, new String[]{}, nonDefaultRm.getBoolean(
                AnnotatorConfigurator.IS_LAZILY_INITIALIZED.key, Configurator.TRUE), nonDefaultRm);
    }


    /**
     * Superclass calls this method either on instantiation or at first call to getView(). Logging
     * has been disabled because non-static logger is not initialized at the time this is called if
     * non-lazy initialization is specified.
     * 
     * @param nerRm configuration parameters passed to constructor
     */
    @Override
    public void initialize(ResourceManager nerRm) {
        if (ViewNames.NER_ONTONOTES.equals(getViewName()))
            nerRm = new NerOntonotesConfigurator().getConfig(config);
        else
            nerRm = new NerBaseConfigurator().getConfig(config);

        ParametersForLbjCode.currentParameters.forceNewSentenceOnLineBreaks = false;
        Parameters.readConfigAndLoadExternalData(nerRm);

        // logger.info("Reading model file: {}",
        // ParametersForLbjCode.currentParameters.pathToModelFile + ".level1");
        NETaggerLevel1 tagger1 =
                new NETaggerLevel1(ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1", ParametersForLbjCode.currentParameters.pathToModelFile
                        + ".level1.lex");

        NETaggerLevel2 tagger2 = null;
        if (ParametersForLbjCode.currentParameters.featuresToUse.containsKey("PredictionsLevel1")) {
            // logger.info("Reading model file: {}",
            // ParametersForLbjCode.currentParameters.pathToModelFile + ".level2");
            tagger2 =
                    new NETaggerLevel2(ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level2", ParametersForLbjCode.currentParameters.pathToModelFile
                            + ".level2.lex");
        }
        this.t1 = tagger1;
        this.t2 = tagger2;
    }

    /**
     * Generate the view representing the list of extracted entities and adds it the
     * {@link TextAnnotation}.
     */
    @Override
    public void addView(TextAnnotation ta) {
        // convert this data structure into one the NER package can deal with.
        ArrayList<LinkedVector> sentences = new ArrayList<>();
        String[] tokens = ta.getTokens();
        int[] tokenindices = new int[tokens.length];
        int tokenIndex = 0;
        int neWordIndex = 0;
        for (int i = 0; i < ta.getNumberOfSentences(); i++) {
            Sentence sentence = ta.getSentence(i);
            String[] wtoks = sentence.getTokens();
            LinkedVector words = new LinkedVector();
            for (String w : wtoks) {
                if (w.length() > 0) {
                    NEWord.addTokenToSentence(words, w, "unlabeled");
                    tokenindices[neWordIndex] = tokenIndex;
                    neWordIndex++;
                } else {
                    logger.error("Bad (zero length) token.");
                }
                tokenIndex++;
            }
            if (words.size() > 0)
                sentences.add(words);
        }

        // Do the annotation.
        Data data = new Data(new NERDocument(sentences, "input"));
        try {
            ExpressiveFeaturesAnnotator.annotate(data);
            Decoder.annotateDataBIO(data, t1, t2);
        } catch (Exception e) {
            logger.error("Cannot annotate the text, the exception was: ", e);
            return;
        }

        // now we have the parsed entities, construct the view object.
        ArrayList<LinkedVector> nerSentences = data.documents.get(0).sentences;
        SpanLabelView nerView = new SpanLabelView(getViewName(), ta);

        // the data always has a single document
        // each LinkedVector in data corresponds to a sentence.
        int tokenoffset = 0;
        for (LinkedVector vector : nerSentences) {
            boolean open = false;

            // there should be a 1:1 mapping btw sentence tokens in record and words/predictions
            // from NER.
            int startIndex = -1;
            String label = null;
            for (int j = 0; j < vector.size(); j++, tokenoffset++) {
                NEWord neWord = (NEWord) (vector.get(j));
                String prediction = neWord.neTypeLevel2;

                // LAM-tlr this is not a great way to ascertain the entity type, it's a bit
                // convoluted, and very
                // inefficient, use enums, or nominalized indexes for this sort of thing.
                if (prediction.startsWith("B-")) {
                    startIndex = tokenoffset;
                    label = prediction.substring(2);
                    open = true;
                } else if (j > 0) {
                    String previous_prediction = ((NEWord) vector.get(j - 1)).neTypeLevel2;
                    if (prediction.startsWith("I-")
                            && (!previous_prediction.endsWith(prediction.substring(2)))) {
                        startIndex = tokenoffset;
                        label = prediction.substring(2);
                        open = true;
                    }
                }

                if (open) {
                    boolean close = false;
                    if (j == vector.size() - 1) {
                        close = true;
                    } else {
                        String next_prediction = ((NEWord) vector.get(j + 1)).neTypeLevel2;
                        if (next_prediction.startsWith("B-"))
                            close = true;
                        if (next_prediction.equals("O"))
                            close = true;
                        if (next_prediction.indexOf('-') > -1
                                && (!prediction.endsWith(next_prediction.substring(2))))
                            close = true;
                    }
                    if (close) {
                        int s = tokenindices[startIndex];

                        /**
                         * MS: fixed bug. Originally, e was set using tokenindices[tokenoffset], but
                         * tokenoffset can reach tokens.length) and this exceeds array length.
                         * Constituent constructor requires one-past-the-end token indexing,
                         * requiring e > s. Hence the complicated setting of endIndex/e below.
                         */

                        int endIndex = Math.min(tokenoffset + 1, tokens.length - 1);
                        int e = tokenindices[endIndex];
                        if (e <= s)
                            e = s + 1;

                        nerView.addSpanLabel(s, e, label, 1d);
                        open = false;
                    }
                }
            }
        }
        ta.addView(viewName, nerView);
    }

    /**
     * Return possible tag values that the NERAnnotator can produce.
     *
     * @return the set of string representing the tag values
     */
    @Override
    public Set<String> getTagValues() {
        if (!isInitialized()) {
            doInitialize();
        }
        Lexicon labelLexicon = t1.getLabelLexicon();
        Set<String> tagSet = new HashSet();
        for (int i =0; i < labelLexicon.size(); ++i) {
            tagSet.add(labelLexicon.lookupKey(i).getStringValue());
        }
        return tagSet;
    }
}
