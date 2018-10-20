/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.lbjava.nlp;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;


/**
 * Use this class to represent a natural language document.
 * <code>SentenceSplitter</code> and <code>Sentence.wordSplit()</code> are
 * used to represent the text of the document internally as a collection of
 * vectors of words.  As such, the text of the document is assumed plain,
 * i.e. there should not be any mark-up.
 *
 * @author Nick Rizzolo
 **/
public class NLDocument extends LinkedVector {
    /**
     * The name of the file this document came from.
     */
    private String fileName;


    /**
     * This constructor takes the entire text of the document in a String array
     * as input and initializes the representation.
     *
     * @param text The entire text of the document.  Each element of this array
     *             should represent a line of input without any line
     *             termination characters.
     **/
    public NLDocument(String[] text) {
        this(null, text);
    }


    /**
     * This constructor takes the entire text of the document in a String array
     * as input and initializes the representation.
     *
     * @param p    The previous child in the parent vector.
     * @param text The entire text of the document.  Each element of this array
     *             should represent a line of input without any line
     *             termination characters.
     **/
    public NLDocument(NLDocument p, String[] text) {
        super(p);
        addAll(new SentenceSplitter(text));
    }


    /**
     * Creates a document from the contents of the named file.
     *
     * @param file The name of the file containing a natural language, plain
     *             text document.
     **/
    public NLDocument(String file) {
        this(null, file);
    }


    /**
     * Creates a document from the contents of the named file.
     *
     * @param p    The previous child in the parent vector.
     * @param file The name of the file containing a natural language, plain
     *             text document.
     **/
    public NLDocument(NLDocument p, String file) {
        super(p);
        fileName = file;
        addAll(new SentenceSplitter(file));
    }


    /**
     * Returns the name of the file this document came from, or
     * <code>null</code> if one was not specified.
     **/
    public String getFileName() {
        return fileName;
    }


    /**
     * Adds all the sentences that come from the argument sentence splitter to
     * this document after using a word splitter to chop them up.
     *
     * @param splitter A sentence splitter.
     **/
    public void addAll(SentenceSplitter splitter) {
        Sentence[] rawSentences = splitter.splitAll();
        for (Sentence rawSentence : rawSentences) add(rawSentence.wordSplit());
    }
}

