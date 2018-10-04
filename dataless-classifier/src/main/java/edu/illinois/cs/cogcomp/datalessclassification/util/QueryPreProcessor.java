/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class QueryPreProcessor {

    private static Set<String> stopSet = new HashSet<>();
    private static Set<String> stopWords;

    public static String process(String query) {

        if (stopSet.size() == 0) {
            stopSet = getStopWords();
        }

        StringBuffer newQuery = new StringBuffer("");

        query =
                query.toLowerCase().replaceAll(",", " ").replaceAll(":", " ")
                        .replaceAll("\\.", " ");
        query = query.toLowerCase().replaceAll("\\?", " ").replaceAll("\\*", " ");
        query = query.toLowerCase().replaceAll("\\[", " ").replaceAll("\\]", " ");
        query = query.toLowerCase().replaceAll("\\(", " ").replaceAll("\\)", " ");
        query = query.toLowerCase().replaceAll("\\{", " ").replaceAll("\\}", " ");
        query = query.toLowerCase().replaceAll("\\<", " ").replaceAll("\\>", " ");
        query = query.toLowerCase().replaceAll("\"", " ");

        String[] queryArray = query.split("\\s+");

        for (String str : queryArray) {
            if (!stopSet.contains(str.trim())) {
                newQuery.append(str).append(" ");
            }
        }

        return newQuery.toString();
    }

    private static Set<String> getStopWords() {
        if (stopWords == null) {
            stopWords = new HashSet<>();

            stopWords.addAll(Arrays.asList("I", "a", "about", "an", "are", "as", "at", "be", "by",
                    "com", "de", "en", "for", "from", "how", "in", "is", "it", "la", "of", "on",
                    "or", "that", "the", "this", "to", "was", "what", "when", "where", "who",
                    "will", "with", "und", "the", "www"));
        }

        return stopWords;
    }
}
