/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class TestBoundedPriorityQueue {

    @Test
    public final void testBoundedPriorityQueue() {
        BoundedPriorityQueue<Integer> queue =
                new BoundedPriorityQueue<>(2, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return (int) Math.signum(o1 - o2);
                    }
                });
        queue.add(5);
        queue.add(4);
        assertEquals(queue.element(), new Integer(4));
        queue.add(3);
        queue.add(2);
        assertEquals(queue.element(), new Integer(2));
    }

}
