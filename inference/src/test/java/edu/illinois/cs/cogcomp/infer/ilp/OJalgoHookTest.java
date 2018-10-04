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

public class OJalgoHookTest {
    @Test
    public void testProgram1() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[2];

        int i = 0;
        while (i < 2) {
            int x = ojaHook.addBooleanVariable(-1.0);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        ojaHook.addGreaterThanConstraint(varInds, coeffs, -3);
        ojaHook.addLessThanConstraint(varInds, coeffs, 4);

        ojaHook.setMaximize(false);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == -2.0);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
    }

    @Test
    public void testProgram2() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[2];

        int i = 0;
        while (i < 2) {
            int x = ojaHook.addBooleanVariable(-1.0);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        ojaHook.addGreaterThanConstraint(varInds, coeffs, -3);
        ojaHook.addLessThanConstraint(varInds, coeffs, 4);

        ojaHook.setMaximize(true);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == 0);
        assertTrue(!ojaHook.getBooleanValue(0));
        assertTrue(!ojaHook.getBooleanValue(1));
    }


    @Test
    public void testProgram3() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[2];

        int i = 0;
        while (i < 2) {
            int x = ojaHook.addBooleanVariable(1.5);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        ojaHook.addGreaterThanConstraint(varInds, coeffs, -3);
        ojaHook.addLessThanConstraint(varInds, coeffs, 4);

        ojaHook.setMaximize(true);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == 3);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
    }

    @Test
    public void testProgram4() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[2];

        int i = 0;
        while (i < 2) {
            int x = ojaHook.addBooleanVariable(1.5);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        ojaHook.addGreaterThanConstraint(varInds, coeffs, -3);
        ojaHook.addLessThanConstraint(varInds, coeffs, 4);

        ojaHook.setMaximize(false);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == 0);
        assertTrue(!ojaHook.getBooleanValue(0));
        assertTrue(!ojaHook.getBooleanValue(1));
    }

    @Test
    public void testProgram5() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[2];

        double[] objcoeffs = {1.5, 2.5};
        int i = 0;
        while (i < 2) {
            int x = ojaHook.addBooleanVariable(objcoeffs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        ojaHook.addGreaterThanConstraint(varInds, coeffs, 1);
        ojaHook.addLessThanConstraint(varInds, coeffs, 4);

        ojaHook.setMaximize(true);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == 4);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
    }

    @Test
    public void testProgram6() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[2];

        double[] objcoeffs = {1.5, 2.5};
        int i = 0;
        while (i < 2) {
            int x = ojaHook.addBooleanVariable(objcoeffs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        ojaHook.addGreaterThanConstraint(varInds, coeffs, 1);
        ojaHook.addLessThanConstraint(varInds, coeffs, 2);

        ojaHook.setMaximize(false);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == 1.5);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(!ojaHook.getBooleanValue(1));
    }

    @Test
    public void testProgram7() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[2];

        double[] objcoeffs = {1.5, 2.5};
        int i = 0;
        while (i < 2) {
            int x = ojaHook.addBooleanVariable(objcoeffs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 2};
        ojaHook.addGreaterThanConstraint(varInds, coeffs, 1);
        ojaHook.addLessThanConstraint(varInds, coeffs, 2);

        ojaHook.setMaximize(true);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == 2.5);
        assertTrue(!ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
    }

    @Test
    public void testProgram8() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[3];

        double[] objcoeffs = {-1, -1, -1};
        int i = 0;
        while (i < 3) {
            int x = ojaHook.addBooleanVariable(objcoeffs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coeffs = {1, 1, 1};
        ojaHook.addEqualityConstraint(varInds, coeffs, 3);
        ojaHook.setMaximize(true);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == -3);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
        assertTrue(ojaHook.getBooleanValue(2));
    }

    @Test
    public void testProgram9() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();

        double[] objcoeffs = {0, -1};
        ojaHook.addDiscreteVariable(objcoeffs);
        ojaHook.addDiscreteVariable(objcoeffs);
        ojaHook.addDiscreteVariable(objcoeffs);

        double[] coeffs = {1, 1, 1};
        int[] varInds = {1, 3, 5};
        ojaHook.addEqualityConstraint(varInds, coeffs, 3);
        ojaHook.setMaximize(true);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ojaHook.printModelInfo();

        assertTrue(ojaHook.objectiveValue() == -3);
        assertTrue(!ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
        assertTrue(!ojaHook.getBooleanValue(2));
        assertTrue(ojaHook.getBooleanValue(3));
        assertTrue(!ojaHook.getBooleanValue(4));
        assertTrue(ojaHook.getBooleanValue(5));
    }
}
