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
