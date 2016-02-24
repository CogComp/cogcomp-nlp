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
