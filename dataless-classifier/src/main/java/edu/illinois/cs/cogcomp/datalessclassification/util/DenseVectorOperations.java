/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.HashMap;

/**
 * A Collection of useful functions for working with {@link DenseVector}
 *
 * @author shashank
 */

public class DenseVectorOperations {

    public static double getNorm(double[] vector) {
        double norm = 0;

        for (double dim : vector) {
            norm += dim * dim;
        }

        norm = Math.sqrt(norm);

        return norm;
    }

    public static double getNorm(DenseVector vector) {
        double norm = getNorm(vector.getVector());
        return norm;
    }

    public static double cosine(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length)
            throw new IllegalArgumentException(
                    "Cosine only allowed for vectors of equal length. Lengths of Vectors --> Vector1 = "
                            + vec1.length + ", Vector2 = " + vec2.length);

        double norm1 = getNorm(vec1);
        double norm2 = getNorm(vec2);

        double dot = 0;

        for (int i = 0; i < vec1.length; i++) {
            dot += vec1[i] * vec2[i];
        }

        return dot / ((norm1 + Double.MIN_NORMAL) * (norm2 + Double.MIN_NORMAL));
    }

    public static double cosine(DenseVector v1, DenseVector v2) {
        double[] vec1 = v1.getVector();
        double[] vec2 = v2.getVector();

        return cosine(vec1, vec2);
    }

    public static double[] add(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length)
            throw new IllegalArgumentException(
                    "Addition only allowed for vectors of equal length. Lengths of Vectors --> Vector1 = "
                            + vec1.length + ", Vector2 = " + vec2.length);

        int size = vec1.length;
        double[] sum = new double[size];

        for (int i = 0; i < size; i++) {
            sum[i] = vec1[i] + vec2[i];
        }

        return sum;
    }

    public static DenseVector add(DenseVector vec1, DenseVector vec2) {
        if (vec1.size() != vec2.size())
            throw new IllegalArgumentException(
                    "Addition only allowed for vectors of equal length. Lengths of Vectors --> Vector1 = "
                            + vec1.size() + ", Vector2 = " + vec2.size());

        double[] scores = add(vec1.getVector(), vec2.getVector());

        DenseVector sum = new DenseVector(scores);
        return sum;
    }

    public static SparseVector<Integer> getSparseVector(DenseVector denseVector) {
        SparseVector<Integer> sparseVector = new SparseVector<>();

        if (denseVector == null)
            return sparseVector;

        HashMap<Integer, Double> finalMap = new HashMap<>();

        for (int dim = 0; dim < denseVector.size(); dim++) {
            finalMap.put(dim, denseVector.getElementAt(dim));
        }

        sparseVector.setVector(finalMap);

        return sparseVector;
    }
}
