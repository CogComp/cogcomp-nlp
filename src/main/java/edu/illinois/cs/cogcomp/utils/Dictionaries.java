package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.transliteration.Production;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

/**
 * Created by stephen on 10/8/15.
 */
public class Dictionaries {

    public static void IncrementOrSet(HashMap<Production, Double> m, Production p, double incrementValue, double setValue){
        if(m.containsKey(p)){
            m.put(p, m.get(p) + incrementValue);
        }else{
            m.put(p, setValue);
        }
    }

    public static void AddTo(HashMap<Production, Double> vector, HashMap<Production, Double> values, int valuesCoefficient) {
        for(Production k : values.keySet()){
            double v = valuesCoefficient*values.get(k);
            IncrementOrSet(vector, k, v, v);
        }
    }

    /**
     * Given two dictionaries, this adds the first to the second, multiplying each value in the second dictionary by valuesCoefficient.
     * @param vector
     * @param values
     * @param valuesCoefficient
     */
    public static void AddTo(HashMap<Production, Double> vector, HashMap<Production, Double> values, double valuesCoefficient) {
        for(Production k : values.keySet()){
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
    public static HashMap<Production, Double> Multiply(HashMap<Production, Double> vector1, HashMap<Production, Double> vector2) {
        HashMap<Production, Double> ret = new HashMap<>();

        for(Production p : vector1.keySet()){
            if(vector2.containsKey(p)){
                ret.put(p, vector1.get(p) * vector2.get(p));
            }
        }

        return ret;

    }
}
