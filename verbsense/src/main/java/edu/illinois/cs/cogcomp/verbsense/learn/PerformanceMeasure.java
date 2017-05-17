package edu.illinois.cs.cogcomp.verbsense.learn;

public interface PerformanceMeasure extends Comparable<PerformanceMeasure> {
    String summarize();
}
