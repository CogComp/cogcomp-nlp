/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.ner.NerAnnotatorManager;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordParseHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * builds an AnnotatorService with a set of NLP components.
 *
 * @author mssammon
 */
public class PipelineFactory {

    /**
     * create an AnnotatorService with default configuration.
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline() throws IOException, AnnotatorException {
        ResourceManager emptyConfig = new ResourceManager(new Properties());
        return buildPipeline(emptyConfig);
    }

    /**
     * create an AnnotatorService with components specified by the ResourceManager (to override defaults
     *    in {@link PipelineConfigurator}
     * @param rm non-default config options
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline(ResourceManager rm) throws IOException, AnnotatorException {
        // Merges default configuration with the user-specified overrides.
        ResourceManager fullRm = (new PipelineConfigurator()).getConfig(rm);
        Boolean splitOnDash = fullRm.getBoolean(PipelineConfigurator.SPLIT_ON_DASH);
        return new BasicAnnotatorService(new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnDash)),
                buildAnnotators(fullRm), fullRm);
    }

    /**
     * instantiate a set of annotators for use in an AnnotatorService object
     * by default, will use lazy initialization where possible
     * -- change this behavior with the {@link PipelineConfigurator#USE_LAZY_INITIALIZATION} property.
     * @param nonDefaultRm ResourceManager with all non-default values for Annotators
     * @return a Map from annotator view name to annotator
     */
    public static Map<String, Annotator> buildAnnotators(ResourceManager nonDefaultRm) throws IOException {
        ResourceManager rm = new PipelineConfigurator().getConfig(nonDefaultRm);
        String timePerSentence = rm.getString(PipelineConfigurator.STFRD_TIME_PER_SENTENCE);
        String maxParseSentenceLength = rm.getString(PipelineConfigurator.STFRD_MAX_SENTENCE_LENGTH);
        boolean useLazyInitialization =
                rm.getBoolean(PipelineConfigurator.USE_LAZY_INITIALIZATION.key, PipelineConfigurator.TRUE);

        Map<String, Annotator> viewGenerators = new HashMap<>();

        if(rm.getBoolean(PipelineConfigurator.USE_POS)) {
            POSAnnotator pos = new POSAnnotator();
            viewGenerators.put(pos.getViewName(), pos);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_LEMMA)) {
            IllinoisLemmatizer lem = new IllinoisLemmatizer(rm);
            viewGenerators.put(lem.getViewName(), lem);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_SHALLOW_PARSE)) {
            viewGenerators.put(ViewNames.SHALLOW_PARSE, new ChunkerAnnotator());
        }
        if (rm.getBoolean(PipelineConfigurator.USE_NER_CONLL)) {
            NERAnnotator nerConll = NerAnnotatorManager.buildNerAnnotator(rm, ViewNames.NER_CONLL);
            viewGenerators.put(nerConll.getViewName(), nerConll);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_NER_ONTONOTES)) {
            NERAnnotator nerOntonotes = NerAnnotatorManager.buildNerAnnotator(rm, ViewNames.NER_ONTONOTES);
            viewGenerators.put(nerOntonotes.getViewName(), nerOntonotes);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_STANFORD_DEP) ||
                rm.getBoolean(PipelineConfigurator.USE_STANFORD_PARSE)) {
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse") ;
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", maxParseSentenceLength);
            stanfordProps.put("parse.maxtime", timePerSentence); // per sentence? could be per document but no idea from stanford javadoc
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            int maxLength = Integer.parseInt(maxParseSentenceLength);
            boolean throwExceptionOnSentenceLengthCheck =
                    rm.getBoolean(PipelineConfigurator.THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK.key);

            if (rm.getBoolean(PipelineConfigurator.USE_STANFORD_DEP)) {
                StanfordDepHandler depParser = new StanfordDepHandler(posAnnotator, parseAnnotator, maxLength,
                        throwExceptionOnSentenceLengthCheck);
                viewGenerators.put(depParser.getViewName(), depParser);
            }
            if (rm.getBoolean(PipelineConfigurator.USE_STANFORD_PARSE)) {
                StanfordParseHandler parser = new StanfordParseHandler(posAnnotator, parseAnnotator, maxLength,
                        throwExceptionOnSentenceLengthCheck);
                viewGenerators.put(parser.getViewName(), parser);
            }
        }

        if (rm.getBoolean(PipelineConfigurator.USE_SRL_VERB)) {
            Properties verbProps = new Properties();
            String verbType = SRLType.Verb.name();
            verbProps.setProperty(SrlConfigurator.SRL_TYPE.key, verbType);
            ResourceManager verbRm = new ResourceManager(verbProps);
            rm = Configurator.mergeProperties(rm, verbRm);
            try{
                SemanticRoleLabeler verbSrl = new SemanticRoleLabeler(rm, useLazyInitialization);
                viewGenerators.put(ViewNames.SRL_VERB, verbSrl);
            }
            catch (Exception e) {
                throw new IOException("SRL verb cannot init: "+e.getMessage());
            }
        }
        if(rm.getBoolean(PipelineConfigurator.USE_SRL_NOM)) {
            Properties nomProps = new Properties();
            String nomType = SRLType.Nom.name();
            nomProps.setProperty(SrlConfigurator.SRL_TYPE.key, nomType);
            ResourceManager nomRm = new ResourceManager(nomProps);
            rm = Configurator.mergeProperties(rm, nomRm);

            try {
                SemanticRoleLabeler nomSrl = new SemanticRoleLabeler(rm, useLazyInitialization);
                // note that you can't call nomSrl (or verbSrl).getViewName() as it may not be initialized yet
                viewGenerators.put(ViewNames.SRL_NOM, nomSrl);
//                viewGenerators.put(ViewNames.SRL_NOM,new SrlHandler("NomSRL", "5.1.9", nomType, ViewNames.SRL_NOM,
//                        useLazyInitialization, rm));
            } catch (Exception e) {
                throw new IOException("SRL nom cannot init .."+e.getMessage());
            }
        }

        if(rm.getBoolean(PipelineConfigurator.USE_QUANTIFIER)) {
            Quantifier quantifierAnnotator = new Quantifier();
            viewGenerators.put(ViewNames.QUANTITIES, quantifierAnnotator);
        }

        return viewGenerators;
    }

}
