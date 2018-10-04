/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CollinsHeadFinderTest {

    private TextAnnotation ta;

    @Before
    public void setUp() {
        String[] sentence =
                "Central Asia , North east Europe and Africa are continents .".split(" ");
        String treeString =
                "(S1 (S (NP (NP (NNP Central)             "
                        + "  (NNP Asia))           (, ,)        "
                        + "   (NP (NNP North)               (NNP east)  "
                        + "             (NNP Europe))           (CC and)           "
                        + "(NP (NNP Africa)))       (VP (AUX are)       "
                        + "    (NP (NNS continents)))       (. .)))";

        Tree<String> tree = TreeParserFactory.getStringTreeParser().parse(treeString);

        ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections
                        .singletonList(sentence));

        TreeView view = new TreeView(ViewNames.PARSE_GOLD, "", ta, 1.0);
        view.setParseTree(0, tree);
        ta.addView(ViewNames.PARSE_GOLD, view);
    }

    @Test
    public void testGetHeadword() throws Exception {
        TreeView view = (TreeView) ta.getView(ViewNames.PARSE_GOLD);

        CollinsHeadFinder headFinder = CollinsHeadFinder.getInstance();

        Constituent headWord = headFinder.getHeadWord(view.getRootConstituent(0));
        assertEquals("are", headWord.getTokenizedSurfaceForm());

        Constituent parsePhrase = view.getParsePhrase(new Constituent("", "", ta, 0, 8));

        headWord = headFinder.getHeadWord(parsePhrase);
        assertEquals("Asia", headWord.getTokenizedSurfaceForm());
    }
}
