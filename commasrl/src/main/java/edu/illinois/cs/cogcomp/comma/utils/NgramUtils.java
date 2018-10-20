/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.utils;

import java.util.ArrayList;
import java.util.List;

public class NgramUtils {

    /**
     * 
     * @param n size of ngram
     * @param words ordered window of words from which ngrams are to be made
     * @return list of n sized contiguous subsequences of words
     */
    public static List<String> ngrams(int n, List<String> words) {
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i < words.size() - n + 1; i++)
            ngrams.add("N" + n + concat(words.subList(i, i + n)) + "I" + i);
        return ngrams;
    }

    private static String concat(List<String> words) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++)
            sb.append(i > 0 ? "@" : "").append(words.get(i));
        return sb.toString();
    }

    public static void main(String[] args) {
        ArrayList<String> words = new ArrayList<>();
        words.add("This");
        words.add("is");
        words.add("my");
        words.add("car");
        words.add(null);

        words.add(null);
        for (int n = 1; n <= 3; n++) {
            for (String ngram : ngrams(n, words))
                System.out.println(ngram);
            System.out.println();
        }
    }
}
