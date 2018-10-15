/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This utility class represents a pair of integers. This is faster than using
 * {@code Pair<Integer, Integer>} because it does not do boxing and unboxing. This class implements
 * {@code equals} and {@code hashcode} and can be used as keys to maps.
 *
 * @author Vivek Srikumar
 */
public class IntPair implements Serializable {

    private static final long serialVersionUID = 7392560065268843663L;

    private int first, second;

    public IntPair(int a, int b) {
        first = a;
        second = b;
    }

    /**
     * @return the first
     */
    public int getFirst() {
        return first;
    }

    /**
     * @param first set the first
     */
    public void setFirst(int first) {
        this.first = first;
    }

    /**
     * @return the second
     */
    public int getSecond() {
        return second;
    }

    /**
     * @param second set the second
     */
    public void setSecond(int second) {
        this.second = second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + first;
        result = prime * result + second;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntPair other = (IntPair) obj;
        return first == other.first && second == other.second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public static Comparator<IntPair> comparatorFirst = new Comparator<IntPair>() {
        public int compare(IntPair arg0, IntPair arg1) {

            if (arg0.getFirst() == arg1.getFirst())
                return 0;
            else if (arg0.getFirst() < arg1.getFirst())
                return -1;
            else
                return 1;

        }
    };

    public static Comparator<IntPair> comparatorSecond = new Comparator<IntPair>() {
        public int compare(IntPair arg0, IntPair arg1) {

            if (arg0.getSecond() == arg1.getSecond())
                return 0;
            else if (arg0.getSecond() < arg1.getSecond())
                return -1;
            else
                return 1;

        }
    };
}
