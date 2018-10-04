/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;

import java.util.*;

/**
 * Identify the longest common substrings between two lists.
 * <p>
 * NOTE: This implements a naive dynamic program and can be improved in terms of memory usage by
 * maintaining only the current row of the dynamic programming table and by using a hash table to
 * store only non-zero values of that row.
 *
 * @param <T> It is assumed that this implements <code>equals()</code>.
 * @author Vivek Srikumar
 *         <p>
 *         Jul 7, 2009
 */
public class LongestCommonSubsequence<T extends Comparable<T>> {

    private double[][] getTable(List<T> seq1, List<T> seq2) {

        int m = seq1.size();
        int n = seq2.size();

        double[][] table = new double[m][n];

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                double val = compareTokens(seq1.get(i), seq2.get(j));
                if (val > 0) {
                    table[i][j] = table[i - 1][j - 1] + val;
                } else {
                    table[i][j] = Math.max(table[i - 1][j], table[i][j - 1]);
                }
            }
        }
        return table;
    }

    protected double compareTokens(T tok1, T tok2) {
        if (tok1 == tok2)
            return 1;

        if (tok1.equals(tok2))
            return 1;
        else
            return 0;
    }

    private Map<Integer, Integer> getBackTraceMap(double[][] table, List<T> seq1, List<T> seq2,
            int i, int j) {
        if (i == 0 || j == 0) {
            return new HashMap<>();

        } else if (compareTokens(seq1.get(i), seq2.get(j)) > 0) {
            Map<Integer, Integer> bt = getBackTraceMap(table, seq1, seq2, i - 1, j - 1);
            bt.put(i, j);

            return bt;
        } else {
            if (table[i][j - 1] > table[i - 1][j]) {
                return getBackTraceMap(table, seq1, seq2, i, j - 1);
            } else {
                return getBackTraceMap(table, seq1, seq2, i - 1, j);
            }
        }
    }

    private List<IntPair> getBackTrace(double[][] table, List<T> seq1, List<T> seq2, int i, int j) {
        if (i == 0 || j == 0) {
            return new ArrayList<>();
        } else if (compareTokens(seq1.get(i), seq2.get(j)) > 0) {
            List<IntPair> bt = getBackTrace(table, seq1, seq2, i - 1, j - 1);
            bt.add(new IntPair(i, j));
            return bt;
        } else {
            if (table[i][j - 1] > table[i - 1][j]) {
                return getBackTrace(table, seq1, seq2, i, j - 1);
            } else {
                return getBackTrace(table, seq1, seq2, i - 1, j);
            }
        }
    }

    public List<IntPair> getLCSMatch(List<T> seq1, List<T> seq2) {
        // make a clone of the sequences, with a dummy start token.
        List<T> mySeq1 = addDummyStart(seq1, null);
        List<T> mySeq2 = addDummyStart(seq2, null);

        double[][] table = getTable(mySeq1, mySeq2);

        return getBackTrace(table, mySeq1, mySeq2, mySeq1.size() - 1, mySeq2.size() - 1);
    }

    private List<T> addDummyStart(List<T> seq, T dummy) {
        List<T> mySeq1 = new ArrayList<>();
        mySeq1.add(dummy);
        for (T item : seq)
            mySeq1.add(item);
        return mySeq1;
    }

    public List<IntPair> getLCSMatch(T[] seq1, T[] seq2) {
        return getLCSMatch(Arrays.asList(seq1), Arrays.asList(seq2));
    }

    public Map<Integer, Integer> getLCSMatchMap(List<T> seq1, List<T> seq2) {
        List<T> mySeq1 = addDummyStart(seq1, null);
        List<T> mySeq2 = addDummyStart(seq2, null);

        double[][] table = getTable(mySeq1, mySeq2);

        return getBackTraceMap(table, mySeq1, mySeq2, mySeq1.size() - 1, mySeq2.size() - 1);
    }

    public Map<Integer, Integer> getLCSMatchMap(T[] seq1, T[] seq2) {
        return getLCSMatchMap(Arrays.asList(seq1), Arrays.asList(seq2));
    }

    public static Map<Integer, Integer> getCharacterLCSMap(String s1, String s2) {
        LongestCommonSubsequence<String> lcs = new LongestCommonSubsequence<>();

        List<String> sequence1 = Arrays.asList(s1.split("(?!^)"));
        List<String> sequence2 = Arrays.asList(s2.split("(?!^)"));

        return lcs.getLCSMatchMap(sequence1, sequence2);
    }

    public static List<IntPair> getCharacterLCS(String s1, String s2) {
        LongestCommonSubsequence<String> lcs = new LongestCommonSubsequence<>();

        List<String> sequence1 = Arrays.asList(s1.split("(?!^)"));
        List<String> sequence2 = Arrays.asList(s2.split("(?!^)"));

        return lcs.getLCSMatch(sequence1, sequence2);
    }
}
