/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.experiments;

/**
 * @author Vivek Srikumar
 *         <p>
 *         Aug 4, 2010
 */
public interface IExperimentFactory<T> {
    IExperiment<T> makeExperiment();
}
