/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.nlp.utilities.CollinsHeadFinder;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Uses the Collins' head percolation table to get a dependency tree using a parse tree
 *
 * @author Vivek Srikumar
 */
public class CollinsHeadDependencyParser {
    CollinsHeadFinder headFinder;

    // This converts prepositions and possessive clitics
    // to edge labels
    boolean collapseEdgeLabels;

    /**
     * Create a CollinsHeadDependencyParser
     *
     * @param convertPrepositionsToEdgeLabels Should prepositions be merged into the edge labels?
     */

    public CollinsHeadDependencyParser(boolean convertPrepositionsToEdgeLabels) {
        this(convertPrepositionsToEdgeLabels, new CollinsHeadFinder());
    }

    public CollinsHeadDependencyParser(boolean convertPrepositionsToEdgeLabels,
            CollinsHeadFinder headFinder) {
        collapseEdgeLabels = convertPrepositionsToEdgeLabels;
        this.headFinder = headFinder;
    }

    public Tree<String> getDependencyTree(Constituent parseTreeRoot) {
        // for each parse non-terminal, starting from the root, find the head.
        // Make a dependency arc from the all the other children to the head
        // child.

        if (TreeView.isLeaf(parseTreeRoot))
            return new Tree<>(parseTreeRoot.getTokenizedSurfaceForm());

        Constituent headWord = headFinder.getHeadWord(parseTreeRoot);
        Constituent headChild = headFinder.getHeadChild(parseTreeRoot);

        Tree<String> rootTree = new Tree<>(headWord.getTokenizedSurfaceForm());
        List<Tree<String>> dependentTrees = new ArrayList<>();

        for (Relation childEdge : parseTreeRoot.getOutgoingRelations()) {
            Constituent child = childEdge.getTarget();

            if (child.equals(headChild)) {
                rootTree = getDependencyTree(child);
            } else {
                dependentTrees.add(getDependencyTree(child));
            }
        }

        rootTree.addSubtrees(dependentTrees);

        return rootTree;
    }

    public Tree<Pair<String, Integer>> getLabeledDependencyTree(Constituent parseTreeRoot) {
        Tree<Pair<String, Integer>> tree = makeDepTree(parseTreeRoot);

        if (this.collapseEdgeLabels) {
            tree = collapsePrepositionLabels(tree);

            tree = collapseClitics(tree);
        }
        return tree;
    }

    protected Tree<Pair<String, Integer>> collapseClitics(Tree<Pair<String, Integer>> tree) {
        Tree<Pair<String, Integer>> newTree = new Tree<>(tree.getLabel());

        for (int i = 0; i < tree.getNumberOfChildren(); i++) {
            Tree<Pair<String, Integer>> child = tree.getChild(i);
            String edgeLabel = tree.getEdgeLabel(i).getFirst();

            if (edgeLabel.contains(",")) {

                String[] tags = edgeLabel.split(", ");

                String childLabel = tags[2].split(": ")[1];

                if (child.getLabel().getFirst().equals("'s")) {

                    if (child.getNumberOfChildren() > 0) {
                        Tree<Pair<String, Integer>> grandChild = collapseClitics(child.getChild(0));

                        for (int childId = 1; childId < child.getNumberOfChildren(); childId++) {
                            grandChild.addSubtree(collapseClitics(child.getChild(childId)),
                                    child.getEdgeLabel(childId));
                        }

                        newTree.addSubtree(grandChild, new Pair<>("'s", -1));
                    } else {
                        newTree.addSubtree(collapseClitics(child), tree.getEdgeLabel(i));
                    }
                } else {
                    newTree.addSubtree(collapseClitics(child), tree.getEdgeLabel(i));
                }
            } else {
                newTree.addSubtree(collapseClitics(child), tree.getEdgeLabel(i));
            }
        }

        return newTree;
    }

    protected Tree<Pair<String, Integer>> collapsePrepositionLabels(Tree<Pair<String, Integer>> tree) {

        Tree<Pair<String, Integer>> newTree = new Tree<>(tree.getLabel());

        for (int i = 0; i < tree.getNumberOfChildren(); i++) {
            Tree<Pair<String, Integer>> child = tree.getChild(i);
            String edgeLabel = tree.getEdgeLabel(i).getFirst();

            String[] tags = edgeLabel.split(", ");

            String childLabel = tags[2].split(": ")[1];

            if (childLabel.equals("PP")) {
                Pair<Tree<Pair<String, Integer>>, String> processedPP = processPPTree(child);
                newTree.addSubtree(processedPP.getFirst(), new Pair<>(processedPP.getSecond(), -1));
            } else {
                newTree.addSubtree(collapsePrepositionLabels(child), tree.getEdgeLabel(i));
            }
        }

        return newTree;
    }

    protected Pair<Tree<Pair<String, Integer>>, String> processPPTree(
            Tree<Pair<String, Integer>> tree) {
        // we are in a prepositional phrase

        List<Pair<String, Integer>> newEdgeLabel = new ArrayList<>();
        newEdgeLabel.add(tree.getLabel());

        List<Tree<Pair<String, Integer>>> trees = new ArrayList<>();
        List<Pair<String, Integer>> internalEdgeLabels = new ArrayList<>();

        List<Tree<Pair<String, Integer>>> ppTrees = new ArrayList<>();
        List<Pair<String, Integer>> ppEdgeLabels = new ArrayList<>();

        // find the "rightmost" child of this tree that is not a preposition.
        // That is the new head.

        for (int i = 0; i < tree.getNumberOfChildren(); i++) {
            Tree<Pair<String, Integer>> child = tree.getChild(i);
            Pair<String, Integer> edgeLabel = tree.getEdgeLabel(i);

            String[] tags = edgeLabel.getFirst().split(", ");

            String childLabel = tags[2].split(": ")[1];

            if (POSUtils.isPOSPreposition(childLabel)) {
                if (child.isLeaf()) {
                    newEdgeLabel.add(child.getLabel());
                } else {
                    internalEdgeLabels.add(child.getLabel());
                    trees.add(collapsePrepositionLabels(child));
                }
            } else if (childLabel.equals("PP")) {
                Pair<Tree<Pair<String, Integer>>, String> processedPP = processPPTree(child);
                ppTrees.add(processedPP.getFirst());
                ppEdgeLabels.add(new Pair<>(processedPP.getSecond(), -1));
            } else {
                trees.add(collapsePrepositionLabels(child));
                internalEdgeLabels.add(edgeLabel);
            }
        }

        Collections.sort(newEdgeLabel, new Comparator<Pair<String, Integer>>() {
            public int compare(Pair<String, Integer> arg0, Pair<String, Integer> arg1) {
                return arg0.getSecond().compareTo(arg1.getSecond());
            }
        });

        StringBuilder sb = new StringBuilder();

        for (Pair<String, Integer> s : newEdgeLabel) {
            sb.append(s.getFirst()).append(" ");
        }
        String label = sb.toString();

        int childrenStart = 0;
        int childrenEnd = trees.size() - 1;

        int ppChildrenStart = 0;
        int ppChildrenEnd = ppTrees.size() - 1;

        Tree<Pair<String, Integer>> newTree;
        if (trees.size() == 0) {

            if (ppTrees.size() > 0) {
                newTree = ppTrees.get(0);
                ppChildrenStart = 1;
            } else {
                // This shouldn't normally happen. But given that people
                // write absolute garbage instead of English, and that the
                // parser is also another fine piece of work, one never knows...
                //
                // This means that the current node has no child that is a PP
                // and neither does it have a child that is not a PP. However,
                // it is marked as a preposition. What it modifies is a
                // mystery... Let's just return the preposition.

                String oldIncomingLabel =
                        tree.getParent().getEdgeLabel(tree.getPositionAmongParentsChildren())
                                .getFirst();

                return new Pair<>(tree, oldIncomingLabel);

            }
        } else {

            newTree = trees.get(trees.size() - 1);
            childrenEnd = trees.size() - 1;

        }

        for (int i = childrenStart; i < childrenEnd; i++) {
            newTree.addSubtree(trees.get(i), internalEdgeLabels.get(i));
        }

        for (int i = ppChildrenStart; i < ppChildrenEnd; i++) {
            newTree.addSubtree(ppTrees.get(i), ppEdgeLabels.get(i));
        }

        return new Pair<>(newTree, label);

    }

    private Tree<Pair<String, Integer>> makeDepTree(Constituent parseTreeRoot) {

        // for each parse non-terminal, starting from the root, find the head.
        // Make a dependency arc from the all the other children to the head
        // child.

        if (TreeView.isLeaf(parseTreeRoot)) {
            int position = parseTreeRoot.getStartSpan();
            return new Tree<>(new Pair<>(parseTreeRoot.getLabel(), position));
        }

        Constituent headChild = headFinder.getHeadChild(parseTreeRoot);

        Tree<Pair<String, Integer>> rootTree = null;

        List<Tree<Pair<String, Integer>>> dependentTrees = new ArrayList<>();

        List<Pair<String, Integer>> edgeLabels = new ArrayList<>();

        int conjunction = -1;

        for (Relation childEdge : parseTreeRoot.getOutgoingRelations()) {

            Constituent child = childEdge.getTarget();

            if (child == headChild) {
                rootTree = makeDepTree(child);
            } else {

                dependentTrees.add(makeDepTree(child));
                edgeLabels.add(getEdgeLabel(parseTreeRoot, headChild.getLabel(), child));

                if (child.getLabel().equals("CC")) {
                    conjunction = dependentTrees.size() - 1;
                }
            }
        }

        if (conjunction >= 0) {
            return doConjunctionHack(parseTreeRoot, headChild, rootTree, dependentTrees,
                    edgeLabels, conjunction);
        } else {
            for (int i = 0; i < dependentTrees.size(); i++) {
                rootTree.addSubtree(dependentTrees.get(i), edgeLabels.get(i));
            }
            return rootTree;
        }
    }

    protected Pair<String, Integer> getEdgeLabel(Constituent parseTreeRoot, String string,
            Constituent child) {
        return new Pair<>(("Parent: " + parseTreeRoot.getLabel() + ", Head: " + string
                + ", Child: " + child.getLabel()).trim(), -1);
    }

    private Tree<Pair<String, Integer>> doConjunctionHack(Constituent parseTreeRoot,
            Constituent headChild, Tree<Pair<String, Integer>> rootTree,
            List<Tree<Pair<String, Integer>>> dependentTrees,
            List<Pair<String, Integer>> edgeLabels, int conjunctionPosition) {
        Tree<Pair<String, Integer>> newRootTree =
                new Tree<>(dependentTrees.get(conjunctionPosition).getLabel());

        int rootToken = rootTree.getLabel().getSecond();

        for (int i = 0; i < dependentTrees.size(); i++) {
            if (i == conjunctionPosition)
                continue;

            if (dependentTrees.get(i).getLabel().getSecond() < rootToken)
                rootTree.addSubtree(dependentTrees.get(i), edgeLabels.get(i));
            else
                newRootTree.addSubtree(dependentTrees.get(i), edgeLabels.get(i));
        }

        Pair<String, Integer> rootEdgeLabel = getEdgeLabel(parseTreeRoot, "CC", headChild);

        newRootTree.addSubtree(rootTree, rootEdgeLabel);

        return newRootTree;
    }
}
