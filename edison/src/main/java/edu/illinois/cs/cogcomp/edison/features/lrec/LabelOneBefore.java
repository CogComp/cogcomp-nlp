/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.POSBaseLineCounter;
import edu.illinois.cs.cogcomp.edison.utilities.POSMikheevCounter;


/**
 * This feature extractor extracts part of speeches of the words one before each word in a span.
 *
 * @keywords pos-tagger, before
 * @author Xinbo Wu
 */
public class LabelOneBefore implements FeatureExtractor {
    private final String viewName;
    private final boolean isPOSFromCounting;
    private final boolean isBaseLineCounting;
    protected POSBaseLineCounter counter;

    public LabelOneBefore(String viewName) {
        this.isPOSFromCounting = false;
        this.isBaseLineCounting = false;
        this.viewName = viewName;
    }

    public LabelOneBefore(String viewName, POSBaseLineCounter counter) {
        this.isPOSFromCounting = true;
        this.isBaseLineCounting = true;
        this.viewName = viewName;
        this.counter = counter;
    }

    public LabelOneBefore(String viewName, POSMikheevCounter counter) {
        this.isPOSFromCounting = true;
        this.isBaseLineCounting = false;
        this.viewName = viewName;
        this.counter = counter;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        String classifier;
        String prefix = "LabelOneBefore";

        TextAnnotation ta = c.getTextAnnotation();

        int start = c.getStartSpan() - 1;
        int end = c.getEndSpan() - 1;

        Set<Feature> features = new LinkedHashSet<>();

        for (int i = start; i < end; i++) {
            if (!isPOSFromCounting) {
                classifier = prefix + "_" + "POS";

                if (i >= 0) {
                    TokenLabelView POSView = (TokenLabelView) ta.getView(ViewNames.POS);

                    String form = ta.getToken(i);
                    String tag = POSView.getLabel(i);
                    features.add(new DiscreteFeature(classifier + ":" + tag + "_" + form));

                } else
                    features.add(new DiscreteFeature(classifier + ":" + ""));

            } else if (isBaseLineCounting) {
                classifier = prefix + "_" + "BaselinePOS";
                if (i >= 0) {
                    String form = ta.getToken(i);
                    String tag = counter.tag(i, ta);
                    features.add(new DiscreteFeature(classifier + ":" + tag + "_" + form));

                } else
                    features.add(new DiscreteFeature(classifier + ":" + ""));

            } else {
                classifier = prefix + "_" + "MikheevPOS";
                if (i >= 0) {
                    String form = ta.getToken(i);
                    String tag = counter.tag(i, ta);
                    features.add(new DiscreteFeature(classifier + ":" + tag + "_" + form));

                } else
                    features.add(new DiscreteFeature(classifier + ":" + ""));

            }
        }

        return features;
    }

    @Override
    public String getName() {
        return "#path#" + this.viewName;
    }
}
