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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author vivek
 */
public class TreeTest {
    private static Logger logger = LoggerFactory.getLogger(TreeTest.class);

    class Node {
        int nodeId;
        String label;
        int[] children;

        Node(int nodeId, String label, int[] children) {
            this.nodeId = nodeId;
            this.label = label;
            this.children = children;
        }
    }

    private Tree<String> t1, t2, tree;
    String treeString = "(Root (Leaf)\n" + "      (Child1 (Child1Leaf))\n"
            + "      (Child2 (Child2Leaf1)\n" + "              (Child2Leaf2))\n" + "      (Leaf))";

    String treeString2 = "(Root (Leaf)\n" + "      (Child1 (Child1Leaf))\n"
            + "      (Child2 (Child2Leaf1 (GrandChild1))\n" + "              (Child2Leaf2))\n"
            + "      (Leaf))";

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */

    @Before
    public void setUp() throws Exception {
        tree = new Tree<>("Root");
        tree.addLeaf("Leaf");

        t1 = new Tree<>("Child1");
        t1.addLeaf("Child1Leaf");

        tree.addSubtree(t1);

        tree.addLeaf("Leaf");

        t2 = new Tree<>("Child2");
        t2.addLeaf("Child2Leaf1");
        t2.addLeaf("Child2Leaf2");

        tree.addSubtreeAt(t2, 2);

    }

    @Test
    public final void testCreateTree() {
        // (A (B ( C )))

        Node a = new Node(0, "A", new int[] {1});
        Node b = new Node(1, "B", new int[] {2});
        Node c = new Node(2, "C", new int[] {});

        List<Node> nodes = new ArrayList<>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        int top = 0;

        Queue<Integer> nodeIndices = new LinkedList<>();
        nodeIndices.add(top);

        Tree<String> tree = new Tree<>(nodes.get(top).label);
        Map<Integer, Tree<String>> nodeMap = new HashMap<>();
        nodeMap.put(top, tree);

        while (!nodeIndices.isEmpty()) {
            int current = nodeIndices.poll();
            Tree<String> parent = nodeMap.get(current);
            Node currentNode = nodes.get(current);

            if (currentNode.children.length > 0) {
                for (int childId : currentNode.children) {
                    Tree<String> childNode = new Tree<>(nodes.get(childId).label);
                    parent.addSubtree(childNode);
                    nodeMap.put(childId, childNode);
                    nodeIndices.add(childId);
                }
            } else {
                // this is a leaf

                assertEquals(3, tree.size());
                assertEquals(parent.getLabel(), "C");
                parent.addLeaf("word");
            }

        }

        Tree<String> expectedTree =
                TreeParserFactory.getStringTreeParser().parse("(A (B (C word)))");

        String treeString = tree.toString();
        assertEquals(expectedTree.toString(), treeString);

        assertEquals(4, tree.size());

        // logger.info(tree);
        tree.getChild(0).addLeaf("B1");

        assertEquals(5, tree.size());

        // logger.info(tree);

        tree.getChild(0).addSubtreeAt(expectedTree, 1);

        assertEquals(tree.size(), 9);
        assertEquals(tree.getChild(0).getChild(1).toString(), treeString);

    }

    @Test
    public final void testAddLeaf() {

        assertEquals(t1.toString(), "(Child1 Child1Leaf)");
    }

    @Test
    public final void testAddSubtree() {
        Tree<String> tree = new Tree<>("Root");
        tree.addLeaf("Leaf");

        Tree<String> t1 = new Tree<>("Child1");
        t1.addLeaf("Child1Leaf");

        tree.addSubtree(t1);

        assertEquals("(Root Leaf\n      (Child1 Child1Leaf))", tree.toString());
    }

    @Test
    public final void testAddSubtreeAt() {
        assertEquals("(Root Leaf\n" + "      (Child1 Child1Leaf)\n" + "      (Child2 Child2Leaf1\n"
                + "              Child2Leaf2)\n" + "      Leaf)", tree.toString());

    }

    @Test
    public final void testGetChild() {
        assertEquals(tree.getChild(0).toString(), "(Leaf)");
        assertEquals(tree.getChild(1).toString(), "(Child1 Child1Leaf)");

    }

    @Test
    public final void testGetLabel() {
        assertEquals(tree.getLabel(), "Root");
    }

    @Test
    public final void testGetHeight() {
        assertEquals(tree.getHeight(), 3);
        tree.getChild(2).getChild(1).addLeaf("GrandChild");
        assertEquals(tree.getHeight(), 4);
    }

    @Test
    public final void testSize() {
        assertEquals(tree.size(), 8);

        tree.getChild(2).getChild(1).addLeaf("GrandChild");

        logger.info("");

        assertEquals(tree.size(), 9);

    }

    @Test
    public final void testGetYield() {
        String[] leaves = new String[] {"Leaf", "Child1Leaf", "Child2Leaf1", "Child2Leaf2", "Leaf"};

        int i = 0;
        for (Tree<String> s : tree.getYield()) {
            // logger.info(leaves[i] + "\t" + s.getLabel());
            assertEquals(leaves[i++], s.getLabel());
        }

        tree.getChild(2).getChild(1).addLeaf("GrandChild");

        leaves = new String[] {"Leaf", "Child1Leaf", "Child2Leaf1", "GrandChild", "Leaf"};
        i = 0;
        for (Tree<String> s : tree.getYield()) {
            assertEquals(leaves[i++], s.getLabel());
        }

    }

    @Test
    public final void testIsLeaf() {
        assertEquals(tree.isLeaf(), false);
        assertEquals(tree.getChild(0).isLeaf(), true);
        assertEquals(tree.getChild(1).getChild(0).isLeaf(), true);
    }

    @Test
    public final void testEquals() {
        Tree<String> t = (new TreeParser<>(new INodeReader<String>() {

            public String parseNode(String string) {
                return string;
            }
        })).parse(treeString);

        assertEquals(tree.equals(t), true);
    }

    @Test
    public final void testClone() throws CloneNotSupportedException {
        assertEquals(tree.equals(tree.clone()), true);
    }

    @Test
    public final void testHashCode() {

        Tree<String> t = (new TreeParser<>(new INodeReader<String>() {

            public String parseNode(String string) {
                return string;
            }
        })).parse(treeString);

        assertEquals(tree.hashCode(), t.hashCode());

    }

    @Test
    public final void testAddSubtrees() {

        Tree<String> tree2 = new Tree<>("Root");

        tree2.addLeaf("Leaf");

        Tree<String> treeChild1 = new Tree<>("Child1");
        treeChild1.addLeaf("Child1Leaf");

        Tree<String> treeLeaf2 = new Tree<>("Leaf");

        Tree<String> treeChild2 = new Tree<>("Child2");
        treeChild2.addLeaf("Child2Leaf1");
        treeChild2.addLeaf("Child2Leaf2");

        ArrayList<Tree<String>> subtrees = new ArrayList<>();
        subtrees.add(treeChild1);
        subtrees.add(treeChild2);
        subtrees.add(treeLeaf2);

        tree2.addSubtrees(subtrees);

        assertEquals(tree2.toString(), tree.toString());

        assertEquals(tree2.equals(tree), true);

    }

    @Test
    public final void testChildrenIterator() {
        // this works
    }

    @Test
    public final void testGetNumberOfChildren() {
        assertEquals(tree.getNumberOfChildren(), 4);
    }

    @Test
    public final void testGetParent() {
        for (Tree<String> t : tree.childrenIterator()) {
            assertEquals(t.getParent().equals(tree), true);
        }
    }

    @Test
    public final void testIsRoot() {
        assertEquals(tree.isRoot(), true);
        for (Tree<String> t : tree.childrenIterator()) {
            assertEquals(t.isRoot(), false);
        }
    }

}
