/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.wikifier.utils;

public class TimeAndMemMonitor {
	public static long usedMemory()
	{
		Runtime.getRuntime().gc();
		Thread.yield ();
		return  Runtime.getRuntime().totalMemory () -  Runtime.getRuntime().freeMemory ();
	}

}
