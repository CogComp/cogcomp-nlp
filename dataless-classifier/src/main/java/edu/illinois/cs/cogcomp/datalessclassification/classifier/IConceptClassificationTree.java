/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.datalessclassification.util.LabelScorePair;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 * An Inference Interface, to be implemented by all variants of Dataless Classifier
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

public interface IConceptClassificationTree<T extends Serializable> {

    /**
     * Returns a Map, where key is the depth, and value is a list of selected labelIDs at that depth with their absolute similarity scores
     *
     * If a particular implementation wants the end-user to be able to select a particular inference algorithm, this function
     *  should internally redirect to the relevant functions
     */
    Map<Integer, List<LabelScorePair>> getFullDepthPredictions(SparseVector<T> vector);


    /**
     * Return a Map, where key is the Depth, and the value is the Set of selected topK labelIDs at that depth
     *
     * Should ideally call getFullDepthPredictions internally and select the topK labels at each depth.
     *
     * Use this function when you want the depth information associated with the selected labelIDs as well, and want to
     *   limit the number of labels selected at each depth
     */
    Map<Integer, Set<String>> getPrunedDepthPredictions(SparseVector<T> docVector, int topK);

    /**
     * Returns just a flat-bag of selected labelIDs (independent of their depth in the tree)
     */
    Set<String> getFlatPredictions(SparseVector<T> docVector, int topK);
}
