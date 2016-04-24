/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

/**
 * @author redman
 *
 */
public enum TokenizerState {
	
	/** state for when we are in a sentence. */
	IN_SENTENCE,
	
	/** state for when we are in a word. */
	IN_WORD,
	    
	/** We are in a string of special characters. */
    IN_SPECIAL
    
}
