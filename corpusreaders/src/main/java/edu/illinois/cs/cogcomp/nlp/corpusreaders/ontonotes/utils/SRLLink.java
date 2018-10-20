/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.ontonotes.utils;

import java.text.ParseException;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

/**
 * This class captures data about an argument to a verb.
 */
class SRLLink {

    /** the argunment. */
    String argument;
    
    /** This is either an argument, or a linkage indicator("*","," or ";") indicating a non contiguous span. */
    String link;

    /** indicates the constituent linked. */
    IntPair where;

    /**
     * Given the attribute, and a string to parse with the location.
     * @param a the argument.
     * @param link the type of linkage for discontiguous ranges (if any, 0 otherwise).
     * @param where the location.
     * @throws ParseException 
     */
    SRLLink(char link, IntPair where) {
        if (link == 0) {
            this.link = new String();
        } else {
            char[] chars = { link };
            this.link = new String(chars);
        }
        this.where = where;
        this.argument = null;
    }

    /**
     * convert to a string.
     */
    public String toString() {
        if (link == null) {
            return where.getFirst() + ":" + where.getSecond() + (argument==null ? "" : "-"+argument+" ");
        } else {
            return link + where.getFirst() + ":" + where.getSecond() + (argument==null ? "" : "-"+argument+" ");
        }
    }
    
    /**
     * @return true if this is a terminal node.
     */
    boolean isTerminal() { 
        return argument != null; 
    }
}
