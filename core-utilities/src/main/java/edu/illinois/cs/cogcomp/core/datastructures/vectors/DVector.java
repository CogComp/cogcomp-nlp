/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.vectors;

import java.util.Arrays;


/**
 * This class implements an expandable array of <code>double</code>s that should be faster than
 * java's <code>Vector</code>. Vector was changed to <code>floats</code> for efficiency.
 *
 * @author Nick Rizzolo
 */
public class DVector implements Cloneable, java.io.Serializable {
    /** generated */
    private static final long serialVersionUID = 2318393173918599422L;

    /** The default capacity of a vector upon first construction. */
    protected static final int defaultCapacity = 8;

    /** The elements of the vector. */
    protected float[] vector;
    /** The number of elements in the vector. */
    protected int size;


    /**
     * Constructs a new vector with capacity equal to {@link #defaultCapacity}.
     */
    public DVector() {
        this(defaultCapacity);
    }

    /**
     * Constructs a new vector with the specified capacity.
     *
     * @param c The initial capacity for the new vector.
     */
    public DVector(int c) {
        vector = new float[Math.max(defaultCapacity, c)];
    }

    /**
     * Constructs a new vector using the specified array as a starting point.
     *
     * @param v The initial array.
     */
    public DVector(double[] v) {
        if (v.length == 0)
            vector = new float[defaultCapacity];
        else {
            vector = new float[v.length];
            for (int i = 0; i < v.length; i++)
            	vector[i] = (float)v[i];
            size = vector.length;
        }
    }

    /**
     * Retrieves the value stored at the specified index of the vector, or 0 if the vector isn't
     * long enough.
     *
     * @param i The index of the value to retrieve.
     * @return The retrieved value.
     * @throws ArrayIndexOutOfBoundsException When <code>i</code> &lt; 0.
     */
    public double get(int i) {
        return get(i, 0);
    }

    /**
     * Retrieves the value stored at the specified index of the vector or <code>d</code> if the
     * vector isn't long enough.
     *
     * @param i The index of the value to retrieve.
     * @param d The default value.
     * @return The retrieved value.
     * @throws ArrayIndexOutOfBoundsException When <code>i</code> &lt; 0.
     */
    public double get(int i, double d) {
        return i < size ? vector[i] : d;
    }


    /**
     * Sets the value at the specified index to the given value.
     *
     * @param i The index of the value to set.
     * @param v The new value at that index.
     * @return The value that used to be at index <code>i</code>.
     * @throws ArrayIndexOutOfBoundsException When <code>i</code> &lt; 0.
     */
    public double set(int i, double v) {
        return set(i, v, 0);
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
     */
    public double set(int i, double v, double d) {
        expandFor(i, d);
        double result = vector[i];
        vector[i] = (float)v;
        return result;
    }


    /**
     * Adds the specified value on to the end of the vector, expanding its capacity as necessary.
     *
     * @param v The new value to appear last in the vector.
     */
    public void add(double v) {
        expandFor(size, 0);
        vector[size - 1] = (float)v;
    }


    /**
     * Adds all the values in the given vector to the end of this vector, expanding its capacity as
     * necessary.
     *
     * @param v The new vector of values to appear at the end of this vector.
     */
    public void addAll(DVector v) {
        expandFor(size + v.size - 1, 0);
        System.arraycopy(v.vector, 0, vector, size - v.size, v.size);
    }


    /**
     * Removes the element at the specified index of the vector.
     *
     * @param i The index of the element to remove.
     * @return The removed element.
     */
    public double remove(int i) {
        if (i >= size)
            throw new ArrayIndexOutOfBoundsException("LBJ: DVector: Can't remove element at index "
                    + i + " as it is larger than the size (" + size + ")");
        double result = vector[i];
        for (int j = i + 1; j < size; ++j)
            vector[j - 1] = vector[j];
        --size;
        return result;
    }


    /** @return the value of {@link #size}. */
    public int size() {
        return size;
    }


    /** @return the value of the maximum element in the vector. */
    public double max() {
    	double result = -Double.MAX_VALUE;
        for (int i = 0; i < size; ++i)
            if (vector[i] > result)
                result = vector[i];
        return result;
    }


    /** Sorts this vector in increasing order. */
    public void sort() {
        Arrays.sort(vector, 0, size);
    }


    /**
     * Searches this vector for the specified value using the binary search algorithm. This vector
     * <strong>must</strong> be sorted (as by the {@link #sort()} method) prior to making this call.
     * If it is not sorted, the results are undefined. If this vector contains multiple elements
     * with the specified value, there is no guarantee which one will be found.
     *
     * @param v The value to be searched for.
     * @return The index of <code>v</code>, if it is contained in the vector; otherwise,
     *         <code>(-(<i>insertion point</i>) - 1)</code>. The <i>insertion point</i> is defined
     *         as the point at which <code>v</code> would be inserted into the vector: the index of
     *         the first element greater than <code>v</code>, or the size of the vector if all
     *         elements in the list are less than <code>v</code>. Note that this guarantees that the
     *         return value will be &gt;= 0 if and only if <code>v</code> is found.
     */
    public int binarySearch(double v) {
        int a = 0, b = size;

        while (b != a) {
            int m = (a + b) >> 1;
            if (vector[m] > v)
                b = m;
            else if (vector[m] < v)
                a = m + 1;
            else
                return m;
        }

        return -a - 1;
    }


    /**
     * Makes sure the capacity and size of the vector can accomodate the given index. The capacity
     * of the vector is simply doubled until it can accomodate its size.
     *
     * @param index The index where a new value will be stored.
     * @param d The default value for other new indexes that might get created.
     */
    protected void expandFor(int index, double d) {
        if (index < size)
            return;
        int oldSize = size, capacity = vector.length;
        size = index + 1;
        if (capacity >= size)
            return;
        while (capacity < size)
            capacity *= 2;
        float[] t = new float[capacity];
        System.arraycopy(vector, 0, t, 0, oldSize);
        if (d != 0)
            Arrays.fill(t, oldSize, size, (float)d);
        vector = t;
    }


    /**
     * @return a new array of <code>double</code>s containing the same data as this vector.
     */
    public double[] toArray() {
        double[] result = new double[size];
        System.arraycopy(vector, 0, result, 0, size);
        return result;
    }


    /**
     * Two <code>DVector</code>s are considered equal if they contain the same elements and have the
     * same size.
     */
    public boolean equals(Object o) {
        if (!(o instanceof DVector))
            return false;
        DVector v = (DVector) o;
        return size == v.size && Arrays.equals(vector, v.vector);
    }


    /** A hash code based on the hash code of {@link #vector}. */
    public int hashCode() {
        return vector.hashCode();
    }


    /** Returns a deep clone of this vector. */
    public Object clone() {
        DVector clone = null;

        try {
            clone = (DVector) super.clone();
        } catch (Exception e) {
            System.err.println("Error cloning " + getClass().getName() + ":");
            e.printStackTrace();
            System.exit(1);
        }

        clone.vector = (float[]) vector.clone();
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


    /**
     * Writes a binary representation of the vector. We will always write the data as single
     * precision(indicated by the negative size), in this was, as people rebuild their models, 
     * the disk footprint is reduced, yet, as seen in the read method, they can still read the
     * old models.
     *
     * @param out The output stream.
     */
    public void write(ExceptionlessOutputStream out) {
        out.writeInt(-size);
        for (int i = 0; i < size; ++i)
            out.writeFloat(vector[i]);
    }


    /**
     * Reads the binary representation of a vector from the specified stream, overwriting the data
     * in this object. The data is either double on disk, or single precision. If the size read
     * from the first int is negative, it is single precision data, if it is positive it is double
     * precision data.
     *
     * @param in The input stream.
     */
    public void read(ExceptionlessInputStream in) {
        size = in.readInt();
        if (size == 0)
            vector = new float[defaultCapacity];
        else if (size < 0) {
            size = -size;
            vector = new float[size];
            for (int i = 0; i < size; ++i)
                vector[i] = in.readFloat();
        } else {
            vector = new float[size];
            for (int i = 0; i < size; ++i)
                vector[i] = (float)in.readDouble();
        }
    }
}
