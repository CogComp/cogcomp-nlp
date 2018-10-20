/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import edu.illinois.cs.cogcomp.core.search.BeamSearch;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * For some reason, Java does not have an implementation of a bounded
 * {@link java.util.PriorityQueue} out of the box. This class fills that gap.
 * <p>
 * Among other places, it is used by the {@link edu.illinois.cs.cogcomp.core.search.BeamSearch}
 * class to maintain the beam.
 *
 * @author Vivek Srikumar
 *         <p>
 *         May 1, 2009
 */
public class BoundedPriorityQueue<E> extends PriorityQueue<E> {

    private static final long serialVersionUID = 5134936172739425621L;

    protected final int maxQueueSize;

    public BoundedPriorityQueue(int maxQueueSize, Comparator<? super E> comparator) {
        super(11, comparator);
        this.maxQueueSize = maxQueueSize;
        if (this.maxQueueSize <= 0) {
            throw new IllegalArgumentException("Maximum queue size should be non negative");
        }
    }

    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }

    @Override
    public boolean add(E o) {
        // first add the new element.
        super.add(o);

        // if the queue crosses the ceiling, drop the largest one.
        if (this.size() == (maxQueueSize + 1)) {
            E largest = this.element();
            for (E item : this) {
                if (this.comparator().compare(largest, item) <= 0) {
                    largest = item;
                }
            }
            remove(largest);
            if (o.equals(largest))
                return false;
        }

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        // worst notation ever.
        boolean value = true;
        for (E e : c) {
            value &= this.add(e);
        }
        return value;
    }
}
