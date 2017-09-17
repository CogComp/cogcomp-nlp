/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.HashSet;
import java.util.Set;

public class QueryPreProcessor {
	
	public static Set<String> stopSet = new HashSet<String>();
	
	public static String process (String query) {
		
		if (stopSet.size() == 0) {
			stopSet = DatalessUtilities.getStopWords();
		}
		
		String newQuery  = "";
		
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
