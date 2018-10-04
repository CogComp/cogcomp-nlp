/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.CommaLabeler;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.utilities.StringUtils;

/**
 * Used to group all the commas in a sentence. The commas can then be accessed based on positional
 * relations to each other within the sentence
 * 
 * @author navari
 *
 */
public class CommaSRLSentence implements Serializable {
    protected final TextAnnotation ta;// automatic annotations
    protected final TextAnnotation goldTa;// gold standard annotations
    private final List<Comma> commas;// commas in sentence ordered by position
    private static final long serialVersionUID = 2522617554768671154L;

    /**
     * helper for constructing sentences in which all commas have a single label
     */
    public static CommaSRLSentence makeSentence(TextAnnotation ta, TextAnnotation goldTa,
                                                List<String> singleLabels) throws Exception {
        List<List<String>> commaLabels = new ArrayList<>();
        for (int i = 0; i < singleLabels.size(); i++) {
            if (commaLabels.get(i) == null)
                commaLabels.add(null);
            else
                commaLabels.add(Collections.singletonList(singleLabels.get(i)));
        }
        return new CommaSRLSentence(ta, goldTa, commaLabels);
    }

    /**
     * If labels are not given construct commas and assign the labels according to the
     * bayraktar-syntax-pattern to comma label mappings
     */
    public CommaSRLSentence(TextAnnotation ta, TextAnnotation goldTa) {
        this.ta = ta;
        this.goldTa = goldTa;
        commas = new ArrayList<>();
        for (int i = 0; i < ta.getTokens().length; i++) {
            if (ta.getToken(i).equals(",")) {
                Comma comma = new Comma(i, this);
                commas.add(comma);
            }
        }
    }

    /**
     * Constructor for sentence which in turn constructs commas in the sentence. Labels for all
     * commas that you are interested in must be provided. Labels-lists for other commas must be set
     * to null and they won't be included.
     * 
     * @param ta the automatic
     * @param goldTa
     * @param labels List of list of labels for each comma
     * @throws Exception throws exception if number of comma-label-lists provided is not equal to
     *         number of commas in the sentence
     */
    public CommaSRLSentence(TextAnnotation ta, TextAnnotation goldTa, List<List<String>> labels)
            throws Exception {
        this.ta = ta;
        this.goldTa = goldTa;
        commas = new ArrayList<>();
        String[] tokens = ta.getTokens();
        int numCommas = 0;
        for (int tokenIdx = 0; tokenIdx < tokens.length; tokenIdx++) {
            if (!tokens[tokenIdx].equals(","))
                continue;
            List<String> labelsForCurrIdx = labels.get(numCommas);
            numCommas++;
            if (labelsForCurrIdx == null) {
                if (!CommaProperties.getInstance().includeNullLabelCommas())
                    continue;
                Comma comma = new Comma(tokenIdx, this, Collections.singletonList("Other"));
                commas.add(comma);
            } else if (CommaProperties.getInstance().allowMultiLabelCommas()) {
                Comma comma = new Comma(tokenIdx, this, labelsForCurrIdx);
                commas.add(comma);
            } else {
                for (int labelIdx = 0; labelIdx < labelsForCurrIdx.size(); labelIdx++) {
                    Comma comma =
                            new Comma(tokenIdx, this, Collections.singletonList(labelsForCurrIdx
                                    .get(labelIdx)));
                    commas.add(comma);
                }
            }
        }

        if (numCommas != labels.size())
            throw new Exception("must provide labels for all commas in sentence");
    }

    public CommaSRLSentence(TextAnnotation ta) {
        this.ta = ta;
        this.goldTa = ta;
        commas = new ArrayList<>();
        PredicateArgumentView commaView = (PredicateArgumentView) ta.getView(CommaLabeler.viewName);
        List<Constituent> preds = commaView.getPredicates();
        Collections.sort(preds, TextAnnotationUtilities.constituentStartComparator);
        for (int predIdx = 0; predIdx < preds.size(); predIdx++) {
            List<String> labels = new ArrayList<>();
            labels.add(preds.get(predIdx).getLabel());
            int commaPosition = preds.get(predIdx).getStartSpan();
            for (int nextPredIdx = predIdx + 1; nextPredIdx < preds.size(); nextPredIdx++) {
                if (preds.get(nextPredIdx).getStartSpan() == commaPosition) {
                    labels.add(preds.get(nextPredIdx).getLabel());
                    predIdx++;
                } else
                    break;
            }
            commas.add(new Comma(commaPosition, this, labels));
        }
    }

    /**
     *
     * @return commas in the sentence ordered by position
     */
    public List<Comma> getCommas() {
        return commas;
    }

    public Collection<Comma> getFirstCommasWhichAreNotLast() {
        Collection<Comma> firstCommasWhichAreNotLast = new ArrayList<>();
        for (Comma c : commas)
            if (getNextComma(c) != null && getPreviousComma(c) == null)
                firstCommasWhichAreNotLast.add(c);
        return firstCommasWhichAreNotLast;
    }

    public Collection<Comma> getMiddleCommas() {
        Collection<Comma> middleCommas = new ArrayList<>();
        for (Comma c : commas)
            if (getNextComma(c) != null && getPreviousComma(c) != null)
                middleCommas.add(c);
        return middleCommas;
    }

    public Comma getNextComma(Comma curr_c) {
        Comma next_c = null;
        int curr_diff = Integer.MAX_VALUE;
        for (Comma c : commas) {
            if (c.commaPosition > curr_c.commaPosition
                    && (c.commaPosition - curr_c.commaPosition < curr_diff)) {
                next_c = c;
                curr_diff = next_c.commaPosition - curr_c.commaPosition;
            }
        }
        return next_c;
    }

    public Comma getPreviousComma(Comma curr_c) {
        Comma prev_c = null;
        int curr_diff = Integer.MAX_VALUE;
        for (Comma c : commas) {
            if (c.commaPosition < curr_c.commaPosition
                    && (curr_c.commaPosition - c.commaPosition < curr_diff)) {
                prev_c = c;
                curr_diff = curr_c.commaPosition - prev_c.commaPosition;
            }
        }
        return prev_c;
    }

    public Collection<Comma> getFirstSiblingCommasWhichAreNotLast() {
        Collection<Comma> firstCommasWhichAreNotLast = new ArrayList<>();
        for (Comma c : commas)
            if (getNextSiblingComma(c) != null && getPreviousSiblingComma(c) == null)
                firstCommasWhichAreNotLast.add(c);

        return firstCommasWhichAreNotLast;
    }

    public Collection<Comma> getMiddleSiblingCommas() {
        Collection<Comma> middleCommas = new ArrayList<>();
        for (Comma c : commas)
            if (getNextSiblingComma(c) != null && getPreviousSiblingComma(c) != null)
                middleCommas.add(c);
        return middleCommas;
    }

    public Comma getNextSiblingComma(Comma curr_c) {
        Comma next_c = null;
        int curr_diff = Integer.MAX_VALUE;
        for (Comma c : curr_c.getSiblingCommas()) {
            if (c.commaPosition > curr_c.commaPosition
                    && (c.commaPosition - curr_c.commaPosition < curr_diff)) {
                next_c = c;
                curr_diff = next_c.commaPosition - curr_c.commaPosition;
            }
        }
        return next_c;
    }

    public Comma getPreviousSiblingComma(Comma curr_c) {
        Comma prev_c = null;
        int curr_diff = Integer.MAX_VALUE;
        for (Comma c : curr_c.getSiblingCommas()) {
            if (c.commaPosition < curr_c.commaPosition
                    && (curr_c.commaPosition - c.commaPosition < curr_diff)) {
                prev_c = c;
                curr_diff = curr_c.commaPosition - prev_c.commaPosition;
            }
        }
        return prev_c;
    }



    /**
     *
     * @return String representation of the sentence with all the commas embedded into the string
     */
    public String getAnnotatedText() {
        String[] tokens = ta.getTokens();
        int commaNum = 0;
        String annotatedText = tokens[0];
        for (int tokenIdx = 1; tokenIdx < tokens.length; tokenIdx++) {
            annotatedText += " " + tokens[tokenIdx];
            String commaAnnotation = "";
            while (commaNum < commas.size() && commas.get(commaNum).commaPosition == tokenIdx) {
                commaAnnotation += StringUtils.join(",", commas.get(commaNum).getLabels());
                commaNum++;
            }
            if (commaAnnotation.length() > 0)
                annotatedText += String.format("[%s]", commaAnnotation);
        }
        return annotatedText;
    }

    public String getId() {
        return commas.get(0).getTextAnnotation(true).getId();
    }

}
