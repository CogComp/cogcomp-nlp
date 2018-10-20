/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.math;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Provides an implementation of various permutations related operations.
 *
 * @author Vivek Srikumar
 */
public class Permutations {
    private static Logger logger = LoggerFactory.getLogger(Permutations.class);

    public static List<int[]> getAllBinaryCombinations(int numElements) {
        List<int[]> output = new ArrayList<>();

        output.add(new int[] {0});
        output.add(new int[] {1});

        for (int elementId = 1; elementId < numElements; elementId++) {

            List<int[]> newOutput = new ArrayList<>();

            for (int[] instance : output) {

                // make a new copy of instance
                int[] newInstance0 = new int[instance.length + 1];
                int[] newInstance1 = new int[instance.length + 1];
                for (int i = 0; i < instance.length; i++) {

                    newInstance0[i] = instance[i];
                    newInstance1[i] = instance[i];
                }

                // add 0, add to new output
                newInstance0[elementId] = 0;
                newOutput.add(newInstance0);

                // add 1, add to new output
                newInstance1[elementId] = 1;
                newOutput.add(newInstance1);

            }

            // replace output to new output
            output = newOutput;
        }

        return output;
    }

    public static int[] getRandPermutation(int size) {
        return getRandPermutation(size, new Random());
    }

    public static int[] getRandPermutation(int size, Random rand) {

        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }

        for (int i = size - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);

            int tmp = array[i];
            array[i] = array[index];
            array[index] = tmp;
        }

        return array;
    }

    public static <T> List<List<T>> crossProduct(List<List<T>> input) {
        if (input.size() == 1) {
            List<List<T>> result = new ArrayList<>();
            for (T item : input.get(0)) {
                List<T> itemList = new ArrayList<>();
                itemList.add(item);
                result.add(itemList);
            }

            return result;
        } else {
            List<List<T>> allButLast = crossProduct(input.subList(0, input.size() - 1));

            List<List<T>> result = new ArrayList<>();
            for (List<T> allButLastElem : allButLast) {
                for (T item : input.get(input.size() - 1)) {
                    List<T> newList = new ArrayList<>(allButLastElem);
                    newList.add(item);
                    result.add(newList);
                }
            }
            return result;

        }
    }

    public static void main(String[] args) {
        String[] a = {"A", "B", "C"};
        String[] b = {"1", "2"};
        String[] c = {"p", "q", "r", "s"};

        List<List<String>> items = new ArrayList<>();
        items.add(Arrays.asList(a));
        items.add(Arrays.asList(b));
        items.add(Arrays.asList(c));

        for (List<String> s : crossProduct(items)) {
            logger.info(s.toString());
        }

    }

}
