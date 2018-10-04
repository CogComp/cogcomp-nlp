/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Given two tokens, this feature extractor extracts the following features:
 * <ul>
 * <li>The path (without tokens) in the dependency tree from the first token of the first
 * constituent to the first token of the second one.</li>
 * <li>The path, where tokens are represented by their POS tag</li>
 * <li>The length of the path</li>
 * </ul>
 * <p>
 * The dependency tree is read from the view name specified in the constructor. If possible, use the
 * static objects for the Easy-first dependency tree (which uses {@link ViewNames#DEPENDENCY}) or
 * the Stanford dependency tree (which uses {@link ViewNames#DEPENDENCY_STANFORD})
 * <p>
 * <b>Important note</b>: To be able to specify the two constituents as input, the feature extractor
 * assumes the following convention: The constituent that is specified as a parameter to the
 * getFeatures function has an incoming relation from the first constituent. Furthermore, this
 * incoming relation should be the only such relation.
 * <p>
 * This convention does not limit the expressivity in any way because the two constituents could be
 * created on the spot before calling the feature extractor.
 * <p>
 *
 * @author Vivek Srikumar
 */
public class DependencyPath implements FeatureExtractor {
    public static DependencyPath EASY_FIRST = new DependencyPath(ViewNames.DEPENDENCY);

    public static DependencyPath STANFORD = new DependencyPath(ViewNames.DEPENDENCY_STANFORD);

    private final String dependencyViewName;

    /**
	 *
	 */
    public DependencyPath(String dependencyViewName) {
        this.dependencyViewName = dependencyViewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();

        TreeView parse = (TreeView) ta.getView(dependencyViewName);

        Constituent c1 =
                parse.getConstituentsCoveringToken(
                        c.getIncomingRelations().get(0).getSource().getStartSpan()).get(0);
        Constituent c2 = parse.getConstituentsCoveringToken(c.getStartSpan()).get(0);

        Pair<List<Constituent>, List<Constituent>> paths =
                PathFeatureHelper.getPathsToCommonAncestor(c1, c2, 400);

        int length = paths.getFirst().size() + paths.getSecond().size() - 1;

        StringBuilder path = new StringBuilder();
        StringBuilder pos = new StringBuilder();

        for (int i = 0; i < paths.getFirst().size() - 1; i++) {
            Constituent cc = paths.getFirst().get(i);
            path.append(cc.getIncomingRelations().get(0).getRelationName()).append(
                    PathFeatureHelper.PATH_UP_STRING);

            pos.append(WordHelpers.getPOS(ta, cc.getStartSpan()));
            pos.append(cc.getIncomingRelations().get(0).getRelationName()).append(
                    PathFeatureHelper.PATH_UP_STRING);

        }

        Constituent top = paths.getFirst().get(paths.getFirst().size() - 1);

        pos.append(WordHelpers.getPOS(ta, top.getStartSpan()));
        pos.append("*");
        path.append("*");

        if (paths.getSecond().size() > 1) {
            for (int i = paths.getSecond().size() - 2; i >= 0; i--) {
                Constituent cc = paths.getSecond().get(i);

                pos.append(WordHelpers.getPOS(ta, cc.getStartSpan()));
                pos.append(PathFeatureHelper.PATH_DOWN_STRING);
                path.append(PathFeatureHelper.PATH_DOWN_STRING);

            }
        }

        Set<Feature> features = new LinkedHashSet<>();

        features.add(DiscreteFeature.create(path.toString()));
        features.add(DiscreteFeature.create("pos" + pos.toString()));
        features.add(RealFeature.create("l", length));

        return features;

    }

    @Override
    public String getName() {
        return "#path#" + dependencyViewName;
    }

}
