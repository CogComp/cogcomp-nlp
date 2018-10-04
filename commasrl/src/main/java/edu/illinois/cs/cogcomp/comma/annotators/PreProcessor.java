/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.annotators;

import edu.illinois.cs.cogcomp.annotation.*;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.ner.NerAnnotatorManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordParseHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

import java.util.*;

/**
 * A class that contains all the necessary pre-processing for each sentence.
 */
public class PreProcessor {
    private AnnotatorService annotatorService = null;
    Tokenizer tokenizer = new IllinoisTokenizer();
    POSAnnotator pos;
    NERAnnotator nerConll;
    ChunkerAnnotator shallowParser;
    StanfordParseHandler parser;

    public PreProcessor() throws Exception {
        System.out.println("initializing 1 ");
        // Initialise AnnotatorServices with default configurations
        Map<String, String> nonDefaultValues = new HashMap<>();
        if (CommaProperties.getInstance().useCurator()) {
            nonDefaultValues.put(CuratorConfigurator.RESPECT_TOKENIZATION.key, Configurator.TRUE);
            nonDefaultValues.put(CuratorConfigurator.CURATOR_FORCE_UPDATE.key, Configurator.FALSE);
            ResourceManager curatorConfig = (new CuratorConfigurator()).getConfig(nonDefaultValues);
            annotatorService = CuratorFactory.buildCuratorClient(curatorConfig);
        } else {
            ResourceManager rm = new Stanford331Configurator().getDefaultConfig();
            String timePerSentence = Stanford331Configurator.STFRD_TIME_PER_SENTENCE.value;
            String maxParseSentenceLength = Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH.value;
            boolean throwExceptionOnSentenceLengthCheck =
                    rm.getBoolean(Stanford331Configurator.THROW_EXCEPTION_ON_FAILED_LENGTH_CHECK.key);

            System.out.println("initializing 2 ");

            this.pos = new POSAnnotator();
            this.nerConll = NerAnnotatorManager.buildNerAnnotator(rm, ViewNames.NER_CONLL);
            this.shallowParser = new ChunkerAnnotator();

            Properties stanfordProps = new Properties();
            stanfordProps.put("annotators", "pos, parse");
            stanfordProps.put("parse.originalDependencies", true);
            stanfordProps.put("parse.maxlen", maxParseSentenceLength);
            stanfordProps.put("parse.maxtime", timePerSentence);
            // per sentence? could be per
            // document but no idea from
            // stanford javadoc
            POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
            ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
            int maxLength = Integer.parseInt(maxParseSentenceLength);
            this.parser = new StanfordParseHandler(posAnnotator, parseAnnotator, maxLength, throwExceptionOnSentenceLengthCheck);
        }
    }

    public TextAnnotation preProcess(List<String[]> text) throws AnnotatorException {
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(text);
        addViewsFromAnnotatorService(ta);
        return ta;
    }

    public TextAnnotation preProcess(String text) throws AnnotatorException {
        String[] tokens = tokenizer.tokenizeSentence(text).getFirst();
        return preProcess(Collections.singletonList(tokens));
    }

    private void addViewsFromAnnotatorService(TextAnnotation ta) throws AnnotatorException {
        if (CommaProperties.getInstance().useCurator()) {
            annotatorService.addView(ta, ViewNames.POS);
            annotatorService.addView(ta, ViewNames.NER_CONLL);
            annotatorService.addView(ta, ViewNames.SHALLOW_PARSE);
            annotatorService.addView(ta, ViewNames.PARSE_STANFORD);
            annotatorService.addView(ta, ViewNames.SRL_VERB);
            annotatorService.addView(ta, ViewNames.SRL_NOM);
            annotatorService.addView(ta, ViewNames.SRL_PREP);
        }
        else {
            ta.addView(pos);
            ta.addView(nerConll);
            ta.addView(shallowParser);
            ta.addView(parser);
        }
    }
}
