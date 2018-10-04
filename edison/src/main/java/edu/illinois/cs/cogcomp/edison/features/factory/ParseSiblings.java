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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Extracts the phrase type, head word and head POS of the left and right siblings of the input
 * constituent. Uses the specified parse view and the Collins' head percolation rules to find the
 * heads.
 * <p>
 * If possible, use one of the two static objects in this class, for the Charniak and Stanford
 * parses respectively.
 *
 * @author Vivek Srikumar
 */
public class ParseSiblings implements FeatureExtractor {

    public static ParseSiblings CHARNIAK = new ParseSiblings(ViewNames.PARSE_CHARNIAK);
    public static ParseSiblings STANFORD = new ParseSiblings(ViewNames.PARSE_STANFORD);

    private final String parseViewName;

    public ParseSiblings(String parseViewName) {
        this.parseViewName = parseViewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();
        TreeView parse = (TreeView) ta.getView(parseViewName);

        Constituent phrase;
        try {
            phrase = parse.getParsePhrase(c);
        } catch (Exception e) {
            throw new EdisonException(e);
        }

        Set<Feature> features = new LinkedHashSet<>();

        if (phrase.getIncomingRelations().size() == 0) {
            features.add(DiscreteFeature.create("ONLY_CHILD"));
        } else {
            Relation incomingEdge = phrase.getIncomingRelations().get(0);
            Constituent parent = incomingEdge.getSource();

            int position = -1;
            for (int i = 0; i < parent.getOutgoingRelations().size(); i++) {
                if (parent.getOutgoingRelations().get(i) == incomingEdge) {
                    position = i;
                    break;
                }
            }

            assert position >= 0;

            if (position == 0)
                features.add(DiscreteFeature.create("FIRST_CHILD"));
            else if (position == parent.getOutgoingRelations().size() - 1)
                features.add(DiscreteFeature.create("LAST_CHILD"));

            if (position != 0) {
                Constituent sibling = parent.getOutgoingRelations().get(position - 1).getTarget();

                String phraseType = sibling.getLabel();
                int headWord = CollinsHeadFinder.getInstance().getHeadWordPosition(sibling);

                String token = ta.getToken(headWord).toLowerCase().trim();
                String pos = WordHelpers.getPOS(ta, headWord);

                features.add(DiscreteFeature.create("lsis.pt:" + phraseType));
                features.add(DiscreteFeature.create("lsis.hw:" + token));
                features.add(DiscreteFeature.create("lsis.hw.pos:" + pos));

            }

            if (position != parent.getOutgoingRelations().size() - 1) {
                Constituent sibling = parent.getOutgoingRelations().get(position + 1).getTarget();

                String phraseType = sibling.getLabel();
                int headWord = CollinsHeadFinder.getInstance().getHeadWordPosition(sibling);

                String token = ta.getToken(headWord).toLowerCase().trim();
                String pos = WordHelpers.getPOS(ta, headWord);

                features.add(DiscreteFeature.create("rsis.pt:" + phraseType));
                features.add(DiscreteFeature.create("rsis.hw:" + token));
                features.add(DiscreteFeature.create("rsis.hw.pos:" + pos));

            }
        }

        return features;
    }

    @Override
    public String getName() {
        return "#parse-siblings#" + parseViewName;
    }

}
