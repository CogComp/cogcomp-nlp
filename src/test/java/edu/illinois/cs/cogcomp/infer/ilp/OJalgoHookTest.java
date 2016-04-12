package edu.illinois.cs.cogcomp.infer.ilp;

import org.junit.Test;

import static org.junit.Assert.*;

public class OJalgoHookTest {
    @Test
    public void testProgram1() throws Exception {
        OJalgoHook ojaHook = new OJalgoHook();
        int[] varInds = new int[2];

        int i = 0;
        while (i< 2) {
            int x = ojaHook.addBooleanVariable(-1.0);
            varInds[i] = x;
            i++;
        }

        double[] coefs = { 1, 2 };
        ojaHook.addGreaterThanConstraint(varInds, coefs, -3);
        ojaHook.addLessThanConstraint(varInds, coefs, 4);

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
        while (i< 2) {
            int x = ojaHook.addBooleanVariable(-1.0);
            varInds[i] = x;
            i++;
        }

        double[] coefs = { 1, 2 };
        ojaHook.addGreaterThanConstraint(varInds, coefs, -3);
        ojaHook.addLessThanConstraint(varInds, coefs, 4);

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
        while (i< 2) {
            int x = ojaHook.addBooleanVariable(1.5);
            varInds[i] = x;
            i++;
        }

        double[] coefs = { 1, 2 };
        ojaHook.addGreaterThanConstraint(varInds, coefs, -3);
        ojaHook.addLessThanConstraint(varInds, coefs, 4);

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
        while (i< 2) {
            int x = ojaHook.addBooleanVariable(1.5);
            varInds[i] = x;
            i++;
        }

        double[] coefs = { 1, 2 };
        ojaHook.addGreaterThanConstraint(varInds, coefs, -3);
        ojaHook.addLessThanConstraint(varInds, coefs, 4);

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

        double[] objCoefs = {1.5, 2.5};
        int i = 0;
        while (i< 2) {
            int x = ojaHook.addBooleanVariable(objCoefs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coefs = { 1, 2 };
        ojaHook.addGreaterThanConstraint(varInds, coefs, 1);
        ojaHook.addLessThanConstraint(varInds, coefs, 4);

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

        double[] objCoefs = {1.5, 2.5};
        int i = 0;
        while (i< 2) {
            int x = ojaHook.addBooleanVariable(objCoefs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coefs = { 1, 2 };
        ojaHook.addGreaterThanConstraint(varInds, coefs, 1);
        ojaHook.addLessThanConstraint(varInds, coefs, 2);

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

        double[] objCoefs = {1.5, 2.5};
        int i = 0;
        while (i< 2) {
            int x = ojaHook.addBooleanVariable(objCoefs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coefs = { 1, 2 };
        ojaHook.addGreaterThanConstraint(varInds, coefs, 1);
        ojaHook.addLessThanConstraint(varInds, coefs, 2);

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

        double[] objCoefs = {-1, -1, -1};
        int i = 0;
        while (i< 3) {
            int x = ojaHook.addBooleanVariable(objCoefs[i]);
            varInds[i] = x;
            i++;
        }

        double[] coefs = { 1, 1, 1};
        ojaHook.addEqualityConstraint(varInds, coefs, 3);
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

        double[] objCoefs = {0, -1};
        ojaHook.addDiscreteVariable(objCoefs);
        ojaHook.addDiscreteVariable(objCoefs);
        ojaHook.addDiscreteVariable(objCoefs);

        double[] coefs = { 1, 1, 1};
        int[] varInds = {1, 3, 5};
        ojaHook.addEqualityConstraint(varInds, coefs, 3);
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