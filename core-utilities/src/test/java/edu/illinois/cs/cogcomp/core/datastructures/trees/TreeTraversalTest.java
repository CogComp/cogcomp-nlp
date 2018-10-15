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

/**
 * @author vivek
 */
public class TreeTraversalTest {

    private Tree<String> tree;
    String treeString = "(Root (Leaf1)\n" + "      (Child1 (Child1Leaf))\n"
            + "      (Child2 (Child2Leaf1)\n" + "              (Child2Leaf2))\n" + "      (Leaf2))";

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */

    @Before
    public void setUp() throws Exception {
        tree = Tree.readTreeFromString(treeString);
    }

    /**
     * Test method for
     * {@link edu.illinois.cs.cogcomp.core.datastructures.trees.TreeTraversal#breadthFirstTraversal(Tree)}
     * .
     */
    @Test
    public final void testBreadthFirstTraversal() {

        String[] output =
                new String[] {"Root", "Leaf1", "Child1", "Child2", "Leaf2", "Child1Leaf",
                        "Child2Leaf1", "Child2Leaf2"};

        int i = 0;
        for (Tree<String> t : TreeTraversal.breadthFirstTraversal(tree)) {
            assertEquals(output[i++], t.getLabel());
        }

    }

    /**
     * Test method for
     * {@link edu.illinois.cs.cogcomp.core.datastructures.trees.TreeTraversal#depthFirstTraversal(Tree)}
     * .
     */
    @Test
    public final void testDepthFirstTraversal() {

        String[] output =
                new String[] {"Root", "Leaf1", "Child1", "Child1Leaf", "Child2", "Child2Leaf1",
                        "Child2Leaf2", "Leaf2"};

        int i = 0;

        for (Tree<String> t : TreeTraversal.depthFirstTraversal(tree)) {
            assertEquals(output[i++], t.getLabel());
        }

    }

}
