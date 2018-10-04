/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.trees;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTreeParser {
    TreeParser<String> parser;
    String treeString = "(Root Leaf\n" + "      (Child1 Child1Leaf)\n"
            + "      (Child2 Child2Leaf1\n" + "              Child2Leaf2)\n" + "      Leaf)";

    String treeString1 = "a";

    @Before
    public void setUp() throws Exception {
        parser = new TreeParser<>(new INodeReader<String>() {
            public String parseNode(String string) {
                return string;
            }
        });
    }

    @Test
    public final void testParse() {
        assertEquals(parser.parse(treeString).toString(), treeString);
        assertEquals(parser.parse(treeString1).toString(), "(" + treeString1 + ")");
    }

    @Test
    public final void test1() {

        String treeStringNew =
                "(NOM (:ORTH \"abandonment\")     (:PLURAL *NONE*)     (:VERB \"abandon\")    :NOM-TYPE ((VERB-NOM))     :VERB-SUBJ ((DET-POSS)                 (PP :PVAL (\"by\")))     :SUBJ-ATTRIBUTE ((COMMUNICATOR))     :VERB-SUBC ((NOM-NP :SUBJECT ((DET-POSS)                                   (PP :PVAL (\"by\")))                         :OBJECT ((DET-POSS)                                  (PP :PVAL (\"of\"))))                 (NOM-NP-PP :SUBJECT ((DET-POSS)                                      (PP :PVAL (\"by\")))                            :OBJECT ((DET-POSS)                                     (PP :PVAL (\"of\")))                            :PVAL (\"for\" \"to\"))                 (NOM-NP-TO-INF-OC :SUBJECT ((DET-POSS)                                             (PP :PVAL (\"by\")))                                   :OBJECT ((DET-POSS)                                            (PP :PVAL (\"of\")))                                   :NOM-SUBC ((TO-INF :OC T)))                 (NOM-NP-AS-NP :SUBJECT ((DET-POSS)                                         (PP :PVAL (\"by\")))                               :OBJECT ((DET-POSS)                                        (PP :PVAL (\"of\")))                               :NOM-SUBC ((AS-NP-PHRASE :OC T))))     :DONE T     :REVISED \"Jan-Update@sapir  17:48 1/13/1999\")";

        Tree<String> tree = Tree.readTreeFromString(treeStringNew);

        assertEquals(tree.getLabel(), "NOM");

        String query = ":VERB";

        for (Tree<String> child : tree.childrenIterator()) {
            if (child.getLabel().equals(query)) {
                assertEquals(child.getChild(0).getLabel(), "\"abandon\"");
            }
        }
    }

}
