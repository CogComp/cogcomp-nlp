/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.aceReader;

/**
 * @author Eric Bengtson Encapsulates various exceptions that may arise during XML processing.
 */
public class XMLException extends Exception {
    private static final long serialVersionUID = 8648357765896557182L;

    protected Exception m_nestedException; /* May be null */
    protected String m_error;

    /**
     * Constructor
     * 
     * @param error The relevant error message
     * @param ex The exception to encapsulate
     */
    public XMLException(String error, Exception ex) {
        super(error);
        m_nestedException = ex;
        m_error = error;
    }

    public XMLException(String error) {
        super(error);
        m_nestedException = null;
        m_error = error;
    }

    /**
     * @return The nested exception. null if none given.
     */
    public Exception getNestedException() {
        return m_nestedException;
    }

    public String getMessage() {
        if (m_nestedException != null)
            return m_error + "\nCause:\n  " + m_nestedException.getMessage();
        else
            return m_error;
    }

    public String toString() {
        String errString = this.getClass().getName() + ": " + m_error;
        if (m_nestedException != null)
            errString += "\nCause:\n  " + m_nestedException.getMessage();
        return errString;
    }

}
