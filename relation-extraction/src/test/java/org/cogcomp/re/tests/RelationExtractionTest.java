/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re.tests;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReaderWithTrueCaseFixer;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.MentionAnnotator;
import org.cogcomp.re.ACEMentionReader;
import org.cogcomp.re.LbjGen.relation_classifier;
import org.cogcomp.re.RelationAnnotator;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Testing class for Mention Detection
 */
public class RelationExtractionTest {
    @Test
    public void testSimpleTraining(){
        File modelDir = null;
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            modelDir = ds.getDirectory("org.cogcomp.re", "ACE_TEST_DOCS", 1.1, false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        ACEMentionReader train_parser = new ACEMentionReader(modelDir.getPath() + File.separator + "ACE_TEST_DOCS", "relations_bi");
        relation_classifier classifier = new relation_classifier();
        classifier.setLexiconLocation("src/test/tmp.lex");
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        Learner preExtractLearner = trainer.preExtract("src/test/tmp.ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = train_parser.relations_bi.size();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Relation r : train_parser.relations_bi){
            classifier.learn(r);
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        train_parser.reset();
        int correct = 0;
        for (Relation r : train_parser.relations_bi){
            String tag = classifier.discreteValue(r);
            if (tag.equals(r.getAttribute("RelationSubtype"))){
                correct ++;
            }
        }
        assertTrue(correct > 0);
    }

    @Test
    public void testAnnotator(){
        File modelDir = null;
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            modelDir = ds.getDirectory("org.cogcomp.re", "ACE_TEST_DOCS", 1.1, false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            ACEReaderWithTrueCaseFixer aceReader = new ACEReaderWithTrueCaseFixer(modelDir.getAbsolutePath() + File.separator + "ACE_TEST_DOCS", false);
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
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_TYPE");
            RelationAnnotator relationAnnotator = new RelationAnnotator();
            for (TextAnnotation ta : aceReader){
                ta.addView(pos_annotator);
                chunker.addView(ta);
                stanfordDepHandler.addView(ta);
                mentionAnnotator.addView(ta);
                relationAnnotator.addView(ta);
                View mentionView = ta.getView(ViewNames.MENTION);
                assertTrue(mentionView.getConstituents().size() > 0);
                View relationView = ta.getView(ViewNames.RELATION);
                assertTrue(relationView.getRelations().size() > 0);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
