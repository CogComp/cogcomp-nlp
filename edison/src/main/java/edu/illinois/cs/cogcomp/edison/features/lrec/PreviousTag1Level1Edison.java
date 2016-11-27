/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.features.helpers.SpanLabelsHelper;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.*;

/**
 * Created by Yewen Fan on 10/11/16.
 */
public class PreviousTag1Level1Edison implements FeatureExtractor {

    public static View TOKENS;
    public static View NER;

    private final String viewName;

    public PreviousTag1Level1Edison(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        String classifier = "PreviousTag1Level1";

        TextAnnotation ta = c.getTextAnnotation();

        TOKENS = ta.getView(ViewNames.TOKENS);
        NER = ta.getView(ViewNames.NER_CONLL);
        // NER = ta.getView(ViewNames.NER_ONTONOTES);

        Set<Feature> result = new LinkedHashSet<Feature>();

        int previous_token_id = c.getStartSpan() - 1;
        if (previous_token_id < 0) {
            return result;
        }

        Constituent previous = TOKENS.getConstituentsCoveringToken(previous_token_id).get(0);

        String id = "-1";
        String value = "";
        List<String> labels = NER.getLabelsCovering(previous);
        if (labels.size() > 0) {
            value = labels.get(0);
        }

        result.add(new DiscreteFeature(classifier + ":" + id + "(" + value + ")"));

        return result;
    }

    @Override
    public String getName() {
        return "#path#" + viewName;
    }
}
