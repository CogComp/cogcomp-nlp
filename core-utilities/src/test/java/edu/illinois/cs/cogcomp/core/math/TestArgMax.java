package edu.illinois.cs.cogcomp.core.math;

import junit.framework.TestCase;

public class TestArgMax extends TestCase {
    public void testArgMax() {
        ArgMax<Integer, Double> argmax = new ArgMax<>(-1, Double.NEGATIVE_INFINITY);

        double[] r = {-1, -30, -4, 1, -4, 94, 19, 1, 10, -19};
        for (int i = 0; i < r.length; i++) {
            argmax.update(i, r[i]);
        }

        assertEquals(5, argmax.getArgmax().intValue());
        assertEquals(94d, argmax.getMaxValue());
    }
}
