package edu.illinois.cs.cogcomp.datalessclassification.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * yqsong@illinois.edu
 */

public class HashSort {
	//the map is sorted from the highest value to the lowest.
	public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValues(final Map<K, V> map) {
	    Comparator<K> valueComparator =  new Comparator<K>() {
	        public int compare(K k1, K k2) {
	            int compare = map.get(k2).compareTo(map.get(k1));
	            if (compare == 0) return 1;
	            else return compare;
	        }
	    };
	    TreeMap<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}


}
