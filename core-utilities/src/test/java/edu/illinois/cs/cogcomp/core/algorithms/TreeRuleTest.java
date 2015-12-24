/**
 *
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeParserFactory;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Srikumar Jun 24, 2009
 */
public class TreeRuleTest extends TestCase {
    TreeRule<String> rule;
    Tree<String> pattern;

    Tree<String> tree;
    private Set<String> afterRule;

    protected void setUp() throws Exception {
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

    public void testApplyRule() {
        List<List<String>> results = rule.applyRule(tree);
        for (List<String> result : results) {

            StringBuffer sb = new StringBuffer();
            for (String r : result)
                sb.append(r + " ");

            assertTrue(afterRule.contains(sb.toString().trim()));

        }
    }

}
