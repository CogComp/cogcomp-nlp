/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.transformers;

/**
 * @author Vivek Srikumar
 *         <p>
 *         Jan 6, 2009
 */
public class StringTransformer<T> extends ITransformer<T, String> {

    private static final long serialVersionUID = 6883146968564408233L;

    public String transform(T input) {
        return input.toString();
    }
}
