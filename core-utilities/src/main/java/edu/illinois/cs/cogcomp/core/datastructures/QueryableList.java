/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import edu.illinois.cs.cogcomp.core.algorithms.Mappers;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;

import java.util.*;

/**
 * A list that can be queried. See {@link edu.illinois.cs.cogcomp.core.datastructures.IQueryable}.
 *
 * @author Vivek Srikumar
 */
public class QueryableList<T> extends ArrayList<T> implements IQueryable<T> {

    private static final long serialVersionUID = -2721183516028986040L;

    public QueryableList(List<T> constituentList) {
        super();
        for (T c : constituentList)
            add(c);
    }

    public QueryableList() {
        super();
    }

    public IQueryable<T> where(Predicate<T> condition) {
        QueryableList<T> output = new QueryableList<>();
        for (T c : this) {
            if (condition.transform(c))
                output.add(c);
        }
        return output;
    }

    public IQueryable<T> orderBy(Comparator<T> comparator) {
        Collections.sort(this, comparator);
        return this;
    }

    public <S> IQueryable<S> select(ITransformer<T, S> transformer) {
        return new QueryableList<>(Mappers.map(this, transformer));
    }

    public IQueryable<T> unique() {
        Set<T> uniq = new HashSet<>();
        uniq.addAll(this);

        QueryableList<T> output = new QueryableList<>();
        output.addAll(uniq);
        return output;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public int count() {
        return size();
    }
}
