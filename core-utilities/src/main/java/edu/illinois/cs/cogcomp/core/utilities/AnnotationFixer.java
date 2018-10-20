/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

import java.util.*;

/**
 * Given a TextAnnotation that stores some possibly incorrect information (initially, sentence boundaries), allow
 *    corrections given a view containing information asserted (by the client) to be correct. (E.g. gold annotations
 *    read from a file, which might conflict with automatic tokenization)
 *
 * @author mssammon
 */
public class AnnotationFixer {

    /**
     * correct automated annotations (tokenization, sentence splitting) based on gold annotations of entity mentions
     * @param ta TextAnnotation with annotated clean text
     * @return a map of view names to indexes indicating where violations were found/corrected
     */
    public static Map<String, Set<Integer>> rationalizeBoundaryAnnotations(TextAnnotation ta, String viewName) {

        Map<String, Set<Integer>> violations = new HashMap<>();
        Set<Integer> badSentenceStartIndexes = new HashSet<>();
        violations.put(ViewNames.SENTENCE, badSentenceStartIndexes);

        View sentences = ta.getView(ViewNames.SENTENCE);
        TreeMap<Integer, Constituent> sentenceStarts = new TreeMap<>();
        for (Constituent s : sentences)
            sentenceStarts.put(s.getStartSpan(), s);

        Set<Pair<Constituent, Constituent>> sentencesToMerge = new HashSet<>();

        View nerMention = ta.getView(viewName);

        for (Constituent m : nerMention.getConstituents()) {

            Constituent lastSent = null;

            for (int sentStart : sentenceStarts.keySet()) {

                int mentEnd = m.getEndSpan();

                if (sentStart > mentEnd) // ordered sentence list, so stop after
                    break;

                Constituent currentSent = sentenceStarts.get(sentStart);
                int mentStart = m.getStartSpan();

                if (sentStart > mentStart && sentStart < mentEnd) {
                    sentencesToMerge.add(new Pair(lastSent, currentSent));
                    badSentenceStartIndexes.add(sentStart);
                }

                lastSent = currentSent;
            }
        }
        Set<Integer> sentStartsProcessed = new HashSet<>();
        for (Pair<Constituent, Constituent> sentPair : sentencesToMerge) {
            Constituent first = sentPair.getFirst();
            Constituent second = sentPair.getSecond();
            int firstStart = first.getStartSpan();
            int secondStart = second.getStartSpan();
            if (sentStartsProcessed.contains(firstStart) || sentStartsProcessed.contains(secondStart)) {
                throw new IllegalStateException("more complex boundary constraints than I can currently handle -- " +
                        "more than two consecutive sentences with boundary errors." );
            }
            Constituent combinedSent = null;
            if (null == first.getLabelsToScores())
                combinedSent = new Constituent(first.getLabel(), first.getConstituentScore(), ViewNames.SENTENCE,
                        first.getTextAnnotation(), first.getStartSpan(), second.getEndSpan());
            else
                combinedSent = new Constituent(first.getLabelsToScores(), ViewNames.SENTENCE,
                        first.getTextAnnotation(), first.getStartSpan(), second.getEndSpan());

            for (String k : first.getAttributeKeys()) {
                combinedSent.addAttribute(k, first.getAttribute(k));
            }
            for (String k : second.getAttributeKeys()) {
                combinedSent.addAttribute(k, first.getAttribute(k));
            }

            sentences.removeConstituent(first);
            sentences.removeConstituent(second);
            sentences.addConstituent(combinedSent);
        }

        ta.setSentences();

        return violations;
    }

}
