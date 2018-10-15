/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 *
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A SpanLabelView is a specialized view which corresponds to contiguous chunks of tokens that have
 * a label. Each chunk corresponds to a single {@code Consituent}. In this view, there will be no
 * {@code Relation}s between the constituents.
 * <p>
 * This class is best suited for views like Shallow parse and Named Entities.
 *
 * @author Vivek Srikumar
 */
public class SpanLabelView extends View {

    /**
     * serialization id
     */
    private static final long serialVersionUID = -7114594071137672509L;

    /**
     * Should overlapping spans be allowed?
     */
    private boolean allowOverlappingSpans;

    /**
     * Create a new SpanLabelView with default {@link #viewGenerator} and {@link #score}.
     * 
     * @param viewName the name of the view
     * @param text the TextAnnotation to augment
     */
    public SpanLabelView(String viewName, TextAnnotation text) {
        this(viewName, viewName + "-annotator", text, 1.0, false);
    }

    /**
     * Create a new SpanLabelView
     */
    public SpanLabelView(String viewName, String viewGenerator, TextAnnotation text, double score) {
        this(viewName, viewGenerator, text, score, false);
    }

    public SpanLabelView(String viewName, String viewGenerator, TextAnnotation text, double score,
            boolean allowOverlappingSpans) {
        super(viewName, viewGenerator, text, score);
        this.allowOverlappingSpans = allowOverlappingSpans;
    }

    @Override
    public void addConstituent(Constituent constituent) {
        super.addConstituent(constituent);

        // this sort is grossly inefficient when appending contiguous tokens one at a time. 
        // we add a check so we only do the sort if the constituents are added out of order.
        // Better yet use an ordered tree map representation, do an insertion sort.
        int size = this.constituents.size();
        if (size > 1) {
            Constituent before = this.constituents.get(size-2);
            Constituent after = this.constituents.get(size-1);
            if (before.getStartSpan() > after.getStartSpan()) {
                Collections.sort(this.constituents, TextAnnotationUtilities.constituentStartComparator);
            }
        }
    }

    /**
     * Adds a new span to this view with a given label and score and returns the newly created
     * constituent.
     * <p>
     * If this {@code SpanLabelView} was defined not to accept overlapping spans (in the
     * constructor), then this function will throw an {@link IllegalArgumentException} when an
     * attempt is made to label an already existing span.
     *
     * @param start the start of the span
     * @param end the end of the span
     * @param label the label of the span
     * @param score the score assigned to this label
     * @return the newly created constituent that labels the given span.
     */
    public Constituent addSpanLabel(int start, int end, String label, double score) {

        Constituent c =
                new Constituent(label, score, this.getViewName(), this.getTextAnnotation(), start,
                        end);

        if (!allowOverlappingSpans && this.getConstituentsCoveringSpan(start, end).size() != 0)
            throw new IllegalArgumentException("Span [" + start + ", " + end + "] already labeled.");

        this.addConstituent(c);

        return c;
    }

    public String getLabel(int tokenId) {
        List<String> labelsCoveringToken = getLabelsCoveringToken(tokenId);

        if (labelsCoveringToken.size() == 0)
            return "";
        else
            return labelsCoveringToken.get(0);
    }

    public List<Constituent> getSpanLabels(int start, int end) {
        List<Constituent> labeledConstituents = new ArrayList<>();
        for (Constituent c : this.constituents) {
            if (c.getStartSpan() >= start && c.getEndSpan() < end) {
                labeledConstituents.add(c);
            }
        }
        return labeledConstituents;
    }

    @Override
    public String toString() {
        List<Constituent> constituents = new ArrayList<>(this.getConstituents());

        Collections.sort(constituents, TextAnnotationUtilities.constituentStartComparator);

        StringBuilder sb = new StringBuilder();
        for (Constituent c : constituents)
            sb.append("[").append(c.getLabel()).append(" ").append(c.getTokenizedSurfaceForm())
                    .append(" ] ");

        return sb.toString();
    }
}
