package edu.illinois.cs.cogcomp.sim;


/**
 * Generic Interface to be used by classes corresponding to various types of similarity measures.
 *
 * @author mssammon
 * @author ngupta18
 * @author sgupta96
 *
 */
public interface Metric<T> {

	/**
	 * Intended to calculate similarity between two things -- generally, elements from text.
	 *
	 * @param arg1 1st component
	 * @param arg2 2nd component
	 * @return similarity score
	 * @throws IllegalArgumentException
	 */
    MetricResponse compare(T arg1, T arg2) throws IllegalArgumentException;


}
