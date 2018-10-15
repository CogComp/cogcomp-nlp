/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.constants.Language;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
//import edu.stanford.nlp.pipeline.CoreNLPProtos.Language;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.BIOFeatureExtractor;
import org.cogcomp.re.LbjGen.semeval_relation_classifier;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * Note that the functions below will not actually run.
 * They only servers a demo purpose.
 */
public class ExampleUsage {

    public static void AnnotatorExample() {
        String text = "He went to Chicago after his Father moved there.";

        String corpus = "story";
        String textId = "001";

        // Create a TextAnnotation From Text
        TextAnnotationBuilder stab =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation ta = stab.createTextAnnotation(corpus, textId, text);

        POSAnnotator pos_annotator = new POSAnnotator();
        ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
        chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
        Properties stanfordProps = new Properties();
        stanfordProps.put("annotators", "pos, parse");
        stanfordProps.put("parse.originalDependencies", true);
        stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
        stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
        ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
        StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
        RelationAnnotator relationAnnotator = new RelationAnnotator();

        try {
            ta.addView(pos_annotator);
            chunker.addView(ta);
            stanfordDepHandler.addView(ta);
            relationAnnotator.addView(ta);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        View mentionView = ta.getView(ViewNames.MENTION);

        List<Constituent> predictedMentions = mentionView.getConstituents();
        List<Relation> predictedRelations = mentionView.getRelations();

        for (Relation r : predictedRelations){
            IOHelper.printRelation(r);
        }
    }

    public static void SemEvalAnnotate() {
        String text = "People have been moving back into downtown.";
        String corpus = "semeval";
        String textId = "001";

        // Create a TextAnnotation From Text
        TextAnnotationBuilder stab =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation ta = stab.createTextAnnotation(corpus, textId, text);


        POSAnnotator pos_annotator = new POSAnnotator();
        ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
        chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
        Properties stanfordProps = new Properties();
        stanfordProps.put("annotators", "pos, parse");
        stanfordProps.put("parse.originalDependencies", true);
        stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
        stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
        POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
        ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
        StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(posAnnotator, parseAnnotator);
        String modelPath = "";
        FlatGazetteers gazetteers = null;
        try {
            ta.addView(pos_annotator);
            chunker.addView(ta);
            stanfordDepHandler.addView(ta);
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File model = ds.getDirectory("org.cogcomp.re", "SEMEVAL", 1.1, false);
            modelPath = model.getPath();
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            gazetteers = (FlatGazetteers) GazetteersFactory.get(5, gazetteersResource.getPath() + File.separator + "gazetteers", 
                true, Language.English);
            WordNetManager.loadConfigAsClasspathResource(true);
            WordNetManager wordnet = WordNetManager.getInstance();
            View annotatedTokenView = new SpanLabelView("RE_ANNOTATED", ta);
            for (Constituent co : ta.getView(ViewNames.TOKENS).getConstituents()){
                Constituent c = co.cloneForNewView("RE_ANNOTATED");
                for (String s : co.getAttributeKeys()){
                    c.addAttribute(s, co.getAttribute(s));
                }
                c.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordnet, c));
                c.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordnet, c));
                annotatedTokenView.addConstituent(c);
            }
            ta.addView("RE_ANNOTATED", annotatedTokenView);
        }
        catch (Exception e){
            e.printStackTrace();
        }


        Constituent source = new Constituent("first", "Mention", ta, 0, 1);
        Constituent target = new Constituent("second", "Mention", ta, 6, 7);
        source.addAttribute("GAZ", gazetteers.annotatePhrase(source));
        target.addAttribute("GAZ", gazetteers.annotatePhrase(target));
        Relation relation = new Relation("TEST", source, target, 1.0f);

        String prefix = modelPath + File.separator + "SEMEVAL" + File.separator + "SEMEVAL";
        semeval_relation_classifier classifier = new semeval_relation_classifier(prefix + ".lc", prefix + ".lex");
        String tag = classifier.discreteValue(relation);

        System.out.println(tag);
    }

    public static void main(String[] args){
        AnnotatorExample();
        SemEvalAnnotate();
    }
}
