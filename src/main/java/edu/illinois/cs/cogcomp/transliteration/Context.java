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

