/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

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
 * Extracts the following features from two constituents
 * <ul>
 * <li>The concatenation of shallow parse chunks between the last token of the constituent to the
 * left and the first token of the other one. This feature also indicates where in the sequence the
 * input constituent occurs (see note below for explanation).</li>
 * <li>The number of such chunks</li>
 * </ul>
 * <p>
 * <p>
 * <b>Important note</b>: To be able to specify the two constituents as input, the feature extractor
 * assumes the following convention: The constituent that is specified as a parameter to the
 * getFeatures function has an incoming relation from the first constituent. Furthermore, this
 * incoming relation should be the only such relation.
 * <p>
 * This convention does not limit the expressivity in any way because the two constituents could be
 * created on the spot before calling the feature extractor.
 *
 * @author Vivek Srikumar
 */
public class ChunkPathPattern implements FeatureExtractor {

    public static ChunkPathPattern SHALLOW_PARSE = new ChunkPathPattern(ViewNames.SHALLOW_PARSE);

    private final String viewName;

    public ChunkPathPattern(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        List<Relation> incomingRelation = c.getIncomingRelations();
        Set<Feature> features = new LinkedHashSet<>();
        if (incomingRelation.size() > 0) {
            TextAnnotation ta = c.getTextAnnotation();
            Constituent pred = incomingRelation.get(0).getSource();

            SpanLabelView shallowParse = (SpanLabelView) ta.getView(viewName);

            List<Constituent> constituents = new ArrayList<>();

            boolean beforePredicate = Queries.before(pred).transform(c);
            boolean afterPredicate = Queries.after(pred).transform(c);
            if (beforePredicate)
                constituents =
                        SpanLabelsHelper.getConstituentsInBetween(shallowParse, c.getEndSpan(),
                                pred.getStartSpan());
            else if (afterPredicate)
                constituents =
                        SpanLabelsHelper.getConstituentsInBetween(shallowParse, pred.getEndSpan(),
                                c.getStartSpan());

            Collections.sort(constituents, new Comparator<Constituent>() {
                public int compare(Constituent o1, Constituent o2) {
                    if (o1.getStartSpan() < o2.getStartSpan())
                        return -1;
                    else if (o1.getStartSpan() > o2.getStartSpan())
                        return 1;
                    else
                        return 0;
                }
            });

            StringBuilder sb = new StringBuilder();
            for (Constituent constituent : constituents) {
                sb.append(constituent.getLabel()).append("-");
            }

            if (constituents.size() <= 1)
                sb.append("<empty>");

            if (beforePredicate)
                features.add(DiscreteFeature.create("*" + sb.toString()));
            else if (afterPredicate)
                features.add(DiscreteFeature.create(sb.toString() + "*"));
            else
                features.add(DiscreteFeature.create("same"));

            features.add(RealFeature.create("l", constituents.size()));
        }
        return features;
    }

    @Override
    public String getName() {
        return "#path#" + viewName;
    }

}
