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
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestHeadFinderDependencyHelper {
    private static Logger logger = LoggerFactory.getLogger(TestHeadFinderDependencyHelper.class);

    @Test
    public final void testHeadFinderDependencyHelper() {
        String s = "There is no recovery period -- it 's go , go , go .";

        String treeString =
                "(S1 (S (S (NP (EX There))    (VP (AUX is)        (NP (DT no)            (NN recovery) "
                        + "           (NN period))))    (: --)    (S (NP (PRP it))       (VP (AUX 's)           (S (VP (VB go)  "
                        + "   (, ,)     (VB go)     (, ,)     (VB go)))))    (. .)))";

        TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(s);

        TreeView parse = new TreeView(ViewNames.PARSE_CHARNIAK, "", ta, 1.0);
        parse.setParseTree(0, TreeParserFactory.getStringTreeParser().parse(treeString));

        ta.addView(ViewNames.PARSE_CHARNIAK, parse);

        logger.info(ta.getView(ViewNames.PARSE_CHARNIAK).toString());

        HeadFinderDependencyViewGenerator dep =
                new HeadFinderDependencyViewGenerator(ViewNames.PARSE_CHARNIAK);

        TreeView depTree = null;
        try {
            depTree = (TreeView) dep.getView(ta);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        logger.info(depTree.toString());

        assertEquals(depTree.getNumberOfConstituents(), ta.size());
    }
}
