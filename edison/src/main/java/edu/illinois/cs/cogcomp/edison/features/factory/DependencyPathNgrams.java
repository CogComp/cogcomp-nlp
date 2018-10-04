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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.features.helpers.FeatureNGramUtility;
import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Given two tokens, this feature extractor extracts the following features:
 * <ul>
 * <li>The path in the dependency tree from the first token of the first constituent to the first
 * token of the second one.</li>
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
@SuppressWarnings("serial")
public class DependencyPathNgrams implements FeatureExtractor {

    private final static ITransformer<String, String> transformer =
            new ITransformer<String, String>() {

                @Override
                public String transform(String input) {
                    return input;
                }
            };
    public static DependencyPathNgrams EASY_FIRST_UNIGRAM = new DependencyPathNgrams(
            ViewNames.DEPENDENCY, 1);
    public static DependencyPathNgrams STANFORD_UNIGRAM = new DependencyPathNgrams(
            ViewNames.DEPENDENCY_STANFORD, 1);
    public static DependencyPathNgrams EASY_FIRST_BIGRAM = new DependencyPathNgrams(
            ViewNames.DEPENDENCY, 2);
    public static DependencyPathNgrams STANFORD_BIGRAM = new DependencyPathNgrams(
            ViewNames.DEPENDENCY_STANFORD, 2);
    private final String dependencyViewName;
    private final int ngramSize;

    /**
     * Extracting ngram features along the dependency path
     */
    public DependencyPathNgrams(String dependencyViewName, int ngramSize) {
        this.dependencyViewName = dependencyViewName;
        this.ngramSize = ngramSize;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();
        Set<Feature> features = new LinkedHashSet<>();
        TreeView parse = (TreeView) ta.getView(dependencyViewName);
        // get equivalent of c in the parse view
        Constituent c2 = parse.getConstituentsCoveringToken(c.getStartSpan()).get(0);
        List<Relation> incomingRelations = c2.getIncomingRelations();
        if (incomingRelations.size() > 0) {
            Constituent c1 =
                    parse.getConstituentsCoveringToken(
                            incomingRelations.get(0).getSource().getStartSpan()).get(0);

            Pair<List<Constituent>, List<Constituent>> paths =
                    PathFeatureHelper.getPathsToCommonAncestor(c1, c2, 400);

            List<String> path = new ArrayList<>();
            List<String> pos = new ArrayList<>();

            for (int i = 0; i < paths.getFirst().size() - 1; i++) {
                Constituent cc = paths.getFirst().get(i);
                path.add(cc.getIncomingRelations().get(0).getRelationName()
                        + PathFeatureHelper.PATH_UP_STRING);

                pos.add(WordHelpers.getPOS(ta, cc.getStartSpan()) + ":"
                        + cc.getIncomingRelations().get(0).getRelationName()
                        + PathFeatureHelper.PATH_UP_STRING);

            }

            Constituent top = paths.getFirst().get(paths.getFirst().size() - 1);

            pos.add(WordHelpers.getPOS(ta, top.getStartSpan()) + ":*");
            path.add("*");

            if (paths.getSecond().size() > 1) {
                for (int i = paths.getSecond().size() - 2; i >= 0; i--) {
                    Constituent cc = paths.getSecond().get(i);

                    pos.add(WordHelpers.getPOS(ta, cc.getStartSpan()) + ":"
                            + PathFeatureHelper.PATH_DOWN_STRING);
                    path.add(PathFeatureHelper.PATH_DOWN_STRING);

                }
            }

            features.addAll(getNgrams(path, ""));
            features.addAll(getNgrams(pos, "pos"));
        }
        return features;

    }

    private Set<Feature> getNgrams(List<String> list, String prefix) {

        Set<Feature> feats = FeatureNGramUtility.getNgramsUnordered(list, ngramSize, transformer);

        return FeatureUtilities.prefix(prefix, feats);
    }

    @Override
    public String getName() {
        return "#path-n#" + dependencyViewName;
    }

}
