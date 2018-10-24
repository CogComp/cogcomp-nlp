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
 * LabelCounter for examples composed of lists of features + counts.
 *
 * @author mssammon 
 */
public class ListExampleLabelCounter extends LabelCountExtractor<Map<String, Integer>> {



    public ListExampleLabelCounter(Map<String, Map<String, Integer>> examples) {
        super(examples);
    }


    /**
     * given a list of examples as maps of fields to counts, generate counts of each field per example.
     *
     * Unlike {@link TextAnnotationLabelCounter}, assume reader has filtered the fields of interest before
     *   passing them to this class, so that only fields of interest are present.
     * @param examples
     */
    @Override
    public void populateLabelCounts(Map<String, Map<String, Integer>> examples) {

        for (String docId : examples.keySet()) {

            Counter<String> docLabelCount = new Counter<>();
            labelCounts.put(docId, docLabelCount);

            Map<String, Integer> ex = examples.get(docId);

            for (String label : ex.keySet()) {
                docLabelCount.incrementCount(label, ex.get(label));
                labelTotals.incrementCount(label, ex.get(label));
            }
        }
    }


    @Override
    public Map<String, Counter<String>> getLabelCounts() {
        return this.labelCounts;
    }

    @Override
    public Counter<String> getLabelTotals() {
        return this.labelTotals;
    }

    /**
     * find the target counts for labels of interest, given the target fraction
     * @param frac
     * @return
     */
    @Override
    public Counter<String> findTargetCounts(double frac) {
        Counter<String> targetCounts = new Counter<>();
        for (String label : labelTotals.keySet()) {
            double count = this.labelTotals.getCount(label);
            targetCounts.incrementCount(label, Math.ceil(count * frac)); // round up to favor small fractions
        }
        return targetCounts;
    }
}
