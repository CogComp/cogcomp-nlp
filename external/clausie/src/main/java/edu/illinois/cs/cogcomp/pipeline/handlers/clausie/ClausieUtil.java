/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pipeline.handlers.clausie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ClausieUtil {

    public static <F> String arrayToString(F[] arr, String separator) {
        StringBuilder s = new StringBuilder();
        if (arr == null || arr.length == 0)
            return s.toString();
        s.append(arr[0].toString());
        for (int i = 1; i < arr.length; i++) {
            s.append(separator).append(arr[i].toString());
        }
        return s.toString();
    }


    public static String readStringFromUser(String msg) throws IOException {
        System.out.print(msg + " ");
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }

    // can't = true, cant = false, raw1 = false, raw_1=true
    public static boolean isNonAlphanumeric(String s) {
        if (s == null || s.length() > 1)
            return false;
        char ch = s.charAt(0);

        if ((ch >= 97 && ch <= 122) || (ch >= 65 && ch <= 90)
                || (ch >= 48 && ch <= 57))
            return false;
        return true;
    }

    /**
     * for (T v: myCollection){} fails with nulls.
     * for (T v: nullableIter(myCollection)){}
     * would not fail with nulls.
     */
    public static <T> Iterable<T> nullableIter(Iterable<T> it) {
        return it != null ? it : Collections.<T>emptySet();
    }

    public static <T> T[] nullableIter(T[] it) {
        return (T[]) (it != null ? it : new ArrayList<T>().toArray());
    }

    public static HashSet<String> STOPWORDS = new HashSet<String>(Arrays.asList(new String[]{
            "a", "able",
            "about", "across", "after", "all", "almost", "also", "always", "am",
            "among", "an", "and", "another", "any", "are", "as", "at", "be",
            "because", "been", "before", "being", "but", "by", "can", "cannot",
            "could", "dear", "did", "do", "does", "either", "else", "ever",
            "every", "few", "for", "from", "get", "got", "had", "has", "have",
            "he", "her", "here", "hers", "him", "his", "how", "however", "i", "if",
            "in", "into", "is", "it", "its", "just", "least", "let", "like",
            "likely", "lrb", "many", "may", "me", "might", "mine", "more", "most",
            "much", "must", "my", "neither", "no", "none", "nor", "not", "nothing",
            "now", "nt", "of", "off", "often", "on", "only", "or", "other", "our",
            "ours", "own", "per", "rather", "rrb", "said", "say", "says", "she",
            "should", "since", "so", "some", "somehow", "still", "such", "than",
            "that", "the", "their", "theirs", "them", "then", "there", "these",
            "they", "this", "those", "though", "tis", "to", "too", "twas", "u",
            "us", "very", "want", "wants", "was", "we", "were", "what", "when",
            "where", "which", "while", "who", "whom", "why", "will", "with",
            "would", "www", "yet", "you", "your", "yours", "yourss", "'m", "'ll",
            "a", "about", "above", "after", "again", "against", "all", "am", "an",
            "and", "any", "are", "as", "at", "be", "because", "been", "before",
            "being", "below", "between", "both", "but", "by", "cannot", "could",
            "did", "do", "does", "doing", "down", "during", "each", "few", "for",
            "from", "further", "had", "has", "have", "having", "he", "her", "here",
            "hers", "herself", "him", "himself", "his", "how", "however", "i",
            "if", "in", "into", "is", "it", "its", "itself", "let", "lrb", "me",
            "more", "most", "must", "my", "myself", "no", "nor", "not", "of",
            "off", "on", "once", "only", "or", "other", "ought", "our", "ours",
            "ourselves", "out", "over", "own", "rrb", "same", "sha", "she",
            "should", "so", "some", "such", "than", "that", "the", "their",
            "theirs", "them", "themselves", "then", "there", "these", "they",
            "this", "those", "through", "to", "too", "under", "until", "up",
            "very", "was", "we", "were", "what", "when", "where", "which", "while",
            "who", "who", "whom", "why", "why", "with", "wo", "would", "would",
            "you", "you", "you", "you", "you", "your", "yours", "yourself",
            "yourselves"}));

    public static HashSet<String> PREPOSITIONS = new HashSet<String>(Arrays.asList(new String[]{

            "in", "on",
            "at", "with", "into", "across", "opposite", "toward", "towards",
            "through", "beyond", "aboard", "amid", "past", "by", "near", "nearby",
            "above", "below", "over", "under", "up", "down", "around", "through",
            "inside", "outside", "outside of", "between", "beside", "besides",
            "beyond", "in front of", "in back of", "behind", "next to",
            "on top of", "within", "beneath", "underneath", "among", "along",
            "against",

            "aboard", "about", "above", "across", "after", "against", "along",
            "amid", "among", "anti", "around", "as", "at", "before", "behind",
            "below", "beneath", "beside", "besides", "between", "beyond", "but",
            "by", "concerning", "considering", "despite", "down", "during",
            "except", "excepting", "excluding", "following", "for", "from", "in",
            "inside", "into", "in front of", "like", "minus", "near", "of", "off",
            "on", "onto", "opposite", "outside", "over", "past", "per", "plus",
            "regarding", "round", "save", "since", "than", "through", "to",
            "toward", "towards", "under", "underneath", "unlike", "until", "up",
            "upon", "versus", "via", "with", "within", "without"}));

    public static HashSet<String> CONJUNCTION = new HashSet<String>(Arrays.asList(new String[]{
            "and", "or",
            "but", "for", "nor", "so", "yet", "after", "although", "as", "because",
            "before", "if", "even", "once", "since", "than", "that", "though",
            "unless", "until", "who", "whoever", "which", "when", "whenever",
            "where", "whereas", "wherever", "whether", "while", "why"}));

    // Words that make no sense as fillerVO in activities e.g. fight but run
    public static HashSet<String> CONJUNCTION_STRICTER = new HashSet<String>(Arrays.asList(new String[]{
            "and",
            "or", "but", "nor", "so", "yet", "although", "because", "if", "even",
            "once", "since", "than", "that", "though", "unless", "until", "who",
            "whoever", "which", "when", "whenever", "where", "whereas", "wherever",
            "whether", "while", "why"}));

    public static HashSet<String> MODAL_VERBS = new HashSet<String>(Arrays
            .asList(new String[]{"can", "could", "may", "might", "will", "would",
                    "must", "shall", "should", "ought to"}));
}
