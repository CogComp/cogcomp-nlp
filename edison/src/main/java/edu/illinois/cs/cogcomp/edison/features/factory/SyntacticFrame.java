/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
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
import edu.illinois.cs.cogcomp.nlp.utilities.ParseTreeProperties;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This feature is described in Xue and Palmer 2004.
 *
 * @author Vivek Srikumar
 */
public class SyntacticFrame implements FeatureExtractor {

    public static final FeatureExtractor CHARNIAK = new SyntacticFrame(ViewNames.PARSE_CHARNIAK);
    public static final FeatureExtractor STANFORD = new SyntacticFrame(ViewNames.PARSE_STANFORD);
    private final String parseViewName;

    /**
	 *
	 */
    public SyntacticFrame(String parseViewName) {
        this.parseViewName = parseViewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<>();
        List<Relation> incomingRelations = c.getIncomingRelations();

        if (incomingRelations.size() > 0) {
            Constituent pred = incomingRelations.get(0).getSource();
            TextAnnotation ta = c.getTextAnnotation();
            TreeView parse = (TreeView) ta.getView(parseViewName);

            Constituent predicate, arg;
            try {
                predicate = parse.getParsePhrase(pred);
                arg = parse.getParsePhrase(c);
            } catch (Exception e) {
                throw new EdisonException(e);
            }

            Constituent vp = TreeView.getParent(predicate);

            // go over VP's siblings before it

            StringBuffer sb1 = new StringBuffer();

            StringBuffer sb2 = new StringBuffer();

            StringBuffer sb3 = new StringBuffer();

            if (!TreeView.isRoot(vp)) {
                Constituent vpParent = TreeView.getParent(vp);

                for (int i = 0; i < vpParent.getOutgoingRelations().size(); i++) {
                    Constituent target = vpParent.getOutgoingRelations().get(i).getTarget();
                    if (target == vp)
                        break;
                    addToFeature(target, arg, sb1, sb2, sb3);
                }
            }

            for (int i = 0; i < vp.getOutgoingRelations().size(); i++) {
                Constituent target = vp.getOutgoingRelations().get(i).getTarget();

                if (target.getSpan().equals(predicate.getSpan())) {
                    sb1.append("v-");
                    sb2.append("v-");
                    sb3.append(WordHelpers.getLemma(ta, target.getStartSpan())).append("-");
                } else {
                    addToFeature(target, arg, sb1, sb2, sb3);
                }
            }

            features.add(DiscreteFeature.create(sb1.toString()));
            features.add(DiscreteFeature.create("general:" + sb2.toString()));
            features.add(DiscreteFeature.create("lemma:" + sb3.toString()));
        }
        return features;
    }

    private void addToFeature(Constituent target, Constituent arg, StringBuffer sb1,
            StringBuffer sb2, StringBuffer sb3) {
        final IntPair span = target.getSpan();
        final String label = target.getLabel();
        if (ParseTreeProperties.isNominal(label)) {

            if (span.equals(arg.getSpan())) {
                sb1.append(label);
                sb2.append("CUR");
                sb3.append("CUR");
            } else {
                sb1.append(label.toLowerCase());
                sb2.append(label.toLowerCase());
                sb3.append(label.toLowerCase());
            }
            sb1.append("-");
            sb2.append("-");
            sb3.append("-");
        }
    }

    @Override
    public String getName() {
        return "#syn-frame#";
    }

}
