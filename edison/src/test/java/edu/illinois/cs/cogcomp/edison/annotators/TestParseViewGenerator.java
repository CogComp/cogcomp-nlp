/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import edu.illinois.cs.cogcomp.edison.features.helpers.ParseHelper;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadDependencyParser;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vivek Srikumar
 */
public class TestParseViewGenerator {
    private static Logger logger = LoggerFactory.getLogger(TestParseViewGenerator.class);

    @Test
    public final void testCharniakParseViewGenerator() {
        String sentence = "This is a test .";

        String treeString =
                "(S1 (S (NP (DT This))   (VP (AUX is)       (NP (DT a)           (NN test)))(. .)))";

        Tree<String> tree = TreeParserFactory.getStringTreeParser().parse(treeString);

        TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(sentence);

        TreeView parseView = new TreeView(ViewNames.PARSE_CHARNIAK, "My_PARSER", ta, 1d);
        parseView.setParseTree(0, tree);

        ta.addView(ViewNames.PARSE_CHARNIAK, parseView);

        TreeView view = (TreeView) ta.getView(ViewNames.PARSE_CHARNIAK);
        logger.info(ParseHelper.getParseTree(ViewNames.PARSE_CHARNIAK, ta, 0).toString());

        assertEquals(tree, view.getTree(0));

        CollinsHeadDependencyParser depParser = new CollinsHeadDependencyParser(true);
        Tree<Pair<String, Integer>> depTree =
                depParser.getLabeledDependencyTree(view.getRootConstituent(0));

        TreeView depView = new TreeView(ViewNames.DEPENDENCY, "HeadRuleDependencyTree", ta, 1.0);

        logger.info(depTree.toString());

        depView.setDependencyTree(0, depTree);

        ta.addView(ViewNames.DEPENDENCY, depView);

        logger.info(depView.toString());

        logger.info(depView.getTree(0).toString());
    }
}
