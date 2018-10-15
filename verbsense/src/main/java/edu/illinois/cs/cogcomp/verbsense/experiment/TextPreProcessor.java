/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.verbsense.experiment;

import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.verbsense.utilities.VerbSenseConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextPreProcessor {
    private final static Logger log = LoggerFactory.getLogger(TextPreProcessor.class);

    private static TextPreProcessor instance;
    private static final boolean forceUpdate = false;
    private final boolean useCurator;
    private final AnnotatorService annotator;
    private final POSAnnotator pos;
    private final ChunkerAnnotator chunker;
    private final NERAnnotator ner;
    private final IllinoisLemmatizer lemma;
    private final TextAnnotationBuilder taBuilder;

    private ResourceManager rm = new VerbSenseConfigurator().getDefaultConfig();

    public TextPreProcessor() throws Exception {

        this.useCurator = Boolean.valueOf(rm.getString(VerbSenseConfigurator.USE_CURATOR));

        if (useCurator) {
            annotator = CuratorFactory.buildCuratorClient();
            taBuilder = null;
            pos = null;
            ner = null;
            lemma = null;
            chunker = null;
        } else {
            annotator = null;
            taBuilder = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(false, false));
            pos = new POSAnnotator();
            ner = new NERAnnotator(ViewNames.NER_CONLL);
            lemma = new IllinoisLemmatizer();
            chunker = new ChunkerAnnotator();
        }
    }

    public static void initialize() {
        try {
            instance = new TextPreProcessor();
        } catch (Exception e) {
            log.error("Unable to initialize the text pre-processor");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static TextPreProcessor getInstance() {
        if (instance == null) {
            // Start a new TextPreProcessor with default values (no Curator, no tokenization)
            try {
                instance = new TextPreProcessor();
            } catch (Exception e) {
                log.error("Unable to initialize the text pre-processor");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return instance;
    }

    public TextAnnotation preProcessText(String text) throws Exception {
        TextAnnotation ta;
        if (useCurator) {
            ta = annotator.createBasicTextAnnotation("", "", text);
        } else {
            ta = taBuilder.createTextAnnotation(text);
        }
        return preProcessText(ta);
    }

    public TextAnnotation preProcessText(TextAnnotation ta) throws Exception {
        if (useCurator) {
            annotator.addView(ta, ViewNames.POS);
            annotator.addView(ta, ViewNames.LEMMA);
            annotator.addView(ta, ViewNames.SHALLOW_PARSE);
            annotator.addView(ta, ViewNames.NER_CONLL);
        } else {
            ta.addView(pos);
            ta.addView(ner);
            ta.addView(lemma);
            ta.addView(chunker);
        }
        return ta;
    }
}
