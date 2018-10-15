/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;


import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.HasAttributes;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.StringTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    static private Logger logger = LoggerFactory.getLogger(TextAnnotationUtilities.class);

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

        if(vu == null) {
            // either the view is not contained, or the view contained is null
            logger.warn("The view `" + vuName + "` for sentence `" + ta.text + "` is empty . . . ");
            return;
        }

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
        }

        List<Pair<Constituent, Constituent>> consMap = new ArrayList<>();
        List<Constituent> constituentsToCopy = null;

        if (ta.size() <= newTA.size())
            constituentsToCopy = vu.getConstituents();
        else
            constituentsToCopy = vu.getConstituentsCoveringSpan(sourceStartTokenIndex, sourceEndTokenIndex + 1);

        for (Constituent c : constituentsToCopy) {
            // replacing the constituents with a new ones, with token ids shifted
            Constituent newC = copyConstituentWithNewTokenOffsets(newTA, c, offset);
            consMap.add(new Pair<>(c, newC));
            newVu.addConstituent(newC, true);
        }

        for (Relation r : vu.getRelations()) {

            // only include relations which have both source and target in the consMap
            boolean sourcePresent = false;
            boolean targetPresent = false;
            for(Pair<Constituent, Constituent> p : consMap){
                // the == is very important. We do not want to check attribute, but object equality.
                // this is because duplicates are technically allowed.
                if(r.getSource() == p.getFirst()){
                    sourcePresent = true;
                }
                if(r.getTarget() == p.getFirst()){
                    targetPresent = true;
                }
            }

            if(sourcePresent && targetPresent) {
                // replacing the relations with a new ones, with their constituents replaced with the shifted ones.
                Relation newR = copyRelation(r, consMap);
                newVu.addRelation(newR);
            }
        }

        newTA.addView(vuName, newVu);

        if (vu instanceof TreeView) {
            ((TreeView) newVu).makeTrees();
        }
        else if (vu instanceof PredicateArgumentView)
            ((PredicateArgumentView) vu).findPredicates();
    }

    /**
     * required: consMap *must* contain the source and target constituents for r as keys, and their values
     *    must be non-null
     * @param r relation to copy
     * @param consMap map from original constituents to new counterparts
     * @return new relation with all info copied from original, but with new source and target constituents
     */
    public static Relation copyRelation(Relation r, Map<Constituent, Constituent> consMap) {
        Relation newRel = null;

        if ( null == r.getLabelsToScores() )
            newRel = new Relation(r.getRelationName(), consMap.get(r.getSource()), consMap.get(r.getTarget()), r.getScore());
        else
            newRel = new Relation(r.getLabelsToScores(), consMap.get(r.getSource()), consMap.get(r.getTarget()));

        copyAttributesFromTo(r, newRel);

        return newRel;
    }

    /**
     * required: consMap *must* contain the source and target constituents for r as keys, and their values
     *    must be non-null
     * @param r relation to copy
     * @param consMap list containing pairs that map from original constituents to new counterparts
     * @return new relation with all info copied from original, but with new source and target constituents
     */
    public static Relation copyRelation(Relation r, List<Pair<Constituent, Constituent>> consMap) {
        Relation newRel = null;

        Constituent src = null;
        Constituent tgt = null;

        // the equality check here is VERY important. Sometimes duplicate constituents are allowed,
        // and we want to check OBJECT equality, not attribute equality.
        for(Pair<Constituent,Constituent> p : consMap){
            if(r.getSource() == p.getFirst()){
                src = p.getSecond();
            }

            if(r.getTarget() == p.getFirst()){
                tgt = p.getSecond();
            }
        }

        if ( null == r.getLabelsToScores() )
            newRel = new Relation(r.getRelationName(), src, tgt, r.getScore());
        else
            newRel = new Relation(r.getLabelsToScores(), src, tgt);

        copyAttributesFromTo(r, newRel);

        return newRel;
    }


    public static void copyAttributesFromTo(HasAttributes origObj, HasAttributes newObj) {
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
    public static Constituent copyConstituentWithNewTokenOffsets(TextAnnotation newTA, Constituent c, int offset) {
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

    /**
     * given a TextAnnotation generated from the transformed text of a StringTransformation object, and the
     *    corresponding StringTransformation object, generate a new TextAnnotation whose annotations correspond
     *    to those of the transformed text TextAnnotation, but whose offsets correspond to the original text
     * Example: you parse an xml-formatted news document, and use a StringTransformation to record all the places
     *    you removed xml markup or made other changes.  You process the cleaned text with a set of NLP
     *    tools. This method takes the output and maps the offsets back to the xml-formatted source document.
     * This is useful for e.g. TAC evaluations, where provenance offsets are important.
     * @param ta
     * @param st
     * @return
     */
    public static TextAnnotation mapTransformedTextAnnotationToSource(TextAnnotation ta,
                                                                      StringTransformation st) {

        if (!ta.getText().equals(st.getTransformedText()))
            throw new IllegalStateException("transformed text does not match the TextAnnotation text: " +
                    "StringTransformation: '" +
            st.getTransformedText() + "'\nTextAnnotation: '" + ta.getText() + "'.");

        IntPair[] updatedTokenCharOffsets = new IntPair[ta.getTokens().length];

        List<Constituent> sentences = ta.getView(ViewNames.SENTENCE).getConstituents();
        int[] updatedSentenceEndPositions = new int[sentences.size()];
        int count = 0;
        for (Constituent sent : sentences) { // same token offsets as originals!
            updatedSentenceEndPositions[count++] = sent.getEndSpan();
        }

        View origTokView = ta.getView(ViewNames.TOKENS);

        count = 0;
        for (Constituent origTok : origTokView.getConstituents()) {
            IntPair newCharOffsets = st.getOriginalOffsets(origTok.getStartCharOffset(), origTok.getEndCharOffset());
            updatedTokenCharOffsets[count++] = newCharOffsets;
        }

        TextAnnotation newTA = new TextAnnotation(ta.getCorpusId(), ta.getId(), st.getOrigText(),
                updatedTokenCharOffsets, ta.getTokens(), updatedSentenceEndPositions);

        for (String vuName : ta.getAvailableViews()) {
            if (ViewNames.TOKENS.equals(vuName) || ViewNames.SENTENCE.equals(vuName))
                continue;
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

            List<Pair<Constituent, Constituent>> consMap = new ArrayList<>();
            List<Constituent> constituentsToCopy = vu.getConstituents();

            for (Constituent c : constituentsToCopy) {
                // replacing the constituents with new ones, token ids remain the same (!)
                Constituent newC = copyConstituentWithNewTokenOffsets(newTA, c, 0);
                consMap.add(new Pair<>(c, newC));
                newVu.addConstituent(newC, true);
            }

            for (Relation r : vu.getRelations()) {

                // only include relations which have both source and target in the consMap
                boolean sourcePresent = false;
                boolean targetPresent = false;
                for(Pair<Constituent, Constituent> p : consMap){
                    // the == is very important. We do not want to check attribute, but object equality.
                    // this is because duplicates are technically allowed.
                    if(r.getSource() == p.getFirst()){
                        sourcePresent = true;
                    }
                    if(r.getTarget() == p.getFirst()){
                        targetPresent = true;
                    }
                }

                if(sourcePresent && targetPresent) {
                    // replacing the relations with a new ones, with their constituents replaced with the shifted ones.
                    Relation newR = copyRelation(r, consMap);
                    newVu.addRelation(newR);
                }
            }


            if (vu instanceof TreeView) {
                ((TreeView) newVu).makeTrees();
            }
        }

        return newTA;
    }

}
