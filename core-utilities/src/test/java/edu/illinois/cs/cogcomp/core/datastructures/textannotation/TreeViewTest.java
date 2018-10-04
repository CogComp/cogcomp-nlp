/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.textannotation;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TreeViewTest {

    private TextAnnotation ta, depTA;
    private Tree<Pair<String, Integer>> depTreeSent0, depTreeSent1;

    @Before
    public void setUp() throws Exception {
        String[] viewsToAdd = {ViewNames.POS, ViewNames.PARSE_STANFORD};
        ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, false, 3);

        // Create a multi-sentence TA and add dummy dep trees
        List<String[]> sents = new ArrayList<>();
        sents.add("It 's a tough job hosting the Academy Awards .".split(" "));
        sents.add("On Friday , the organization behind the Academy Awards named a host ."
                .split(" "));
        depTA = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(sents);
        depTreeSent0 = new Tree<>(new Pair<>("hosting", 5));
        Tree<Pair<String, Integer>> tree1 = new Tree<>(new Pair<>("job", 4));
        tree1.addSubtree(new Tree<>(new Pair<>("It", 0)), new Pair<>("dep", 0));
        tree1.addSubtree(new Tree<>(new Pair<>("'s", 1)), new Pair<>("dep", 1));
        tree1.addSubtree(new Tree<>(new Pair<>("a", 2)), new Pair<>("dep", 2));
        tree1.addSubtree(new Tree<>(new Pair<>("tough", 3)), new Pair<>("dep", 3));
        tree1.addSubtree(new Tree<>(new Pair<>("the", 6)), new Pair<>("dep", 6));
        tree1.addSubtree(new Tree<>(new Pair<>("Academy", 7)), new Pair<>("dep", 7));
        tree1.addSubtree(new Tree<>(new Pair<>("Awards", 8)), new Pair<>("dep", 8));
        tree1.addSubtree(new Tree<>(new Pair<>(".", 9)), new Pair<>("dep", 9));
        depTreeSent0.addSubtree(tree1, new Pair<>("dep", 4));

        depTreeSent1 = new Tree<>(new Pair<>("behind", 5));
        Tree<Pair<String, Integer>> tree2 = new Tree<>(new Pair<>("organization", 4));
        tree2.addSubtree(new Tree<>(new Pair<>("On", 0)), new Pair<>("dep", 0));
        tree2.addSubtree(new Tree<>(new Pair<>("Friday", 1)), new Pair<>("dep", 1));
        tree2.addSubtree(new Tree<>(new Pair<>(",", 2)), new Pair<>("dep", 2));
        tree2.addSubtree(new Tree<>(new Pair<>("the", 3)), new Pair<>("dep", 3));
        tree2.addSubtree(new Tree<>(new Pair<>("the", 6)), new Pair<>("dep", 6));
        tree2.addSubtree(new Tree<>(new Pair<>("Academy", 7)), new Pair<>("dep", 7));
        tree2.addSubtree(new Tree<>(new Pair<>("Awards", 8)), new Pair<>("dep", 8));
        tree2.addSubtree(new Tree<>(new Pair<>("named", 9)), new Pair<>("dep", 9));
        tree2.addSubtree(new Tree<>(new Pair<>("a", 10)), new Pair<>("dep", 10));
        tree2.addSubtree(new Tree<>(new Pair<>("host", 11)), new Pair<>("dep", 11));
        tree2.addSubtree(new Tree<>(new Pair<>(".", 12)), new Pair<>("dep", 12));
        depTreeSent1.addSubtree(tree2, new Pair<>("dep", 4));
    }

    @Test
    public void testGetConstituentTree() throws Exception {
        Tree<Constituent> constituentTree =
                ((TreeView) ta.getView(ViewNames.PARSE_STANFORD)).getConstituentTree(0);
        // Confusingly, the constituent of each node of the tree is called a label
        Constituent root = constituentTree.getLabel();
        assertEquals("S1", root.getLabel());
        assertEquals(new IntPair(0, 11), root.getSpan());
        Constituent firstNoun =
                constituentTree.getChild(0).getChild(0).getChild(0).getChild(1).getLabel();
        assertEquals("construction", firstNoun.getSurfaceForm());
        Constituent prepPhrase = constituentTree.getChild(0).getChild(1).getChild(1).getLabel();
        assertEquals("on time", prepPhrase.getSurfaceForm());
        assertEquals("IN", ta.getView(ViewNames.POS).getLabelsCovering(prepPhrase).get(0));
        
        constituentTree = ((TreeView) ta.getView(ViewNames.PARSE_STANFORD)).getConstituentTree(1);
    }

    @Test
    public void testSetDepTreeWithDuplicateTokens() {
        TreeView treeView = new TreeView("test", depTA);
        treeView.addDependencyTree(depTreeSent0, 0, new Constituent("", "test", depTA, 5, 6));
        treeView.addDependencyTree(depTreeSent1, 10, new Constituent("", "test", depTA, 15, 16));
        // Here, "Academy Awards" is repeated at exactly the same location in both sentences,
        // so we need to make sure we create a different token the second time
        List<Constituent> c = treeView.getConstituentsCoveringSpan(17, 18);
        assertEquals(1, c.size());
        assertEquals("Academy", c.get(0).getSurfaceForm());
    }
}
