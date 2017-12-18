package edu.illinois.cs.cogcomp.chunker.main.lbjava;

import edu.illinois.cs.cogcomp.chunker.main.ChunkerTrain;
import edu.illinois.cs.cogcomp.chunker.utils.CoNLL2000Parser;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

/**
 * Created by daniel on 12/17/17.
 */
public class Test {

    public static void main(String[] args) {
        System.out.println("Starting to read the data . . . ");
        Parser parser = new CoNLL2000Parser("files/paragraphs-conllformat-train-full.txt");
        System.out.println("Done reading the data . . . ");
        ChunkerTrain ct = new ChunkerTrain();
        ct.trainModelsWithParser(parser);
        System.out.println("Done training . . . ");
    }
}
