/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.vectors;


/**
 * This class provides methods for sorting primitive arrays with user specified comparators.
 *
 * @author Nick Rizzolo
 **/
public class Sort {
    /**
     * Sorts the given array of <code>int</code>s according to the given comparator.
     *
     * @param x An array of values.
     * @param c The comparator used to compare the values.
     **/
    public static void sort(int[] x, IntComparator c) {
        quickSort(x, 0, x.length, c);
    }


    /**
     * Sorts the specified portion (including <code>fromIndex</code> and excluding
     * <code>toIndex</code>) of the given array of <code>int</code>s according to the given
     * comparator.
     *
     * @param x An array of values.
     * @param fromIndex The index of the first element to be sorted.
     * @param toIndex One past the index of the last element to be sorted.
     * @param c The comparator used to compare the values.
     **/
    public static void sort(int[] x, int fromIndex, int toIndex, IntComparator c) {
        quickSort(x, fromIndex, toIndex, c);
    }


    /**
     * Sorts the specified portion of the given array of <code>int</code>s according to the given
     * comparator.
     *
     * @param x An array of values.
     * @param fromIndex The index of the first element to sort.
     * @param toIndex One past the index of the last element to sort.
     * @param cmp The comparator used to compare the values.
     **/
    private static void quickSort(int[] x, int fromIndex, int toIndex, IntComparator cmp) {
        if (toIndex - fromIndex < 7) {
            for (int i = fromIndex + 1; i < toIndex; ++i)
                for (int j = i; j > fromIndex && cmp.compare(x[j - 1], x[j]) > 0; --j)
                    swap(x, j, j - 1);
            return;
        }

        swap(x, fromIndex, getMedianIndex(x, fromIndex, toIndex, cmp));
        int median = x[fromIndex];

        int i = fromIndex + 1, j = toIndex - 1;
        while (j >= i) {
            while (i <= j && cmp.compare(x[i], median) <= 0)
                ++i;
            while (j >= i && cmp.compare(x[j], median) >= 0)
                --j;
            if (j > i)
                swap(x, i++, j--);
        }

        swap(x, fromIndex, i - 1);

        if (i - 2 > fromIndex)
            quickSort(x, fromIndex, i - 1, cmp);
        if (toIndex - i > 1)
            quickSort(x, i, toIndex, cmp);
    }


    /**
     * Swaps the element at <code>x[i1]</code> with the element at <code>x[i2]</code>.
     *
     * @param i1 One index involved in the swap.
     * @param i2 The other index involved in the swap.
     **/
    private static void swap(int x[], int i1, int i2) {
        int t = x[i1];
        x[i1] = x[i2];
        x[i2] = t;
    }


    /**
     * Picks three elements from the array and finds the median value according to the given
     * comparator.
     *
     * @param x The array.
     * @param fromIndex The index of the first element to sort.
     * @param toIndex One past the index of the last element to sort.
     * @param cmp The comparator.
     * @return The median of the three selected values.
     **/
    private static int getMedianIndex(int[] x, int fromIndex, int toIndex, IntComparator cmp) {
        int last = toIndex - 1;
        int m = (fromIndex + toIndex) / 2;
        return cmp.compare(x[fromIndex], x[last]) < 0 ? (cmp.compare(x[last], x[m]) < 0 ? last
                : cmp.compare(x[fromIndex], x[m]) > 0 ? fromIndex : m) : (cmp.compare(x[fromIndex],
                x[m]) < 0 ? fromIndex : cmp.compare(x[last], x[m]) > 0 ? last : m);
    }


    /**
     * Allows a user to implement their own comparison function for integers.
     *
     * @author Nick Rizzolo
     **/
    public static interface IntComparator {
        /** The comparison function. */
        public int compare(int i1, int i2);
    }
}
