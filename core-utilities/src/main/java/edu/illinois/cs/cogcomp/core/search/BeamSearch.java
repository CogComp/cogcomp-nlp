package edu.illinois.cs.cogcomp.core.search;

import edu.illinois.cs.cogcomp.core.datastructures.BoundedPriorityQueue;

import java.util.Comparator;
import java.util.Queue;

/**
 * @author Vivek Srikumar
 *         <p/>
 *         May 1, 2009
 */
public class BeamSearch<T> extends GraphSearch<T> {
    public BeamSearch(int beamSize, Comparator<T> comparator) {
        super(new BoundedPriorityQueue<>(beamSize, comparator));
    }

    protected BeamSearch(Queue<T> queue) {
        super(queue);
    }

}
