/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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
