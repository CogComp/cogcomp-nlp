/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.search;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Vivek Srikumar
 *         <p>
 *         May 1, 2009
 */
public class BreadthFirstSearch<T> extends GraphSearch<T> {
    public BreadthFirstSearch() {
        super(new LinkedList<T>());
    }

    protected BreadthFirstSearch(Queue<T> queue) {
        super(queue);
    }
}
