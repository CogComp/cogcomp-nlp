package edu.illinois.cs.cogcomp.utils;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

/**
 * Created by stephen on 10/8/15.
 */
public class Dictionaries {

    public static void IncrementOrSet(HashMap<Pair<String, String>, Double> m, Pair<String, String> p, double incrementValue, double setValue){
        if(m.containsKey(p)){
            m.put(p, m.get(p) + incrementValue);
        }else{
            m.put(p, setValue);
        }
    }

    public static void AddTo(HashMap<Pair<String, String>, Double> vector, HashMap<Pair<String, String>, Double> values, int valuesCoefficient) {
        for(Pair<String, String> k : values.keySet()){
            double v = valuesCoefficient*values.get(k);
            IncrementOrSet(vector, k, v, v);
        }
    }

    public static void AddTo(HashMap<Pair<String, String>, Double> vector, HashMap<Pair<String, String>, Double> values, double valuesCoefficient) {
        for(Pair<String, String> k : values.keySet()){
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
    public static HashMap<Pair<String, String>, Double> Multiply(HashMap<Pair<String, String>, Double> vector1, HashMap<Pair<String, String>, Double> vector2) {
        HashMap<Pair<String, String>, Double> ret = new HashMap<>();

        for(Pair<String, String> p : vector1.keySet()){
            if(vector2.containsKey(p)){
                ret.put(p, vector1.get(p) * vector2.get(p));
            }
        }

        return ret;

    }
}
