/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.sl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaProperties;
import edu.illinois.cs.cogcomp.comma.datastructures.CommaSRLSentence;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.sl.core.SLProblem;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class CommaIOManager {
    public static final String unknownFeature = "unknwonfeature";

    public static SLProblem readProblem(List<CommaSRLSentence> sentences, Lexiconer lexicon,
                                        List<Classifier> lbjExtractors, Classifier lbjLabeler) {
        if (lexicon.isAllowNewFeatures())
            lexicon.addFeature(unknownFeature);
        // lexicon.addLabel("occupy-zero-label-for-some-reason");

        SLProblem sp = new SLProblem();

        // READ PROBLEM
        for (CommaSRLSentence sentence : sentences) {
            List<CommaSequence> commaSequences =
                    getCommaSequences(sentence, lexicon, lbjExtractors);
            for (CommaSequence commaSequence : commaSequences) {
                CommaLabelSequence labelSequence =
                        new CommaLabelSequence(commaSequence, lexicon, lbjLabeler);
                sp.addExample(commaSequence, labelSequence);
            }
        }

        return sp;
    }

    public static List<CommaSequence> getCommaSequences(CommaSRLSentence sentence, Lexiconer lexicon,
                                                        List<Classifier> lbjExtractors) {
        LinkedList<Comma> allCommasInSentence = new LinkedList<>(sentence.getCommas());
        List<CommaSequence> commaSequences = new ArrayList<>();
        boolean isCommaStructureFullSentence =
                CommaProperties.getInstance().isCommaStructureFullSentence();
        if (isCommaStructureFullSentence) {
            commaSequences.add(new CommaSequence(allCommasInSentence, lexicon, lbjExtractors));
        } else {
            while (!allCommasInSentence.isEmpty()) {
                Comma currentComma = allCommasInSentence.pollFirst();
                List<Comma> commasInCurrentStructure = new LinkedList<>();
                commasInCurrentStructure.add(currentComma);
                Iterator<Comma> unusedCommasIt = allCommasInSentence.iterator();
                while (unusedCommasIt.hasNext()) {
                    Comma otherComma = unusedCommasIt.next();
                    if (currentComma.isSibling(otherComma)) {
                        commasInCurrentStructure.add(otherComma);
                        unusedCommasIt.remove();
                    }
                }
                commaSequences.add(new CommaSequence(commasInCurrentStructure, lexicon,
                        lbjExtractors));
            }
        }
        return commaSequences;
    }
}
