package main.java.edu.illinois.cs.cogcomp.wikifier.utils;

public class TimeAndMemMonitor {
	public static long usedMemory()
	{
		Runtime.getRuntime().gc();
		Thread.yield ();
		return  Runtime.getRuntime().totalMemory () -  Runtime.getRuntime().freeMemory ();
	}

}
