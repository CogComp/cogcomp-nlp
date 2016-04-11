package edu.illinois.cs.cogcomp.core.algorithms;

import java.util.List;

/**
 * @author Vivek Srikumar
 */
public abstract class ListMatch<T> {

    protected List<T> pattern;

    public ListMatch(List<T> pattern) {
        if (pattern.size() == 0)
            throw new IllegalArgumentException("Pattern length = 0");

        this.pattern = pattern;
    }

    public abstract List<Integer> matches(List<T> text);

    protected boolean doItemsMatch(T item1, T item2) {
        return item1.equals(item2);
    }

    public static <T> ListMatch<T> getMatcher(List<T> pattern) {
        // return new BoyerMooreHorspoolMatch<T>(pattern);
        return new RegexBasedMatch<>(pattern);
    }

}
