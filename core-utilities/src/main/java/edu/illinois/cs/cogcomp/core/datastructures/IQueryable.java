/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;

import java.util.Comparator;

/**
 * An interface that allows SQL like querying over objects. This is inspired by C#'s linq. Any
 * iterator that implements this interface should allow querying over the elements of the iterator.
 *
 * @param <T> The type of objects in the queryable iterator
 * @author Vivek Srikumar
 */
public interface IQueryable<T> extends Iterable<T> {

    /**
     * Get a queryable iterator that contains the elements of this iterator which satisfy the
     * condition.
     *
     * @param condition A {@link edu.illinois.cs.cogcomp.core.transformers.Predicate}, which takes
     *        an object of type T and returns true/false.
     * @return A queryable iterator containing only those elements for which the condition is true.
     */
    IQueryable<T> where(Predicate<T> condition);

    /**
     * Returns a queryable iterator, where the elements of this iterator are sorted using the
     * comparator.
     *
     * @param comparator The comparator for type T
     * @return A sorted queryable iterator
     */
    IQueryable<T> orderBy(Comparator<T> comparator);

    /**
     * Given a transformer that converts objects of type T to those of type S, this function returns
     * a new queryable iterator, containing objects of type S which are created by applying the
     * transformer to each element of this iterator.
     *
     * @param <S> The type of the elements of the returned iterator
     * @param transformer An {@code ITransformer} that converts objects of type T to those of type S
     * @return A queryable iterator containing objects of type S.
     */
    <S> IQueryable<S> select(ITransformer<T, S> transformer);

    /**
     * Get a queryable iterator containing the unique elements of this iterator.
     */
    IQueryable<T> unique();

    /**
     * Get the number of records in this iterator.
     */
    int count();

}
