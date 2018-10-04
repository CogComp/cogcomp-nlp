/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection of useful functions for working with the {@link SparseVector}
 *
 * @author shashank
 */

public class SparseVectorOperations {

    public static <T> double getNorm(Map<T, Double> vector) {
        Map<T, Double> weights = new HashMap<>();

        return getNorm(vector, weights);
    }

    public static <T> double getNorm(Map<T, Double> vector, Map<T, Double> weights) {
        if (weights == null) {
            weights = new HashMap<>();
        }

        double norm = 0;

        for (T key : vector.keySet()) {
            double value = vector.get(key);
            double weight = 1;

            if (weights.containsKey(key))
                weight = weights.get(key);

            norm += value * value * weight;
        }

        norm = Math.sqrt(norm);

        return norm;
    }

    public static <T> Map<T, Double> add(Map<T, Double> v1, Map<T, Double> v2) {
        return add(v1, v2, 1, 1);
    }

    public static <T> Map<T, Double> addMultipleMaps(List<Map<T, Double>> vectorList) {
        Map<T, Double> finalVector = new HashMap<>();

        for (Map<T, Double> vectorMap : vectorList) {
            finalVector = add(finalVector, vectorMap, 1, 1);
        }

        return finalVector;
    }

    public static <T> Map<T, Double> add(Map<T, Double> v1, Map<T, Double> v2, double weight1,
            double weight2) {
        Map<T, Double> sum = new HashMap<>(v1.size());

        for (T key : v1.keySet()) {
            if (v2.containsKey(key)) {
                double value1 = v1.get(key) * weight1;
                double value2 = v2.get(key) * weight2;
                sum.put(key, value1 + value2);
            } else {
                double value1 = v1.get(key) * weight1;
                sum.put(key, value1);
            }
        }

        for (T key : v2.keySet()) {
            if (!v1.containsKey(key)) {
                double value2 = v2.get(key) * weight2;
                sum.put(key, value2);
            }
        }

        return sum;
    }

    public static <T> Map<T, Double> addMultipleMaps(List<Map<T, Double>> vectorList,
            List<Double> scoreList) {
        Map<T, Double> finalVector = new HashMap<>();

        for (int i = 0; i < vectorList.size(); i++) {
            finalVector = add(finalVector, vectorList.get(i), 1, scoreList.get(i));
        }

        return finalVector;
    }

    public static <T extends Serializable> SparseVector<T> add(SparseVector<T> v1,
            SparseVector<T> v2) {
        Map<T, Double> sum = add(v1.getKeyValueMap(), v2.getKeyValueMap());
        SparseVector<T> sumVec = new SparseVector<>(sum);

        return sumVec;
    }

    public static <T extends Serializable> SparseVector<T> addMultipleVectors(
            List<SparseVector<T>> vectorList) {
        List<Map<T, Double>> maps = new ArrayList<>(vectorList.size());

        for (SparseVector<T> vector : vectorList) {
            maps.add(vector.getKeyValueMap());
        }

        Map<T, Double> sum = addMultipleMaps(maps);
        SparseVector<T> sumVec = new SparseVector<>(sum);

        return sumVec;
    }

    public static <T extends Serializable> SparseVector<T> averageMultipleVectors(
            List<SparseVector<T>> vectorList) {
        SparseVector<T> sumVec = addMultipleVectors(vectorList);
        sumVec.scaleAll(1.0 / vectorList.size());

        return sumVec;
    }

    public static <T extends Serializable> SparseVector<T> add(SparseVector<T> v1,
            SparseVector<T> v2, double weight1, double weight2) {
        Map<T, Double> sum = add(v1.getKeyValueMap(), v2.getKeyValueMap(), weight1, weight2);
        SparseVector<T> sumVec = new SparseVector<>(sum);

        return sumVec;
    }

    public static <T extends Serializable> SparseVector<T> addMultipleVectors(
            List<SparseVector<T>> vectorList, List<Double> weightList) {
        List<Map<T, Double>> maps = new ArrayList<>(vectorList.size());

        for (SparseVector<T> vector : vectorList) {
            maps.add(vector.getKeyValueMap());
        }

        Map<T, Double> sum = addMultipleMaps(maps, weightList);
        SparseVector<T> sumVec = new SparseVector<>(sum);

        return sumVec;
    }

    public static <T extends Serializable> SparseVector<T> averageMultipleVectors(
            List<SparseVector<T>> vectorList, List<Double> weightList) {
        SparseVector<T> sumVec = addMultipleVectors(vectorList, weightList);
        sumVec.scaleAll(1.0 / vectorList.size());

        return sumVec;
    }

    public static <T extends Serializable> SparseVector<T> add(SparseVector<T> v1,
            SparseVector<T> v2, Map<T, Double> weights) {
        Map<T, Double> sum = add(v1.getKeyValueMap(), v2.getKeyValueMap());
        SparseVector<T> sumVec = new SparseVector<>(sum, weights);

        return sumVec;
    }

    public static <T> double cosine(Map<T, Double> vector1, Map<T, Double> vector2) {
        Map<T, Double> weights = new HashMap<>();

        return cosine(vector1, vector2, weights);
    }

    public static <T> double cosine(Map<T, Double> vector1, Map<T, Double> vector2,
            Map<T, Double> weights) {
        double norm1 = getNorm(vector1, weights);
        double norm2 = getNorm(vector2, weights);

        return cosine(vector1, vector2, norm1, norm2, weights);
    }

    public static <T> double cosine(Map<T, Double> vector1, Map<T, Double> vector2, double norm1,
            double norm2) {
        Map<T, Double> weights = new HashMap<>();

        return cosine(vector1, vector2, norm1, norm2, weights);
    }

    public static <T> double cosine(Map<T, Double> vector1, Map<T, Double> vector2, double norm1,
            double norm2, Map<T, Double> weights) {
        if (weights == null) {
            weights = new HashMap<>();
        }

        double dot = 0;

        if (vector1.size() < vector2.size()) {
            for (T key : vector1.keySet()) {
                if (vector2.containsKey(key)) {
                    double value1 = vector1.get(key);
                    double value2 = vector2.get(key);
                    double weight = 1;

                    if (weights.containsKey(key))
                        weight = weights.get(key);

                    dot += value1 * value2 * weight;
                }
            }
        } else {
            for (T key : vector2.keySet()) {
                if (vector1.containsKey(key)) {
                    double value1 = vector1.get(key);
                    double value2 = vector2.get(key);
                    double weight = 1;

                    if (weights.containsKey(key))
                        weight = weights.get(key);

                    dot += value1 * value2 * weight;
                }
            }
        }

        return dot / (norm1 + Double.MIN_NORMAL) / (norm2 + Double.MIN_NORMAL);
    }

    public static <T extends Serializable> double cosine(SparseVector<T> v1, SparseVector<T> v2) {
        return cosine(v1.getKeyValueMap(), v2.getKeyValueMap());
    }

    public static <T extends Serializable> double cosine(SparseVector<T> vector1,
            SparseVector<T> vector2, double norm1, double norm2) {
        return cosine(vector1.getKeyValueMap(), vector2.getKeyValueMap(), norm1, norm2);
    }

    public static <T extends Serializable> double cosine(SparseVector<T> v1, SparseVector<T> v2,
            Map<T, Double> weights) {
        return cosine(v1.getKeyValueMap(), v2.getKeyValueMap(), weights);
    }

    public static <T extends Serializable> double cosine(SparseVector<T> vector1,
            SparseVector<T> vector2, double norm1, double norm2, Map<T, Double> weights) {
        return cosine(vector1.getKeyValueMap(), vector2.getKeyValueMap(), norm1, norm2, weights);
    }


    static <T extends Serializable> double jaccard(SparseVector<T> v1, SparseVector<T> v2) {
        Map<T, Double> vector1 = v1.getKeyValueMap();
        Map<T, Double> vector2 = v2.getKeyValueMap();
        return jaccard(vector1, vector2);
    }

    // TODO: Check this function
    static <T> double jaccard(Map<T, Double> vector1, Map<T, Double> vector2) {
        Set<T> set1 = new HashSet<>(vector1.keySet());
        Set<T> set2 = vector2.keySet();
        set1.retainAll(set2);

        int overlap = set1.size();

        return ((double) overlap) / (set1.size() + set2.size());
    }

    static <T extends Serializable> double SkewDivergence(SparseVector<T> vector1,
                                                          SparseVector<T> vector2, double gamma) {
        return SkewDivergence(vector1.getKeyValueMap(), vector2.getKeyValueMap(), gamma);
    }

    // TODO: Check this function
    static <T> double SkewDivergence(Map<T, Double> vector1, Map<T, Double> vector2,
            double gamma) {
        double result = 0.0;

        // combine two vectors and get a middle
        Map<T, Double> middleVector = new HashMap<T, Double>();

        double sumV1 = 0;

        for (T key : vector1.keySet()) {
            sumV1 += vector1.get(key);
        }

        for (T key : vector1.keySet()) {
            vector1.put(key, vector1.get(key) / sumV1);
        }

        double sumV2 = 0;

        for (T key : vector2.keySet()) {
            sumV2 += vector2.get(key);
        }

        for (T key : vector2.keySet()) {
            vector2.put(key, vector2.get(key) / sumV2);
        }

        for (T key : vector2.keySet()) {
            double value2 = vector2.get(key);

            if (vector1.containsKey(key)) {
                double value1 = vector1.get(key);
                middleVector.put(key, (gamma * value1 + (1 - gamma) * value2));
            } else {
                middleVector.put(key, (1 - gamma) * value2);
            }
        }

        for (T key : vector1.keySet()) {
            if (!middleVector.containsKey(key)) {
                double value1 = vector1.get(key);
                middleVector.put(key, gamma * value1);
            }
        }

        double kld1 = KLDivergence(vector1, middleVector);
        result = (Double.MIN_VALUE + kld1);

        if (result == 0)
            return 0;
        else
            return 1 / result;
    }

    static <T extends Serializable> double JensenShannon(SparseVector<T> vector1,
                                                         SparseVector<T> vector2) {
        return JensenShannon(vector1.getKeyValueMap(), vector2.getKeyValueMap());
    }

    // TODO: Check this function
    static <T> double JensenShannon(Map<T, Double> vector1, Map<T, Double> vector2) {
        double result = 0.0;

        // combine two vectors and get a middle
        Map<T, Double> middleVector = new HashMap<T, Double>();

        double sumV1 = 0;

        for (T key : vector1.keySet()) {
            sumV1 += vector1.get(key);
        }

        for (T key : vector1.keySet()) {
            vector1.put(key, vector1.get(key) / sumV1);
        }

        double sumV2 = 0;

        for (T key : vector2.keySet()) {
            sumV2 += vector2.get(key);
        }

        for (T key : vector2.keySet()) {
            vector2.put(key, vector2.get(key) / sumV2);
        }

        for (T key : vector2.keySet()) {
            double value2 = vector2.get(key);

            if (vector1.containsKey(key)) {
                double value1 = vector1.get(key);
                middleVector.put(key, (value1 + value2) / 2);
            } else {
                middleVector.put(key, value2 / 2);
            }
        }

        for (T key : vector1.keySet()) {
            if (!middleVector.containsKey(key)) {
                double value1 = vector1.get(key);
                middleVector.put(key, value1 / 2);
            }
        }


        // result = (Double.MIN_VALUE + (KLDivergence(vector1, vector2)) / 2);
        // result = (Double.MIN_VALUE + (KLDivergence(vector2, vector1)) / 2);

        // result = (Double.MIN_VALUE + (KLDivergence(vector1, middleVector)) / 2);
        // result = (Double.MIN_VALUE + (KLDivergence(vector2, middleVector)) / 2);

        double kld1 = KLDivergence(vector1, middleVector);
        double kld2 = KLDivergence(vector2, middleVector);
        result = (Double.MIN_VALUE + (kld1 + kld2) / 2);

        if (result == 0)
            return 0;
        else
            return 1 / result;
    }

    static <T extends Serializable> double KLDivergence(SparseVector<T> vector1,
                                                        SparseVector<T> vector2) {
        return KLDivergence(vector1.getKeyValueMap(), vector2.getKeyValueMap());
    }

    // TODO: Check this function
    static <T> double KLDivergence(Map<T, Double> vector1, Map<T, Double> vector2) {
        double result = 0.0;

        if (vector1.size() == 0 || vector2.size() == 0)
            return 0;

        double tempValue = 0.0; // save p(i)*log(p(i)/q(i))

        for (T key : vector2.keySet()) { // traverse the longer vector
            double value2 = vector2.get(key);

            if (vector1.containsKey(key)) { // find key in another vector
                double value1 = vector1.get(key);
                tempValue = value1 * Math.log(value1 / value2) / Math.log(2);

                // String newKey = key + "";
                // if (pageTitleIDMap.containsKey(key)) {
                // newKey = pageTitleIDMap.get(key);
                // }

                result += tempValue;
            }
        }

        return result;
    }
}
