/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

/**
 * A discrete feature
 *
 * @author Vivek Srikumar
 */
public class DiscreteFeature extends Feature {

    public DiscreteFeature(String name) {
        super(name);
    }

    protected DiscreteFeature(byte[] bytes) {
        super(bytes);
    }

    public static DiscreteFeature create(String s) {
        return new DiscreteFeature(s);
    }

    @Override
    public float getValue() {
        return 1;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return this.getNameHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiscreteFeature other = (DiscreteFeature) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        return true;
    }

}
