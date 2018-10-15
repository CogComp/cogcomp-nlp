/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.stats;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Counter test: max, sorted items
 * 
 * @author Shyam Upadhyay
 * @author Christos Christodoulopoulos
 * @since 12/10/15.
 */
public class TestCounter {

    @Test
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

        assertEquals("z", cnt.getSortedItems().get(0));
        assertEquals("a", cnt.getSortedItemsHighestFirst().get(0));
    }
}
