/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestHeadFinderDependencyHelper {

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

        System.out.println(ta.getView(ViewNames.PARSE_CHARNIAK));

        HeadFinderDependencyViewGenerator dep =
                new HeadFinderDependencyViewGenerator(ViewNames.PARSE_CHARNIAK);

        TreeView depTree = null;
        try {
            depTree = (TreeView) dep.getView(ta);
        } catch (AnnotatorException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        System.out.println(depTree);

        assertEquals(depTree.getNumberOfConstituents(), ta.size());
    }
}
