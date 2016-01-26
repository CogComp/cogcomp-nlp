package edu.illinois.cs.cogcomp.core.math;

/**
 * Keeps track of maximum and the object that corresponds to the maximum.
 * <p/>
 * K: type of value over which the max decision is made. T: type of the objects which generate the
 * values
 * <p/>
 * In case of ties, the first object is chosen.
 * <p/>
 * <b>Example:</b>
 * <p/>
 * Suppose we have a list of strings that can be scored and we wish to track the maximum.
 * <p/>
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
 */
public class ArgMin<T, K extends Comparable<K>> {
    private K key;
    private T object;

    public void update(T object, K value) {
        if (key.compareTo(value) >= 0) {
            key = value;
            this.object = object;
        }
    }

    public K getMinValue() {
        return key;
    }

    public T getArgmin() {
        return object;
    }

    public ArgMin(T initialObject, K initialValue) {
        key = initialValue;
        this.object = initialObject;
    }

    @Override
    public String toString() {
        return "value: " + key + ", object: " + object;
    }

}
