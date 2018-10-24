/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

/**
 * A feature that takes a real value
 *
 * @author Vivek Srikumar
 */
public class RealFeature extends Feature {

    private final float value;

    public RealFeature(String name, float value) {
        super(name);
        this.value = value;
    }

    protected RealFeature(byte[] bytes, float value) {
        super(bytes);
        this.value = value;

    }

    /**
     * Create a real valued feature
     */
    public static RealFeature create(String name, float value) {
        return new RealFeature(name, value);
    }

    @Override
    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getName() + "=" + value;
    }

    @Override
    public int hashCode() {
        final int prime = 41;
        int result = 1;
        result = prime * result + this.getNameHashCode();
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RealFeature other = (RealFeature) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        return Double.doubleToLongBits(value) == Double.doubleToLongBits(other.value);
    }
}
