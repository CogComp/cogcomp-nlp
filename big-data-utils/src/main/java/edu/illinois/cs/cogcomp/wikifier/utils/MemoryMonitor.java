/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * 
 * @author cheng88
 *
 */
public class MemoryMonitor {
    
    private static final MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
    public static final long memLimit = Runtime.getRuntime().maxMemory();
	private static final Logger logger = LoggerFactory.getLogger(MemoryMonitor.class);
    /**
     * 
     * @return whether used memory is more than 75% of max available
     * to JVM
     */
    public static boolean almostFull() {
        return memLimit * 3 / 4 < usedMemory();
    }
    
	public static void printMemoryUsage(String announcement){
//		runGC(r,10);
		logger.info(announcement+" memory usage: {}MB\n",memoryUsageInMB());
	}
	
	public static void printSystemMemory(){
	    long heap = bean.getHeapMemoryUsage().getUsed() >> 20;
	    long nonheap = bean.getNonHeapMemoryUsage().getUsed() >> 20;
        logger.info("Heap: {}MB;Non-heap {}MB\n", heap, nonheap);
	}

	public static long usedMemory(){
	    Runtime r = Runtime.getRuntime();
	    return r.totalMemory() - r.freeMemory();
	}
	
    public static String memoryUsageInMB() {
        return String.valueOf(usedMemory() >> 20);
    }

	public static void runGC(Runtime r, int loops) throws Exception {
		logger.info("running garbage collecting");
		for(int i=0; i<loops; i++) {
			r.gc();
			Thread.sleep(1000);
		}
		logger.info("done running garbage collecting");
	}
}
