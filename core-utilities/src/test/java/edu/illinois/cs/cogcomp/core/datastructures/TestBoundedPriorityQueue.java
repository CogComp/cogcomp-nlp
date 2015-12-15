package edu.illinois.cs.cogcomp.core.datastructures;

import junit.framework.TestCase;

import java.util.Comparator;

public class TestBoundedPriorityQueue extends TestCase {

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
