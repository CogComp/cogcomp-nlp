/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Created by stephen on 9/23/15.
 */
public class TestPairTriple {

    @Test
    public void testPair() {
        Pair<String, String> p1 = new Pair<>("Hi", "Hello");
        Pair<String, String> p2 = new Pair<>("Hi", "Hello");
        Pair<String, String> p3 = new Pair<>("Hi", "Howdy");

        assertEquals(p1, p2);

        assertNotSame(p1, p3);

    }

    @Test
    public void testTriple() {
        Triple<String, String, Integer> t1 = new Triple<>("Fearlessness", "Rub", 45);
        Triple<String, String, Integer> t2 = new Triple<>("Fearlessness", "Rub", 45);
        Triple<String, String, Integer> t3 = new Triple<>("Fearlessness", "blah blah", 41);

        assertEquals(t1, t2);

        assertNotSame(t1, t3);
    }
}
