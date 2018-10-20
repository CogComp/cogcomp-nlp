/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

// This file used to be called ContextualWord
// This used to be a struct.

public class Context {
    public Context(String leftContext, String rightContext) {
        this.leftContext = leftContext;
        this.rightContext = rightContext;
    }

    public String leftContext;
    public String rightContext;
}

