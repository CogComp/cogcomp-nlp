/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of the Boyer-Moore-Horspool algorithm for finding the matches of a
 * pattern sequence within another sequence. If we want to find sub string match, we can use the
 * Java regular expression library. This provides a similar functionality (not full regular
 * expressions, only sequence matches) for arbitrary lists.
 * <p>
 * <p>
 * It is assumed that the type <code>E</code> implements the <code>hashCode()</code> and
 * <code>equals()</code> functions.
 *
 * @author Vivek Srikumar
 */
public class BoyerMooreHorspoolMatch<E> extends ListMatch<E> {

    protected TObjectIntHashMap<E> badCharSkip;

    protected int defaultBadCharSkip;

    /**
     *
     */
    public BoyerMooreHorspoolMatch(List<E> pattern) {
        super(pattern);
        preprocess(pattern);
    }

    private void preprocess(List<E> pattern) {
        badCharSkip = new TObjectIntHashMap<>();
        defaultBadCharSkip = pattern.size();

        for (int scan = 0; scan < pattern.size() - 1; scan++) {
            badCharSkip.put(pattern.get(scan), pattern.size() - scan - 1);
        }
    }

    public int size() {
        return pattern.size();
    }

    protected int getBadCharSkip(E item) {
        if (badCharSkip.contains(item))
            return badCharSkip.get(item);
        else
            return defaultBadCharSkip;
    }

    /**
     * Get a list of positions pointing into the input where the pattern matches.
     */
    public List<Integer> matches(List<E> text) {
        List<Integer> matchPositions = new ArrayList<>();
        int j = 0;
        int n = text.size();
        int m = pattern.size();

        while (j <= n - m) {
            E c = text.get(j + m - 1);

            if (doItemsMatch(pattern.get(m - 1), c)) {
                if (doesTextSpanMatch(text, j))
                    matchPositions.add(j);
            }

            j += getBadCharSkip(c);

        }

        return matchPositions;
    }

    protected boolean doesTextSpanMatch(List<E> text, int start) {
        for (int i = 0; i < pattern.size(); i++) {
            if (!doItemsMatch(pattern.get(i), text.get(i + start)))
                return false;
        }
        return true;
    }
}
