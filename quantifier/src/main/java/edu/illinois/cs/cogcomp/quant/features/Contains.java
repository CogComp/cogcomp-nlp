/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.features;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.*;

public class Contains extends LBJavaFeatureExtractor {

    private static final long serialVersionUID = 1L;
    private final static DiscreteFeature Y = DiscreteFeature.create("Y");
    public final static String YValue = "[] |B|:Y(true)";
    private final static DiscreteFeature N = DiscreteFeature.create("N");
    public final static String NValue = "[] |B|:N(true)";
    private final List<String> contained;
    private final String viewName;

    /** Checks for prepositions (including "TO"), particles and adverbs */
    public static final Contains containsPrepPartAdv = new Contains(ViewNames.POS, "IN", "TO",
            "RP", "RB");

    public Contains() {
        this("", "");
    }

    public Contains(String viewName, String... contained) {
        this.contained = Arrays.asList(contained);
        this.viewName = viewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent instance) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<Feature>();

        TextAnnotation ta = instance.getTextAnnotation();

        View view = ta.getView(viewName);
        List<Constituent> lsc = view.getConstituentsCovering(instance);

        if (lsc.size() == 0) {
            features.add(N);
            return features;
        }
        boolean contains = false;
        for (Constituent c : lsc)
            if (contained.contains(c.getTokenizedSurfaceForm()) || contained.contains(c.getLabel())) {
                contains = true;
                break;
            }

        if (contains)
            features.add(Y);
        else
            features.add(N);

        return features;
    }
}
