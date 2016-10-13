/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.wikifier.utils;

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
		System.out.printf(announcement+" memory usage: %sMB\n",memoryUsageInMB());
	}
	
	public static void printSystemMemory(){
	    long heap = bean.getHeapMemoryUsage().getUsed() >> 20;
	    long nonheap = bean.getNonHeapMemoryUsage().getUsed() >> 20;
        System.out.printf("Heap: %dMB;Non-heap %dMB\n", heap, nonheap);
	}

	public static long usedMemory(){
	    Runtime r = Runtime.getRuntime();
	    return r.totalMemory() - r.freeMemory();
	}
	
    public static String memoryUsageInMB() {
        return String.valueOf(usedMemory() >> 20);
    }

	public static void runGC(Runtime r, int loops) throws Exception {
		System.out.println("running garbage collecting");
		for(int i=0; i<loops; i++) {
			r.gc();
			Thread.sleep(1000);
		}
		System.out.println("done running garbage collecting");
	}
}
