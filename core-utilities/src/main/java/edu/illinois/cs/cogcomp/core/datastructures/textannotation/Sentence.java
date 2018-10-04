/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.core.utilities.AvoidUsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentence class is a read-only projection of a TextAnnotation with all views restricted to a single sentence.
 * For making changes to any views, you will have to edit the source TextAnnotation instance that was used to
 * create the Sentence instance.
 *
 * @author Vivek Srikumar
 */
public class Sentence extends AbstractTextAnnotation implements Serializable {

    private static final long serialVersionUID = 526983395380035851L;

    protected TextAnnotation textAnnotation;

    protected Constituent sentenceConstituent;

    @SuppressWarnings("serial")
    protected final static ITransformer<View, Double> defaultViewScoreSplitter =
            new ITransformer<View, Double>() {

                @Override
                public Double transform(View input) {
                    return input.getScore();
                }
            };

    /**
     * Create a sentence out of a sentenceConstituent.
     */
    public Sentence(Constituent sentenceConstituent) {
        super();
        this.sentenceConstituent = sentenceConstituent;
        this.textAnnotation = sentenceConstituent.textAnnotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Sentence))
            return false;

        Sentence that = (Sentence) obj;

        return this.sentenceConstituent.equals(that.sentenceConstituent)
                && this.getTokenizedText().equals(that.getTokenizedText());
    }

    public int getEndSpan() {
        return this.sentenceConstituent.getEndSpan();
    }

    @Override
    public View getView(String viewName) {
        checkViewAvailable(viewName);
        return super.getView(viewName);
    }

    @Override
    public boolean hasView(String viewName) {
        return this.textAnnotation.hasView(viewName);
    }

    public Constituent getSentenceConstituent() {
        return sentenceConstituent;
    }

    public int getStartSpan() {
        return this.sentenceConstituent.getStartSpan();
    }

    @Override
    public List<View> getTopKViews(String viewName) {
        checkViewAvailable(viewName);
        return super.getTopKViews(viewName);
    }

    @Override
    public String[] getTokens() {
        initializeTokens();
        return tokens;
    }

    @Override
    public int hashCode() {
        return this.sentenceConstituent.hashCode() * 43 + this.getTokenizedText().hashCode() * 31;
    }

    @Override
    public String toString() {
        return this.getTokenizedText();
    }

    @Override
    public int size() {
        initializeTokens();
        return super.size();
    }

    @Override
    public String getText() {

        int start = sentenceConstituent.getStartCharOffset();
        int end = sentenceConstituent.getEndCharOffset();
        return textAnnotation.getText().substring(start, end);
    }

    @Override
    public String getTokenizedText() {
        initializeTokens();
        return sentenceConstituent.getTokenizedSurfaceForm();
    }

    @Override
    public String getToken(int position) {
        initializeTokens();
        return super.getToken(position);
    }

    private void checkViewAvailable(String viewName) {
        if (!views.containsKey(viewName)) {

            List<View> taViews = textAnnotation.getTopKViews(viewName);
            List<View> myViews = new ArrayList<>();

            for (View v : taViews) {
                View restriction =
                        v.getViewCoveringSpan(this.getStartSpan(), this.getEndSpan(),
                                Sentence.defaultViewScoreSplitter);
                myViews.add(restriction);
            }

            this.addTopKView(viewName, myViews);
        }
    }

    private void initializeTokens() {
        if (tokens == null) {
            tokens = textAnnotation.getTokensInSpan(this.getStartSpan(), this.getEndSpan());
        }
    }

    public int getSentenceId() {
        return this.textAnnotation.getSentenceId(this.getStartSpan());
    }

    /** Overrides for forbidden methods. */

    @Override
    @AvoidUsing(reason = "Sentence class is read-only. Edit the source TextAnnotation instance directly.")
    public void addTopKView(String viewName, List<View> view) {
        throw new UnsupportedOperationException("Editing a Sentence instance is not supported. " +
                "You should edit the source text annotation instance.");
    }

    @Override
    @AvoidUsing(reason = "Sentence class is read-only. Edit the source TextAnnotation instance directly.")
    public void addViews(String[] viewNames, View[] views) {
        throw new UnsupportedOperationException("Editing a Sentence instance is not supported. " +
                "You should edit the source TextAnnotation instance.");
    }

    @Override
    @AvoidUsing(reason = "Sentence class is read-only. Edit the source TextAnnotation instance directly.")
    public void addView(String viewName, View view) {
        throw new UnsupportedOperationException("Editing a Sentence instance is not supported. " +
                "You should edit the source TextAnnotation instance.");
    }

    @Override
    @AvoidUsing(reason = "Sentence class is read-only. Edit the source TextAnnotation instance directly.")
    public void removeView(String viewName) {
        throw new UnsupportedOperationException("Editing a Sentence instance is not supported. " +
                "You should edit the source TextAnnotation instance.");
    }

    @Override
    @AvoidUsing(reason = "Sentence class is read-only. Edit the source TextAnnotation instance directly.")
    public void removeAllViews() {
        throw new UnsupportedOperationException("Editing a Sentence instance is not supported. " +
                "You should edit the source TextAnnotation instance.");
    }

    @Override
    @AvoidUsing(reason = "Sentence class is read-only. Edit the source TextAnnotation instance directly.")
    public void setTokens(String[] tokens, IntPair[] tokenCharacterOffsets) {
        throw new UnsupportedOperationException("Editing a Sentence instance is not supported. " +
                "You should edit the source TextAnnotation instance.");
    }
}
