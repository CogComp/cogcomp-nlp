/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils;

import edu.illinois.cs.cogcomp.core.stats.Counter;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface defining API for a class that generates a map of example ID to label (or feature) counts.
 * Derived classes should define a constructor that seeds the data structures used to generate these counts.
 *
 * NOTE: this class is mid-refactor; see TODO
 *
 * TODO: change definition of getExampleLabelCounts() to take a generic map of string to example data structure; this will
 *       obviate the need to store the original data set.
 * TODO: assess possibility of moving counting logic from derived classes to this class.
 * @author mssammon
 */
public abstract class LabelCountExtractor<K> {


    protected Map<String, Counter<String>> labelCounts;

    protected Counter<String> labelTotals;

    public LabelCountExtractor(Map<String, K> examples) {
        labelCounts = new HashMap<>();
        labelTotals = new Counter<>();

        populateLabelCounts(examples);
    }


    public Map<String, Counter<String>> getLabelCounts() {
        return labelCounts;
    }

    public Counter<String> getLabelTotals() {
        return labelTotals;
    }


    abstract public void populateLabelCounts(Map<String, K> examples);

    abstract public Counter<String> findTargetCounts(double frac);
}