/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 
 * @author Xinbo Wu
 */
public class PPFeatures implements FeatureExtractor {
    private final String parseViewName;

    public PPFeatures(String parseViewName) {
        this.parseViewName = parseViewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();

        TreeView parse = (TreeView) ta.getView(parseViewName);

        Set<Feature> feats = new HashSet<>();
        try {
            Constituent phrase = parse.getParsePhrase(c);
            // if the phrase is a PP, then the head word of its
            // rightmost NP child.

            List<Relation> rels = phrase.getOutgoingRelations();
            for (int i = rels.size() - 1; i >= 0; i--) {
                Relation relation = rels.get(i);
                if (relation == null)
                    continue;
                Constituent target = relation.getTarget();
                if (ParseTreeProperties.isNominal(target.getLabel())) {
                    int head = CollinsHeadFinder.getInstance().getHeadWordPosition(phrase);

                    feats.add(DiscreteFeature.create("np-head:" + ta.getToken(head).toLowerCase()));
                    feats.add(DiscreteFeature.create("np-head-pos:" + WordHelpers.getPOS(ta, head)));

                    break;
                }
            }

            // if the phrase's parent is a PP, then the head of that PP.
            Constituent parent = phrase.getIncomingRelations().get(0).getSource();

            if (parent.getLabel().equals("PP")) {
                int head = CollinsHeadFinder.getInstance().getHeadWordPosition(phrase);
                feats.add(DiscreteFeature.create("p-head:" + ta.getToken(head).toLowerCase()));
            }

        } catch (EdisonException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return feats;
    }

    @Override
    public String getName() {
        return "#pp-feats";
    }
}
