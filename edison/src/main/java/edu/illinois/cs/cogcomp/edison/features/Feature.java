/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author Vivek Srikumar
 */
public abstract class Feature {

    private final static Logger log = LoggerFactory.getLogger(Feature.class);

    private static final String US_ASCII = "US-ASCII";

    /**
     * Are the feature names encoded in ASCII?
     */
    protected static boolean ascii = false;

    /**
     * Should the feature name be retained in case the encoding is ASCII?
     */
    protected static boolean keepString = false;
    private final String name;
    private final byte[] bytes;
    private final int nameHashCode;

    public Feature(String name) {
        if (ascii) {
            if (keepString)
                this.name = name;
            else
                this.name = null;

            try {
                bytes = name.getBytes(US_ASCII);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        } else {
            this.name = name;
            this.bytes = null;
        }

        this.nameHashCode = name.hashCode() * 31 + 41;

    }

    protected Feature(byte[] bytes) {

        try {

            if (ascii) {

                if (keepString) {
                    this.name = new String(bytes, US_ASCII);
                } else {
                    this.name = null;
                }

                this.bytes = bytes;

                // we can do this and still retain conistency of hash code
                // because java's algorithm of hash code is the same for strings
                // and arrays. this can change. possible bug alert!
                this.nameHashCode = Arrays.hashCode(bytes) * 31 + 41;

            } else {
                this.bytes = null;
                this.name = new String(bytes, US_ASCII);
                this.nameHashCode = name.hashCode() * 31 + 41;

            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    public static void setUseAscii() {
        log.debug("Using ASCII encoding for features");
        ascii = true;
    }

    public static void resetUseAscii() {
        log.debug("Using Unicode encoding for features");
        ascii = false;
    }

    public static void setKeepString() {
        log.debug("Retaining original string for ASCII features.");
        keepString = true;
    }

    public static void resetKeepString() {
        log.debug("Discarding original string for ASCII features");
        keepString = false;
    }

    public abstract float getValue();

    public String getName() {
        if (DiscreteFeature.ascii && bytes != null)

            try {
                if (keepString) {
                    assert name != null;
                    return name;
                } else {
                    return new String(bytes, US_ASCII);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        else
            return name;
    }

    protected int getNameHashCode() {
        return this.nameHashCode;
    }

    public Feature conjoinWith(Feature f) {

        byte ampersandAscii = 38;

        if (ascii && this.bytes != null && f.bytes != null) {
            byte[] newBytes = new byte[f.bytes.length + this.bytes.length + 1];

            System.arraycopy(f.bytes, 0, newBytes, 0, f.bytes.length);
            newBytes[f.bytes.length] = ampersandAscii;
            System.arraycopy(this.bytes, 0, newBytes, f.bytes.length + 1, this.bytes.length);

            if (f instanceof DiscreteFeature) {
                if (this instanceof DiscreteFeature)
                    return new DiscreteFeature(newBytes);
                else
                    return new RealFeature(newBytes, this.getValue());
            } else {
                if (this instanceof DiscreteFeature)
                    return new RealFeature(newBytes, f.getValue());
                else
                    return new RealFeature(newBytes, f.getValue() * this.getValue());

            }

        } else {

            String newName = this.getName() + "&" + f.getName();
            if (f instanceof DiscreteFeature) {
                if (this instanceof DiscreteFeature)
                    return new DiscreteFeature(newName);
                else
                    return new RealFeature(newName, this.getValue());
            } else {
                if (this instanceof DiscreteFeature)
                    return new RealFeature(newName, f.getValue());
                else
                    return new RealFeature(newName, f.getValue() * this.getValue());

            }
        }
    }

    public Feature prefixWith(String s) {

        byte colonAscii = 58;

        if (ascii && this.bytes != null) {

            if (keepString) {

                String name = s + ":" + this.name;
                if (this instanceof DiscreteFeature)
                    return new DiscreteFeature(name);
                else
                    return new RealFeature(name, this.getValue());

            } else {
                try {
                    byte[] bytes = s.getBytes(US_ASCII);

                    byte[] newBytes = new byte[bytes.length + this.bytes.length + 1];

                    System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                    newBytes[bytes.length] = colonAscii;
                    System.arraycopy(this.bytes, 0, newBytes, bytes.length + 1, this.bytes.length);

                    if (this instanceof DiscreteFeature)
                        return new DiscreteFeature(newBytes);
                    else
                        return new RealFeature(newBytes, this.getValue());

                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

            }
        } else {
            String name = s + ":" + this.getName();
            if (this instanceof DiscreteFeature)
                return new DiscreteFeature(name);
            else
                return new RealFeature(name, this.getValue());
        }
    }
}
