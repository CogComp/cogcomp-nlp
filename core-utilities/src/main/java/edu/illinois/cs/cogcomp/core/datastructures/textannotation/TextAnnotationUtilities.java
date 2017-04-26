/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;


import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.HasAttributes;
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
    public static final Comparator<IntPair> IntPairComparator = (arg0, arg1) -> {

        int firstCompare = IntPair.comparatorFirst.compare(arg0, arg1);
        if (0 == firstCompare)
            return IntPair.comparatorSecond.compare(arg0, arg1);

        return firstCompare;
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
     * given a {@link TextAnnotation} for a sentence with annotations, map its annotations into a
     * TextAnnotation object for a longer text containing that sentence.
     * @param sentenceTa annotated TextAnnotation for sentence
     * @param textTa TextAnnotation for longer text containing sentence, without annotations for that sentence
     * @param sentenceId index of the sentence in the longer text
     */
    static public void mapSentenceAnnotationsToText(TextAnnotation sentenceTa, TextAnnotation textTa, int sentenceId ) {
        assert(sentenceId < textTa.getNumberOfSentences());
        assert(sentenceTa.getText().equals(textTa.getSentence(sentenceId).getText()));

        int start = textTa.getSentence(sentenceId).getStartSpan();
        int end = textTa.getSentence(sentenceId).getEndSpan();

        copyViewsFromTo(sentenceTa, textTa, start, end, start);
    }

    /**
     * Given a {@link TextAnnotation} object, and a sentence id, it gives a smaller {@link TextAnnotation} which contains
     * the annotations specific to the given sentence id. The underlying text is just the sentence text, and
     * character offsets are modified to correspond to this new shorter text.
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

        copyViewsFromTo(ta, newTA, start, end, -start);
        return newTA;
    }

    /**
     * copy views from the relevant span from ta to newTA.  If ta is smaller than newTA, map all constituents,
     *    changing offsets according to the value 'offset'.
     * Otherwise, only map those constituents within the span sourceStartTokenIndex, sourceEndTokenIndex to  newTA.
     *
     * @param ta
     * @param newTA
     * @param sourceStartTokenIndex
     * @param sourceEndTokenIndex
     * @param offset
     */
    public static void copyViewsFromTo(TextAnnotation ta, TextAnnotation newTA, int sourceStartTokenIndex,
                                        int sourceEndTokenIndex, int offset) {
        for (String vuName : ta.getAvailableViews()) {
            if (ViewNames.TOKENS.equals(vuName) || ViewNames.SENTENCE.equals(vuName))
                continue;
            copyViewFromTo(vuName, ta, newTA, sourceStartTokenIndex, sourceEndTokenIndex, offset);
        }
    }

    public static void copyViewFromTo(String vuName, TextAnnotation ta, TextAnnotation newTA, int sourceStartTokenIndex, int sourceEndTokenIndex, int offset) {
        View vu = ta.getView(vuName);

        View newVu = null;
        if (newTA.hasView(vuName))
            newVu = newTA.getView(vuName);
        else {
            if (vu instanceof TokenLabelView) {
                newVu = new TokenLabelView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            } else if (vu instanceof SpanLabelView) {
                newVu = new SpanLabelView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            } else if (vu instanceof CoreferenceView) {
                newVu = new CoreferenceView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            } else if (vu instanceof PredicateArgumentView) {
                newVu = new PredicateArgumentView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            } else if (vu instanceof TreeView) {
                newVu = new TreeView(vu.viewName, vu.viewGenerator, newTA, vu.score);
            } else {
                newVu = new View(vu.viewName, vu.viewGenerator, newTA, vu.score);
            }
            newTA.addView(vuName, newVu);
        }

        Map<Constituent, Constituent> consMap = new HashMap<>();
        List<Constituent> constituentsToCopy = null;

        if (ta.size() <= newTA.size())
            constituentsToCopy = vu.getConstituents();
        else
            constituentsToCopy = vu.getConstituentsCoveringSpan(sourceStartTokenIndex, sourceEndTokenIndex + 1);

        for (Constituent c : constituentsToCopy) {
            // replacing the constituents with a new ones, with token ids shifted
            Constituent newC = copyConstituentWithNewOffsets(newTA, c, offset);
            consMap.put(c, newC);
            newVu.addConstituent(newC);
        }
        for (Relation r : vu.getRelations()) {
            //don't include relations that cross into irrelevant span
            if (!consMap.containsKey(r.getSource()) || !consMap.containsKey(r.getTarget()))
                continue;
            // replacing the relations with a new ones, with their constituents replaced with the shifted ones.
            Relation newR = copyRelation(r, consMap);
            newVu.addRelation(newR);
        }
        if (vu instanceof TreeView) {
            ((TreeView) newVu).makeTrees();
        }
    }

    /**
     * required: consMap *must* contain the source and target constituents for r as keys, and their values
     *    must be non-null
     * @param r relation to copy
     * @param consMap map from original constituents to new counterparts
     * @return new relation with all info copied from original, but with new source and target constituents
     */
    private static Relation copyRelation(Relation r, Map<Constituent, Constituent> consMap) {
        Relation newRel = null;

        if ( null == r.getLabelsToScores() )
            newRel = new Relation(r.getRelationName(), consMap.get(r.getSource()), consMap.get(r.getTarget()), r.getScore());
        else
            newRel = new Relation(r.getLabelsToScores(), consMap.get(r.getSource()), consMap.get(r.getTarget()));

        copyAttributesFromTo(r, newRel);

        return newRel;
    }

    private static void copyAttributesFromTo(HasAttributes origObj, HasAttributes newObj) {
        for(String key : origObj.getAttributeKeys())
            newObj.addAttribute(key, origObj.getAttribute(key));
    }

    /**
     * create a new constituent with token offsets shifted by the specified amount
     * @param newTA TextAnnotation which will contain the new Constituent
     * @param c original Constituent to copy
     * @param offset the offset to shift token indexes of new Constituent. Can be negative.
     * @return the new Constituent
     */
    private static Constituent copyConstituentWithNewOffsets(TextAnnotation newTA, Constituent c, int offset) {
        int newStart = c.getStartSpan() + offset;
        int newEnd = c.getEndSpan() + offset;

        assert(newStart >= 0 && newStart <= newTA.size());
        assert(newEnd >= 0 && newEnd <= newTA.size());

        Constituent newCon = null;
        if (null != c.getLabelsToScores())
            newCon = new Constituent(c.getLabelsToScores(), c.viewName, newTA, newStart, newEnd);
        else
            newCon = new Constituent(c.getLabel(), c.getConstituentScore(), c.viewName, newTA, newStart, newEnd);

        copyAttributesFromTo(c, newCon);

        return newCon;
    }

}
