/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Vivek Srikumar
 */
public class TestCollinsHeadFinder {

    @Test
    public final void test1() throws Exception {

        String sentence = "Central Asia , North east Europe and Africa are continents .";
        String treeString =
                "(S1 (S (NP (NP (NNP Central)               (NNP Asia))           (, ,)          "
                        + " (NP (NNP North)               (NNP east)               (NNP Europe))          "
                        + " (CC and)           (NP (NNP Africa)))       (VP (AUX are)         "
                        + "  (NP (NNS continents)))       (. .)))";

        Tree<String> tree = TreeParserFactory.getStringTreeParser().parse(treeString);

        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections
                        .singletonList(sentence.split("\\s+")));

        TreeView view = new TreeView("", "", ta, 1.0);
        view.setParseTree(0, tree);

        CollinsHeadFinder headFinder = new CollinsHeadFinder();

        Constituent headWord = headFinder.getHeadWord(view.getRootConstituent(0));

        assertEquals(headWord.getStartSpan(), 8);

        Constituent c = new Constituent("", "", ta, 9, 10);
        headWord = headFinder.getHeadWord(view.getParsePhrase(c));

        assertEquals(headWord.getStartSpan(), 9);
    }

    @Test
    public final void test2() throws Exception {
        String sentence = "Mr. Gorbachev 's publicized tongue - lashing of the press on Oct. 13";

        String treeString =
                "(S (NP (NNP Mr.)	(NNP Gorbachev)	(POS 's))    (VP (VBN publicized)      "
                        + "  (NP (NP (NN tongue)		(HYPH -)		(VBG lashing))            (PP (IN of)    "
                        + "            (NP (DT the)                    (NN press))))        (PP (IN on)  "
                        + "          (NP (NNP Oct.)                (CD 13)))))";

        Tree<String> tree = TreeParserFactory.getStringTreeParser().parse(treeString);

        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(Collections
                        .singletonList(sentence.split("\\s+")));

        TreeView view = new TreeView("", "", ta, 1.0);
        view.setParseTree(0, tree);

        CollinsHeadFinder headFinder = new CollinsHeadFinder();

        Constituent headWord = headFinder.getHeadWord(view.getRootConstituent(0));

        assertEquals(headWord.getStartSpan(), 3);

        Constituent c = new Constituent("", "", ta, 7, 10);
        headWord = headFinder.getHeadWord(view.getParsePhrase(c));

        assertEquals(headWord.getStartSpan(), 7);
    }
}
