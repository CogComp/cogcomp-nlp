/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.math.MathUtilities;

import java.util.Arrays;
import java.util.List;

/**
 * This class provides implementations of Levenstein distance.
 *
 * @author Vivek Srikumar
 */
public abstract class LevensteinDistance {

    /**
     * Get the Levenstein distance between the two lists s1 and s2.
     */
    public static <T> int getLevensteinDistance(List<T> s1, List<T> s2) {
        final int m = s1.size() + 1;
        final int n = s2.size() + 1;

        final int[][] dist = initializeDistance(m, n);

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                int cost = 0;
                if (!s1.get(i - 1).equals(s2.get(j - 1)))
                    cost = 1;

                dist[i][j] =
                        MathUtilities.min(
                                new int[] {dist[i][j - 1] + 1, dist[i - 1][j] + 1,
                                        dist[i - 1][j - 1] + cost}).getSecond();
            }
        }

        return dist[m - 1][n - 1];
    }

    /**
     * Get the Levenstein distance between the two arrays s1 and s2
     */
    public static <T> int getLevensteinDistance(T[] s1, T[] s2) {
        return getLevensteinDistance(Arrays.asList(s1), Arrays.asList(s2));
    }

    /**
     * Get the Levenstein distance between two strings s1 and s2
     */
    public static int getLevensteinDistance(String s1, String s2) {
        final int m = s1.length() + 1;
        final int n = s2.length() + 1;

        final int[][] dist = initializeDistance(m, n);

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                int cost = 0;
                if (s1.charAt(i - 1) != s2.charAt(j - 1))
                    cost = 1;

                dist[i][j] =
                        MathUtilities.min(
                                new int[] {dist[i][j - 1] + 1, dist[i - 1][j] + 1,
                                        dist[i - 1][j - 1] + cost}).getSecond();
            }
        }

        return dist[m - 1][n - 1];
    }

    private static int[][] initializeDistance(int m, int n) {
        final int[][] dist = new int[m][];
        for (int i = 0; i < m; i++) {
            dist[i] = new int[n];
            for (int j = 0; j < n; j++)
                dist[i][j] = 0;
        }

        for (int i = 0; i < m; i++)
            dist[i][0] = i;

        for (int j = 0; j < n; j++)
            dist[0][j] = j;
        return dist;
    }

    /**
     * Given the input string s, get the string in the input array that is closest to s in terms of
     * Levenstein distance.
     */
    public static String getNearestMatch(String s, String[] array) {
        String closest = array[0];
        int distance = getLevensteinDistance(s, closest);
        for (final String neighbor : array) {
            final int d = getLevensteinDistance(s, neighbor);
            if (distance >= d) {
                distance = d;
                closest = neighbor;
            }
        }
        return closest;
    }

}
