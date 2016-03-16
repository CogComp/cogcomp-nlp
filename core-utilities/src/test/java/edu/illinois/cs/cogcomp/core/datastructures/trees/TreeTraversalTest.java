package edu.illinois.cs.cogcomp.core.datastructures.trees;

import junit.framework.TestCase;

/**
 * @author vivek
 */
public class TreeTraversalTest extends TestCase {

    private Tree<String> tree;
    String treeString = "(Root (Leaf1)\n" + "      (Child1 (Child1Leaf))\n"
            + "      (Child2 (Child2Leaf1)\n" + "              (Child2Leaf2))\n" + "      (Leaf2))";

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        tree = Tree.readTreeFromString(treeString);
    }

    /**
     * Test method for
     * {@link edu.illinois.cs.cogcomp.core.datastructures.trees.TreeTraversal#breadthFirstTraversal(Tree)}
     * .
     */
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
