/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

/**
 * @author Vivek Srikumar
 */
public class EdisonException extends Exception {

    private static final long serialVersionUID = -5080029198341850702L;

    public EdisonException(String message) {
        super(message);
    }

    public EdisonException(Exception inner) {
        super(inner);
    }

    public EdisonException(String message, Exception e) {
        super(message, e);
    }

}
