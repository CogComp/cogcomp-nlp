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
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.features.helpers.PathFeatureHelper;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Given two constituents, this feature extractor extracts the following features:
 * <ul>
 * <li>The path in the parse tree from the one constituent to another</li>
 * <li>The length of the parse path</li>
 * </ul>
 * <p>
 * The parse tree is read from a view name that is specified in the constructor. If possible, use
 * the static objects for Charniak and Stanford parses.
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
public class ParsePath implements FeatureExtractor {
    public static ParsePath CHARNIAK = new ParsePath(ViewNames.PARSE_CHARNIAK);
    public static ParsePath STANFORD = new ParsePath(ViewNames.PARSE_STANFORD);

    private final static Logger log = LoggerFactory.getLogger(Feature.class);

    private final String parseViewName;

    public ParsePath(String parseViewName) {
        // it should belong to the parse view
        if (!ViewNames.isItParseView(parseViewName)) {
            log.warn("The view doesn't seem to belong to the parse view . . . ");
        }
        this.parseViewName = parseViewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();
        TreeView parse = (TreeView) ta.getView(parseViewName);
        Set<Feature> features = new LinkedHashSet<>();
        List<Relation> incomingRelations = c.getIncomingRelations();

        if(incomingRelations.size() > 0) {
            Constituent c1, c2;
            try {
                c1 = parse.getParsePhrase(incomingRelations.get(0).getSource());
                c2 = parse.getParsePhrase(c);
            } catch (Exception e) {
                throw new EdisonException(e);
            }

            Pair<List<Constituent>, List<Constituent>> paths =
                    PathFeatureHelper.getPathsToCommonAncestor(c1, c2, 400);
            List<Constituent> list = new ArrayList<>();

            for (int i = 0; i < paths.getFirst().size() - 1; i++) {
                list.add(paths.getFirst().get(i));
            }

            Constituent top = paths.getFirst().get(paths.getFirst().size() - 1);
            list.add(top);

            for (int i = paths.getSecond().size() - 2; i >= 0; i--) {
                list.add(paths.getSecond().get(i));
            }

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < paths.getFirst().size() - 1; i++) {

                Constituent cc = paths.getFirst().get(i);
                sb.append(cc.getLabel());

                sb.append(PathFeatureHelper.PATH_UP_STRING);
            }
            String pathToAncestor = sb.toString();

            String pathString = PathFeatureHelper.getPathString(paths, true, false);

            features.add(DiscreteFeature.create(pathString));
            features.add(DiscreteFeature.create(pathToAncestor));
            features.add(RealFeature.create("l", list.size()));
        }
        return features;

    }

    @Override
    public String getName() {
        return "#path#" + parseViewName;
    }

}
