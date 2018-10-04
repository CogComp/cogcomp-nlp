/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.main;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.comma.CommaLabeler;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.datalessclassification.config.ESADatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.config.W2VDatalessConfigurator;
import edu.illinois.cs.cogcomp.datalessclassification.ta.ESADatalessAnnotator;
import edu.illinois.cs.cogcomp.datalessclassification.ta.W2VDatalessAnnotator;
import edu.illinois.cs.cogcomp.depparse.DepAnnotator;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.ner.NerAnnotatorManager;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordParseHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.prepsrl.PrepSRLAnnotator;
import edu.illinois.cs.cogcomp.quant.driver.Quantifier;
import edu.illinois.cs.cogcomp.question_typer.QuestionTypeAnnotator;
import edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import edu.illinois.cs.cogcomp.srl.core.SRLType;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalChunkerAnnotator;
import edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalChunkerConfigurator;
import edu.illinois.cs.cogcomp.verbsense.VerbSenseAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.md.MentionAnnotator;
import org.cogcomp.re.RelationAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * builds an AnnotatorService with a set of NLP components.
 *
 * @author mssammon
 */
public class PipelineFactory {
    private static Logger logger = LoggerFactory.getLogger(PipelineFactory.class);

    /**
     * create an AnnotatorService with the given view names in the argument. The names are supposed
     * be strings, separated by space.
     *
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline(Boolean disableCache, String... views) throws IOException,
            AnnotatorException {
        List<String> allViewNames = ViewNames.getAllViewNames();
        Map<String, String> nonDefaultValues = new HashMap<>();
        for (String vu : views) {
            if (allViewNames.contains(vu)) {
                switch (vu) {
                    case ViewNames.POS:
                        nonDefaultValues.put(PipelineConfigurator.USE_POS.key, Configurator.TRUE);
                        break;
                    case ViewNames.LEMMA:
                        nonDefaultValues.put(PipelineConfigurator.USE_LEMMA.key, Configurator.TRUE);
                        break;
                    case ViewNames.NER_CONLL:
                        nonDefaultValues.put(PipelineConfigurator.USE_NER_CONLL.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.NER_ONTONOTES:
                        nonDefaultValues.put(PipelineConfigurator.USE_NER_ONTONOTES.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.QUANTITIES:
                        nonDefaultValues.put(PipelineConfigurator.USE_QUANTIFIER.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.SHALLOW_PARSE:
                        nonDefaultValues.put(PipelineConfigurator.USE_SHALLOW_PARSE.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.SRL_VERB:
                        nonDefaultValues.put(PipelineConfigurator.USE_SRL_VERB.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.SRL_NOM:
                        nonDefaultValues.put(PipelineConfigurator.USE_SRL_NOM.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.DEPENDENCY_STANFORD:
                        nonDefaultValues.put(PipelineConfigurator.USE_STANFORD_DEP.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.DEPENDENCY:
                        nonDefaultValues.put(PipelineConfigurator.USE_DEP.key, Configurator.TRUE);
                        break;
                    case ViewNames.PARSE_STANFORD:
                        nonDefaultValues.put(PipelineConfigurator.USE_STANFORD_PARSE.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.SRL_PREP:
                        nonDefaultValues.put(PipelineConfigurator.USE_SRL_PREP.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.SRL_COMMA:
                        nonDefaultValues.put(PipelineConfigurator.USE_SRL_COMMA.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.VERB_SENSE:
                        nonDefaultValues.put(PipelineConfigurator.USE_VERB_SENSE.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.TRANSLITERATION:
                        nonDefaultValues.put(PipelineConfigurator.USE_TRANSLITERATION.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.TIMEX3:
                        nonDefaultValues.put(PipelineConfigurator.USE_TIMEX3.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.MENTION:
                        nonDefaultValues.put(PipelineConfigurator.USE_MENTION.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.RELATION:
                        nonDefaultValues.put(PipelineConfigurator.USE_RELATION.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.DATALESS_ESA:
                        nonDefaultValues.put(PipelineConfigurator.USE_DATALESS_ESA.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.DATALESS_W2V:
                        nonDefaultValues.put(PipelineConfigurator.USE_DATALESS_W2V.key,
                                Configurator.TRUE);
                        break;
                    case ViewNames.QUESTION_TYPE:
                        nonDefaultValues.put(PipelineConfigurator.USE_QUESTION_TYPER.key,
                                Configurator.TRUE);
                        break;
                    default:
                        logger.warn("View name "
                                + vu
                                + " is not supported yet. Look into the readme of the pipeline to see the list of valid annotators. ");
                }
            } else {
                throw new IllegalArgumentException("The view name " + vu
                        + " is not a valid view name. "
                        + "The possible view names are static members of the class `ViewName`. ");
            }
        }

        if(disableCache) {
            nonDefaultValues.put(AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.TRUE);
        }
        else {
            nonDefaultValues.put(AnnotatorServiceConfigurator.DISABLE_CACHE.key, Configurator.FALSE);
        }

        // using the default settings and changing the views
        ResourceManager fullRm = (new PipelineConfigurator()).getConfig(new Stanford331Configurator().getConfig(nonDefaultValues));
        boolean splitOnHypen = fullRm.getBoolean(PipelineConfigurator.SPLIT_ON_DASH.key);

        TextAnnotationBuilder taBldr =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHypen, false));
        Map<String, Annotator> annotators = buildAnnotators(fullRm);
        return new SentencePipeline(taBldr, annotators, fullRm);
    }

    public static BasicAnnotatorService buildPipeline(String... views) throws IOException,
            AnnotatorException {
        return buildPipeline(false, views);
    }

    /**
     * create an AnnotatorService with default configuration.
     * 
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline() throws IOException, AnnotatorException {
        ResourceManager emptyConfig = new ResourceManager(new Properties());
        return buildPipeline(emptyConfig);
    }

    /**
     * create an AnnotatorService with all the possible views in the pipeline. Be careful if you use
     * this, potentially you will requires lots of memory.
     * 
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipelineWithAllViews() throws IOException,
            AnnotatorException {
        return buildPipelineWithAllViews(false);
    }

    /**
     * create an AnnotatorService with all the possible views in the pipeline. Be careful if you use
     * this, potentially you will requires lots of memory.
     *
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipelineWithAllViews(Boolean disableCache) throws IOException,
            AnnotatorException {
        return buildPipeline(disableCache, ViewNames.getAllViewNames().toArray(
                new String[ViewNames.getAllViewNames().size()]));
    }

    /**
     * create an AnnotatorService with components specified by the ResourceManager (to override
     * defaults in {@link PipelineConfigurator}
     * 
     * @param rm non-default config options
     * @return AnnotatorService with specified NLP components
     * @throws IOException
     * @throws AnnotatorException
     */
    public static BasicAnnotatorService buildPipeline(ResourceManager rm) throws IOException,
            AnnotatorException {
        // Merges default configuration with the user-specified overrides.
        ResourceManager fullRm = (new PipelineConfigurator()).getConfig(new Stanford331Configurator().getConfig(rm));
        Boolean splitOnDash = fullRm.getBoolean(PipelineConfigurator.SPLIT_ON_DASH);
        boolean isSentencePipeline =
                fullRm.getBoolean(PipelineConfigurator.USE_SENTENCE_PIPELINE.key);

        if (isSentencePipeline) { // update cache directory to be distinct from regular pipeline
            String cacheDir = fullRm.getString(AnnotatorServiceConfigurator.CACHE_DIR.key);
            cacheDir += "_sentence";
            Properties props = fullRm.getProperties();
            props.setProperty(AnnotatorServiceConfigurator.CACHE_DIR.key, cacheDir);
            fullRm = new ResourceManager(props);
        }

        TextAnnotationBuilder taBldr =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnDash, false));

        Map<String, Annotator> annotators = buildAnnotators(fullRm);
        return isSentencePipeline ? new SentencePipeline(taBldr, annotators, fullRm) :
                new BasicAnnotatorService(taBldr, annotators, fullRm);
    }

    /**
     * instantiate a set of annotators for use in an AnnotatorService object by default, will use
     * lazy initialization where possible -- change this behavior with the
     * {@link PipelineConfigurator#USE_LAZY_INITIALIZATION} property.
     * 
     * @param nonDefaultRm ResourceManager with all non-default values for Annotators
     * @return a Map from annotator view name to annotator
     */
    private static Map<String, Annotator> buildAnnotators(ResourceManager nonDefaultRm)
            throws IOException {
        ResourceManager rm = new PipelineConfigurator().getConfig(new Stanford331Configurator().getConfig(nonDefaultRm));
        String timePerSentence = rm.getString(Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
        String maxParseSentenceLength =
                rm.getString(Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
        boolean useLazyInitialization =
                rm.getBoolean(PipelineConfigurator.USE_LAZY_INITIALIZATION.key,
                        PipelineConfigurator.TRUE);

        Map<String, Annotator> viewGenerators = new HashMap<>();

        if (rm.getBoolean(PipelineConfigurator.USE_POS)) {
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
            NERAnnotator nerOntonotes =
                    NerAnnotatorManager.buildNerAnnotator(rm, ViewNames.NER_ONTONOTES);
            viewGenerators.put(nerOntonotes.getViewName(), nerOntonotes);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_DEP)) {
            DepAnnotator dep = new DepAnnotator();
            viewGenerators.put(dep.getViewName(), dep);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_STANFORD_DEP)
                || rm.getBoolean(PipelineConfigurator.USE_STANFORD_PARSE)) {
            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", maxParseSentenceLength);
            stanfordProps.put("parse.maxtime", timePerSentence); // per sentence? could be per
                                                                 // document but no idea from
                                                                 // stanford javadoc
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            int maxLength = Integer.parseInt(maxParseSentenceLength);
            boolean throwExceptionOnSentenceLengthCheck =
                    rm.getBoolean(Stanford331Configurator.THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK.key);

            if (rm.getBoolean(PipelineConfigurator.USE_STANFORD_DEP)) {
                StanfordDepHandler depParser =
                        new StanfordDepHandler(posAnnotator, parseAnnotator, maxLength,
                                throwExceptionOnSentenceLengthCheck);
                viewGenerators.put(depParser.getViewName(), depParser);
            }
            if (rm.getBoolean(PipelineConfigurator.USE_STANFORD_PARSE)) {
                StanfordParseHandler parser =
                        new StanfordParseHandler(posAnnotator, parseAnnotator, maxLength,
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
            try {
                SemanticRoleLabeler verbSrl = new SemanticRoleLabeler(rm, useLazyInitialization);
                viewGenerators.put(ViewNames.SRL_VERB, verbSrl);
            } catch (Exception e) {
                throw new IOException("SRL verb cannot init: " + e.getMessage());
            }
        }
        if (rm.getBoolean(PipelineConfigurator.USE_SRL_NOM)) {
            Properties nomProps = new Properties();
            String nomType = SRLType.Nom.name();
            nomProps.setProperty(SrlConfigurator.SRL_TYPE.key, nomType);
            ResourceManager nomRm = new ResourceManager(nomProps);
            rm = Configurator.mergeProperties(rm, nomRm);

            try {
                SemanticRoleLabeler nomSrl = new SemanticRoleLabeler(rm, useLazyInitialization);
                // note that you can't call nomSrl (or verbSrl).getViewName() as it may not be
                // initialized yet
                viewGenerators.put(ViewNames.SRL_NOM, nomSrl);
                // viewGenerators.put(ViewNames.SRL_NOM,new SrlHandler("NomSRL", "5.1.9", nomType,
                // ViewNames.SRL_NOM,
                // useLazyInitialization, rm));
            } catch (Exception e) {
                throw new IOException("SRL nom cannot init .." + e.getMessage());
            }
        }

        if (rm.getBoolean(PipelineConfigurator.USE_QUANTIFIER)) {
            Quantifier quantifierAnnotator = new Quantifier();
            viewGenerators.put(ViewNames.QUANTITIES, quantifierAnnotator);
        }

        if (rm.getBoolean(PipelineConfigurator.USE_TRANSLITERATION)) {
            for(Language lang : TransliterationAnnotator.supportedLanguages) {
                TransliterationAnnotator transliterationAnnotator = new TransliterationAnnotator(true, lang);
                viewGenerators.put(ViewNames.TRANSLITERATION + "_" + lang.getCode(), transliterationAnnotator);
            }
        }

        if (rm.getBoolean(PipelineConfigurator.USE_SRL_PREP)) {
            PrepSRLAnnotator prepSRLAnnotator = new PrepSRLAnnotator();
            viewGenerators.put(ViewNames.SRL_PREP, prepSRLAnnotator);
        }

        if (rm.getBoolean(PipelineConfigurator.USE_SRL_COMMA)) {
            CommaLabeler commaLabeler = new CommaLabeler();
            viewGenerators.put(ViewNames.SRL_COMMA, commaLabeler);
        }

        if(rm.getBoolean(PipelineConfigurator.USE_VERB_SENSE)) {
            VerbSenseAnnotator verbSense = new VerbSenseAnnotator();
            viewGenerators.put(ViewNames.VERB_SENSE, verbSense);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_MENTION)){
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            viewGenerators.put(ViewNames.MENTION, mentionAnnotator);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_RELATION)){
            viewGenerators.put(ViewNames.RELATION, new RelationAnnotator(true));
        }
        if (rm.getBoolean(PipelineConfigurator.USE_TIMEX3)){
            Properties rmProps = new TemporalChunkerConfigurator().getDefaultConfig().getProperties();
            TemporalChunkerAnnotator tca = new TemporalChunkerAnnotator(new ResourceManager(rmProps));
            viewGenerators.put(ViewNames.TIMEX3, tca);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_DATALESS_ESA)){
        	rm = new ESADatalessConfigurator().getConfig(nonDefaultRm);
        	ESADatalessAnnotator esaDataless = new ESADatalessAnnotator(rm);
            viewGenerators.put(ViewNames.DATALESS_ESA, esaDataless);
        }
        if (rm.getBoolean(PipelineConfigurator.USE_DATALESS_W2V)){
        	rm = new W2VDatalessConfigurator().getConfig(nonDefaultRm);
        	W2VDatalessAnnotator w2vDataless = new W2VDatalessAnnotator(rm);
            viewGenerators.put(ViewNames.DATALESS_W2V, w2vDataless);
        }
        if(rm.getBoolean(PipelineConfigurator.USE_QUESTION_TYPER)) {
            QuestionTypeAnnotator questionTyper = new QuestionTypeAnnotator();
            viewGenerators.put(ViewNames.QUESTION_TYPE, questionTyper);
        }

        return viewGenerators;
    }

}
