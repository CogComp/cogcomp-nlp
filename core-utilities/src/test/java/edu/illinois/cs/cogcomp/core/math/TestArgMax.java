/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.math;

import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestArgMax {

    @Test
    public void testArgMax() {
        ArgMax<Integer, Double> argmax = new ArgMax<>(-1, Double.NEGATIVE_INFINITY);

        double[] r = {-1, -30, -4, 1, -4, 94, 19, 1, 10, -19};
        for (int i = 0; i < r.length; i++) {
            argmax.update(i, r[i]);
        }

        assertEquals(5, argmax.getArgmax().intValue());
        assertEquals(94d, argmax.getMaxValue(), 0.0);
    }

    @Test
    public void testRandomTieBreak() {
        ArgMax<Integer, Double> argmax = new ArgMax<>(-1, Double.NEGATIVE_INFINITY, true);

        double[] r = {-1, -30, -4, 1, -4, 94, 19, 1, 94, -19};

        // Try the update 5 times and check if at least one of them is different
        // NB: Given that the random seed is fixed, this test should always pass
        // (the switch happens on the 2nd iteration)
        for (int i = 0; i < r.length; i++) {
            argmax.update(i, r[i]);
        }
        int argMaxPos = argmax.getArgmax();
        boolean changed = false;
        for (int iters = 0; iters < 5; iters++) {
            for (int i = 0; i < r.length; i++) {
                argmax.update(i, r[i]);
            }
            if (argMaxPos != argmax.getArgmax()) {
                changed = true;
                break;
            }
        }

        assertTrue(changed);
        assertEquals(94d, argmax.getMaxValue(), 0.0);
    }
}
