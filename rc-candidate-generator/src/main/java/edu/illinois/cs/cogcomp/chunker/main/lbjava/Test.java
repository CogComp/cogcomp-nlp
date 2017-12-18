package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerTrain;
import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Created by daniel on 12/17/17.
 */
public class Test {

    public static void trainWith(String filename) {
        System.out.println("Starting to read the data . . . " + filename);
        Parser parser = new CoNLL2000Parser("files/" + filename  + ".txt");
        System.out.println("Done reading the data . . . ");
        ChunkerTrain ct = new ChunkerTrain(30);
        //ct.trainModelsWithParser(parser);
        //ct.trainModelsWithParser(parser, "model", filename, 0.2);
        ct.trainModelsWithParser(parser);
        System.out.println("Done training . . . ");
        ct.writeModelsToDisk("model", filename);
        System.out.println("Writing on disk . . .");
    }

    public static void main(String[] args) {
        //trainWith("paragraphs-conllformat-tiny");
        trainWith("paragraphs-conllformat-train-window-1");
        trainWith("paragraphs-conllformat-train-full");
    }
}
