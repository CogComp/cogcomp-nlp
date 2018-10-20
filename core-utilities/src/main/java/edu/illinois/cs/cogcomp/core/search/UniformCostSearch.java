/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.search;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author Vivek Srikumar
 *         <p>
 *         May 1, 2009
 */
public class UniformCostSearch<T> extends GraphSearch<T> {
    public UniformCostSearch(Comparator<T> comparator) {
        super(new PriorityQueue<>(11, comparator));
    }

    protected UniformCostSearch(Queue<T> queue) {
        super(queue);
    }

}
