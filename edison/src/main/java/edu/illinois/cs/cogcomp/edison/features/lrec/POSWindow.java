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
 * Feature extractor that senses the parts of speech of the four context words immediately
 * surrounding the target word (two before and two after).
 *
 * @keywords pos-tagger, window
 * @author Xinbo Wu
 */
public class POSWindow implements FeatureExtractor {
    private final String viewName;
    private final boolean isPOSFromCounting;
    private final boolean isBaseLineCounting;
    protected POSBaseLineCounter counter;

    public POSWindow(String viewName) {
        this.isPOSFromCounting = false;
        this.isBaseLineCounting = false;
        this.viewName = viewName;
    }

    public POSWindow(String viewName, POSBaseLineCounter counter) {
        this.isPOSFromCounting = true;
        this.isBaseLineCounting = true;
        this.viewName = viewName;
        this.counter = counter;
    }

    public POSWindow(String viewName, POSMikheevCounter counter) {
        this.isPOSFromCounting = true;
        this.isBaseLineCounting = false;
        this.viewName = viewName;
        this.counter = counter;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        String classifier;
        String prefix = "POSWindow";

        TextAnnotation ta = c.getTextAnnotation();
        int lenOfTokens = ta.getTokens().length;

        int start = c.getStartSpan();
        int end = c.getEndSpan();

        Set<Feature> features = new LinkedHashSet<>();

        for (int i = start; i < end; i++) {
            int curr = i, last = i;

            // for (; curr >= i-2 && curr > 0; --curr)
            // for (; last <= i+2 && last < lenOfTokens; ++last)
            for (int j = 0; j < 2 && curr > 0; j++)
                curr -= 1;
            for (int j = 0; j < 2 && last < lenOfTokens - 1; j++)
                last += 1;

            if (!isPOSFromCounting) {
                classifier = prefix + "_" + "POS";

                for (; curr <= last; curr++) {
                    TokenLabelView POSView = (TokenLabelView) ta.getView(ViewNames.POS);

                    String form = ta.getToken(curr);
                    String tag = POSView.getLabel(curr);
                    features.add(new DiscreteFeature(classifier + ":" + tag + "_" + form));
                }

            } else if (isBaseLineCounting) {
                classifier = prefix + "_" + "BaselinePOS";
                for (; curr <= last; curr++) {
                    String form = ta.getToken(curr);
                    String tag = counter.tag(curr, ta);
                    features.add(new DiscreteFeature(classifier + ":" + tag + "_" + form));
                }


            } else {
                classifier = prefix + "_" + "MikheevPOS";
                for (; curr <= last; curr++) {
                    String form = ta.getToken(curr);
                    String tag = counter.tag(curr, ta);
                    features.add(new DiscreteFeature(classifier + ":" + tag + "_" + form));
                }
            }
        }

        return features;
    }

    @Override
    public String getName() {
        return "#path#" + this.viewName;
    }
}
