/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.transliteration.Production;

import java.util.HashMap;

/**
 * Created by stephen on 10/8/15.
 */
public class Dictionaries {

    public static <K> void IncrementOrSet(HashMap<K, Integer> m, K p, int incrementValue, int setValue){
        if(m.containsKey(p)){
            m.put(p, m.get(p) + incrementValue);
        }else{
            m.put(p, setValue);
        }
    }

    public static <K> void IncrementOrSet(HashMap<K, Double> m, K p, double incrementValue, double setValue){
        if(m.containsKey(p)){
            m.put(p, m.get(p) + incrementValue);
        }else{
            m.put(p, setValue);
        }
    }

    public static <K> void AddTo(HashMap<K, Integer> vector, HashMap<K, Integer> values, int valuesCoefficient) {
        for(K k : values.keySet()){
            int v = valuesCoefficient*values.get(k);
            IncrementOrSet(vector, k, v, v);
        }
    }

    /**
     * Given two dictionaries, this adds the first to the second, multiplying each value in the second dictionary by valuesCoefficient.
     * @param vector
     * @param values
     * @param valuesCoefficient
     */
    public static <K> void AddTo(HashMap<K, Double> vector, HashMap<K, Double> values, double valuesCoefficient) {
        for(K k : values.keySet()){
            double v = valuesCoefficient*values.get(k);
            IncrementOrSet(vector, k, v, v);
        }
    }

    /**
     * Treats the two dictionaries as sparse vectors and multiplies their values on matching keys.  Things without a key are treated as having value 0.
     * @param vector1
     * @param vector2
     * @return
     */
    public static <K> HashMap<K, Integer> MultiplyInt(HashMap<K, Integer> vector1, HashMap<K, Integer> vector2) {
        HashMap<K, Integer> ret = new HashMap<>();

        for(K p : vector1.keySet()){
            if(vector2.containsKey(p)){
                ret.put(p, vector1.get(p) * vector2.get(p));
            }
        }

        return ret;

    }

    /**
     * Treats the two dictionaries as sparse vectors and multiplies their values on matching keys.  Things without a key are treated as having value 0.
     * @param vector1
     * @param vector2
     * @return
     */
    public static <K> HashMap<K, Double> MultiplyDouble(HashMap<K, Double> vector1, HashMap<K, Double> vector2) {
        HashMap<K, Double> ret = new HashMap<>();

        for(K p : vector1.keySet()){
            if(vector2.containsKey(p)){
                ret.put(p, vector1.get(p) * vector2.get(p));
            }
        }

        return ret;

    }
}
