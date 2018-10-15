/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.vectors;

import java.util.Arrays;
import java.util.Comparator;


/**
 * This class implements an expandable array of objects that should be faster than java's
 * <code>Vector</code>.
 *
 * @author Nick Rizzolo
 **/
public class OVector implements Cloneable, java.io.Serializable {
    /** The default capacity of a vector upon first construction. */
    protected static final int defaultCapacity = 8;

    /** The elements of the vector. */
    protected Object[] vector;
    /** The number of elements in the vector. */
    protected int size;


    /**
     * Constructs a new vector with capacity equal to {@link #defaultCapacity}.
     **/
    public OVector() {
        this(defaultCapacity);
    }

    /**
     * Constructs a new vector with the specified capacity.
     *
     * @param c The initial capacity for the new vector.
     **/
    public OVector(int c) {
        vector = new Object[Math.max(defaultCapacity, c)];
    }

    /**
     * Constructs a new vector using the specified array as a starting point.
     *
     * @param v The initial array.
     **/
    public OVector(Object[] v) {
        if (v.length == 0)
            vector = new Object[defaultCapacity];
        else {
            vector = v;
            size = vector.length;
        }
    }

    /**
     * Constructs a copy of a vector starting with capacity equal to that vector's size.
     *
     * @param v The vector to copy.
     **/
    public OVector(OVector v) {
        int N = v.size();
        if (N == 0)
            vector = new Object[defaultCapacity];
        else {
            vector = new Object[N];
            size = N;
            System.arraycopy(v.vector, 0, vector, 0, N);
        }
    }


    /**
     * Throws an exception when the specified index is negative.
     *
     * @param i The index.
     * @throws ArrayIndexOutOfBoundsException When <code>i</code> &lt; 0.
     **/
    protected void boundsCheck(int i) {
        if (i < 0)
            throw new ArrayIndexOutOfBoundsException(
                    "Attempted to access negative index of OVector.");
    }


    /**
     * Retrieves the value stored at the specified index of the vector, or <code>null</code> if the
     * vector isn't long enough.
     *
     * @param i The index of the value to retrieve.
     * @return The retrieved value.
     * @throws ArrayIndexOutOfBoundsException When <code>i</code> &lt; 0.
     **/
    public Object get(int i) {
        return get(i, null);
    }

    /**
     * Retrieves the value stored at the specified index of the vector or <code>d</code> if the
     * vector isn't long enough.
     *
     * @param i The index of the value to retrieve.
     * @param d The default value.
     * @return The retrieved value.
     * @throws ArrayIndexOutOfBoundsException When <code>i</code> &lt; 0.
     **/
    public Object get(int i, Object d) {
        boundsCheck(i);
        return i < size ? vector[i] : d;
    }


    /**
     * Sets the value at the specified index to the given value.
     *
     * @param i The index of the value to set.
     * @param v The new value at that index.
     * @return The value that used to be at index <code>i</code>.
     * @throws ArrayIndexOutOfBoundsException When <code>i</code> &lt; 0.
     **/
    public Object set(int i, Object v) {
        return set(i, v, null);
    }

    /**
     * Sets the value at the specified index to the given value. If the given index is greater than
     * the vector's current size, the vector will expand to accomodate it.
     *
     * @param i The index of the value to set.
     * @param v The new value at that index.
     * @param d The default value for other new indexes that might get created.
     * @return The value that used to be at index <code>i</code>.
     * @throws ArrayIndexOutOfBoundsException When <code>i</code> &lt; 0.
     **/
    public Object set(int i, Object v, Object d) {
        boundsCheck(i);
        expandFor(i, d);
        Object result = vector[i];
        vector[i] = v;
        return result;
    }


    /**
     * Adds the specified value on to the end of the vector, expanding its capacity as necessary.
     *
     * @param v The new value to appear last in the vector.
     **/
    public void add(Object v) {
        expandFor(size, null);
        vector[size - 1] = v;
    }


    /**
     * Adds all the values in the given vector to the end of this vector, expanding its capacity as
     * necessary.
     *
     * @param v The new vector of values to appear at the end of this vector.
     **/
    public void addAll(OVector v) {
        expandFor(size + v.size - 1, null);
        System.arraycopy(v.vector, 0, vector, size - v.size, v.size);
    }


    /**
     * Removes the element at the specified index of the vector.
     *
     * @param i The index of the element to remove.
     * @return The removed element.
     **/
    public Object remove(int i) {
        boundsCheck(i);
        if (i >= size)
            throw new ArrayIndexOutOfBoundsException("LBJ: OVector: Can't remove element at index "
                    + i + " as it is larger than the size (" + size + ")");
        Object result = vector[i];
        for (int j = i + 1; j < size; ++j)
            vector[j - 1] = vector[j];
        vector[--size] = null;
        return result;
    }


    /** Returns the value of {@link #size}. */
    public int size() {
        return size;
    }


    /**
     * Sorts this vector in increasing order according to the given comparator.
     *
     * @param c A comparator for the elements of this vector.
     **/
    public void sort(Comparator c) {
        Arrays.sort(vector, 0, size, c);
    }


    /**
     * Makes sure the capacity and size of the vector can accomodate the given index. The capacity
     * of the vector is simply doubled until it can accomodate its size.
     *
     * @param index The index where a new value will be stored.
     * @param d The default value for other new indexes that might get created.
     **/
    protected void expandFor(int index, Object d) {
        if (index < size)
            return;
        int oldSize = size, capacity = vector.length;
        size = index + 1;
        if (capacity >= size)
            return;
        while (capacity < size)
            capacity *= 2;
        Object[] t = new Object[capacity];
        System.arraycopy(vector, 0, t, 0, oldSize);
        if (d != null)
            Arrays.fill(t, oldSize, size, d);
        vector = t;
    }


    /**
     * Returns a new array of <code>objects</code>s containing the same data as this vector.
     **/
    public Object[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(vector, 0, result, 0, size);
        return result;
    }


    /**
     * Two <code>OVector</code>s are considered equal if they contain equivalent elements and have
     * the same size.
     **/
    public boolean equals(Object o) {
        if (!(o instanceof OVector))
            return false;
        OVector v = (OVector) o;
        return size == v.size && Arrays.equals(vector, v.vector);
    }


    /** A hash code based on the hash code of {@link #vector}. */
    public int hashCode() {
        return vector.hashCode();
    }


    /**
     * Returns a shallow clone of this vector; the vector itself is cloned, but the element objects
     * aren't.
     **/
    public Object clone() {
        OVector clone = null;

        try {
            clone = (OVector) super.clone();
        } catch (Exception e) {
            System.err.println("Error cloning " + getClass().getName() + ":");
            e.printStackTrace();
            System.exit(1);
        }

        clone.vector = (Object[]) vector.clone();
        return clone;
    }


    /** Returns a text representation of this vector. */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        for (int i = 0; i < size; ++i) {
            result.append(vector[i]);
            if (i + 1 < size)
                result.append(", ");
        }
        result.append("]");
        return result.toString();
    }
}
