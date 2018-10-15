/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.FeatureNGramUtility;
import edu.illinois.cs.cogcomp.edison.features.helpers.SpanLabelsHelper;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class SpanFeaturesUnordered implements FeatureExtractor {
    public static SpanFeaturesUnordered POS_UNIGRAMS = new SpanFeaturesUnordered(ViewNames.POS, 1);
    public static SpanFeaturesUnordered POS_BIGRAMS = new SpanFeaturesUnordered(ViewNames.POS, 2);
    public static SpanFeaturesUnordered POS_TRIGRAMS = new SpanFeaturesUnordered(ViewNames.POS, 3);
    public static SpanFeaturesUnordered SHALLOW_PARSE_UNIGRAMS = new SpanFeaturesUnordered(
            ViewNames.SHALLOW_PARSE, 1);
    public static SpanFeaturesUnordered SHALLOW_PARSE_BIGRAMS = new SpanFeaturesUnordered(
            ViewNames.SHALLOW_PARSE, 2);

    private final String viewName;
    private final int ngramLength;

    public SpanFeaturesUnordered(String viewName, int ngramLength) {
        this.viewName = viewName;
        this.ngramLength = ngramLength;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        SpanLabelView chunks = (SpanLabelView) c.getTextAnnotation().getView(viewName);

        List<Constituent> list =
                SpanLabelsHelper.getConstituentsInBetween(chunks, c.getStartSpan(), c.getEndSpan());

        Collections.sort(list, TextAnnotationUtilities.constituentStartComparator);

        return FeatureNGramUtility.getLabelNgramsUnordered(list, ngramLength);
    }

    @Override
    public String getName() {
        return "#unordered-span#" + viewName;
    }
}
