/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.stats;

import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by haowu on 12/10/15.
 */
public class OneVariableStatsTest {

    @Test
    public void testMean() throws Exception {
        OneVariableStats oneVariableStats = new OneVariableStats();
        assertEquals(0.0, oneVariableStats.mean(), 0.0);
        oneVariableStats.add(1.0);
        oneVariableStats.add(2.0);
        oneVariableStats.add(3.0);
        assertEquals(2.0, oneVariableStats.mean(), 0.0);
    }

    @Test
    public void testStd() throws Exception {
        OneVariableStats oneVariableStats = new OneVariableStats();
        assertEquals(0.0, oneVariableStats.std(), 0.0);
        oneVariableStats.add(1.0);
        oneVariableStats.add(2.0);
        oneVariableStats.add(3.0);
        oneVariableStats.add(4.0);
        oneVariableStats.add(5.0);
        assertEquals(1.4142, oneVariableStats.std(), 0.001);
    }

    @Test
    public void testMinMax() throws Exception {
        OneVariableStats oneVariableStats = new OneVariableStats();
        assertEquals(Double.POSITIVE_INFINITY, oneVariableStats.min(), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, oneVariableStats.max(), 0.0);
        oneVariableStats.add(1.0);
        oneVariableStats.add(2.0);
        oneVariableStats.add(3.0);
        assertEquals(1.0, oneVariableStats.min(), 0.0);
        assertEquals(3.0, oneVariableStats.max(), 0.0);
    }

    @Test
    public void testStdErr() throws Exception {
        OneVariableStats oneVariableStats = new OneVariableStats();
        assertEquals(0.0, oneVariableStats.stdErr(), 0.0);
        oneVariableStats.add(1.0);
        oneVariableStats.add(2.0);
        oneVariableStats.add(3.0);
        oneVariableStats.add(4.0);
        oneVariableStats.add(5.0);
        assertEquals(0.6324, oneVariableStats.stdErr(), 0.001);
    }

}
