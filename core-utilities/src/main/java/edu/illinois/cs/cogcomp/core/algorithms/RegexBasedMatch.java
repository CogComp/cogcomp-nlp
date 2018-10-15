/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vivek Srikumar
 */
public class RegexBasedMatch<E> extends ListMatch<E> {

    // private Pattern regexPattern;

    private String patternString;

    /**
     *
     */
    public RegexBasedMatch(List<E> pattern) {
        super(pattern);

        StringBuilder sb = new StringBuilder();
        for (E e : pattern) {
            String string = "-:" + e.toString() + "._";
            sb.append(string);
        }

        patternString = sb.toString();

    }

    public int size() {
        return pattern.size();
    }

    /**
     * Get a list of positions pointing into the input where the pattern matches.
     */
    public List<Integer> matches(List<E> text) {
        List<Integer> matchPositions = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        // List<Integer> wordPositions = new ArrayList<Integer>();

        // int[] wordPositions = new int[text.size()];
        Map<Integer, Integer> wordPositions = new HashMap<>();

        int prev = 0;

        for (int id = 0; id < text.size(); id++) {
            E e = text.get(id);

            sb.append("-:").append(e.toString()).append("._");
            // for (int i = 0; i < string.length(); i++)
            // wordPositions.add(id);

            wordPositions.put(prev, id);

            prev += ("-:" + e.toString() + "._").length();

        }
        String t = sb.toString();

        int index = t.indexOf(patternString);

        while (index >= 0) {
            matchPositions.add(wordPositions.get(index));

            index = t.indexOf(patternString, index + 1);

        }

        return matchPositions;
    }

}
