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
import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class ClauseFeatureExtractor implements FeatureExtractor {

    public static ClauseFeatureExtractor CHARNIAK = new ClauseFeatureExtractor(
            ViewNames.PSEUDO_PARSE_CHARNIAK, ViewNames.CLAUSES_CHARNIAK);
    public static ClauseFeatureExtractor STANFORD = new ClauseFeatureExtractor(
            ViewNames.PSEUDO_PARSE_STANFORD, ViewNames.CLAUSES_STANFORD);
    public static ClauseFeatureExtractor BERKELEY = new ClauseFeatureExtractor(
            ViewNames.PSEUDO_PARSE_BERKELEY, ViewNames.CLAUSES_BERKELEY);
    private final String parseViewName;
    private final String clauseViewName;

    public ClauseFeatureExtractor(String pseudoParseViewName, String clauseViewName) {
        this.parseViewName = pseudoParseViewName;
        this.clauseViewName = clauseViewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();
        TreeView pseudoParseView;
        // If the real parse view exists, use that.
        // Normally, this feature requires the PSEUDO_PARSE to be manually created by the user
        // as it is cheaper to make than a full parse. However, there are cases
        // where the user has already created a full parse view (for other features)
        if (ta.hasView(parseViewName.replace("PSEUDO_", "")))
            pseudoParseView = (TreeView) ta.getView(parseViewName.replace("PSEUDO_", ""));
        else
            pseudoParseView = (TreeView) ta.getView(parseViewName);
        List<Relation> incomingRelations = c.getIncomingRelations();
        Set<Feature> features = new LinkedHashSet<>();

        if (incomingRelations.size() > 0) {
            Constituent p = incomingRelations.get(0).getSource();

            String clauseRelativePosition;
            try {
                Constituent arg = pseudoParseView.getParsePhrase(c);
                Constituent pred = pseudoParseView.getParsePhrase(p);

                Constituent ca = PathFeatureHelper.getCommonAncestor(arg, pred, 400);
                Constituent argParent = TreeView.getParent(arg);
                Constituent predParent = TreeView.getParent(pred);
                if (argParent == ca && predParent == ca)
                    clauseRelativePosition = "S";
                else if (argParent == ca || arg == ca)
                    clauseRelativePosition = "A";
                else if (predParent == ca || pred == ca)
                    clauseRelativePosition = "B";
                else
                    clauseRelativePosition = "O";
            } catch (Exception ex) {
                clauseRelativePosition = "O";
            }

            SpanLabelView clause = (SpanLabelView) ta.getView(clauseViewName);
            List<Constituent> clauses = clause.getConstituentsCovering(p);
            float ratio = 0;
            if (clauses.size() > 0) {
                Constituent local = clauses.get(0);
                if (Queries.hasOverlap(local).transform(c))
                    ratio = c.size() * 1.0f / local.size();
            }

            features.add(RealFeature.create("coverage", ratio));
            features.add(DiscreteFeature.create("rel-pos:" + clauseRelativePosition));
        }
        return features;
    }

    @Override
    public String getName() {
        return "#clause#" + clauseViewName;
    }

}
