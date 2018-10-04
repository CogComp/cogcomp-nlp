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
 * @author Vivek Srikumar
 */
public class Sorters {
    public static <T, S extends Comparable<S>> List<T> sortMapByValue(final Map<T, S> input) {
        List<T> keys = new ArrayList<>();
        for (T val : input.keySet()) {
            keys.add(val);
        }

        Collections.sort(keys, new Comparator<T>() {

            public int compare(T arg0, T arg1) {
                return input.get(arg0).compareTo(input.get(arg1));
            }
        });

        return keys;
    }

    public static <T, S extends Comparable<S>> List<T> sortMapByValue(final Map<T, S> input,
            final Comparator<S> valueComparator) {
        List<T> keys = new ArrayList<>();
        for (T val : input.keySet()) {
            keys.add(val);
        }

        Collections.sort(keys, new Comparator<T>() {

            public int compare(T arg0, T arg1) {
                return valueComparator.compare(input.get(arg0), input.get(arg1));
            }
        });

        return keys;
    }

    public static <T extends Comparable<T>> List<T> sortSet(final Collection<T> set) {
        List<T> values = new ArrayList<>(set);
        Collections.sort(values);
        return values;
    }

    public static <T> List<T> sortSet(final Collection<T> set, Comparator<T> comparator) {
        List<T> values = new ArrayList<>(set);
        Collections.sort(values, comparator);
        return values;
    }
}
