package edu.illinois.cs.cogcomp.core.transformers;

/**
 * @author Vivek Srikumar
 *         <p/>
 *         Jan 6, 2009
 */
public class StringTransformer<T> extends ITransformer<T, String> {

    private static final long serialVersionUID = 6883146968564408233L;

    public String transform(T input) {
        return input.toString();
    }
}
