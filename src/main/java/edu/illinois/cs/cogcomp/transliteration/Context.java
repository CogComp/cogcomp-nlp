package edu.illinois.cs.cogcomp.transliteration;


public class Context {
    public Context(String leftContext, String rightContext) {
        this.leftContext = leftContext;
        this.rightContext = rightContext;
    }

    public String leftContext;
    public String rightContext;
}

