package edu.illinois.cs.cogcomp.srl.learn;

public interface PerformanceMeasure extends Comparable<PerformanceMeasure> {
	String summarize();
}