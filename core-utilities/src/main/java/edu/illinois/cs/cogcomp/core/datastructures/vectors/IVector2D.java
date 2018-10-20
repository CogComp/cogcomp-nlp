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
 * This class implements an expandable two dimensional array of ints that should be faster than
 * java's <code>Vector</code>.
 *
 * @author Nick Rizzolo
 **/
public class IVector2D implements Cloneable, java.io.Serializable {
    /** The default capacity of the first dimension of this 2D vector. */
    protected static final int defaultCapacity1 = 8;
    /** The default capacity of the second dimension of this 2D vector. */
    protected static final int defaultDefaultCapacity2 = 8;

    /** The elements of the vector. */
    protected int[][] vector;
    /** The sizes of each vector in the second dimension. */
    protected IVector sizes;
    /** The capacity of new vectors created in the second dimension. */
    protected int defaultCapacity2;


    /**
     * Constructs a new vector with default capacities {@link #defaultCapacity1} and
     * {@link #defaultCapacity2}.
     **/
    public IVector2D() {
        this(defaultCapacity1, defaultDefaultCapacity2);
    }

    /**
     * Constructs a new vector with the specified capacities.
     *
     * @param c1 The initial capacity for the first dimension of the new vector.
     * @param c2 The initial capacity for the second dimension of the new vector.
     **/
    public IVector2D(int c1, int c2) {
        defaultCapacity2 = Math.max(defaultDefaultCapacity2, c2);
        vector = new int[Math.max(defaultCapacity1, c1)][defaultCapacity2];
        sizes = new IVector(c1);
    }

    /**
     * Constructs a new vector using the specified array as a starting point.
     *
     * @param v The initial array.
     **/
    public IVector2D(int[][] v) {
        defaultCapacity2 = defaultDefaultCapacity2;

        if (v.length == 0) {
            vector = new int[defaultCapacity1][defaultCapacity2];
            sizes = new IVector(defaultCapacity1);
        } else {
            vector = v;
            sizes = new IVector(v.length);

            for (int i = 0; i < v.length; ++i) {
                sizes.set(i, v[i].length);
                defaultCapacity2 = Math.max(defaultCapacity2, v[i].length);
            }

            for (int i = 0; i < v.length; ++i)
                if (v[i].length == 0)
                    v[i] = new int[defaultCapacity2];
        }
    }


    /**
     * Throws an exception when either of the specified indexes are negative.
     *
     * @param i1 The index in the first dimension.
     * @param i2 The index in the second dimension.
     * @throws ArrayIndexOutOfBoundsException When <code>i1</code> or <code>i2</code> &lt; 0.
     **/
    protected void boundsCheck(int i1, int i2) {
        if (i1 < 0 || i2 < 0)
            throw new ArrayIndexOutOfBoundsException(
                    "Attempted to access negative index of IVector2D.");
    }


    /**
     * Retrieves the value stored at the specified index of the vector.
     *
     * @param i1 The index in the first dimension.
     * @param i2 The index in the second dimension.
     * @return The retrieved value.
     * @throws ArrayIndexOutOfBoundsException When <code>i1</code> or <code>i2</code> &lt; 0.
     **/
    public int get(int i1, int i2) {
        return get(i1, i2, 0);
    }

    /**
     * Retrieves the value stored at the specified index of the vector.
     *
     * @param i1 The index in the first dimension.
     * @param i2 The index in the second dimension.
     * @param d The default value.
     * @return The retrieved value.
     * @throws ArrayIndexOutOfBoundsException When <code>i1</code> or <code>i2</code> &lt; 0.
     **/
    public int get(int i1, int i2, int d) {
        boundsCheck(i1, i2);

        // Because of the way IVector works, the only way i2 < sizes.get(i1) will
        // be true is if i1 is in fact a valid index for the first dimension.
        return i2 < sizes.get(i1) ? vector[i1][i2] : d;
    }


    /**
     * Sets the value at the specified index to the given value.
     *
     * @param i1 The index in the first dimension.
     * @param i2 The index in the second dimension.
     * @param v The new value at that index.
     * @return The value that used to be at index <code>i</code>.
     * @throws ArrayIndexOutOfBoundsException When <code>i1</code> or <code>i2</code> &lt; 0.
     **/
    public int set(int i1, int i2, int v) {
        return set(i1, i2, v, 0);
    }

    /**
     * Sets the value at the specified index to the given value.
     *
     * @param i1 The index in the first dimension.
     * @param i2 The index in the second dimension.
     * @param v The new value at that index.
     * @param d The default value for other new indexes that might get created.
     * @return The value that used to be at index <code>i</code>.
     * @throws ArrayIndexOutOfBoundsException When <code>i1</code> or <code>i2</code> &lt; 0.
     **/
    public int set(int i1, int i2, int v, int d) {
        boundsCheck(i1, i2);
        expandFor(i1, i2, d);
        int result = vector[i1][i2];
        vector[i1][i2] = v;
        return result;
    }


    /**
     * Removes the row at the specified index.
     *
     * @param i The index of the row to remove.
     * @return The removed row.
     **/
    public int[] remove(int i) {
        boundsCheck(i, 0);
        int rows = sizes.size();
        if (i >= rows)
            throw new ArrayIndexOutOfBoundsException("LBJ: IVector2D: Can't remove row at index "
                    + i + " as it is larger than the size (" + rows + ")");
        int[] result = vector[i];
        for (int j = i + 1; j < rows; ++j)
            vector[j - 1] = vector[j];
        vector[rows - 1] = null;
        sizes.remove(i);
        return result;
    }


    /**
     * Removes the element at the specified index of the vector.
     *
     * @param i1 The row index of the element to remove.
     * @param i2 The column index of the element to remove.
     * @return The removed element.
     **/
    public int remove(int i1, int i2) {
        boundsCheck(i1, i2);
        int rows = sizes.size(), columns = sizes.get(i1);
        if (i1 >= rows || i2 >= columns)
            throw new ArrayIndexOutOfBoundsException("LBJ: IVector2D: Can't remove index [" + i1
                    + ", " + i2 + "] as it is out of bounds (" + rows + ", " + columns + ")");
        int result = vector[i1][i2];
        for (int j = i2 + 1; j < columns; ++j)
            vector[i1][j - 1] = vector[i1][j];
        sizes.set(i1, columns - 1);
        return result;
    }


    /**
     * Increments the integer at the given index by 1.
     *
     * @param i1 The index in the first dimension.
     * @param i2 The index in the second dimension.
     * @throws ArrayIndexOutOfBoundsException When <code>i1</code> or <code>i2</code> &lt; 0.
     **/
    public void increment(int i1, int i2) {
        increment(i1, i2, 0);
    }

    /**
     * Increments the integer at the given index by 1.
     *
     * @param i1 The index in the first dimension.
     * @param i2 The index in the second dimension.
     * @param d The default value for other new indexes that might get created.
     * @throws ArrayIndexOutOfBoundsException When <code>i1</code> or <code>i2</code> &lt; 0.
     **/
    public void increment(int i1, int i2, int d) {
        boundsCheck(i1, i2);
        expandFor(i1, i2, d);
        vector[i1][i2]++;
    }


    /** Returns the size of the first dimension of this vector.. */
    public int size() {
        return sizes.size();
    }

    /**
     * Returns the size associated with the specified vector.
     *
     * @param i The index of the vector whose size will be returned.
     **/
    public int size(int i) {
        return sizes.get(i);
    }


    /**
     * Returns the value of the maximum element in the <code>i</code><sup>th</sup> vector.
     *
     * @param i An index into the first dimension of this vector.
     **/
    public int max(int i) {
        if (i < 0 || i >= sizes.size())
            throw new ArrayIndexOutOfBoundsException(
                    "Attempted to access negative index of IVector2D.");
        int result = Integer.MIN_VALUE, size = sizes.get(i);
        for (int j = 0; j < size; ++j)
            if (vector[i][j] > result)
                result = vector[i][j];
        return result;
    }


    /**
     * Sorts the selected row in increasing order.
     *
     * @param i The row to sort.
     **/
    public void sort(int i) {
        Arrays.sort(vector[i], 0, sizes.get(i));
    }


    /**
     * Searches the selected row vector for the specified value using the binary search algorithm.
     * The vector <strong>must</strong> be sorted (as by the {@link #sort(int)} method) prior to
     * making this call. If it is not sorted, the results are undefined. If the vector contains
     * multiple elements with the specified value, there is no guarantee which one will be found.
     *
     * @param i The selected row.
     * @param v The value to be searched for.
     * @return The index of <code>v</code>, if it is contained in the vector; otherwise,
     *         <code>(-(<i>insertion point</i>) - 1)</code>. The <i>insertion point</i> is defined
     *         as the point at which <code>v</code> would be inserted into the vector: the index of
     *         the first element greater than <code>v</code>, or the size of the vector if all
     *         elements in the vector are less than <code>v</code>. Note that this guarantees that
     *         the return value will be &gt;= 0 if and only if <code>v</code> is found.
     **/
    public int binarySearch(int i, int v) {
        int a = 0, b = sizes.get(i);

        while (b != a) {
            int m = (a + b) >> 1;
            if (vector[i][m] > v)
                b = m;
            else if (vector[i][m] < v)
                a = m + 1;
            else
                return m;
        }

        return -a - 1;
    }


    /**
     * Makes sure the capacities and sizes of the vectors can accomodate the given indexes. The
     * capacities of the vectors are simply doubled until they can accomodate their sizes.
     *
     * @param i1 The index in the first dimension.
     * @param i2 The index in the second dimension.
     * @param d The default value for other new indexes that might get created.
     **/
    protected void expandFor(int i1, int i2, int d) {
        if (i1 >= sizes.size()) {
            int oldSize = sizes.size(), capacity = vector.length;
            sizes.set(i1, 0);
            if (capacity < sizes.size()) {
                while (capacity < sizes.size())
                    capacity *= 2;
                int[][] t = new int[capacity][];
                System.arraycopy(vector, 0, t, 0, oldSize);
                vector = t;
            }
            for (int i = oldSize; i < sizes.size(); ++i)
                vector[i] = new int[defaultCapacity2];
        }

        if (i2 < sizes.get(i1))
            return;
        int oldSize = sizes.get(i1), capacity = vector[i1].length;
        sizes.set(i1, i2 + 1);
        if (capacity >= sizes.get(i1))
            return;
        while (capacity < sizes.get(i1))
            capacity *= 2;
        int[] t = new int[capacity];
        System.arraycopy(vector[i1], 0, t, 0, oldSize);
        if (d != 0)
            Arrays.fill(t, oldSize, sizes.get(i1), d);
        vector[i1] = t;
    }


    /**
     * Returns a new 2D array of <code>int</code>s containing the same data as this vector.
     **/
    public int[][] toArray() {
        int[][] result = new int[sizes.size()][];
        for (int i = 0; i < result.length; ++i) {
            result[i] = new int[sizes.get(i)];
            System.arraycopy(vector[i], 0, result[i], 0, result[i].length);
        }
        return result;
    }


    /**
     * Two <code>IVector2D</code>s are considered equal if they contain all the same elements,
     * sizes, and capacities.
     **/
    public boolean equals(Object o) {
        if (!(o instanceof IVector2D))
            return false;
        IVector2D v = (IVector2D) o;
        if (vector.length != v.vector.length || !sizes.equals(v.sizes))
            return false;
        for (int i = 0; i < vector.length; ++i)
            if (!Arrays.equals(vector[i], v.vector[i]))
                return false;
        return true;
    }


    /**
     * A hash code based on the hash codes of the constituents of {@link #vector}.
     **/
    public int hashCode() {
        int result = vector.hashCode();
        for (int i = 0; i < vector.length; ++i)
            result = 17 * result + vector[i].hashCode();
        return result;
    }


    /**
     * Returns a clone of this vector that is one level deep; in particular, the objects in the
     * vector themselves are not cloned, but the underlying array is.
     **/
    public Object clone() {
        IVector2D clone = null;

        try {
            clone = (IVector2D) super.clone();
        } catch (Exception e) {
            System.err.println("Error cloning " + getClass().getName() + ":");
            e.printStackTrace();
            System.exit(1);
        }

        clone.vector = (int[][]) vector.clone();
        for (int i = 0; i < vector.length; ++i)
            if (clone.vector[i] != null)
                clone.vector[i] = (int[]) vector[i].clone();
        return clone;
    }


    /** Returns a text representation of this vector. */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        int s1 = sizes.size();

        for (int i = 0; i < s1; ++i) {
            int s2 = sizes.get(i);
            result.append("[");

            for (int j = 0; j < s2; ++j) {
                result.append(vector[i][j]);
                if (j + 1 < s2)
                    result.append(", ");
            }

            result.append("]");
            if (i + 1 < s1)
                result.append(",\n ");
        }

        result.append("]");
        return result.toString();
    }


    /**
     * Writes a binary representation of the vector.
     *
     * @param out The output stream.
     **/
    public void write(ExceptionlessOutputStream out) {
        sizes.write(out);
        for (int i = 0; i < sizes.size(); ++i)
            for (int j = 0; j < sizes.get(i); ++j)
                out.writeInt(vector[i][j]);
        out.writeInt(defaultCapacity2);
    }


    /**
     * Reads the binary representation of a vector from the specified stream, overwriting the data
     * in this object.
     *
     * @param in The input stream.
     **/
    public void read(ExceptionlessInputStream in) {
        sizes = new IVector();
        sizes.read(in);

        if (sizes.size() == 0) {
            defaultCapacity2 = defaultDefaultCapacity2;
            vector = new int[defaultCapacity1][defaultCapacity2];
        } else {
            vector = new int[sizes.size()][];

            for (int i = 0; i < vector.length; ++i) {
                vector[i] = new int[sizes.get(i)];
                for (int j = 0; j < vector[i].length; ++j)
                    vector[i][j] = in.readInt();
            }

            defaultCapacity2 = in.readInt();
        }
    }
}
