/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.corpusreaders.corpusutils;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.stats.Counter;

import java.util.*;

/**
 * counts number of TextAnnotation Constituent labels and Relation labels that are present in a collection
 *    of TextAnnotations, building a map of TextAnnotation id to target label counts.
 *
 * @author mssammon
 */
public class TextAnnotationLabelCounter extends LabelCountExtractor<Set<View>> {

    private final HashSet<String> labelsToConsider;
    private final boolean useAllLabels;

    /**
     * A label counter
     * @param useAllLabels if true, generate counts for every constituent label and relation label
     * @param labels if not empty, a subset of relation and constituent names in the view that should be used
     *               to generate counts
     */
    public TextAnnotationLabelCounter(boolean useAllLabels, String[] labels, Map<String, Set<View>> annotationViews) {
        super(annotationViews);
        this.useAllLabels = useAllLabels;
        this.labelsToConsider = new HashSet<>();
        this.labelsToConsider.addAll(Arrays.asList(labels));
    }


    /**
     * generate the target label/feature counts.
     * @param annotationViews map from doc id to set of views containing the annotations (constituents, relations)
     *                        that will be split.
     */
    @Override
    public void populateLabelCounts(Map<String, Set<View>> annotationViews) {
        for (String docId : annotationViews.keySet()) {
            Counter<String> docLabelCount = new Counter<>();
            labelCounts.put(docId, docLabelCount);
            for (View v : annotationViews.get(docId)) {
                for (Relation r : v.getRelations()) {
                    String label = r.getRelationName();
                    if (useAllLabels || labelsToConsider.contains(label)) {
                        docLabelCount.incrementCount(label);
                        labelTotals.incrementCount(label);
                    }
                }
                for (Constituent c: v.getConstituents()) {
                    String label = c.getLabel();
                    if (useAllLabels || labelsToConsider.contains(label)) {
                        docLabelCount.incrementCount(label);
                        labelTotals.incrementCount(label);
                    }
                }
            }
        }

    }


    /**
     * find the target counts for labels of interest, given the target fraction
     * @param frac
     * @return
     */
    @Override
    public Counter<String> findTargetCounts(double frac) {
        Counter<String> targetCounts = new Counter<>();
        for (String label : labelsToConsider) {
            double count = this.labelTotals.getCount(label);
            targetCounts.incrementCount(label, Math.ceil(count * frac)); // round up to favor small fractions
        }
        return targetCounts;
    }




}
