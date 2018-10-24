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
 * This utility class represents a triple of objects, the first one of type {@code A}, the second of
 * type {@code B}, and the third of type {@code C}. This class implements {@code equals} and
 * {@code hashcode} and can be used as keys to maps.
 *
 * @param <A> The type of the first object in the triple.
 * @param <B> The type of the second object in the triple.
 * @param <C> The type of the third object in the triple.
 * @author Stephen Mayhew
 */
public class Triple<A, B, C> implements Serializable {

    private static final long serialVersionUID = -3617612179908996819L;

    private A aObj;
    private B bObj;
    private C cObj;

    public Triple(A obj, B obj2, C obj3) {
        aObj = obj;
        bObj = obj2;
        cObj = obj3;
    }

    public A getFirst() {
        return aObj;
    }

    public B getSecond() {
        return bObj;
    }

    public C getThird() {
        return cObj;
    }

    public void setFirst(A a) {
        aObj = a;
    }

    public void setSecond(B b) {
        bObj = b;
    }

    public void setThird(C c) {
        cObj = c;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Triple))
            return false;

        Triple that = (Triple) obj;

        boolean firstEqual = false;
        boolean secondEqual = false;
        boolean thirdEqual = false;

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

        if (this.getThird() == null && that.getThird() == null)
            thirdEqual = true;
        else if (this.getThird() != null && that.getThird() != null) {
            thirdEqual = this.getThird().equals(that.getThird());
        }

        return firstEqual && secondEqual && thirdEqual;
    }

    @Override
    public int hashCode() {
        int result = aObj.hashCode();
        result = 31 * result + bObj.hashCode();
        result = 31 * result + cObj.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(" + getFirst() + ", " + getSecond() + "," + getThird() + ")";
    }
}
