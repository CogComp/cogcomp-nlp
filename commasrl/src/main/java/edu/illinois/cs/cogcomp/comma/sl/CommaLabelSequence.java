/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.sl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class CommaLabelSequence implements IStructure {
    public final List<String> labels;
    public final int[] labelIds;

    /**
     * This constructor is used while constructing an SLProblem
     * 
     * @param commaSequence
     * @param lexicon
     */
    public CommaLabelSequence(CommaSequence commaSequence, Lexiconer lexicon, Classifier lbjLabeler) {
        labels = new ArrayList<>();
        for (Comma comma : commaSequence.sortedCommas)
            labels.add(lbjLabeler.discreteValue(comma));

        labelIds = new int[labels.size()];
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            if (lexicon.isAllowNewFeatures()) {
                lexicon.addLabel(label);
            }
            labelIds[i] = lexicon.getLabelId(labels.get(i));
        }
    }

    /**
     * This constructor is used by the inference solver which is only uses label-ids
     * 
     * @param labelIds the label-ids corresponding to the labels in the lexicon such that the labels
     * @param lexicon THe lexicon used to obtain the label-ids
     */
    public CommaLabelSequence(int[] labelIds, Lexiconer lexicon) {
        this.labelIds = labelIds;

        labels = new ArrayList<>();
        for (int labelId : labelIds) {
            labels.add(lexicon.getLabelString(labelId));
        }
    }

    @Override
    public boolean equals(Object aThat) {

        // check for self-comparison
        if (this == aThat)
            return true;

        if (!(aThat instanceof CommaLabelSequence))
            return false;

        // cast to native object is now safe
        CommaLabelSequence that = (CommaLabelSequence) aThat;

        // check if their tags are the same
        return this.labels.equals(that.labels) && Arrays.equals(labelIds, that.labelIds);
    }

    @Override
    public int hashCode() {
        return labels.hashCode() * 37 + labelIds.hashCode();
    }
}
