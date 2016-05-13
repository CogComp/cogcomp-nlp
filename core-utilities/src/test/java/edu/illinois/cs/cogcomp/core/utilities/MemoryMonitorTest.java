/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import junit.framework.TestCase;
import org.junit.Test;

public class MemoryMonitorTest extends TestCase {

    @Test
    public void testNoTest() {
        // not really a test, just shows how to use.
        // kinda obvious, but for sake of completeness.
        if (!MemoryMonitor.almostFull()) {
            System.out.println("not full!");
        }
        System.out.println(MemoryMonitor.memoryUsageInMB());
        MemoryMonitor.printSystemMemory();
    }
}
