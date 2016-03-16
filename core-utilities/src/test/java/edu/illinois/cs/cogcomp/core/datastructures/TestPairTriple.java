package edu.illinois.cs.cogcomp.core.datastructures;

import junit.framework.TestCase;

import java.util.*;

/**
 * Created by stephen on 9/23/15.
 */
public class TestPairTriple extends TestCase {

    public void testPair() {
        Pair<String, String> p1 = new Pair<>("Hi", "Hello");
        Pair<String, String> p2 = new Pair<>("Hi", "Hello");
        Pair<String, String> p3 = new Pair<>("Hi", "Howdy");

        assertEquals(p1, p2);

        assertNotSame(p1, p3);

    }


    public void testTriple() {
        Triple<String, String, Integer> t1 = new Triple<>("Fearlessness", "Rub", 45);
        Triple<String, String, Integer> t2 = new Triple<>("Fearlessness", "Rub", 45);
        Triple<String, String, Integer> t3 = new Triple<>("Fearlessness", "blah blah", 41);

        assertEquals(t1, t2);

        assertNotSame(t1, t3);
    }
}
