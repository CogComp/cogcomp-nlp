package edu.illinois.cs.cogcomp.core.stats;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.math.ArgMax;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.Serializable;
import java.util.*;

/**
 * Create a counter for type T. T must be a type that implements a hash function
 * and equals.
 * <p/>
 * A counter keeps track of the count of a family of objects. It also tracks the
 * max, argMax min, argMin.
 * <p/>
 *
 * @author Vivek Srikumar
 */
public class Counter<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 4944749365855704760L;

    TObjectDoubleHashMap<T> counts;

    double total = 0;

    transient ArgMax<T, Double> argMax;
    transient ArgMax<T, Double> argMin;

    OneVariableStats stats;

    public Counter() {
        reset();
    }

    public void reset() {
        counts = new TObjectDoubleHashMap<>();
        total = 0;
        argMax = new ArgMax<>(null, Double.MIN_VALUE);
        argMin = new ArgMax<>(null, Double.MAX_VALUE);
        stats = new OneVariableStats();
    }

    public void incrementCount(T object, double increment) {
        if (counts.containsKey(object)) {
            counts.put(object, counts.get(object) + increment);
        } else {
            counts.put(object, increment);
        }

        total += increment;

        argMax.update(object, increment);

        argMax.update(object, -increment);

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

    public Pair<T, Double> getMax() {
        return new Pair<>(this.argMax.getArgmax(),
                this.argMax.getMaxValue());
    }

    public Pair<T, Double> getMin() {
        return new Pair<>(this.argMin.getArgmax(),
                -this.argMin.getMaxValue());
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

        Collections.sort(keys, new Comparator<T>() {

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
        });

        return keys;
    }

    @SuppressWarnings("unchecked")
    public List<T> getSortedItemsHighestFirst() {
        List<T> keys = new ArrayList<>();
        for (Object key : this.counts.keys())
            keys.add((T) key);

        Collections.sort(keys, new Comparator<T>() {

            public int compare(T arg0, T arg1) {
                double d0 = counts.get(arg0);
                double d1 = counts.get(arg1);

                if (d0 < d1)
                    return 1;
                else if (d0 > d1)
                    return -1;
                else
                    return 0;
            }
        });

        return keys;
    }
}
