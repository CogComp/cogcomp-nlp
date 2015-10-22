package edu.illinois.cs.cogcomp.core.transformers;

/**
 * This interface is equivalent to a transformer that does not return anything.
 * It is defined explicitly to emphasize the difference.
 *
 * @author Vivek Srikumar Dec 20, 2008
 */
public abstract class IMethod<T> extends ITransformer<T, Void> {

    private static final long serialVersionUID = -7693075007156222539L;

    public abstract Void transform(T argument);
}
