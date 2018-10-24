/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import static org.junit.Assert.fail;

public class ClauseViewGeneratorTest {

    private static Logger logger = LoggerFactory.getLogger(ClauseViewGeneratorTest.class);

//    public void setUp() throws Exception {
//        super.setUp();
//    }

    @Test
    public final void testClauseViewGenerator() {
        String text =
                "Freeport-McMoRan Inc. said it will convert its Freeport-McMoRan Energy Partners Ltd. "
                        + "partnership into a publicly traded company through the exchange of units of the partnership "
                        + "for common shares .";
        TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(text);

        Tree<String> tree =
                TreeParserFactory
                        .getStringTreeParser()
                        .parse("(S1 (S (NP-SBJ (NNP Freeport-McMoRan)               (NNP Inc.))       (VP (VBD said)"
                                + "           (SBAR (-NONE- 0)                 (S (NP-SBJ (PRP it))                    "
                                + "(VP (MD will)                        (VP (VB convert)                            "
                                + "(NP (PRP$ its)                                (NNP Freeport-McMoRan) "
                                + "                               (NNP Energy)                                (NNPS Partners)"
                                + "                                (NNP Ltd.)                                (NN partnership)) "
                                + "                           (PP-CLR (IN into)                                    (NP (DT a)"
                                + "                                        (ADJP (RB publicly)"
                                + "                                              (VBN traded))"
                                + "                                        (NN company))) "
                                + "                           (PP-MNR (IN through) "
                                + "                                   (NP (NP (DT the)    "
                                + "                                        (NN exchange))    "
                                + "                                    (PP (IN of)            "
                                + "                                (NP (NP (NNS units))           "
                                + "                                     (PP (IN of)                   "
                                + "                                 (NP (DT the)                           "
                                + "                             (NN partnership)))))                            "
                                + "            (PP (IN for)                                            (NP (JJ common) "
                                + "                                               (NNS shares))))))))))       (. .)))");

        TreeView parse = new TreeView("", ta);
        parse.setParseTree(0, tree);

        ta.addView(ViewNames.PARSE_GOLD, parse);

        ClauseViewGenerator clg = new ClauseViewGenerator(ViewNames.PARSE_GOLD, "clauses");

        try {
            ta.addView(clg);
        } catch (AnnotatorException e) {
            fail(e.getMessage());
        }

        logger.info(ta.getView("clauses").toString());
    }
}
