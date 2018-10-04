/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParser;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vivek Srikumar Jun 23, 2009
 */
public class TreeGrepTest {
    private static Logger logger = LoggerFactory.getLogger(TreeGrepTest.class);

    Tree<String> tree;
    List<Tree<String>> foundPatterns;
    List<Tree<String>> notFoundPatterns;

    @Before
    public void init() throws Exception {

        TreeParser<String> parser = TreeParserFactory.getStringTreeParser();

        String treeString =
                "(A (A (B E)       C)   (B (E (D F)	 A)      F)   (C (A (B F G)	 C	 E	 (D H)))   (D H)   (B F G))";
        // String treeString = "(D F)";
        tree = parser.parse(treeString);

        String[] foundPatternStrings =
                new String[] {"(D F)", "A", "(A B)", "(B E)", "(B F)", "(A B C)", "(D H)",
                        "(A B C D)", "(A (B F) C)", "(A (B E) C D)", "(B F G)", "(A (B E) C)",
                        "(A (B E))"};

        // String[] foundPatternStrings = new String[] { "(A B)" };

        foundPatterns = new ArrayList<>();
        for (String p : foundPatternStrings) {
            foundPatterns.add(parser.parse(p));
        }

        String[] notFoundPatternStrings = new String[] {"(A B D)", "(X)", "(A (C E))"};
        notFoundPatterns = new ArrayList<>();
        for (String p : notFoundPatternStrings) {
            notFoundPatterns.add(parser.parse(p));
        }

    }

    @Test
    public void testNotFoundPatternGetMatchPositions() throws Exception {
        for (Tree<String> p : notFoundPatterns) {
            TreeGrep<String> matcher = new TreeGrep<>(p);
            assertEquals(matcher.matches(tree), false);

        }
    }

    @Test
    public void testGetMatchPositions() {

        for (Tree<String> p : foundPatterns) {
            TreeGrep<String> matcher = new TreeGrep<>(p);
            boolean result = matcher.matches(tree);

            // for (TreeGrepMatch<String> match : matcher.getMatches()) {
            // logger.info(match);
            // }

            assertEquals(true, result);

        }

    }

    @Test
    public void testEnd() {
        String[] endPatternStrings = {"(B F $$$)", "(A B C $$$)"};

        for (String endPatternString : endPatternStrings) {
            Tree<String> endPattern =
                    TreeParserFactory.getStringTreeParser().parse(endPatternString);

            TreeGrep<String> matcher = new TreeGrep<>(endPattern);
            boolean result = matcher.matches(tree);

            // for (TreeGrepMatch<String> match : matcher.getMatches()) {
            // logger.info("End: " + match.getRootMatch());
            // }

            assertEquals(true, result);
        }
    }

    @Test
    public void testStart() {
        String[] startPatternStrings = {"(B ^^^ F)", "(A ^^^ B C)"};

        for (String startPatternString : startPatternStrings) {
            Tree<String> startPattern =
                    TreeParserFactory.getStringTreeParser().parse(startPatternString);

            TreeGrep<String> matcher = new TreeGrep<>(startPattern);
            boolean result = matcher.matches(tree);

            // for (TreeGrepMatch<String> match : matcher.getMatches()) {
            // logger.info("Start: " + match.getRootMatch());
            // }

            assertEquals(true, result);
        }
    }

}
