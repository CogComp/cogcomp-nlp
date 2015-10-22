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
