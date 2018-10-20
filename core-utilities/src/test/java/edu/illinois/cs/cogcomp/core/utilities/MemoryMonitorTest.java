/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryMonitorTest extends TestCase {
    private static Logger logger = LoggerFactory.getLogger(MemoryMonitorTest.class);

    @Test
    public void testNoTest() {
        // not really a test, just shows how to use.
        // kinda obvious, but for sake of completeness.
        if (!MemoryMonitor.almostFull()) {
            logger.info("not full!");
        }
        logger.info(MemoryMonitor.memoryUsageInMB());
        MemoryMonitor.printSystemMemory();
    }
}
