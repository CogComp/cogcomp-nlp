/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.tagger;

/**
 * This interface is used by the text annotation thread to provide continuous flow of data in, and
 * provide a mechanism to publish the results in some way.
 * 
 * @author redman
 */
public interface AnnotationJob {

    /**
     * Get the data to annotate.
     * 
     * @return the text data as a string.
     */
    String getData();

    /**
     * compute the annotations.
     */
    void labelData();

    /**
     * publish the results in whatever way is appropriate, could write to a file, cache the results
     * somewhere, and compile information to produce a report.
     */
    void publishResults();

}
