package edu.illinois.cs.cogcomp.nlp.utilities;

/**
 * tries to provide some methods to standardize certain kinds of logging or other human-readable
 * output generation Created by mssammon on 9/19/15.
 */
public class PrintUtils {
    public static String printTimeTakenMs(String msg, long start, long finish) {
        StringBuilder bldr = new StringBuilder();
        bldr.append(msg).append(" Time taken: ").append((finish - start)).append(" milliseconds.");
        return bldr.toString();
    }
}
