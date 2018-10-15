/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.transformers;

import java.io.Serializable;

/**
 * This is a basic interface for all data transformations. It contains one function
 * {@code S transform(T input)}, which takes an object of type T and returns one of type S.
 *
 * @author Vivek Srikumar
 *         <p>
 *         Dec 20, 2008
 */
public abstract class ITransformer<T, S> implements Serializable {

    private static final long serialVersionUID = 7160345473473235729L;

    public abstract S transform(T input);

    @SuppressWarnings("serial")
    public <R> ITransformer<R, S> chain(final ITransformer<R, T> transformer) {
        final ITransformer<T, S> t = this;
        return new ITransformer<R, S>() {
            @Override
            public S transform(R input) {
                return t.transform(transformer.transform(input));
            }
        };
    }
}
