/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.core.datastructures.IQueryable;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.QueryableList;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;

import java.io.Serializable;
import java.util.*;

/**
 * This is the abstract class that represents annotation for text -- this could represent a
 * paragraph or just a sentence.
 * <p>
 * This class stores the raw text, the tokens and the list of views available for the text and
 * provides methods for adding new views.
 * <p>
 *
 * @author Vivek Srikumar
 * @see edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation
 * @see edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence
 */
public abstract class AbstractTextAnnotation implements Serializable {

    private static final long serialVersionUID = 4055102006554736491L;

    protected AbstractTextAnnotation() {
        views = new HashMap<>();
        tokenizedText = "";
    }

    /**
     * The raw text
     */
    public String text;

    /**
     * The list of tokens in the text. This might be a contentious issue because different
     * annotators might use different tokenization schemes. One way to fix that would be to use some
     * sort of common subsequence finding algorithm to align the tokenization to the canonical one.
     */
    protected String[] tokens;

    /**
     * The character offsets of the tokens.
     */
    protected IntPair[] tokenCharacterOffsets;

    /**
     * The list of views
     */
    protected Map<String, List<View>> views;

    /**
     * The tokenized text
     */
    protected String tokenizedText;

    /**
     * Adds a new view identified by {@code viewName} and sets the top K values for this view.
     *
     * @param viewName The name of the new view to be added.
     * @param view The top K views
     */
    public void addTopKView(String viewName, List<View> view) {
        if (this instanceof TextAnnotation && view.get(0).getTextAnnotation() != this) {
            throw new IllegalArgumentException(
                    "Trying to add a view belonging to a different TextAnnotation!");
        }

        // sort in descending order by score
        Collections.sort(view, new Comparator<View>() {
            public int compare(View o1, View o2) {
                if (o1.getScore() > o2.getScore())
                    return -1;
                else if (o1.getScore() == o2.getScore())
                    return 0;
                else
                    return 1;
            }
        });

        views.put(viewName, view);
    }

    public void addViews(String[] viewNames, View[] views) {
        if (viewNames.length != views.length)
            throw new IllegalArgumentException("different number of viewNames (" + viewNames.length
                    + ") and views (" + views.length + ").");

        for (int i = 0; i < viewNames.length; ++i)
            addView(viewNames[i], views[i]);
    }

    /**
     * Adds the top scoring value for the view identified by {@code viewName}.
     */
    public void addView(String viewName, View view) {
        views.put(viewName, Collections.singletonList(view));
    }

    /**
     * Remove a given view
     */
    public void removeView(String viewName) {
        views.remove(viewName);
    }

    public void removeAllViews() {
        views.clear();
    }

    /**
     * Gets the set of views that are available
     *
     * @return A {@code Set<String>} of views that are available for this text annotation.
     */
    public Set<String> getAvailableViews() {
        return this.views.keySet();
    }

    /**
     * Gets the highest scoring value for the view identified by {@code viewName}.
     */
    public View getView(String viewName) {
        if (!hasView(viewName))
            throw new IllegalArgumentException("View " + viewName + " not found");
        return views.get(viewName).get(0);
    }

    /**
     * Get all the values available for a given view, identified by {@code viewName}
     */
    public List<View> getTopKViews(String viewName) {
        if (!hasView(viewName))
            throw new IllegalArgumentException("View " + viewName + " not found");
        return views.get(viewName);
    }

    /**
     * Gets the raw text
     *
     * @return The raw text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the tokenized text.
     *
     * @return The tokenized text, as a string where each token is separated by a space.
     */
    public String getTokenizedText() {
        if (tokenizedText == null || tokenizedText.length() == 0) {
            StringBuilder sb = new StringBuilder();
            for (String str : getTokens()) {
                sb.append(str).append(" ");
            }
            tokenizedText = sb.toString().trim();
        }
        return tokenizedText;
    }

    /**
     * Gets the tokens in this text.
     */
    public String[] getTokens() {
        return tokens;
    }

    /**
     * Get the token at position from this text
     */
    public String getToken(int position) {
        return tokens[position].trim();
    }

    /**
     * Get the character offset of the token at position in the raw text.
     *
     * @return An {@link edu.illinois.cs.cogcomp.core.datastructures.IntPair} representing the pair
     *         (start, end+1)
     */
    public IntPair getTokenCharacterOffset(int position) {
        return this.tokenCharacterOffsets[position];
    }

    /**
     * Gets the tokens in the span.
     *
     * @param start The start of the span
     * @param end The end of the span
     */
    public String[] getTokensInSpan(int start, int end) {
        String[] tokensInSpan = new String[end - start];
        System.arraycopy(tokens, start, tokensInSpan, 0, end - start);
        return tokensInSpan;
    }

    /**
     * Checks if this text annotation has a view identified by {@code viewName}
     *
     * @return {@code true} if the text annotation contains a view called {@code viewName} and
     *         <code>false</code> otherwise
     */
    public boolean hasView(String viewName) {
        return views.containsKey(viewName);
    }

    /**
     * Set the tokens for this text.
     * <p>
     * <b>NOTE</b>: This function should not be called by any method that is not a {@code Tokenizer}
     * and will throw an exception if tokens are set more than once. So do not call this function
     * unless you know what you are doing.
     * <p>
     *
     * @param tokens An array of tokens
     * @param tokenCharacterOffsets An array, whose length is the same as {@code tokens}. Each
     *        element indicates the character offset of this token in the raw text.
     * @throws IllegalArgumentException if the tokens are set more than once.
     * @deprecated There is now a TOKENS view that should be used to access the tokens
     */
    public void setTokens(String[] tokens, IntPair[] tokenCharacterOffsets) {
        if (this.tokens == null) {
            this.tokens = tokens;
            this.tokenCharacterOffsets = tokenCharacterOffsets;
        } else
            throw new IllegalStateException("Attempting to set the tokens of the sentence twice.");
    }

    /**
     * Get the number of tokens in this text.
     *
     * @return the number of tokens
     */
    public int size() {
        return tokens.length;
    }

    /**
     * Gets a queryable list of all constituents from the view, represented by {@code viewName}.
     * This function can be used for SQL-like queries on the text.
     * <p>
     * Examples: In all the examples that follow, we assume that {@code text} is a variable of type
     * {@code AbstractTextAnnotation}.
     * <p>
     * <ol>
     * <li>Given a constituent {@code srlConstituent} from the SRL_VERB view, find all constituents
     * in the parse view that cover exactly this constituent.
     * <p>
     * <p>
     * 
     * <pre>
     * List&lt;Constituent&gt; parseConstituent = text.select(ViewNames.PARSE).where(
     *         Queries.sameSpanAsConstituent(srlConstituent));
     * </pre>
     * 
     * <p>
     * </ol>
     *
     * @see QueryableList
     * @see edu.illinois.cs.cogcomp.core.datastructures.ViewNames
     */
    public IQueryable<Constituent> select(String viewName) {
        if (this.hasView(viewName)) {
            return this.getView(viewName);
        } else {
            return new QueryableList<>();
        }
    }
}
