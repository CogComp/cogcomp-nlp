/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class TreeRule<T> {

    protected final TreeGrep<T> matcher;

    boolean hasSubstitution;

    List<List<Pair<Integer, T>>> introductions;
    List<List<Pair<Integer, T>>> substitutions;

    public TreeRule(Tree<T> pattern) {
        matcher = new TreeGrep<>(pattern);

        substitutions = new ArrayList<>();
        introductions = new ArrayList<>();
        hasSubstitution = false;
    }

    public void addSubstition(List<Pair<Integer, T>> substitution) {
        substitutions.add(substitution);
        hasSubstitution = true;
    }

    public void addIntroduction(List<Pair<Integer, T>> introduction) {
        introductions.add(introduction);
    }

    public List<List<T>> applyRule(Tree<T> tree) {
        List<List<T>> output = new ArrayList<>();

        if (!matcher.matches(tree))
            return output;

        for (TreeGrepMatch<T> match : matcher.getMatches()) {
            output.addAll(generateRelations(tree, match));
        }
        return output;
    }

    public TreeGrep<T> getMatcher() {
        return this.matcher;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pattern:\n ").append(matcher.toString()).append("\n");
        sb.append("Introductions:\n");

        for (List<Pair<Integer, T>> item : introductions) {
            sb.append(item).append("\n");
        }

        sb.append("Substitutions:\n");

        for (List<Pair<Integer, T>> item : substitutions) {
            sb.append(item).append("\n");
        }

        return sb.toString();
    }

    protected List<List<T>> generateRelations(Tree<T> tree, TreeGrepMatch<T> match) {
        List<List<T>> output = new ArrayList<>();

        // get the matching nodes in the tree for each node in the DFS of the
        // pattern.
        List<Tree<T>> dfsPatternMatches = match.getPatternDFSMatches();

        // generate all the substitutions
        doSubstitution(output, dfsPatternMatches);

        // generate introductions
        addIntroductions(output, dfsPatternMatches);

        return output;
    }

    private void doSubstitution(List<List<T>> output, List<Tree<T>> dfsPatternMatches) {
        if (!hasSubstitution)
            return;

        // This is the node that matches the root of the pattern.
        Tree<T> patternRoot = dfsPatternMatches.get(0);

        patternRoot.getParent();

        // get the yield that is to the left of the patternRoot
        List<T> leftYield = new ArrayList<>();

        Tree<T> tree = patternRoot;
        do {
            List<T> l = new ArrayList<>();
            getLeftYield(tree, l);
            Collections.reverse(l);
            leftYield.addAll(l);
            tree = tree.getParent();
        } while (!tree.isRoot());

        Collections.reverse(leftYield);

        // get the yield that is to the right of the patternRoot
        List<T> rightYield = new ArrayList<>();

        tree = patternRoot;
        do {
            List<T> r = new ArrayList<>();
            getRightYield(tree, r);
            rightYield.addAll(r);
            tree = tree.getParent();
        } while (!tree.isRoot());

        // we have the left yield and the right yield.

        for (List<Pair<Integer, T>> substitution : substitutions) {
            List<T> substitutionList = new ArrayList<>();
            // first add the left yield
            substitutionList.addAll(leftYield);

            // add the substitution yield here.
            for (Pair<Integer, T> item : substitution) {
                if (item.getFirst() < 0)
                    substitutionList.add(item.getSecond());
                else {
                    Tree<T> node = dfsPatternMatches.get(item.getFirst());
                    for (Tree<T> nodeChild : node.getChildren()) {

                        if (dfsPatternMatches.contains(nodeChild))
                            continue;
                        List<Tree<T>> yield = nodeChild.getYield();
                        for (Tree<T> t : yield) {
                            substitutionList.add(t.getLabel());
                        }
                    }
                }
            }

            // now add the right yield
            substitutionList.addAll(rightYield);

            // add to the output;
            output.add(substitutionList);
        }

    }

    private void getRightYield(Tree<T> node, List<T> rightYield) {
        for (int rightChildId = node.getPositionAmongParentsChildren() + 1; rightChildId < node
                .getParent().getNumberOfChildren(); rightChildId++) {
            List<Tree<T>> rightChildYield = node.getParent().getChild(rightChildId).getYield();

            for (Tree<T> rightYieldItem : rightChildYield) {
                rightYield.add(rightYieldItem.getLabel());
            }
        }
    }

    private void getLeftYield(Tree<T> node, List<T> leftYield) {
        for (int leftChildId = 0; leftChildId < node.getPositionAmongParentsChildren(); leftChildId++) {
            List<Tree<T>> leftChildYield = node.getParent().getChild(leftChildId).getYield();
            for (Tree<T> leftYieldItem : leftChildYield) {
                leftYield.add(leftYieldItem.getLabel());
            }
        }
    }

    private void addIntroductions(List<List<T>> output, List<Tree<T>> dfsPatternMatches) {
        for (List<Pair<Integer, T>> introduction : introductions) {
            List<T> relation = new ArrayList<>();
            for (Pair<Integer, T> item : introduction) {
                if (item.getFirst() < 0) {
                    relation.add(item.getSecond());
                } else {
                    for (Tree<T> yieldNode : dfsPatternMatches.get(item.getFirst()).getYield()) {
                        relation.add(yieldNode.getLabel());
                    }
                }
            }

            output.add(relation);
        }
    }
}
