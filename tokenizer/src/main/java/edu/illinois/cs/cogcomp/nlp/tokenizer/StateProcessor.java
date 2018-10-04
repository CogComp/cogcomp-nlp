/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

/**
 * @author redman
 *
 */
public interface StateProcessor {

    /**
     * process the given token, incure any state transitions necessary.
     * 
     * @param token the character to process.
     */
    public void process(char token);
}
