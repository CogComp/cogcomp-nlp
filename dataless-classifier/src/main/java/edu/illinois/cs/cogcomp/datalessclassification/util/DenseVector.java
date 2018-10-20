/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.Map;

/**
 * A general purpose DenseVector implementation
 *
 * @author yqsong@illinois.edu
 * @author sgupta96
 */

public class DenseVector {
    private double[] elems;

    public DenseVector() {
        elems = new double[0];
    }

    public DenseVector(int dims) {
        elems = new double[dims];
    }

    public DenseVector(double[] scores) {
        this.elems = scores;
    }

    public void incrementAll(double value) {
        for (int i = 0; i < elems.length; i++) {
            elems[i] += value;
        }
    }

    public void increment(int index, double value) {
        if (index < size())
            elems[index] += value;
    }

    public void scaleAll(double value) {
        for (int i = 0; i < elems.length; i++) {
            elems[i] *= value;
        }
    }

    public void scale(int index, double value) {
        if (index < size())
            elems[index] *= value;
    }

    public double[] getVector() {
        return this.elems;
    }

    public int size() {
        return elems.length;
    }

    public double getElementAt(int index) {
        if (index < size())
            return elems[index];
        else
            throw new ArrayIndexOutOfBoundsException("Desired index exceeds the size of the vector");
    }

    public void setElementAt(int index, double value) {
        if (index < size())
            elems[index] = value;
        else
            throw new ArrayIndexOutOfBoundsException("Desired index exceeds the size of the vector");
    }

    public static DenseVector createDenseVector(SparseVector<Integer> sparseVector) {
        DenseVector denseVector = new DenseVector();

        if (sparseVector == null)
            return denseVector;

        int max = Integer.MIN_VALUE;

        Map<Integer, Double> map = sparseVector.keyValueMap;

        for (Integer key : map.keySet()) {
            if (key > max)
                max = key;
        }

        double[] finalVector = new double[map.size()];

        for (Integer key : map.keySet()) {
            finalVector[key] = map.get(key);
        }

        denseVector = new DenseVector(finalVector);

        return denseVector;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("");

        for (int i = 0; i < elems.length; i++) {
            str.append(i);
            str.append(",");
            str.append(elems[i]);
            str.append(";");
        }

        return str.toString();
    }
}
