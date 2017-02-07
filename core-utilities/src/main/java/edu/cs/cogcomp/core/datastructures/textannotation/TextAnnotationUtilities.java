/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.core.datastructures.textannotation;


import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;

import java.io.PrintStream;
import java.util.*;

/**
 * @author Vivek Srikumar
 */
public class TextAnnotationUtilities {

    /**
     * This comparator will sort entities on start location, but where start is equal on end as well
     * so the shorter entities come first.
     */
    public final static Comparator<Constituent> constituentStartEndComparator =
            (arg0, arg1) -> {
                int start0 = arg0.getStartSpan();
                int start1 = arg1.getStartSpan();
                if (start0 < start1)
                    return -1;
                else if (start0 == start1) {
                    int end0 = arg0.getEndSpan();
                    int end1 = arg1.getEndSpan();
                    if (end0 < end1)
                        return -1;
                    else if (end0 == end1) {
                        return 0;
                    } else
                        return 1;
                } else
                    return 1;
            };
    public final static Comparator<Constituent> constituentStartComparator =
            (arg0, arg1) -> {
                int start0 = arg0.getStartSpan();
                int start1 = arg1.getStartSpan();
                if (start0 < start1)
                    return -1;
                else if (start0 == start1)
                    return 0;
                else
                    return 1;
            };
    public final static Comparator<Sentence> sentenceStartComparator = (o1, o2) -> constituentStartComparator.compare(o1.sentenceConstituent,
            o2.sentenceConstituent);

    public final static Comparator<Constituent> constituentEndComparator =
            (arg0, arg1) -> {
                int end0 = arg0.getEndSpan();
                int end1 = arg1.getEndSpan();

                if (end0 < end1)
                    return -1;
                else if (end0 > end1)
                    return 1;
                else
                    return 0;
            };

    public final static Comparator<Constituent> constituentLengthComparator =
            (arg0, arg1) -> {
                int size0 = arg0.size();
                int size1 = arg1.size();

                if (size0 < size1)
                    return -1;
                else if (size0 > size1)
                    return 1;
                else
                    return 0;
            };

    public static TextAnnotation createFromTokenizedString(String text) {
        return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections
                .singletonList(text.split(" ")));
    }

    public static String getTokenSequence(TextAnnotation ta, int start, int end) {
        return new Constituent("", "", ta, start, end).toString();
    }

    public static List<String> getSentenceList(TextAnnotation ta) {
        List<String> sentencesList = new ArrayList<>();

        for (Sentence sentence : ta.sentences()) {
            String sentenceString = sentence.getTokenizedText().trim();

            sentencesList.add(sentenceString);
        }
        return sentencesList;
    }

    static public void printTextAnnotation(PrintStream out, TextAnnotation ta) {
        out.println("TextAnnotation with id: " + ta.getId());

        String rawText = ta.getText();
        out.println("Raw Text: " + rawText);

        out.println(getLineFill());

        out.println("TextAnnotation Views:");

        for (String name : ta.getAvailableViews()) {
            out.println("View Name: " + name);
            out.println(ta.getView(name).toString());
            out.println(getLineFill());
        }
    }

    private static String getLineFill() {
        return "------------------------------------";
    }

    /**
     * Given a {@link TextAnnotation} object, and a sentence id, it gives a smaller {@link TextAnnotation} which contains
     * the annotations specific to the given sentence id.
     */
    static public TextAnnotation getSubTextAnnotation(TextAnnotation ta, int sentenceId) {
        assert sentenceId < ta.getNumberOfSentences();
        List<IntPair> tokensPairs = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        int firstCharOffset = -1;
        int start = -1;
        int end = -1;
        for(int i = 0; i < ta.tokens.length; i++) {
            if(ta.getSentenceId(i) == sentenceId) {
                if(start == -1) start = i;
                end = i;
                int first = ta.getTokenCharacterOffset(i).getFirst();
                int second = ta.getTokenCharacterOffset(i).getSecond();
                if(firstCharOffset == -1) firstCharOffset = first;
                tokensPairs.add(new IntPair(first - firstCharOffset, second - firstCharOffset)); // apply the char offsets
                tokens.add(ta.getToken(i));
            }
        }
        int tokenSize = end - start + 1;
        assert tokensPairs.size() == tokenSize;
        String text = ta.getText().substring(tokensPairs.get(0).getFirst() + firstCharOffset, tokensPairs.get(tokensPairs.size()-1).getSecond() + firstCharOffset);
        TextAnnotation newTA = new TextAnnotation(ta.corpusId, ta.id, text,
                tokensPairs.toArray(new IntPair[tokenSize]), tokens.toArray(new String[tokenSize]), new int[]{tokenSize});
        for(String vuName : ta.getAvailableViews()) {
            View vu = ta.getView(vuName);
            View newVu = null;
            if(vu instanceof TokenLabelView) {
                newVu = new TokenLabelView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            }
            else if(vu instanceof SpanLabelView) {
                newVu = new SpanLabelView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            }
            else if(vu instanceof CoreferenceView) {
                newVu = new CoreferenceView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            }
            else if(vu instanceof PredicateArgumentView) {
                newVu = new PredicateArgumentView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            }
            else if(vu instanceof TreeView) {
                newVu = new TreeView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            }
            else {
                newVu = new View(vu.viewName, vu.viewGenerator, newTA, vu.score);
            }
            Map<Constituent, Constituent> consMap = new HashMap<>();
            for(Constituent c : vu.getConstituentsCoveringSpan(start, end + 1)) {
                // replacing the constituents with a new ones, with token ids shifted
                Constituent newC = new Constituent(c.getLabel(), c.viewName, newTA, c.getStartSpan() - start, c.getEndSpan() - start);
                consMap.put(c, newC);
                newVu.addConstituent(newC);
            }
            for(Relation r : vu.getRelations()) {
                if( r.getSource().getSentenceId() != sentenceId || r.getTarget().getSentenceId() != sentenceId )
                    continue;
                assert consMap.containsKey(r.getSource());
                assert consMap.containsKey(r.getTarget());
                // replacing the relations with a new ones, with their constituents replaced with the shifted ones.
                Relation newR = new Relation(r.getRelationName(), consMap.get(r.getSource()), consMap.get(r.getTarget()), r.getScore());
                newVu.addRelation(newR);
            }
            newTA.addView(vuName, newVu);
        }
        return newTA;
    }
}
