/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import com.google.common.collect.Maps;
import edu.illinois.cs.cogcomp.core.datastructures.HasAttributes;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.math.ArgMax;

import java.io.Serializable;
import java.util.*;

/**
 * A Constituent represents a unit of text (not necessarily contiguous) that participates in a view.
 * In terms of the nodes and edges representation of views, Constituents are the nodes and
 * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation}s are edges. Each
 * Constituent corresponds to a set of tokens in a {@link TextAnnotation}.
 *
 * @author Vivek Srikumar
 */
public class Constituent implements Serializable, HasAttributes {

    private static final long serialVersionUID = -4241917156773356414L;

    protected final double constituentScore;

    protected final TextAnnotation textAnnotation;
    protected final IntPair span; // span start/end token offsets
    // Curator-style offsets -- end char offset numbers position AFTER
    // constituent
    protected final int startCharOffset;
    protected final int endCharOffset;
    protected final List<Relation> outgoingRelations;
    protected final List<Relation> incomingRelations;
    protected final int label;
    /**
     * This indicates whether the element {@code Constituent#constituentTokens} is a two element
     * list consisting of a start and an end tokenId, specifying a span, instead of explicitly
     * listing the each element. This would use less memory.
     */
    // protected boolean useConstituentTokensAsSpan;

    protected final String viewName;
    protected final Map<String, Double> labelsToScores;
    protected Map<String, String> attributes;

    /**
     * start, end offsets are token indexes, and use one-past-the-end indexing -- so a one-token
     * constituent right at the beginning of a text has start/end (0,1) offsets are relative to the
     * entire text span (i.e. NOT sentence-relative) This constructor assigns default score to
     * constituent
     *
     * @param label label of this Constituent
     * @param viewName name of
     *        {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View} this
     *        Constituent belongs to
     * @param text TextAnnotation this Constituent belongs to
     * @param start start token offset
     * @param end end token offset (one-past-the-end)
     */
    public Constituent(String label, String viewName, TextAnnotation text, int start, int end) {
        this(null, label, 1.0, viewName, text, start, end);
    }

    /**
     * instantiate a constituent with a set of labels and corresponding scores: the 'main' label (returned by
     *    #getLabel()) will be the label with the highest score, as decided by {@link ArgMax}.
     *
     * Start, end offsets are token indexes, and use one-past-the-end indexing -- so a one-token
     * constituent right at the beginning of a text has start/end (0,1) offsets are relative to the
     * entire text span (i.e. NOT sentence-relative) This constructor assigns default score to
     * the constituent.
     *
     * @param labelsToScores set of possible labels and corresponding scores.
     * @param viewName name of
     *        {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View} this
     *        Constituent belongs to
     * @param text TextAnnotation this Constituent belongs to
     * @param start start token offset
     * @param end end token offset (one-past-the-end)
     */
    public Constituent(Map<String, Double> labelsToScores, String viewName, TextAnnotation text, int start, int end)
    {
        this( labelsToScores, new ArgMax<>(labelsToScores).getArgmax(), new ArgMax<>(labelsToScores).getMaxValue(), viewName, text, start, end );
    }

    /**
     * start, end offsets are token indexes, and use one-past-the-end indexing -- so a one-token
     * constituent right at the beginning of a text has start/end (0,1) offsets are relative to the
     * entire text span (i.e. NOT sentence-relative)
     *
     * @param label label of this Constituent
     * @param score confidence in label
     * @param viewName name of
     *        {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View} this
     *        Constituent belongs to
     * @param text TextAnnotation this Constituent belongs to
     * @param start start token offset
     * @param end end token offset (one-past-the-end)
     */
    public Constituent(String label, double score, String viewName, TextAnnotation text, int start,
            int end) {
        this(null, label, score, viewName, text, start, end);
    }

    /**
     * private constructor to allow immutable labels to scores map
     *
     * @param label label of this Constituent
     * @param score confidence in label
     * @param viewName name of
     *        {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.View} this
     *        Constituent belongs to
     * @param text TextAnnotation this Constituent belongs to
     * @param start start token offset
     * @param end end token offset (one-past-the-end)
     */
    private Constituent(Map<String, Double> labelsToScores, String label, double score, String viewName, TextAnnotation text, int start, int end) {
        if (null != labelsToScores) {
            this.labelsToScores = Maps.newHashMap();
            this.labelsToScores.putAll(labelsToScores);
        }
        else
            this.labelsToScores = null;

        this.label = getLabelId(label, text);
        this.constituentScore = score;
        this.viewName = viewName;
        textAnnotation = text;
        //
        // this.startSpan = start;
        // this.endSpan = end;
        this.span = new IntPair(start, end);
        int startSpan = this.getStartSpan();
        int endSpan = this.getEndSpan();

        if (start >= 0) {

            assert startSpan >= 0;
            assert endSpan >= 0;
        }

        this.outgoingRelations = new ArrayList<>();
        this.incomingRelations = new ArrayList<>();

        // IF this is a zero length constituent, and beyond the end of the text data,
        // we will set the text char offset to -1 and 0 I guess.
        if (startSpan == endSpan && startSpan >= text.tokenCharacterOffsets.length) {
            int ip = text.tokenCharacterOffsets[text.tokenCharacterOffsets.length-1].getSecond();
            startCharOffset = ip;
            endCharOffset = ip;
        } else {
            if (startSpan >= 0) {
                startCharOffset = text.getTokenCharacterOffset(startSpan).getFirst();
            } else
                startCharOffset = -1;
    
            if (endSpan > 0 && endSpan <= text.size()) {
                // TODO: verify correct token offset behavior. If a Constituent must always use
                // one-past-the-end indexing wrt token
                // indexes, this should be a requirement on instantiation. The check below was throwing
                // an exception
                // when start and end span were the same.
                if (endSpan > startSpan)
                    endCharOffset = text.getTokenCharacterOffset(endSpan - 1).getSecond();
                else
                    // FIXED -- MS 4/17/2015
                    endCharOffset = text.getTokenCharacterOffset(endSpan).getSecond();
            } else
                endCharOffset = 0;
        }

        assert endCharOffset >= startCharOffset : "End character offset of constituent less than start!\n"
                + text.getTokenizedText()
                + "("
                + start
                + ", "
                + end
                + "), -> ("
                + startCharOffset
                + ", " + endCharOffset + ")";
    }

    /**
     * Return map of labels to scores. If not explicitly created, returns null.
     * The returned map is a copy, to avoid inadvertent changes to the label/score mapping.
     *
     * @return map of labels to scores
     */
    public Map<String, Double> getLabelsToScores() {
        Map<String, Double> returnMap = null;

        if ( null != labelsToScores) {
            returnMap = new HashMap<>();
            returnMap.putAll(labelsToScores);
        }
        return returnMap;
    }

    private int getLabelId(String label, TextAnnotation text) {
        if (label == null)
            label = "";
        int labelId = text.symtab.getId(label);
        if (labelId == -1)
            labelId = text.symtab.add(label);
        return labelId;
    }

    public int getStartCharOffset() {
        return startCharOffset;
    }

    public int getEndCharOffset() {
        return endCharOffset;
    }

    public int getInclusiveStartCharOffset() {
        return startCharOffset;
    }

    public int getInclusiveEndCharOffset() {
        return endCharOffset - 1;
    }

    public void addAttribute(String key, String value) {
        if (attributes == null)
            attributes = new HashMap<>();
        attributes.put(key, value);
    }

    public boolean doesConstituentCover(int tokenId) {
        return this.getStartSpan() <= tokenId && this.getEndSpan() > tokenId;
    }

    public boolean doesConstituentCover(Constituent other) {
        return this.getStartSpan() <= other.getStartSpan()
                && this.getEndSpan() >= other.getEndSpan();
    }

    public boolean doesConstituentCoverAll(Collection<Integer> tokenIds) {

        for (int token : tokenIds) {
            if (!doesConstituentCover(token))
                return false;
        }
        return true;
    }

    /**
     * This function can be used in scenarios where there is a need for Constituent equality, ignoring the values
     * of the attributes. Note that many equality functions in Java automatically call the `equals' function.
     * @param that the input constituent you compare with
     * @return whether the two constituents are the same or not.
     */
    public boolean equalsWithoutAttributeEqualityCheck(Constituent that) {
//        if (this.getIncomingRelations().size() != that.getIncomingRelations().size())
//            return false;

//        for (int relationId = 0; relationId < this.getIncomingRelations().size(); relationId++) {
//            Relation myRelation = this.getIncomingRelations().get(relationId);
//            Relation otherRelation = that.getIncomingRelations().get(relationId);
//
//            int myRelationName = myRelation.relationName;
//
//            int otherRelationName = otherRelation.relationName;
//            if (myRelationName != otherRelationName)
//                return false;
//
//            if (!myRelation.getSource().getSpan().equals(otherRelation.getSource().getSpan()))
//                return false;
//
//            if (myRelation.getSource().label != otherRelation.getSource().label)
//                return false;
//        }
//
//        if (this.getOutgoingRelations().size() != that.getOutgoingRelations().size())
//            return false;
//
//        for (int relationId = 0; relationId < this.getOutgoingRelations().size(); relationId++) {
//            Relation myRelation = this.getOutgoingRelations().get(relationId);
//            Relation otherRelation = that.getOutgoingRelations().get(relationId);
//
//            int myRelationName = myRelation.relationName;
//
//            int otherRelationName = otherRelation.relationName;
//
//            if (myRelationName != otherRelationName)
//                return false;
//
//            if (!myRelation.getTarget().getSpan().equals(otherRelation.getTarget().getSpan()))
//                return false;
//
//            if (myRelation.getTarget().label != otherRelation.getTarget().label)
//                return false;
//        }

        return this.textAnnotation.getText().equals(that.textAnnotation.getText())
                && this.getStartSpan() == that.getStartSpan()
                && this.getEndSpan() == that.getEndSpan()
                && this.getLabel().equals(that.getLabel())
                && this.constituentScore == that.constituentScore
                && this.getViewName().equals(that.getViewName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Constituent))
            return false;

        Constituent that = (Constituent) obj;

        if (this.getStartSpan() < 0) {
            return false;
        }

        if (this.attributes == null && that.attributes != null)
            return false;
        if (this.attributes != null && that.attributes == null)
            return false;

        if (this.attributes != null && that.attributes != null)
            if (!this.attributes.equals(that.attributes))
                return false;

        if (null != this.labelsToScores && null == that.labelsToScores)
            return false;
        if (null == this.labelsToScores && null != that.labelsToScores)
            return false;
        if ( null != this.labelsToScores && null != that.labelsToScores)
            if (!this.labelsToScores.equals(that.labelsToScores))
                return false;

        return equalsWithoutAttributeEqualityCheck(that);
    }

    public String getAttribute(String key) {
        if (attributes == null)
            return null;
        else
            return attributes.get(key);
    }

    public Set<String> getAttributeKeys() {

        if (this.attributes == null)
            return new HashSet<>();
        else
            return this.attributes.keySet();
    }

    /**
     * @return the constituentScore
     */
    public double getConstituentScore() {
        return constituentScore;
    }

    public int getEndSpan() {
        return this.span.getSecond();
    }

    public String getLabel() {
        return this.textAnnotation.symtab.getLabel(label);
    }

    public int getNumberOfTokens() {
        return this.getEndSpan() - this.getStartSpan();
    }

    public int size() {
        return this.getNumberOfTokens();
    }

    public int length() {
        return this.getNumberOfTokens();
    }

    public int getStartSpan() {
        return this.span.getFirst();
    }

    public IntPair getSpan() {
        return span;
    }

    /**
     * This method returns a <i>tokenized</i> representation of the surface form of the constituent.
     * This is <b>not</b> the original surface form of the constituent. To retrieve that, please use
     * {@link #getSurfaceForm}
     *
     * @return A constructed form based on the tokens covered by the constituent and a single
     *         whitespace separating them.
     */
    public String getTokenizedSurfaceForm() {
        StringBuilder sb = new StringBuilder();

        if (getStartSpan() < 0) {
            sb.append(this.getLabel());
        } else {
            for (int i = this.getStartSpan(); i < this.getEndSpan(); i++) {
                sb.append(this.textAnnotation.getToken(i)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * This method returns the <i>original</i> surface form of the constituent. To retrieve a
     * <i>tokenized</i> form please use {@link #getTokenizedSurfaceForm()}
     *
     * @return The original (char-offset-based) form of the constituent.
     */
    public String getSurfaceForm() {
        return this.textAnnotation.text.substring(startCharOffset, endCharOffset);
    }

    /**
     * @return the textAnnotation
     */
    public TextAnnotation getTextAnnotation() {
        return textAnnotation;
    }

    /**
     * @return the viewName
     */
    public String getViewName() {
        return viewName;
    }

    public View getView() {
        return this.getTextAnnotation().getView(viewName);
    }

    public boolean hasAttribute(String key) {
        return (this.attributes != null) && (attributes.containsKey(key));
    }

    /**
     * @return
     */
    @Override
    public int hashCode() {

        int hashCode = this.getTextAnnotation().getText().hashCode() * 37;

        hashCode += this.getStartSpan() * 41;
        hashCode += this.getEndSpan() * 43;

        hashCode += this.getLabel().hashCode() * 91;
        hashCode += (this.attributes == null ? 0 : this.attributes.hashCode() * 7);
        hashCode += (this.labelsToScores == null ? 0 : this.labelsToScores.hashCode() * 23);
        hashCode += (new Double(this.constituentScore)).hashCode() * 67;
        hashCode += this.getViewName().hashCode();

//        for (Relation relation : this.getIncomingRelations()) {
//
//            hashCode += relation.getRelationName().hashCode() * 3;
//            hashCode += relation.getSource().getStartSpan() * 11;
//            hashCode += relation.getSource().getEndSpan() * 17;
//        }
//
//        for (Relation relation : this.getOutgoingRelations()) {
//
//            hashCode += relation.getRelationName().hashCode() * 5;
//            hashCode += relation.getTarget().getStartSpan() * 13;
//            hashCode += relation.getTarget().getEndSpan() * 19;
//
//        }

        return hashCode;

    }

    public boolean isConsituentInRange(int start, int end) {
        return (this.getStartSpan() >= start) && (this.getEndSpan() <= end);
    }

    void registerRelationSource(Relation relation) {
        this.outgoingRelations.add(relation);
    }

    void registerRelationTarget(Relation relation) {
        this.incomingRelations.add(relation);
    }

    /**
     * Get a list of relations where the source is this constituent.
     *
     * @return A list of {@code Relation}s
     */
    public List<Relation> getOutgoingRelations() {
        return this.outgoingRelations;
    }

    /**
     * Get a list of relations where the target is this constituent
     *
     * @return A list of {@code Relation}s
     */
    public List<Relation> getIncomingRelations() {
        return this.incomingRelations;
    }

    /**
     * Convert this constituent into an S-Expression, including relation labels.
     *
     * @return A string, where
     */
    public String toSExpression() {
        return toSExpression(true);
    }

    public String toSExpression(boolean includeEdgeLabels) {
        return toSExpression(0, true, "", includeEdgeLabels);
    }

    protected String toSExpression(int spaces, boolean firstChild, String prefix,
            boolean includeEdgeLabels) {
        StringBuilder sb = new StringBuilder();

        if (!firstChild) {
            for (int i = 0; i < spaces; i++) {
                sb.append(" ");
            }
        }

        sb.append("(");

        if (includeEdgeLabels && prefix.length() > 0)
            sb.append(":LABEL:").append(prefix).append(" ");

        sb.append(this.toString()).append(" ");
        int len = sb.length();
        boolean isFirstChild = true;

        // for (Relation relation : this.sourceOfRelations())
        for (int i = 0; i < this.getOutgoingRelations().size(); i++) {
            String childPrefix = this.getOutgoingRelations().get(i).getRelationName();

            Constituent child = this.getOutgoingRelations().get(i).getTarget();
            sb.append(child.toSExpression(spaces + len, isFirstChild, childPrefix,
                    includeEdgeLabels));
            isFirstChild = false;

            if (i < this.getOutgoingRelations().size() - 1)
                sb.append("\n");
        }

        sb.append(")");

        return sb.toString();

    }

    @Override
    public String toString() {
        return this.getTokenizedSurfaceForm();
    }

    /**
     * Return the identifier of the sentence that contains this constituent. No sentence contains
     * this constituent (that is, if this constituent is an implicit one), then return -1.
     */
    public int getSentenceId() {
        try {
            return this.getTextAnnotation().getSentenceId(this.getStartSpan());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Do not use this function. Use getSpan() instead.
     */
    @Deprecated
    public TreeSet<Integer> getConstituentTokens() {
        TreeSet<Integer> toksCovered = new TreeSet<>();
        int start = this.getStartSpan();
        int end = start + this.getNumberOfTokens();

        for (int i = start; i < end; ++i) {
            toksCovered.add(i);
        }

        return toksCovered;
    }

    public Constituent cloneForNewView(String newViewName) {
        Constituent cloneC =
                new Constituent(this.labelsToScores, this.getLabel(), this.getConstituentScore(), newViewName,
                        this.getTextAnnotation(), this.getStartSpan(), this.getEndSpan());

        for (String k : this.getAttributeKeys()) {
            cloneC.addAttribute(k, this.getAttribute(k));
        }

        return cloneC;
    }

    public Constituent cloneForNewViewWithDestinationLabel(String newViewName, String Dlabel) {
        Constituent cloneC =
                new Constituent(this.labelsToScores, Dlabel, this.getConstituentScore(), newViewName,
                        this.getTextAnnotation(), this.getStartSpan(), this.getEndSpan());
        for (String k : this.getAttributeKeys()) {
            cloneC.addAttribute(k, this.getAttribute(k));
        }
        return cloneC;
    }

    public void removeIncomingRelaton(Relation r) {
        this.incomingRelations.remove(r);
    }

    public void removeAllIncomingRelatons() {
        this.incomingRelations.clear();
    }

    public void removeOutgoingRelation(Relation r) {
        this.outgoingRelations.remove(r);
    }

    public void removeAllOutgoingRelaton() {
        this.outgoingRelations.clear();
    }

    /**
     * Removes all attributes from a Constituent.
     */
    public void removeAllAttributes() {
        this.attributes = null;
    }

}
