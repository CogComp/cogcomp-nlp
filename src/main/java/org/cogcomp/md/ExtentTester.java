package org.cogcomp.md;

import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Created by xuany on 7/23/2017.
 */
public class ExtentTester {
    public static void testSimpleExtent(){
        int labeled = 0;
        int predicted = 0;
        int correct = 0;
        for (int i = 0; i < 5; i++){
            Parser train_parser = new ExtentReader("data/partition_with_dev/train/"  + i);
            extent_classifier classifier = new extent_classifier();
            classifier.setLexiconLocation("tmp/extent_classifier_fold_" + i + ".lex");
            BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
            Lexicon lexicon = trainer.preExtract("tmp/extent_classifier_fold_" + i + ".ex", true);
            classifier.setLexicon(lexicon);
            trainer.train(1);

            extentLabel output = new extentLabel();
            Parser test_parser = new ExtentReader("data/partition_with_dev/eval/" + i);
            TestDiscrete testDiscrete = TestDiscrete.testDiscrete(classifier, output, test_parser);
            testDiscrete.printPerformance(System.out);
            /*
            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                String pTag = classifier.discreteValue(example);
                String gTag = output.discreteValue(example);
                if (pTag.equals("true"))
                if (classifier.discreteValue(example).equals(output.discreteValue(example))){

                }
            }
            */
        }
    }
    public static void main(String[] args){
        testSimpleExtent();
    }
}
