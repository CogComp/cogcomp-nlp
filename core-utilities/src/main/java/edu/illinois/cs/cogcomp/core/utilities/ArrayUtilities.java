/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to help with the nuisance of converting primitive types arrays to the
 * corresponding boxed lists and vice versa. These functions should really have been in the
 * framework class library!
 *
 * @author Vivek Srikumar
 */
public class ArrayUtilities {

    public static List<Double> asDoubleList(double[] d) {
        List<Double> out = new ArrayList<>();

        for (double dd : d) {
            out.add(dd);
        }
        return out;
    }

    public static double[] asDoubleArray(List<Double> list) {
        int size = list.size();
        double[] array = new double[size];

        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    public static float[] asFloatArray(List<Float> list) {
        int size = list.size();
        float[] array = new float[size];

        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    public static double sum(double[] array) {
        double sum = 0;
        for (double i : array) {
            sum += i;
        }
        return sum;
    }

    public static List<Integer> asIntList(int[] array) {
        List<Integer> list = new ArrayList<>();

        for (int i : array)
            list.add(i);
        return list;
    }

    public static int[] asIntArray(List<Integer> list) {
        int size = list.size();
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    public static int sum(int[] array) {
        int sum = 0;
        for (int i : array) {
            sum += i;
        }
        return sum;
    }

}
