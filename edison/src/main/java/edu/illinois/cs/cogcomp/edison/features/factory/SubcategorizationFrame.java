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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Given a constituent, this feature finds the corresponding node in the parse tree and reports
 * expands its parent. For verb SRL_VERB, for example, the subcategorization frame would be the
 * expansion of the verb's parent in the parse tree. The parse phrase corresponding to the input
 * constituent is marked in the feature.
 *
 * @author Vivek Srikumar
 */
public class SubcategorizationFrame implements FeatureExtractor {

    public static SubcategorizationFrame CHARNIAK = new SubcategorizationFrame(
            ViewNames.PARSE_CHARNIAK);
    public static SubcategorizationFrame STANFORD = new SubcategorizationFrame(
            ViewNames.PARSE_STANFORD);

    private final String parseViewName;

    public SubcategorizationFrame(String parseViewName) {
        this.parseViewName = parseViewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        Set<Feature> features = new LinkedHashSet<>();
        TreeView view = (TreeView) c.getTextAnnotation().getView(parseViewName);

        Constituent phrase;
        try {
            phrase = view.getParsePhrase(c);
        } catch (Exception e) {
            throw new EdisonException(e);
        }

        List<Relation> incomingRelations = phrase.getIncomingRelations();

        if (incomingRelations == null) {
            features.add(DiscreteFeature.create("root"));
        } else {
            Constituent parent = incomingRelations.get(0).getSource();

            StringBuilder subcat = new StringBuilder();

            subcat.append(parent.getLabel()).append(">");

            for (Relation r : parent.getOutgoingRelations()) {
                if (r.getTarget() == phrase) {
                    subcat.append("(").append(r.getTarget().getLabel()).append(")");
                } else {
                    subcat.append(r.getTarget().getLabel());
                }
            }

            features.add(DiscreteFeature.create(subcat.toString()));
        }

        return features;
    }

    @Override
    public String getName() {
        return "#subcat:" + parseViewName;
    }

}
