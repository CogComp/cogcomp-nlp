package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

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

    public static void main(String[] args){
        AnnotatorExample();
    }
}
