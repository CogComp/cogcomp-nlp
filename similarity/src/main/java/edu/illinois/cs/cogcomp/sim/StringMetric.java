/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.sim;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.wsim.wordnet.WnsimConstants;

/**
 * Basic functionality for Metric classes that can operate on a pair of String
 * inputs, in addition to whatever non-basic Types the Metric can compare.
 *
 * Created by mssammon on 2/29/16.
 */

public abstract class StringMetric<T> implements Metric<T> {

	/**
	 * returns the name of this metric. Used as the reason by the default
	 * {@link #compareStringValues } method.
	 * 
	 * @return the name of this metric.
	 */
	abstract public String getName();

	/**
	 * alternate representation of the compare() method using only Map and
	 * String values, to support XMLRPC-like interfaces
	 *
	 * @param arguments
	 *            a map of key-value pairs, where each key indicates the
	 *            intended use of the corresponding value. Must allow the
	 *            implementor to map these values into the data structures used
	 *            by {@link Metric <T>.compare}.
	 * @return a map of key-value pairs, where they keys indicate the intended
	 *         use of the entry
	 */
	public Map<String, String> compareStringValues(Map<String, String> arguments) {
		String firstWord = arguments.get(WnsimConstants.FIRST_WORD);
		String secondWord = arguments.get(WnsimConstants.SECOND_WORD);
		String reason = getName();
		double score = -1;
		if (null == firstWord || null == secondWord) {
			reason = "One or both words was NULL. Cannot compare.";
		} else {
			MetricResponse response = compare(wrapStringArgument(firstWord), wrapStringArgument(secondWord));
			reason = response.reason;
			score = response.score;
		}

		Map<String, String> returnVal = new HashMap<String, String>();

		returnVal.put(WnsimConstants.REASON, reason);
		returnVal.put(WnsimConstants.SCORE, Double.toString(score));

		return returnVal;
	}

	/**
	 * construct a T instance from just a String to allow the
	 * {@link #compareStringValues(Map)} to interact with
	 * {@link Metric<T>.compare }
	 * 
	 * @param word
	 *            the word to wrap
	 * @return the corresponding object of type T specified by the implementor
	 */
	protected abstract T wrapStringArgument(String word);

}
