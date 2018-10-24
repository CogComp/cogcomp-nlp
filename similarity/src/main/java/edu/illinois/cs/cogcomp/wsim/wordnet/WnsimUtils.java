/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.wordnet;

/**
 * A home for general-purpose classes.
 *
 * Created by mssammon on 12/17/14.
 */
public class WnsimUtils {

	/**
	 * explicitly force failure if array of parameters is not suitable for a
	 * given main() method
	 *
	 * @param args
	 *            arguments to be checked
	 * @param numArgs
	 *            how many arguments there should be
	 * @param className
	 *            name of client (for error reporting)
	 * @param argDesc
	 *            description of arguments required
	 * @return
	 */
	public static String[] checkArgsOrDie(String[] args, int numArgs, String className, String argDesc) {
		if (args.length != numArgs) {
			System.err.println("Usage: " + className + " " + argDesc);
			System.exit(-1);
		}
		return args;
	}
}
