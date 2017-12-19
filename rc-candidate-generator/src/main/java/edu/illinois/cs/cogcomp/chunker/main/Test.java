package edu.illinois.cs.cogcomp.chunker.main;

import edu.illinois.cs.cogcomp.chunker.main.lbjava.ChunkLabel;
import edu.illinois.cs.cogcomp.chunker.main.lbjava.ReadingComprehensionCandidateGenerator;
import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.ChildrenFromVectors;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.util.Vector;

public class Test {

    static String dataFolder = "files/";
    static String modelFolder = "model";

    public static void trainWith(String filename) {
        System.out.println("Starting to read the data . . . " + filename);
        Parser parser = new CoNLL2000Parser( dataFolder + filename  + ".txt");
        System.out.println("Done reading the data . . . ");
        ChunkerTrain ct = new ChunkerTrain(30);
        //ct.trainModelsWithParser(parser);
        //ct.trainModelsWithParser(parser, , filename, 0.2);
        ct.trainModelsWithParser(parser);
        System.out.println("Done training . . . ");
        ct.writeModelsToDisk(modelFolder, filename);
        System.out.println("Writing on disk . . .");
    }

    /**
     * A BIO classifier that classifies {@link Token}s.
     */
    static Classifier classifier;
    /**
     * A BIO classifier that produces the true labels of the {@link Token}s.
     */
    static Classifier labeler;
    /**
     * A parser that produces {@link Token}s.
     */
    static Parser parser;


    public static void testWith(String filename, String modelName) {
        System.out.println("Starting to read the data . . . " + filename);
        Parser parser1 = new CoNLL2000Parser("files/" + filename  + ".txt");
        System.out.println("Done reading the data . .  . ");
        System.out.println("done loading the classifier . . . ");
        classifier = new ReadingComprehensionCandidateGenerator(modelFolder + "/" + modelName + ".lc",modelFolder + "/" + modelName + ".lex");
        labeler = new ChunkLabel();
        parser = new ChildrenFromVectors(parser1);
        System.out.println("Done testing . . . ");
        test().printPerformance(System.out);
        //double[] result = test().getOverallStats();
        //System.out.println(" F1 score on devset: " + result[2]);
        //System.out.println(Arrays.toString(result));
    }

    public static TestDiscrete test() {
        TestDiscrete results = new TestDiscrete();
        results.addNull("O");

        for (Token t = (Token) parser.next(); t != null;
             t = (Token) parser.next()) {
            Vector<Token> vector = new Vector<>();
            for (; t.next != null; t = (Token) parser.next()) vector.add(t);
            vector.add(t);

            int N = vector.size();
            String[] predictions = new String[N], labels = new String[N];

            for (int i = 0; i < N; ++i) {
                predictions[i] = classifier.discreteValue(vector.get(i));
                labels[i] = labeler.discreteValue(vector.get(i));
            }

            for (int i = 0; i < N; ++i) {
                String p = "O", l = "O";
                int pEnd = -1, lEnd = -1;

                //System.out.println("prediction: " + predictions[i]);
                //System.out.println("labels: " + labels[i]);

                if (predictions[i].contains("-B")
                        || predictions[i].contains("-I")
                        && (i == 0
                        || !predictions[i - 1]
                        .endsWith(predictions[i].substring(2)))) {
                    p = predictions[i].substring(2);
                    pEnd = i;
                    while (pEnd + 1 < N && predictions[pEnd + 1].equals("I-" + p))
                        ++pEnd;
                }

                if (labels[i].contains("-B")
                        || labels[i].contains("-I")
                        && (i == 0 || !labels[i - 1].endsWith(labels[i].substring(2)))) {
                    l = labels[i].substring(2);
                    lEnd = i;
                    while (lEnd + 1 < N && labels[lEnd + 1].equals("I-" + l)) ++lEnd;
                }

                if (!p.equals("O") || !l.equals("O")) {
                    if (pEnd == lEnd) results.reportPrediction(p, l);
                    else {
                        if (!p.equals("O")) results.reportPrediction(p, "O");
                        if (!l.equals("O")) results.reportPrediction("O", l);
                    }
                }
            }
        }

        return results;
    }

    public static void testWithSingleInstance(String filename, String modelName) {
        //System.out.println("Starting to read the data . . . " + filename);
        //Parser parser = new CoNLL2000Parser("files/" + filename  + ".txt");
        //System.out.println("Done reading the data . .  . ");
        ReadingComprehensionCandidateGenerator chunker = new ReadingComprehensionCandidateGenerator(modelFolder + "/" + modelName + ".lc",modelFolder + "/" + modelName + ".lex");
        //chunker.read(modelFolder + "/" + modelName + ".lc",modelFolder + "/" + modelName + ".lex");
        Token t = new Token(new Word("The", "DT"), null, "B-L");
        System.out.println(chunker.discreteValue(t));
    }


    public static void main(String[] args) {
        trainWith("paragraphs-conllformat-tiny");
        trainWith("paragraphs-conllformat-train-window-1");
        trainWith("paragraphs-conllformat-train-full");
        //testWith("paragraphs-conllformat-dev-window-1", "paragraphs-conllformat-train-full");
        //testWith("paragraphs-conllformat-dev-full", "paragraphs-conllformat-train-full");
        //testWithSingleInstance("paragraphs-conllformat-dev-window-1", "paragraphs-conllformat-train-window-1");
    }
}
