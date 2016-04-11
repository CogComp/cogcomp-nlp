/**
 * 
 */
package edu.illinois.cs.cogcomp.nlp.tokenizer;

/**
 * These are the types of tokens we deal with.
 * @author redman
 */
public enum TokenType {
	
	/** any kind of punctuation. */
	PUNCTUATION,
	
	/** printable text characters. */
	TEXT,
	
	/** whitespace of any kind. */
	WHITESPACE,
	
    /** unprintable characters. */
    UNPRINTABLE
    
}
