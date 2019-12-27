/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ScipHookTest {
    @Test
    public void testProgram1() throws Exception {
        ScipHook scipHook = new ScipHook("");
        int[] varInds = new int[2];

        int i = 0;
        while (i < 2) {
            int x = scipHook.addBooleanVariable(-1.0);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        scipHook.addGreaterThanConstraint(varInds, coeffs, -3);
        scipHook.addLessThanConstraint(varInds, coeffs, 4);

        scipHook.setMaximize(false);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(scipHook.objectiveValue() == -2.0);
        assertTrue(scipHook.getBooleanValue(0));
        assertTrue(scipHook.getBooleanValue(1));
    }

    @Test
    public void testProgram2() throws Exception {
        ScipHook scipHook = new ScipHook("");
        int[] varInds = new int[2];

        int i = 0;
        while (i < 2) {
            int x = scipHook.addBooleanVariable(-1.0);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        scipHook.addGreaterThanConstraint(varInds, coeffs, -3);
        scipHook.addLessThanConstraint(varInds, coeffs, 4);

        scipHook.setMaximize(true);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(scipHook.objectiveValue() == 0);
        assertTrue(!scipHook.getBooleanValue(0));
        assertTrue(!scipHook.getBooleanValue(1));
    }

    @Test
    public void testProgram3() throws Exception {
        ScipHook scipHook = new ScipHook("");
        int[] varInds = new int[2];

        int i = 0;
        while (i < 2) {
            int x = scipHook.addBooleanVariable(1.5);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        scipHook.addGreaterThanConstraint(varInds, coeffs, -3);
        scipHook.addLessThanConstraint(varInds, coeffs, 4);

        scipHook.setMaximize(true);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }


        assertTrue(scipHook.objectiveValue() == 3);
        assertTrue(scipHook.getBooleanValue(0));
        assertTrue(scipHook.getBooleanValue(1));
    }

    @Test
    public void testProgram4() throws Exception {
        ScipHook scipHook = new ScipHook("");
        int[] varInds = new int[2];

        int i = 0;
        while (i < 2) {
            int x = scipHook.addBooleanVariable(1.5);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        scipHook.addGreaterThanConstraint(varInds, coeffs, -3);
        scipHook.addLessThanConstraint(varInds, coeffs, 4);

        scipHook.setMaximize(false);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(scipHook.objectiveValue() == 0);
        assertTrue(!scipHook.getBooleanValue(0));
        assertTrue(!scipHook.getBooleanValue(1));
    }

    @Test
    public void testProgram5() throws Exception {
        ScipHook scipHook = new ScipHook("");
        int[] varInds = new int[2];

        double[] objcoeffs = {1.5, 2.5};
        int i = 0;
        while (i < 2) {
            int x = scipHook.addBooleanVariable(objcoeffs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        scipHook.addGreaterThanConstraint(varInds, coeffs, 1);
        scipHook.addLessThanConstraint(varInds, coeffs, 4);

        scipHook.setMaximize(true);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(scipHook.objectiveValue() == 4);
        assertTrue(scipHook.getBooleanValue(0));
        assertTrue(scipHook.getBooleanValue(1));
    }

    @Test
    public void testProgram6() throws Exception {
        ScipHook scipHook = new ScipHook("");
        int[] varInds = new int[2];

        double[] objcoeffs = {1.5, 2.5};
        int i = 0;
        while (i < 2) {
            int x = scipHook.addBooleanVariable(objcoeffs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        scipHook.addGreaterThanConstraint(varInds, coeffs, 1);
        scipHook.addLessThanConstraint(varInds, coeffs, 2);

        scipHook.setMaximize(false);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(scipHook.objectiveValue() == 1.5);
        assertTrue(scipHook.getBooleanValue(0));
        assertTrue(!scipHook.getBooleanValue(1));
    }

    @Test
    public void testProgram7() throws Exception {
        ScipHook scipHook = new ScipHook("");
        int[] varInds = new int[2];

        double[] objcoeffs = {1.5, 2.5};
        int i = 0;
        while (i < 2) {
            int x = scipHook.addBooleanVariable(objcoeffs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        scipHook.addGreaterThanConstraint(varInds, coeffs, 1);
        scipHook.addLessThanConstraint(varInds, coeffs, 2);

        scipHook.setMaximize(true);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(scipHook.objectiveValue() == 2.5);
        assertTrue(!scipHook.getBooleanValue(0));
        assertTrue(scipHook.getBooleanValue(1));
    }

    @Test
    public void testProgram8() throws Exception {
        ScipHook scipHook = new ScipHook("");
        int[] varInds = new int[3];

        double[] objcoeffs = {-1, -1, -1};
        int i = 0;
        while (i < 3) {
            int x = scipHook.addBooleanVariable(objcoeffs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 1, 1};
        scipHook.addEqualityConstraint(varInds, coeffs, 3);
        scipHook.setMaximize(true);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(scipHook.objectiveValue() == -3);
        assertTrue(scipHook.getBooleanValue(0));
        assertTrue(scipHook.getBooleanValue(1));
        assertTrue(scipHook.getBooleanValue(2));
    }

    @Test
    public void testProgram9() throws Exception {
        ScipHook scipHook = new ScipHook("");

        double[] objcoeffs = {0, -1};
        scipHook.addDiscreteVariable(objcoeffs);
        scipHook.addDiscreteVariable(objcoeffs);
        scipHook.addDiscreteVariable(objcoeffs);

        double[] coeffs = {1, 1, 1};
        int[] varInds = {1, 3, 5};
        scipHook.addEqualityConstraint(varInds, coeffs, 3);
        scipHook.setMaximize(true);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(scipHook.objectiveValue() == -3);
        assertTrue(!scipHook.getBooleanValue(0));
        assertTrue(scipHook.getBooleanValue(1));
        assertTrue(!scipHook.getBooleanValue(2));
        assertTrue(scipHook.getBooleanValue(3));
        assertTrue(!scipHook.getBooleanValue(4));
        assertTrue(scipHook.getBooleanValue(5));
    }

    @Test
    public void testProgram10() throws Exception {
        ScipHook scipHook = new ScipHook("");

        double[] objcoeffs = {0, 1, 2};
        int[] indices = scipHook.addDiscreteVariable(objcoeffs);


        scipHook.setMaximize(true);

        try {
            scipHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(indices[0] + "/ " + indices[1] + " / " + indices[2]);

        assertTrue(scipHook.objectiveValue() == 2);
        assertTrue(scipHook.getBooleanValue(indices[2]));
        assertTrue(!scipHook.getBooleanValue(indices[1]));
        assertTrue(!scipHook.getBooleanValue(indices[0]));
    }
}
