/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.math;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static edu.illinois.cs.cogcomp.core.math.MathUtilities.*;
import static edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities.asDoubleList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestMathUtilities {

    List<Double> doubleList1;
    double[] a1;

    List<Integer> intList1;
    List<Double> emptyDoubleList;

    @Before
    public void setUp() throws Exception {

        doubleList1 = new ArrayList<>();
        intList1 = new ArrayList<>();

        a1 = new double[] {3, 1, 3, 5, 1, -1, 51, 4};

        for (double d : a1) {
            doubleList1.add(d);
            intList1.add((int) d);
        }

        emptyDoubleList = new ArrayList<>();

    }

    @Test
    public void testMin() {
        assertEquals(min(a1), new Pair<>(5, -1.0));

        assertEquals(min(doubleList1), new Pair<>(5, -1.0));

        assertEquals(min(intList1), new Pair<>(5, -1));

        assertEquals(min(emptyDoubleList), new Pair<Integer, Double>(-1, null));
    }

    @Test
    public void testMax() {

        assertEquals(max(a1), new Pair<>(6, 51.0));

        assertEquals(max(doubleList1), new Pair<>(6, 51.0));

        assertEquals(max(intList1), new Pair<>(6, 51));

        assertEquals(max(emptyDoubleList), new Pair<Integer, Double>(-1, null));

    }

    @Test
    public void testSoftmax() {

        List<Double> l = new ArrayList<>();
        l.add(1.0);

        for (double d : softmax(l))
            assertEquals(d, 1.0, 0.0);

        l.add(1.0);

        for (double d : softmax(l))
            assertEquals(d, 0.5, 0.0);

        l.add(1.0);
        l.add(1.0);

        for (double d : softmax(l))
            assertEquals(d, 0.25, 0.0);

        for (double d : softmax(new double[] {10, 100}, new double[] {100, 10}))
            assertEquals(d, 0.5, 0.0);

    }

    @Test
    public void testLogAdd() {
        assertEquals(logAdd(Double.NEGATIVE_INFINITY, 1), 1.0, 0.0);
        assertEquals(logAdd(10, Double.NEGATIVE_INFINITY), 10.0, 0.0);

        double[] d = new double[] {-15, -1, 0, 1, 3};
        for (double x : d) {
            for (double y : d) {
                double l1 = logAdd(x, y);
                double l2 = Math.log(Math.exp(x) + Math.exp(y));

                assertEquals(true, epsilonEquals(l1, l2));
            }
        }
    }

    @Test
    public void testLogAddCollection() {
        assertEquals(logAdd(new double[] {Double.NEGATIVE_INFINITY, 1}), 1.0, 0.0);
        assertEquals(logAdd(new double[] {10, Double.NEGATIVE_INFINITY}), 10.0, 0.0);

        double[] d = new double[] {-15, -1, 0, 1, 3};
        for (double x : d) {
            for (double y : d) {
                for (double z : d) {
                    double[] array = new double[] {x, y, z};
                    double l1 = logAdd(array);

                    double l2 = logAdd(asDoubleList(array));

                    double l3 = Math.log(Math.exp(x) + Math.exp(y) + Math.exp(z));

                    assertEquals(true, epsilonEquals(l1, l2));
                    assertEquals(true, epsilonEquals(l1, l3));
                }
            }
        }
    }

    @Test
    public void testLogGamma() {

        assertTrue(epsilonEquals(lnGamma(6 + 1), Math.log(720)));

        // gamma(0.5) = sqrt(pi)
        assertTrue(epsilonEquals(lnGamma(0.5), Math.log(Math.sqrt(Math.PI))));

        // gamma(z+1) = z gamma(z)
        for (double d : new double[] {0.1, 0.3, 1.0, 2.0, 5.0, 10, 100}) {
            assertTrue(epsilonEquals(lnGamma(1 + d), Math.log(d) + lnGamma(d)));

        }

        // some values from wolfram alpha
        assertTrue(epsilonEquals(lnGamma(Math.PI), 0.82769459232343));
    }

    @Test
    public void testLogFactorial() {
        myAssertEpsilonEquals(lnFactorial(6), Math.log(720));
        myAssertEpsilonEquals(lnFactorial(3), Math.log(6));
        myAssertEpsilonEquals(lnFactorial(1), 0);
    }

    private void myAssertEpsilonEquals(double d1, double d2) {
        assertTrue(epsilonEquals(d1, d2));
    }

    @Test
    public void testBinomialCoeffs() {
        int[] c = new int[] {1, 5, 10, 10, 5, 1};
        for (int i = 0; i <= 5; i++) {
            assertEquals(binomialCoeffs(5, i), c[i]);
        }
    }

    @Test
    public void testBeta() {

        // beta is symmetric
        assertTrue(beta(1, 2) == beta(2, 1));
        assertTrue(beta(4, 3) == beta(3, 4));

        // some values from wolfram alpha
        myAssertEpsilonEquals(beta(1, 2), 0.5);
        myAssertEpsilonEquals(beta(3, Math.PI), 2 / (Math.PI * (Math.PI + 1) * (Math.PI + 2)));
    }

    @Test
    public void testIncompleteGamma() {
        // P(a,0) = 0, Q(a,0) =1

        assertTrue(incompleteGammaP(1, 0) == 0);
        assertTrue(incompleteGammaP(100, 0) == 0);

        myAssertEpsilonEquals(incompleteGammaQ(1, 0), 1);
        myAssertEpsilonEquals(incompleteGammaQ(100, 0), 1);

        // P(a,Inf) = 1, Q(a, Inf) =0
        myAssertEpsilonEquals(incompleteGammaP(1, 1.0e20), 1);
        myAssertEpsilonEquals(incompleteGammaP(100, 1.0e20), 1);

        myAssertEpsilonEquals(incompleteGammaQ(1, 1.0e20), 0);
        myAssertEpsilonEquals(incompleteGammaQ(100, 1.0e20), 0);

        // P(1,x) = 1 - exp(-x)

        myAssertEpsilonEquals(incompleteGammaP(1, 1), 1 - Math.exp(-1));
        myAssertEpsilonEquals(incompleteGammaP(1, 0.01), 1 - Math.exp(-0.01));
        myAssertEpsilonEquals(incompleteGammaP(1, 100), 1 - Math.exp(-100));

        // Q(1,x) = exp(-x)

        myAssertEpsilonEquals(incompleteGammaQ(1, 1), Math.exp(-1));
        myAssertEpsilonEquals(incompleteGammaQ(1, 0.01), Math.exp(-0.01));
        myAssertEpsilonEquals(incompleteGammaQ(1, 100), Math.exp(-100));

    }

    @Test
    public void testErf() {
        // erf(0) = 0, erfc(0) = 1
        myAssertEpsilonEquals(erf(0), 0);
        myAssertEpsilonEquals(erfc(0), 1);

        // erf(Inf) = 1, erfc(Inf) = 0
        myAssertEpsilonEquals(erf(1e20), 1);
        myAssertEpsilonEquals(erfc(1e20), 0);

        // erf(-x) = -erf(x)
        // erfc(-x) = 2 - erfc(x)
        for (double x : new double[] {0.001, 0.01, 0.1, 1, 10}) {
            myAssertEpsilonEquals(erf(-x), -erf(x));
            myAssertEpsilonEquals(erfc(-x), 2 - erfc(x));
        }

        // Test some values
        myAssertEpsilonEquals(erf(0.1), 0.1124629);
        myAssertEpsilonEquals(erf(0.75), 0.7111556);

        myAssertEpsilonEquals(erfc(0.5), 0.4795001);
        myAssertEpsilonEquals(erfc(0.3), 0.6713732);

    }
}
