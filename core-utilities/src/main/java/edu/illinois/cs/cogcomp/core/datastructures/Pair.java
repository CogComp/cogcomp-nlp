/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import java.io.Serializable;

/**
 * This utility class represents a pair of objects, the first one of type {@code A} and the second
 * of type {@code B}. This class implements {@code equals} and {@code hashcode} and can be used as
 * keys to maps.
 *
 * @param <A> The type of the first object in the pair.
 * @param <B> The type of the second object in the pair.
 * @author Vivek Srikumar
 */
public class Pair<A, B> implements Serializable {

    private static final long serialVersionUID = -7912552379908996819L;

    private A aObj;
    private B bObj;

    public Pair(A obj, B obj2) {
        aObj = obj;
        bObj = obj2;
    }

    public A getFirst() {
        return aObj;
    }

    public B getSecond() {
        return bObj;
    }

    public void setFirst(A a) {
        aObj = a;
    }

    public void setSecond(B b) {
        bObj = b;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Pair))
            return false;

        Pair that = (Pair) obj;

        boolean firstEqual = false;
        boolean secondEqual = false;

        if (this.getFirst() == null && that.getFirst() == null)
            firstEqual = true;
        else if (this.getFirst() != null && that.getFirst() != null) {
            firstEqual = this.getFirst().equals(that.getFirst());
        }

        if (this.getSecond() == null && that.getSecond() == null)
            secondEqual = true;
        else if (this.getSecond() != null && that.getSecond() != null) {
            secondEqual = this.getSecond().equals(that.getSecond());
        }

        return firstEqual && secondEqual;
    }

    @Override
    public int hashCode() {
        return this.getFirst().hashCode() * 79 + this.getSecond().hashCode();
    }

    @Override
    public String toString() {
        return "(" + getFirst() + ", " + getSecond() + ")";
    }
}
