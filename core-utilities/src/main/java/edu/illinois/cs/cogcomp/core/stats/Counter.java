/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.stats;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.math.ArgMax;
import edu.illinois.cs.cogcomp.core.math.ArgMin;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.Serializable;
import java.util.*;

/**
 * Create a counter for type T. T must be a type that implements a hash function and equals.
 * <p>
 * A counter keeps track of the count of a family of objects. It also tracks the max, argMax min,
 * argMin.
 * <p>
 *
 * @author Vivek Srikumar
 * @author mssammon
 */
public class Counter<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 4944749365855704760L;

    TObjectDoubleHashMap<T> counts;

    double total = 0;

    transient ArgMax<T, Double> argMax;
    transient ArgMin<T, Double> argMin;

    OneVariableStats stats;

    private final Comparator<T> comparator = new Comparator<T>() {
        public int compare(T arg0, T arg1) {
            double d0 = counts.get(arg0);
            double d1 = counts.get(arg1);

            if (d0 < d1)
                return -1;
            else if (d0 > d1)
                return 1;
            else
                return 0;
        }
    };

    public Counter() {
        reset();
    }

    public void reset() {
        counts = new TObjectDoubleHashMap<>();
        total = 0;
        argMax = new ArgMax<>(null, Double.MIN_VALUE);
        argMin = new ArgMin<>(null, Double.MAX_VALUE);
        stats = new OneVariableStats();
    }

    public void incrementCount(T object, double increment) {
        if (counts.containsKey(object)) {
            counts.put(object, counts.get(object) + increment);
        } else {
            counts.put(object, increment);
        }

        total += increment;

        stats.add(increment);
    }

    public void incrementCount(T object) {
        incrementCount(object, 1);
    }

    public void decrementCount(T object, double decrement) {
        incrementCount(object, -decrement);
    }

    public void decrementCount(T object) {
        decrementCount(object, 1);
    }

    public double getCount(T object) {
        if (this.counts.containsKey(object))
            return this.counts.get(object);
        else
            return 0;
    }

    public double mean() {
        return stats.mean();
    }

    public double std() {
        return stats.std();
    }

    @SuppressWarnings("unchecked")
    public Pair<T, Double> getMax() {
        for (Object k : counts.keys()) {
            argMax.update((T) k, counts.get(k));
        }
        return new Pair<>(this.argMax.getArgmax(), this.argMax.getMaxValue());
    }

    @SuppressWarnings("unchecked")
    public Pair<T, Double> getMin() {
        for (Object k : counts.keys()) {
            argMin.update((T) k, counts.get(k));
        }
        return new Pair<>(this.argMin.getArgmin(), this.argMin.getMinValue());
    }

    public Set<T> items() {
        return keySet();
    }

    public boolean contains(T item) {
        return this.counts.containsKey(item);
    }

    public int size() {
        return this.counts.size();
    }

    public Set<T> keySet() {
        return this.counts.keySet();
    }

    public double getTotal() {
        return total;
    }

    @SuppressWarnings("unchecked")
    public List<T> getSortedItems() {
        List<T> keys = new ArrayList<>();
        for (Object key : this.counts.keys())
            keys.add((T) key);

        Collections.sort(keys, comparator);
        return keys;
    }

    public List<T> getSortedItemsHighestFirst() {
        List<T> inverseSortedItems = getSortedItems();
        // sort the list in reverse order
        Collections.sort(inverseSortedItems, Collections.reverseOrder(comparator));
        return inverseSortedItems;
    }

    /**
     * create a copy of this Counter
     * @return
     */
    public Counter<T> copy() {
        Counter<T> copy = new Counter<T>();
        for (Object key : this.counts.keys())
            copy.incrementCount((T) key, this.counts.get(key));
        return copy;
    }
}
