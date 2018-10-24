/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import org.junit.Test;

import static org.junit.Assert.*;

public class BeamSearchTest {

    @Test
    public void testBeam() throws Exception {
        ILPSolver solver = new BeamSearch(2);
        //
        // max x + y + 2z,
        //
        // subject to
        // x + 2 y + 3 z <= 4
        // x + y >= 1
        // x, y, z binary.

        int x = solver.addBooleanVariable(1);
        int y = solver.addBooleanVariable(1);
        int z = solver.addBooleanVariable(2);

        solver.addLessThanConstraint(new int[] {x, y, z}, new double[] {1, 2, 3}, 4d);

        solver.addGreaterThanConstraint(new int[] {x, y}, new double[] {1, 1}, 1);

        solver.setMaximize(true);

        solver.solve();

        if (solver.isSolved()) {
            assertEquals(true, solver.getBooleanValue(x));
            assertEquals(false, solver.getBooleanValue(y));
            assertEquals(true, solver.getBooleanValue(z));
        } else {
            fail("Couldn't solve the problem");
        }
    }
}
