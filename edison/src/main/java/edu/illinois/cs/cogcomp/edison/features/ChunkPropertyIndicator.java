/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.transformers.Predicate;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class ChunkPropertyIndicator implements FeatureExtractor {

    private static final DiscreteFeature N = DiscreteFeature.create("N");
    private static final DiscreteFeature Y = DiscreteFeature.create("Y");
    private final Predicate<Constituent> property;
    private final String name;
    private final String viewName;

    public ChunkPropertyIndicator(String viewName, String name, Predicate<Constituent> property) {
        this.viewName = viewName;
        this.name = name;
        this.property = property;

    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        TextAnnotation ta = c.getTextAnnotation();

        SpanLabelView shallowParse = (SpanLabelView) ta.getView(viewName);
        List<Constituent> lsc = shallowParse.getConstituentsCovering(c);

        Set<Feature> features = new LinkedHashSet<>();
        if (lsc.size() == 0) {
            features.add(N);
        } else {
            for (Constituent chunk : lsc) {
                if (property.transform(chunk)) {
                    features.add(Y);
                    break;
                }
            }
        }

        return features;

    }

    @Override
    public String getName() {
        return "chunk-" + name + "-" + viewName;
    }

}
