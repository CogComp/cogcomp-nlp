/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 *
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author Vivek Srikumar Jun 24, 2009
 */
public class TreeRuleTest {
    TreeRule<String> rule;
    Tree<String> pattern;

    Tree<String> tree;
    private Set<String> afterRule;

    @Before
    public void init() throws Exception {
        pattern = TreeParserFactory.getStringTreeParser().parse("(NP NP , NP)");
        rule = new TreeRule<>(pattern);

        List<Pair<Integer, String>> introduction = new ArrayList<>();
        introduction.add(new Pair<>(1, ""));
        introduction.add(new Pair<>(-1, "is"));
        introduction.add(new Pair<>(3, ""));
        introduction.add(new Pair<>(-1, "."));
        rule.addIntroduction(introduction);

        List<Pair<Integer, String>> substitution = new ArrayList<>();
        substitution.add(new Pair<>(1, ""));
        rule.addSubstition(substitution);

        substitution = new ArrayList<>();
        substitution.add(new Pair<>(3, ""));
        rule.addSubstition(substitution);

        // String sentence = "Bob , the builder , stepped forward .";

        tree =
                TreeParserFactory
                        .getStringTreeParser()
                        .parse("(S1 (S (NP (NP (NNP Bob))           (, ,)           (NP (DT the)               (NN builder))           (, ,))       (VP (VBD stepped)           (ADVP (RB forward)))       (. .)))");

        afterRule = new HashSet<>();
        afterRule.add("Bob stepped forward .");
        afterRule.add("Bob is the builder .");
        afterRule.add("the builder stepped forward .");
    }

    @Test
    public void testApplyRule() {
        List<List<String>> results = rule.applyRule(tree);
        for (List<String> result : results) {

            StringBuilder sb = new StringBuilder();
            for (String r : result)
                sb.append(r).append(" ");

            assertTrue(afterRule.contains(sb.toString().trim()));

        }
    }

}
