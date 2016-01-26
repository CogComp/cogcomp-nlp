package edu.illinois.cs.cogcomp.core.stats;

import junit.framework.TestCase;

/**
 * testing Counter.min() and .max() behavior.
 * Created by Shyam on 12/10/15.
 */
public class TestCounter extends TestCase {
    public void testCounter() {
        Counter<String> cnt = new Counter<>();
        cnt.incrementCount("a");
        cnt.incrementCount("a");
        cnt.incrementCount("z");
        cnt.incrementCount("a");
        cnt.incrementCount("a");
        cnt.incrementCount("a");
        cnt.incrementCount("c");
        cnt.incrementCount("c");
        cnt.incrementCount("c");
        cnt.incrementCount("c");
        assertEquals("a", cnt.getMax().getFirst());
        assertEquals("z", cnt.getMin().getFirst());
    }
}
