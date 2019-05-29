/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.ilp;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GurobiHookTest {

    @Ignore
    @Test
    public void testAll() throws Exception {
        // if we are running the test on Semaphore, ignore this test, since Gurobi is not provided on Semaphore.
        if (System.getenv().containsKey("CI") && System.getenv().get("CI").equals("true")
                && System.getenv().containsKey("SEMAPHORE")
                && System.getenv().get("SEMAPHORE").equals("true")) {
            System.out.println("Running the test on Semaphore. Skipping this test  . . . ");
        } else {
            testProgram1();
            testProgram2();
            testProgram3();
            testProgram4();
            testProgram5();
            testProgram6();
            testProgram7();
            testProgram8();
            testProgram9();
            testProgram10();
        }
    }

    @Ignore
    public void testProgram1() throws Exception {
        GurobiHook ojaHook = new GurobiHook();
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

        assertTrue(ojaHook.objectiveValue() == -2.0);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
    }

    @Ignore
    public void testProgram2() throws Exception {
        GurobiHook ojaHook = new GurobiHook();
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

        assertTrue(ojaHook.objectiveValue() == 0);
        assertTrue(!ojaHook.getBooleanValue(0));
        assertTrue(!ojaHook.getBooleanValue(1));
    }

    @Ignore
    public void testProgram3() throws Exception {
        GurobiHook ojaHook = new GurobiHook();
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


        assertTrue(ojaHook.objectiveValue() == 3);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
    }

    @Ignore
    public void testProgram4() throws Exception {
        GurobiHook ojaHook = new GurobiHook();
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

        assertTrue(ojaHook.objectiveValue() == 0);
        assertTrue(!ojaHook.getBooleanValue(0));
        assertTrue(!ojaHook.getBooleanValue(1));
    }

    @Ignore
    public void testProgram5() throws Exception {
        GurobiHook ojaHook = new GurobiHook();
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

        assertTrue(ojaHook.objectiveValue() == 4);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
    }

    @Ignore
    public void testProgram6() throws Exception {
        GurobiHook ojaHook = new GurobiHook();
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

        assertTrue(ojaHook.objectiveValue() == 1.5);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(!ojaHook.getBooleanValue(1));
    }

    @Ignore
    public void testProgram7() throws Exception {
        GurobiHook ojaHook = new GurobiHook();
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

        assertTrue(ojaHook.objectiveValue() == 2.5);
        assertTrue(!ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
    }

    @Ignore
    public void testProgram8() throws Exception {
        GurobiHook ojaHook = new GurobiHook();
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

        assertTrue(ojaHook.objectiveValue() == -3);
        assertTrue(ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
        assertTrue(ojaHook.getBooleanValue(2));
    }

    @Ignore
    public void testProgram9() throws Exception {
        GurobiHook ojaHook = new GurobiHook();

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

        assertTrue(ojaHook.objectiveValue() == -3);
        assertTrue(!ojaHook.getBooleanValue(0));
        assertTrue(ojaHook.getBooleanValue(1));
        assertTrue(!ojaHook.getBooleanValue(2));
        assertTrue(ojaHook.getBooleanValue(3));
        assertTrue(!ojaHook.getBooleanValue(4));
        assertTrue(ojaHook.getBooleanValue(5));
    }

    @Ignore
    public void testProgram10() throws Exception {
        GurobiHook ojaHook = new GurobiHook();

        double[] objcoeffs = {0, 1, 2};
        int[] indices = ojaHook.addDiscreteVariable(objcoeffs);


        ojaHook.setMaximize(true);

        try {
            ojaHook.solve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(indices[0] + "/ " + indices[1] + " / " + indices[2]);
        ojaHook.printSolution();
        ojaHook.printModelStatus();

        assertTrue(ojaHook.objectiveValue() == 2);
        assertTrue(ojaHook.getBooleanValue(indices[2]));
        assertTrue(!ojaHook.getBooleanValue(indices[1]));
        assertTrue(!ojaHook.getBooleanValue(indices[0]));
    }

}
