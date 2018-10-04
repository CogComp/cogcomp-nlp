/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.math;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Keeps track of maximum and the object that corresponds to the maximum.
 * <p>
 * V: type of value over which the max decision is made. T: type of the objects which generate the
 * values
 * <p>
 * In case of ties, if {@link #randomTieBreak} is {@code false} the first object is chosen;
 * otherwise it's a random choice.
 * <p>
 * <b>Example:</b>
 * <p>
 * Suppose we have a list of strings that can be scored and we wish to track the maximum.
 * <p>
 * 
 * <pre>
 * ArgMax&lt;Double, String&gt; argmax = new Argmax&lt;Double, String&gt;(Double.NEGATIVE_INFINITY, &quot;&quot;);
 * 
 * for (String str : stringList) // stringList is probably an array of Strings
 * {
 *     argmax.update(str, getScore(str));
 * }
 * 
 * double maxValue = argmax.getMaxValue();
 * String argmaxString = argmax.getArgMax();
 * </pre>
 *
 * @author Vivek Srikumar
 * @author Christos Christodouloupoulos
 */
public class ArgMax<T, V extends Comparable<V>> {
    private V value;
    private T object;
    private boolean randomTieBreak;

    // Always starting with the same seed for replicability
    private Random random = new Random(42);

    /**
     * instantiate ArgMax with a collection of key/value pairs to simplify use
     *
     * @param values key/value pairs for which you want the ArgMax
     */
        public ArgMax(Map< T, V > values) {
             Iterator<Map.Entry<T, V>> valIterator = values.entrySet().iterator();
            if ( valIterator.hasNext() )
            {
                Map.Entry<T, V> e = valIterator.next();
                this.object = e.getKey();
                this.value = e.getValue();
            }
            while( valIterator.hasNext() )
            {
                Map.Entry<T, V> e = valIterator.next();
                update(e.getKey(), e.getValue());
            }
        }

    public ArgMax(T initialObject, V initialValue) {
        this(initialObject, initialValue, false);
    }

    public ArgMax(T initialObject, V initialValue, boolean randomTieBreak) {
        this.value = initialValue;
        this.object = initialObject;
        this.randomTieBreak = randomTieBreak;
    }

    public void update(T object, V value) {
        if (randomTieBreak && this.value.compareTo(value) == 0) {
            if (random.nextBoolean()) {
                this.value = value;
                this.object = object;
            }
        } else if (this.value.compareTo(value) < 0) {
            this.value = value;
            this.object = object;
        }
    }

    public V getMaxValue() {
        return value;
    }

    public T getArgmax() {
        return object;
    }

    @Override
    public String toString() {
        return "value: " + value + ", object: " + object;
    }

}
