/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import java.util.*;

/**
 * This is an implementation of Borda Count algorithm for aggregating multiple rankings into a
 * single ranking.
 *
 * @author Narender Gupta
 */
public class BordaCount {

    /**
     *
     * @param rankings A list of rankings of object T. It is assumed that the type <code>T</code>
     *        implements the <code>hashCode()</code> and <code>equals()</code> functions.
     * @return A ranking of objects T, aggregated by simple Borda count algorithm.
     */
    public static <T> List<T> getAggregatedRanking(List<List<T>> rankings) {
        List<T> result = new ArrayList<>();
        if (rankings != null && rankings.size() > 0) {
            int size = rankings.get(0).size();
            for (List<T> ranking : rankings) {
                if (ranking.size() != size)
                    throw new IllegalArgumentException(
                            "Number of element in each ranking should be same.");
            }
            Map<T, Integer> scores = new HashMap<>();
            for (List<T> ranking : rankings) {
                for (int i = 0; i < size; i++) {
                    int index = size - 1 - i;
                    T item = ranking.get(index);
                    if (!scores.containsKey(item))
                        scores.put(item, 0);
                    scores.put(item, scores.get(item) + i);
                }
            }
            result = Sorters.sortMapByValue(scores);
            Collections.reverse(result);
        }
        return result;
    }
}
