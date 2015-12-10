package edu.illinois.cs.cogcomp.core.stats;

import junit.framework.TestCase;

/**
 * Created by haowu on 12/10/15.
 */
public class OneVariableStatsTest extends TestCase {

    public void testMean() throws Exception {
        OneVariableStats oneVariableStats = new OneVariableStats();
        assertEquals(0.0, oneVariableStats.mean());
        oneVariableStats.add(1.0);
        oneVariableStats.add(2.0);
        oneVariableStats.add(3.0);
        assertEquals(2.0, oneVariableStats.mean());
    }

    public void testStd() throws Exception {
        OneVariableStats oneVariableStats = new OneVariableStats();
        assertEquals(0.0, oneVariableStats.std());
        oneVariableStats.add(1.0);
        oneVariableStats.add(2.0);
        oneVariableStats.add(3.0);
        oneVariableStats.add(4.0);
        oneVariableStats.add(5.0);
        assertEquals(1.4142, oneVariableStats.std(), 0.001);
    }

    public void testMinMax() throws Exception {
        OneVariableStats oneVariableStats = new OneVariableStats();
        assertEquals(Double.POSITIVE_INFINITY, oneVariableStats.min());
        assertEquals(Double.NEGATIVE_INFINITY, oneVariableStats.max());
        oneVariableStats.add(1.0);
        oneVariableStats.add(2.0);
        oneVariableStats.add(3.0);
        assertEquals(1.0, oneVariableStats.min());
        assertEquals(3.0, oneVariableStats.max());
    }

    public void testStdErr() throws Exception {
        OneVariableStats oneVariableStats = new OneVariableStats();
        assertEquals(0.0, oneVariableStats.stdErr());
        oneVariableStats.add(1.0);
        oneVariableStats.add(2.0);
        oneVariableStats.add(3.0);
        oneVariableStats.add(4.0);
        oneVariableStats.add(5.0);
        assertEquals(0.6324, oneVariableStats.stdErr(), 0.001);
    }

}
