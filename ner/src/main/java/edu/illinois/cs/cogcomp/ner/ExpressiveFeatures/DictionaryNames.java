/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

/**
 * This class contains several permutations of the dictionary name indicating the relative position
 * within a phrase and the dictionary the phrase (or term) matched. These string are compiled and
 * cached so we do less string manipulation.
 * 
 * @author redman
 */
class DictionaryNames {

    /** unknown marker index. */
    static public final int UNIT = 0;

    /** begin marker index. */
    static public final int BEGIN = 1;

    /** inner marker index. */
    static public final int INSIDE = 2;

    /** end marker index. */
    static public final int END = 3;

    /** markers for unknown, begin, inner, and end in that order. */
    String[] markers = new String[4];

    /** construct all the names. */
    DictionaryNames(String name, String suffix) {
        markers[UNIT] = "U-" + name + suffix;
        markers[BEGIN] = "B-" + name + suffix;
        markers[INSIDE] = "I-" + name + suffix;
        markers[END] = "L-" + name + suffix;
    }
}
