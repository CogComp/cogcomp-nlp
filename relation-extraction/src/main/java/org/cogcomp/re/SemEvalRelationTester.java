/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package org.cogcomp.re;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import org.cogcomp.re.LbjGen.semeval_relation_classifier;

import java.io.File;

/**
 * Created by xuany on 9/23/2017.
 */
public class SemEvalRelationTester {
    public static void test_simple(){
        int labeled = 0;
        int predicted = 0;
        int correct = 0;
        File ff = new File("relation-extraction/data/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT");
        if (!ff.exists()){
            System.out.println("File does not exist");
            System.exit(0);
        }
        Parser train_parser = new SemEvalMentionReader("relation-extraction/data/SemEval2010_task8_all_data/SemEval2010_task8_training/TRAIN_FILE.TXT", "TRAIN");
        semeval_relation_classifier classifier = new semeval_relation_classifier();
        classifier.setLexiconLocation("models/SEMEVAL.lex");
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        Learner preExtractLearner = trainer.preExtract("models/SEMEVAL.ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        classifier.setModelLocation("models/SEMEVAL.lc");
        classifier.save();

        Parser test_parser = new SemEvalMentionReader("relation-extraction/data/SemEval2010_task8_all_data/SemEval2010_task8_testing_keys/TEST_FILE_FULL.TXT", "TRAIN");
        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            String gold = ((Relation)example).getRelationName();
            String predict = classifier.discreteValue(example);
            if (!gold.equals("Other")){
                labeled ++;
            }
            if (!predict.equals("Other")){
                predicted ++;
            }
            if (gold.equals(predict) && !gold.equals("Other")){
                correct ++;
            }
        }
        System.out.println("====SemEval Results====");
        System.out.println("Total Labeled Relation: " + labeled);
        System.out.println("Total Predicted Relation: " + predicted);
        System.out.println("Total Correct Relation: " + correct);
        double p = (double)correct / (double)predicted;
        double r = (double)correct / (double)labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p * 100.0);
        System.out.println("Recall: " + r * 100.0);
        System.out.println("F1: " + f * 100.0);
    }
    public static void main(String[] args){
        test_simple();
    }
}