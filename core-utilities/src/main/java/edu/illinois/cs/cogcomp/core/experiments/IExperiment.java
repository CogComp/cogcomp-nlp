package edu.illinois.cs.cogcomp.core.experiments;

import java.util.List;

/**
 * @author Vivek Srikumar
 *         <p/>
 *         Jan 30, 2009
 */
public interface IExperiment<T> {
    /**
     * Returns the accuracy/precision/recall or some other real valued evaluation of the test set
     * after training on the training set.
     */
    double run(List<T> trainingSet, List<T> testSet) throws Exception;

    String getDescription();

    void setParameters(List<Double> params);
}
