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
