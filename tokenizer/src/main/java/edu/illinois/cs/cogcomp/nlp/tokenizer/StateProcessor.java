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
     * @param token the character to process.
	 */
	public void process(char token);
}
