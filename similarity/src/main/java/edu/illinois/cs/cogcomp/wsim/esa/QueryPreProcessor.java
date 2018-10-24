/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.esa;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.descartes.util.Utilities;

/**
 * 
 * This class is used to preprocess the query for ESA concepts retrieve and
 * comparison
 * 
 * @author shaoshi
 *
 */
public class QueryPreProcessor {

	public static Set<String> stopSet = new HashSet<String>();

	public static String process(String query) {

		if (stopSet.size() == 0) {
			stopSet = Utilities.getStopWords();
		}

		String newQuery = "";

		// "(" ...
		// "*" ...
		// <QUOTED> ...
		// <TERM> ...
		// <PREFIXTERM> ...
		// <WILDTERM> ...
		// "[" ...
		// "{" ...
		// <NUMBER> ...
		// <TERM> ...
		// "*" ...
		query = query.toLowerCase().replaceAll(",", " ").replaceAll(":", " ").replaceAll("\\.", " ");
		query = query.toLowerCase().replaceAll("\\?", " ").replaceAll("\\*", " ");
		query = query.toLowerCase().replaceAll("\\[", " ").replaceAll("\\]", " ");
		query = query.toLowerCase().replaceAll("\\(", " ").replaceAll("\\)", " ");
		query = query.toLowerCase().replaceAll("\\{", " ").replaceAll("\\}", " ");
		query = query.toLowerCase().replaceAll("\\<", " ").replaceAll("\\>", " ");
		query = query.toLowerCase().replaceAll("\"", " ");

		String[] queryArray = query.split("\\s+");

		for (String str : queryArray) {
			if (stopSet.contains(str.trim()) == false) {
				newQuery += str + " ";
			}
		}

		return newQuery;
	}

}
