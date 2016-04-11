package edu.illinois.cs.cogcomp.core.experiments;

/**
 * @author Vivek Srikumar
 *         <p>
 *         Aug 4, 2010
 */
public interface IExperimentFactory<T> {
    IExperiment<T> makeExperiment();
}
