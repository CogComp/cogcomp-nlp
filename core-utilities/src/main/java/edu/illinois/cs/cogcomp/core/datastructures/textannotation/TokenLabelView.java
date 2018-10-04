/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import java.util.List;

/**
 * A TokenLabelView is a specialization of a
 * {@link edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView}, where the
 * length of a labeled span is one. In terms of the constituents and relations view, this means that
 * the TokenLabelView has {@code Constituent}s that are one token long and does not have any
 * {@code Relation}s. This class provides methods to add and get labels for a single token.
 * <p>
 * This class is best suited for views like Part-of-speech, lemma, etc.
 *
 * @author Vivek Srikumar
 */
public class TokenLabelView extends SpanLabelView {

    private static final long serialVersionUID = 2993609232596055554L;

    /**
     * Create a new TokenLabelView with default {@link #viewGenerator} and {@link #score}.
     */
    public TokenLabelView(String viewName, TextAnnotation text) {
        this(viewName, viewName + "-annotator", text, 1.0);
    }

    public TokenLabelView(String viewName, String viewGenerator, TextAnnotation text, double score) {
        super(viewName, viewGenerator, text, score);
    }

    /**
     * Adds a label to a token and returns the newly created constituent.
     *
     * @see edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView#addSpanLabel(int,
     *      int, String, double)
     */
    public Constituent addTokenLabel(int tokenId, String label, double score) {
        return this.addSpanLabel(tokenId, tokenId + 1, label, score);
    }

    public void addTokenAttribute(int tokenId, String attributeKey, String attributeValue)
            throws Exception {
        Constituent c = getConstituentAtToken(tokenId);

        if (c == null)
            throw new Exception("Trying to add attribute to non-existent constituent at token "
                    + tokenId);

        c.addAttribute(attributeKey, attributeValue);
    }

    public String getTokenAttribute(int tokenId, String key) {
        Constituent c = getConstituentAtToken(tokenId);

        if (c == null)
            return null;

        return c.getAttribute(key);
    }

    public Constituent getConstituentAtToken(int tokenId) {
        List<Constituent> c = this.getConstituentsCoveringToken(tokenId);

        if (c.size() == 0)
            return null;
        else
            return c.get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.textAnnotation.size(); i++) {

            String label = this.getLabel(i);
            if (label.length() == 0)
                sb.append(this.getTextAnnotation().getToken(i)).append(" ");
            else
                sb.append("(").append(this.getLabel(i)).append(" ")
                        .append(this.textAnnotation.getToken(i)).append(") ");
        }
        return sb.toString();
    }
}
