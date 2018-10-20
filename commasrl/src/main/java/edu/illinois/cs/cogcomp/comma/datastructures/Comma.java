/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.datastructures;

import edu.illinois.cs.cogcomp.comma.bayraktar.BayraktarPatternLabeler;
import edu.illinois.cs.cogcomp.comma.utils.NgramUtils;
import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.QueryableList;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseUtils;

import java.io.Serializable;
import java.util.*;

/**
 * A data structure containing all the information related to a comma.
 */
public class Comma implements Serializable {
    private final List<String> labels;
    public final int commaPosition;
    private CommaSRLSentence s;

    private static final long serialVersionUID = 715976951486905422L;

    // These properties are used while extracting features
    private static boolean GOLD = CommaProperties.getInstance().useGold();
    private static final boolean NERlexicalise = CommaProperties.getInstance().lexicaliseNER();
    private static final boolean POSlexicalise = CommaProperties.getInstance().lexicalisePOS();
    // Which automatic parse to use
    private static final String CONSTITUENT_PARSER = ViewNames.PARSE_STANFORD;

    public static void useGoldFeatures(boolean useGold) {
        GOLD = useGold;
    }

    public String getCommaID() {
        return commaPosition + " " + s.goldTa.getId();
    }

    /**
     * Comma constructor when labels are known
     * 
     * @param commaPosition The token index of the comma
     * @param s The tokenized string of the sentence
     */
    protected Comma(int commaPosition, CommaSRLSentence s, List<String> labels) {
        this.commaPosition = commaPosition;
        this.s = s;
        this.labels = labels;
    }


    /**
     * Comma constructor for when labels are not known
     * 
     * @param commaPosition The token index of the comma
     * @param s The tokenized string of the sentence
     */
    protected Comma(int commaPosition, CommaSRLSentence s) {
        this(commaPosition, s, null);
    }


    /**
     * @return the label of the comma. If a comma can have multiple labels, only return the first
     */
    public String getLabel() {
        return labels.get(0);
    }

    public List<String> getLabels() {
        return labels;
    }

    public int getPosition() {
        return commaPosition;
    }

    public CommaSRLSentence getSentence() {
        return s;
    }

    public TextAnnotation getTextAnnotation(boolean gold) {
        return gold ? s.goldTa : s.ta;
    }

    public String getWordToRight(int distance) {
        // Dummy symbol for sentence end (in case comma is the second to last word in the sentence)
        if (commaPosition + distance >= s.ta.getTokens().length)
            return "###";
        return s.ta.getToken(commaPosition + distance);
    }

    public String getWordToLeft(int distance) {
        // Dummy symbol for sentence start (in case comma is the second word in the sentence)
        if (commaPosition - distance < 0)
            return "$$$";
        return s.ta.getToken(commaPosition - distance);
    }

    public String getPOSToLeft(int distance) {
        TokenLabelView posView;
        if (GOLD)
            posView = (TokenLabelView) s.goldTa.getView(ViewNames.POS);
        else
            posView = (TokenLabelView) s.ta.getView(ViewNames.POS);
        String pos = posView.getLabel(commaPosition - distance);
        if (pos.equals("DT") && distance == 1 && getWordToRight(distance).equalsIgnoreCase("the"))
            return "DT-the";
        else
            return pos;
    }

    public String getPOSToRight(int distance) {
        TokenLabelView posView;
        if (GOLD)
            posView = (TokenLabelView) s.goldTa.getView(ViewNames.POS);
        else
            posView = (TokenLabelView) s.ta.getView(ViewNames.POS);
        String pos = posView.getLabel(commaPosition + distance);
        if (pos.equals("DT") && distance == 1 && getWordToRight(distance).equalsIgnoreCase("the"))
            return "DT-the";
        else
            return pos;
    }

    public Constituent getChunkToRightOfComma(int distance) {
        // We don't have gold SHALLOW_PARSE
        SpanLabelView chunkView = (SpanLabelView) s.ta.getView(ViewNames.SHALLOW_PARSE);


        List<Constituent> chunksToRight =
                chunkView.getSpanLabels(commaPosition + 1, s.ta.getTokens().length);
        Collections.sort(chunksToRight, TextAnnotationUtilities.constituentStartComparator);

        Constituent chunk;
        if (distance <= 0 || distance > chunksToRight.size())
            chunk = null;
        else
            chunk = chunksToRight.get(distance - 1);
        return chunk;
    }

    public Constituent getChunkToLeftOfComma(int distance) {
        // We don't have gold SHALLOW_PARSE
        SpanLabelView chunkView = (SpanLabelView) s.ta.getView(ViewNames.SHALLOW_PARSE);


        List<Constituent> chunksToLeft = chunkView.getSpanLabels(0, commaPosition + 1);
        Collections.sort(chunksToLeft, TextAnnotationUtilities.constituentStartComparator);

        Constituent chunk;
        if (distance <= 0 || distance > chunksToLeft.size())
            chunk = null;
        else
            chunk = chunksToLeft.get(distance - 1);
        return chunk;
    }

    public Constituent getPhraseToLeftOfComma(int distance) {
        TreeView parseView;
        if (GOLD)
            parseView = (TreeView) s.goldTa.getView(ViewNames.PARSE_GOLD);
        else
            parseView = (TreeView) s.ta.getView(CONSTITUENT_PARSER);
        Constituent comma = getCommaConstituentFromTree(parseView);

        return getSiblingToLeft(distance, comma, parseView);
    }

    public Constituent getPhraseToRightOfComma(int distance) {
        TreeView parseView;
        if (GOLD)
            parseView = (TreeView) s.goldTa.getView(ViewNames.PARSE_GOLD);
        else
            parseView = (TreeView) s.ta.getView(CONSTITUENT_PARSER);
        Constituent comma = getCommaConstituentFromTree(parseView);

        return getSiblingToRight(distance, comma, parseView);
    }

    public Constituent getPhraseToLeftOfParent(int distance) {
        TreeView parseView;
        if (GOLD)
            parseView = (TreeView) s.goldTa.getView(ViewNames.PARSE_GOLD);
        else
            parseView = (TreeView) s.ta.getView(CONSTITUENT_PARSER);
        Constituent comma = getCommaConstituentFromTree(parseView);
        Constituent parent = TreeView.getParent(comma);
        return getSiblingToLeft(distance, parent, parseView);
    }

    public Constituent getPhraseToRightOfParent(int distance) {
        TreeView parseView;
        if (GOLD)
            parseView = (TreeView) s.goldTa.getView(ViewNames.PARSE_GOLD);
        else
            parseView = (TreeView) s.ta.getView(CONSTITUENT_PARSER);
        Constituent comma = getCommaConstituentFromTree(parseView);
        Constituent parent = TreeView.getParent(comma);
        return getSiblingToRight(distance, parent, parseView);
    }

    public String[] getLeftToRightDependencies() {
        TreeView depTreeView = (TreeView) s.ta.getView(ViewNames.DEPENDENCY_STANFORD);
        List<Constituent> constituentsOnLeft =
                depTreeView.getConstituentsCoveringSpan(0, commaPosition);
        List<Relation> ltors = new ArrayList<>();

        for (Constituent constituent : constituentsOnLeft) {
            for (Relation relation : constituent.getOutgoingRelations()) {
                Constituent target = relation.getTarget();
                if (target.getStartSpan() > commaPosition)
                    ltors.add(relation);
            }
        }

        String[] ltorNames = new String[ltors.size()];
        for (int i = 0; i < ltorNames.length; i++)
            ltorNames[i] = ltors.get(i).getRelationName();
        return ltorNames;
    }

    public String[] getRightToLeftDependencies() {
        TreeView depTreeView = (TreeView) s.ta.getView(ViewNames.DEPENDENCY_STANFORD);
        List<Constituent> constituentsOnLeft =
                depTreeView.getConstituentsCoveringSpan(0, commaPosition);
        List<Relation> rtols = new ArrayList<>();

        for (Constituent constituent : constituentsOnLeft) {
            for (Relation relation : constituent.getIncomingRelations()) {
                Constituent target = relation.getSource();
                if (target.getStartSpan() > commaPosition)
                    rtols.add(relation);
            }
        }
        String[] rtolNames = new String[rtols.size()];
        for (int i = 0; i < rtolNames.length; i++)
            rtolNames[i] = rtols.get(i).getRelationName();
        return rtolNames;
    }

    public Constituent getCommaConstituentFromTree(TreeView parseView) {
        Constituent comma = null;
        for (Constituent c : parseView.getConstituents()) {
            if (c.isConsituentInRange(commaPosition, commaPosition + 1)) {
                try {
                    comma = parseView.getParsePhrase(c);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                break;
            }
        }
        return comma;
    }

    public Constituent getSiblingToLeft(int distance, Constituent c, TreeView parseView) {
        Constituent leftSibling = c;
        IQueryable<Constituent> siblings = parseView.where(Queries.isSiblingOf(c));
        while (distance-- > 0) {
            Iterator<Constituent> leftSiblingIt =
                    siblings.where(Queries.adjacentToBefore(leftSibling)).iterator();
            if (leftSiblingIt.hasNext())
                leftSibling = leftSiblingIt.next();
            else
                return null;
        }
        return leftSibling;
    }

    public Constituent getSiblingToRight(int distance, Constituent c, TreeView parseView) {
        Constituent rightSibling = c;
        IQueryable<Constituent> siblings = parseView.where(Queries.isSiblingOf(c));
        while (distance-- > 0) {
            Iterator<Constituent> rightSiblingIt =
                    siblings.where(Queries.adjacentToAfter(rightSibling)).iterator();
            if (rightSiblingIt.hasNext())
                rightSibling = rightSiblingIt.next();
            else
                return null;
        }
        return rightSibling;
    }


    /**
     *
     * @return the list of commas that are children of the parent of the current comma, i.e.
     *         siblings of the current comma.
     */
    public List<Comma> getSiblingCommas() {
        TreeView parseView;
        if (GOLD)
            parseView = (TreeView) s.goldTa.getView(ViewNames.PARSE_GOLD);
        else
            parseView = (TreeView) s.ta.getView(CONSTITUENT_PARSER);
        List<Constituent> commaConstituents = new ArrayList<>();
        Map<Constituent, Comma> constituentCommaMap = new HashMap<>();
        for (Comma c : s.getCommas()) {
            Constituent commaConstituent = c.getCommaConstituentFromTree(parseView);
            commaConstituents.add(commaConstituent);
            constituentCommaMap.put(commaConstituent, c);
        }
        QueryableList<Constituent> qlCommas = new QueryableList<>(commaConstituents);
        Iterable<Constituent> siblingCommaConstituents =
                qlCommas.where(Queries.isSiblingOf(this.getCommaConstituentFromTree(parseView)));
        List<Comma> siblingCommas = new ArrayList<>();
        for (Constituent commaConstituent : siblingCommaConstituents)
            siblingCommas.add(constituentCommaMap.get(commaConstituent));
        return siblingCommas;
    }

    public boolean isSibling(Comma otherComma) {
        TreeView parseView;
        if (GOLD)
            parseView = (TreeView) s.goldTa.getView(ViewNames.PARSE_GOLD);
        else
            parseView = (TreeView) s.ta.getView(CONSTITUENT_PARSER);
        Constituent thisCommaConstituent = getCommaConstituentFromTree(parseView);
        Constituent otherCommmaConstituent = otherComma.getCommaConstituentFromTree(parseView);
        return TreeView.getParent(thisCommaConstituent) == TreeView
                .getParent(otherCommmaConstituent);
    }

    /**
     *
     * @return the first comma by position from the list of sibling commas
     */
    public Comma getSiblingCommaHead() {
        List<Comma> siblingCommas = getSiblingCommas();
        Comma head = siblingCommas.get(0);
        for (Comma c : siblingCommas)
            if (c.commaPosition < head.commaPosition)
                head = c;
        return head;
    }

    public String getNotation(Constituent c) {
        if (c == null)
            return "NULL";
        String notation = c.getLabel();

        if (c.getOutgoingRelations().size() > 0
                && (c.getViewName().equals(ViewNames.PARSE_GOLD) || c.getViewName().equals(
                        CONSTITUENT_PARSER)))
            notation += c.getOutgoingRelations().get(0).getTarget().getLabel();

        if (NERlexicalise)
            notation += "-" + getNamedEntityTag(c);

        if (POSlexicalise) {
            notation += "-";
            IntPair span = c.getSpan();
            TextAnnotation ta = c.getTextAnnotation();
            for (int tokenId = span.getFirst(); tokenId < span.getSecond(); tokenId++)
                notation += " " + POSUtils.getPOS(ta, tokenId);
        }

        return notation;
    }

    public String getStrippedNotation(Constituent c) {
        if (c == null)
            return "NULL";
        String notation = c.getLabel().split("-")[0];

        if (NERlexicalise)
            notation += "-" + getNamedEntityTag(c);

        if (POSlexicalise) {
            notation += "-";
            IntPair span = c.getSpan();
            TextAnnotation ta = c.getTextAnnotation();
            for (int tokenId = span.getFirst(); tokenId < span.getSecond(); tokenId++)
                notation += " " + POSUtils.getPOS(ta, tokenId);
        }

        return notation;
    }

    public List<String> getContainingSRLs() {
        List<String> list = new ArrayList<>();
        TextAnnotation srlTA = (GOLD) ? s.goldTa : s.ta;
        PredicateArgumentView pav;
        pav = (PredicateArgumentView) srlTA.getView(ViewNames.SRL_VERB);
        for (Constituent pred : pav.getPredicates()) {
            for (Relation rel : pav.getArguments(pred)) {
                if (rel.getTarget().getEndSpan() > commaPosition
                        && rel.getTarget().getStartSpan() >= commaPosition)
                    list.add(pav.getPredicateLemma(rel.getSource()) + rel.getRelationName());
            }
        }
        pav = (PredicateArgumentView) srlTA.getView(ViewNames.SRL_NOM);
        for (Constituent pred : pav.getPredicates()) {
            for (Relation rel : pav.getArguments(pred)) {
                if (rel.getTarget().getEndSpan() > commaPosition
                        && rel.getTarget().getStartSpan() >= commaPosition)
                    list.add(pav.getPredicateLemma(rel.getSource()) + rel.getRelationName());
            }
        }
        // We don't have gold prepSRL (for now)
        pav = (PredicateArgumentView) s.ta.getView(ViewNames.SRL_PREP);
        for (Constituent pred : pav.getPredicates()) {
            for (Relation rel : pav.getArguments(pred)) {
                if (rel.getTarget().getEndSpan() > commaPosition
                        && rel.getTarget().getStartSpan() >= commaPosition)
                    list.add(pav.getPredicateLemma(rel.getSource()) + rel.getRelationName());
            }
        }
        return list;
    }

    public String getNamedEntityTag(Constituent c) {
        // We don't have gold NER
        SpanLabelView nerView = (SpanLabelView) s.ta.getView(ViewNames.NER_CONLL);
        List<Constituent> NEs = nerView.getConstituentsCovering(c);
        String result = "";
        /*
         * String result = NEs.size()==0? "NO-NER" : NEs.get(0).getLabel(); for(int i = 1;
         * i<NEs.size(); i++) result += "+" + NEs.get(i).getLabel();
         */
        for (Constituent ne : NEs) {
            if (!ne.getLabel().equals("MISC") && c.doesConstituentCover(ne)
                    && (ne.getNumberOfTokens() >= 0.6 * c.getNumberOfTokens()))
                result += "+" + ne.getLabel();
        }
        return result;
    }

    public String getBayraktarLabel() {
        String bayraktarLabel = BayraktarPatternLabeler.getLabel(this);
        if (bayraktarLabel == null)
            return "Other";// assigning majority label
        else
            return bayraktarLabel;

    }

    /*
     * public String getBayraktarLabels() { String[] labels; if (GOLD){ labels = new String[1];
     * TreeView goldParseView = (TreeView) s.goldTa.getView(ViewNames.PARSE_GOLD); String
     * goldBayraktarPattern = getBayraktarPattern(goldParseView); labels[0] =
     * BayraktarPatternLabeler.getBayraktarLabel(goldBayraktarPattern); } else{ labels = new
     * String[2];
     * 
     * TreeView charniakParseView = (TreeView) s.ta.getView(ViewNames.PARSE_CHARNIAK); String
     * charniakBayraktarPattern = getBayraktarPattern(charniakParseView); labels[0] =
     * BayraktarPatternLabeler.getBayraktarLabel(charniakBayraktarPattern);
     * 
     * TreeView stanfordParseView = (TreeView) s.ta.getView(ViewNames.PARSE_STANFORD); String
     * stanfordBayraktarPattern = getBayraktarPattern(stanfordParseView); labels[1] =
     * BayraktarPatternLabeler.getBayraktarLabel(stanfordBayraktarPattern); if(labels[0]==null)
     * labels[0] = labels[1]; } return labels[0]; }
     */

    public String getBayraktarPattern() {
        TreeView parseView;
        if (GOLD)
            parseView = (TreeView) s.goldTa.getView(ViewNames.PARSE_GOLD);
        else
            parseView = (TreeView) s.ta.getView(CONSTITUENT_PARSER);
        return getBayraktarPattern(parseView);
    }

    public String getBayraktarPattern(TreeView parseView) {
        String pattern;
        Constituent comma = getCommaConstituentFromTree(parseView);
        Constituent parent = TreeView.getParent(comma);
        if (!ParseTreeProperties.isPunctuationToken(parent.getLabel())
                && ParseTreeProperties.isPreTerminal(parent)) {
            if (parent.getLabel().equals("CC"))
                pattern = parent.getSurfaceForm();
            else
                pattern = "***";
        } else
            pattern = parent.getLabel().split("-")[0];
        pattern += " -->";
        for (Relation childRelation : parent.getOutgoingRelations()) {
            Constituent child = childRelation.getTarget();
            if (!POSUtils.isPOSPunctuation(child.getLabel())
                    && ParseTreeProperties.isPreTerminal(child)) {
                if (child.getLabel().equals("CC")) {
                    pattern += " " + child.getSurfaceForm();
                } else if (!pattern.endsWith("***"))
                    pattern += " ***";
            } else
                pattern += " " + ParseUtils.stripFunctionTags(child.getLabel());
        }
        return pattern;
    }

    public String[] getWordNgrams() {
        int window = 2;
        List<String> wordWindow = new ArrayList<>();
        for (int i = window; i > 0; i--)
            wordWindow.add(getWordToLeft(i));
        for (int i = 1; i <= window; i++)
            wordWindow.add(getWordToRight(i));
        List<String> ngrams = new ArrayList<>();

        ngrams.addAll(NgramUtils.ngrams(1, wordWindow));
        // ngrams.addAll(FeatureUtils.ngrams(2, wordWindow));

        return ngrams.toArray(new String[ngrams.size()]);
    }

    public String[] getPOSNgrams() {
        int window = 5;
        List<String> posWindow = new ArrayList<>();
        for (int i = window; i > 0; i--)
            posWindow.add(getPOSToLeft(i));
        for (int i = 1; i <= window; i++)
            posWindow.add(getPOSToRight(i));
        List<String> ngrams = new ArrayList<>();

        ngrams.addAll(NgramUtils.ngrams(2, posWindow));
        ngrams.addAll(NgramUtils.ngrams(3, posWindow));
        ngrams.addAll(NgramUtils.ngrams(4, posWindow));
        ngrams.addAll(NgramUtils.ngrams(5, posWindow));

        return ngrams.toArray(new String[ngrams.size()]);
    }

    public String[] getChunkNgrams() {
        int window = 2;
        List<String> chunkWindow = new ArrayList<>();
        for (int i = window; i > 0; i--)
            chunkWindow.add(getNotation(getChunkToLeftOfComma(i)));
        for (int i = 1; i <= window; i++)
            chunkWindow.add(getNotation(getChunkToRightOfComma(i)));
        List<String> ngrams = new ArrayList<>();

        ngrams.addAll(NgramUtils.ngrams(1, chunkWindow));
        ngrams.addAll(NgramUtils.ngrams(2, chunkWindow));

        return ngrams.toArray(new String[ngrams.size()]);
    }

    public String[] getSiblingPhraseNgrams() {
        int window = 2;
        List<String> phraseWindow = new ArrayList<>();
        for (int i = window; i > 0; i--)
            phraseWindow.add(getNotation(getPhraseToLeftOfComma(i)));
        for (int i = 1; i <= window; i++)
            phraseWindow.add(getNotation(getPhraseToRightOfComma(i)));
        List<String> ngrams = new ArrayList<>();

        ngrams.addAll(NgramUtils.ngrams(1, phraseWindow));
        ngrams.addAll(NgramUtils.ngrams(2, phraseWindow));

        return ngrams.toArray(new String[ngrams.size()]);
    }

    public String[] getParentSiblingPhraseNgrams() {
        int window = 3;
        List<String> parentPhraseWindow = new ArrayList<>();
        for (int i = window; i > 0; i--)
            parentPhraseWindow.add(getNotation(getPhraseToLeftOfParent(i)));
        parentPhraseWindow.add(getNotation(getPhraseToLeftOfParent(0)));
        for (int i = 1; i <= window; i++)
            parentPhraseWindow.add(getNotation(getPhraseToRightOfParent(i)));
        List<String> ngrams = new ArrayList<>();

        ngrams.addAll(NgramUtils.ngrams(1, parentPhraseWindow));
        ngrams.addAll(NgramUtils.ngrams(2, parentPhraseWindow));

        return ngrams.toArray(new String[ngrams.size()]);
    }

    public String getAnnotatedText() {
        List<String> tokens = Arrays.asList(s.ta.getTokens());
        return StringUtils.join(" ", tokens.subList(0, commaPosition + 1)) + "["
                + StringUtils.join(",", labels) + "] "
                + StringUtils.join(" ", tokens.subList(commaPosition + 1, tokens.size()));
    }

    public String getBayraktarAnnotatedText() {
        List<String> tokens = Arrays.asList(s.ta.getTokens());
        return StringUtils.join(" ", tokens.subList(0, commaPosition + 1)) + "["
                + getBayraktarLabel() + "] "
                + StringUtils.join(" ", tokens.subList(commaPosition + 1, tokens.size()));
    }
}
