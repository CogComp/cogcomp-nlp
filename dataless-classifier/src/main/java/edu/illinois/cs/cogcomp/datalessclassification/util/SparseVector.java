/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A general-purpose SparseVector implementation.
 *
 * @author yqsong@illinois.edu
 * @author sgupta96
 */

public class SparseVector<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 7206236102813490658L;

    Map<T, Double> keyValueMap;
    double norm;

    public SparseVector() {
        keyValueMap = new HashMap<>();
        norm = 0;
    }

    public SparseVector(List<T> keys, List<Double> scores) {
        setVector(keys, scores);
    }

    public SparseVector(List<T> keys, List<Double> scores, Map<T, Double> weights) {
        setVector(keys, scores, weights);
    }

    public SparseVector(Map<T, Double> map) {
        setVector(map);
    }

    public SparseVector(Map<T, Double> map, Map<T, Double> weights) {
        setVector(map, weights);
    }

    public SparseVector(SparseVector<T> v) {
        keyValueMap = v.keyValueMap;
        norm = v.norm;
    }

    public static <K extends Serializable> SparseVector<K> deepCopy(SparseVector<K> thatVector) {
        Map<K, Double> thisMap = new HashMap<>();
        Map<K, Double> thatMap = thatVector.getKeyValueMap();

        for (K key : thatMap.keySet()) {
            thisMap.put(key, thatMap.get(key));
        }

        SparseVector<K> thisVector = new SparseVector<>(thisMap);
        return thisVector;
    }

    public void setVector(Map<T, Double> map) {
        keyValueMap = map;
        norm = SparseVectorOperations.getNorm(keyValueMap);
    }

    public void setVector(Map<T, Double> map, Map<T, Double> weights) {
        keyValueMap = map;
        norm = SparseVectorOperations.getNorm(keyValueMap, weights);
    }

    public void setVector(List<T> keys, List<Double> scores) {
        keyValueMap = createKeyValueHashMap(keys, scores);
        norm = SparseVectorOperations.getNorm(keyValueMap);
    }

    public void setVector(List<T> keys, List<Double> scores, Map<T, Double> weights) {
        if (weights == null) {
            weights = new HashMap<>();
        }

        keyValueMap = createKeyValueHashMap(keys, scores);
        norm = SparseVectorOperations.getNorm(keyValueMap, weights);
    }

    public void incrementAll(double value) {
        for (T key : keyValueMap.keySet()) {
            keyValueMap.put(key, keyValueMap.get(key) + value);
        }

        updateNorm();
    }

    /**
     * TODO: Decide what to do when the key is not found
     * Currently, it just adds the key to the map if it is not found
     */
    public void increment(T key, double value) {
        if (keyValueMap.containsKey(key))
            keyValueMap.put(key, keyValueMap.get(key) + value);
        else
            keyValueMap.put(key, value);

        updateNorm();
    }

    public void scaleAll(double value) {
        for (T key : keyValueMap.keySet()) {
            keyValueMap.put(key, keyValueMap.get(key) * value);
        }

        updateNorm();
    }

    /**
     * TODO: Decide what to do when the key is not found
     * Currently, it just adds the key to the map if it is not found
     */
    public void scale(T key, double value) {
        if (keyValueMap.containsKey(key))
            keyValueMap.put(key, keyValueMap.get(key) * value);
        else
            keyValueMap.put(key, value);

        updateNorm();
    }

    public Map<T, Double> getKeyValueMap() {
        return this.keyValueMap;
    }

    public Set<T> getKeys() {
        return this.keyValueMap.keySet();
    }

    public double getNorm() {
        return this.norm;
    }

    public int size() {
        return keyValueMap.size();
    }

    public void updateNorm() {
        norm = SparseVectorOperations.getNorm(this.keyValueMap);
    }

    public void updateNorm(Map<T, Double> weights) {
        norm = SparseVectorOperations.getNorm(this.keyValueMap, weights);
    }

    public static <T> Comparator<Map.Entry<T, Double>> decreasingScores() {
        return new Comparator<Map.Entry<T, Double>>() {

            public int compare(Map.Entry<T, Double> o1, Map.Entry<T, Double> o2) {
                return -1 * (o1.getValue().compareTo(o2.getValue()));
            }
        };
    }

    public static <T> Comparator<Map.Entry<T, Double>> increasingScores() {
        return new Comparator<Map.Entry<T, Double>>() {

            public int compare(Map.Entry<T, Double> o1, Map.Entry<T, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        };
    }

    public static <K extends Serializable> SparseVector<K> getOrderedSparseVector(
            SparseVector<K> vector, Comparator<Map.Entry<K, Double>> c, int topK) {

        SparseVector<K> sparseVector = new SparseVector<>();

        List<Map.Entry<K, Double>> entries =
                new LinkedList<>(vector.keyValueMap.entrySet());

        Collections.sort(entries, c);

        Map<K, Double> sortedMap = new LinkedHashMap<>();

        int i = 0;

        for (Map.Entry<K, Double> entry : entries) {
            if (i >= topK)
                break;

            i++;

            sortedMap.put(entry.getKey(), entry.getValue());
        }

        sparseVector.setVector(sortedMap);
        return sparseVector;
    }

    public static <K extends Serializable> SparseVector<K> getOrderedSparseVector(
            SparseVector<K> vector, Comparator<Map.Entry<K, Double>> c) {

        int size = vector.size();
        return getOrderedSparseVector(vector, c, size);
    }

    private static <T> Map<T, Double> createKeyValueHashMap(List<T> keys, List<Double> scores) {
        Map<T, Double> keyValueMap = new HashMap<>();

        for (int i = 0; i < keys.size(); i++) {
            keyValueMap.put(keys.get(i), scores.get(i));
        }

        return keyValueMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        double epsilon = 0.0001;

        if (!(o instanceof SparseVector<?>))
            return false;

        SparseVector<T> that = (SparseVector<T>) o;

        if (this.size() != that.size())
            return false;

        Map<T, Double> thatMap = that.getKeyValueMap();

        for (T key : keyValueMap.keySet()) {
            if (!thatMap.containsKey(key))
                return false;

            Double thisVal = this.keyValueMap.get(key);
            Double thatVal = thatMap.get(key);

            if (Math.abs(thisVal - thatVal) > epsilon)
                return false;
        }

        Double thisNorm = this.getNorm();
        Double thatNorm = that.getNorm();

        if (Math.abs(thisNorm - thatNorm) > epsilon)
            return false;

        return true;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("");

        for (T key : keyValueMap.keySet()) {
            str.append(key.toString());
            str.append(",");
            str.append(keyValueMap.get(key));
            str.append(";");
        }

        return str.toString();
    }

    // TODO: add a suitable hashCode function?
}
