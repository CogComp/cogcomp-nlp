/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.datastructures.trees.TreeTraversal;
import edu.illinois.cs.cogcomp.core.math.Permutations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Search trees for a fixed pattern.
 * <p>
 * <b>Usage:</b>
 * <p>
 * Suppose <code>tree</code> is some tree and we wish to find all instances of <code>pattern</code>
 * in it.
 * 
 * <pre>
 * TreeGrep&lt;String&gt; grepper = new TreeGrep&lt;String&gt;(pattern);
 * if (grepper.matches(tree)) {
 *     List&lt;TreeGrepMatch&lt;String&gt;&gt; matches = grepper.getMatches();
 *     for (TreeGrepMatch&lt;String&gt; match : matches) {
 *         // do something with the match
 *     }
 * }
 *
 * </pre>
 * 
 * Refer the documentation of {@link TreeGrepMatch} for details about how to use it the match
 * object. This class does not yet support regular expression searches for trees like tregex or
 * tgrep. Moreover, the current implementation can be improved by using something like the
 * Boyer-Moore algorithm for both vertical and horizontal search.
 *
 * @author Vivek Srikumar
 * @see TreeGrepMatch
 */
public class TreeGrep<T> {
    private static Logger logger = LoggerFactory.getLogger(TreeGrep.class);

    public static String endOfChildrenString = "$$$";
    public static String startOfChildrenString = "^^^";

    private final Tree<T> pattern;

    protected List<TreeGrepMatch<T>> matchesList;

    boolean verbose = false;

    private List<TreeGrepMatch<T>> nodeTreeMatches;

    /**
     *
     */
    public TreeGrep(Tree<T> pattern) {
        this.pattern = pattern;

    }

    /**
     * This checks whether any subtrees of <code>tree</code> match the pattern.
     * <p>
     * Note: Each subtree might have multiple matches within it that match the pattern. For example,
     * if the pattern is
     * <p>
     * <code>(VP (NP NN )) </code>
     * <p>
     * and the tree has
     * <p>
     * <code> (VP (NP NN) (NP NN))</code>
     * <p>
     * then, this function will only return the <code>(VP...)</code> subtree, and does not indicate
     * that there are two matches within that subtree, both rooted at the <code>VP</code>.
     */
    public boolean matches(Tree<T> tree) {
        boolean found = false;
        matchesList = new ArrayList<>();

        for (Tree<T> node : TreeTraversal.breadthFirstTraversal(tree)) {
            if (verbose) {
                logger.info("Comparing  node: ");
                logger.info(node.toString());
            }

            nodeTreeMatches = new ArrayList<>();
            nodeTreeMatches = doesNodeMatchPattern(node, getPattern(), nodeTreeMatches);
            if (nodeTreeMatches.size() > 0) {

                if (verbose) {
                    logger.info("Found...");
                }

                matchesList.addAll(nodeTreeMatches);
                found = true;
            }
        }

        return found;
    }

    /**
     * Checks if the tree that is passed as a parameter matches the pattern. This is different from
     * {@link #matches(Tree)} because it does not recursively check interior nodes for matches.
     */
    public boolean doesThisTreeMatch(Tree<T> tree) {
        boolean found = false;
        matchesList = new ArrayList<>();

        nodeTreeMatches = new ArrayList<>();
        nodeTreeMatches = doesNodeMatchPattern(tree, pattern, nodeTreeMatches);
        if (nodeTreeMatches.size() > 0) {

            if (verbose) {
                logger.info("Found...");
            }

            matchesList.addAll(nodeTreeMatches);
            found = true;
        }
        return found;
    }

    /**
     * Gets a list of matches for the pattern in the most recently searched tree.
     *
     * @return A list of {@link TreeGrepMatch} objects. See the general documentation of this class
     *         for a sample usage.
     */
    public List<TreeGrepMatch<T>> getMatches() {
        return matchesList;
    }

    protected boolean doesLabelMatchPatternLabel(T treeLabel, T patternLabel) {
        return treeLabel.equals(patternLabel);
    }


    protected List<TreeGrepMatch<T>> doesNodeMatchPattern(Tree<T> tree, Tree<T> currentPattern,
            List<TreeGrepMatch<T>> nodeMatches) {

        if (verbose) {
            logger.info("Tree:" + tree);
            logger.info("Current pattern: " + currentPattern);
        }

        // if (tree.size() < currentPattern.size())
        // {
        // if (verbose)
        // logger.info(tree.size() + " less than "
        // + currentPattern.size() + ". Returning false.");
        // return new ArrayList<TreeGrepMatch<T>>();
        // }

        if (!doesLabelMatchPatternLabel(tree.getLabel(), currentPattern.getLabel())) {
            if (verbose)
                logger.info("Labels don't match");
            return new ArrayList<>();

        }

        if (currentPattern.isLeaf()) {
            if (verbose)
                logger.info("Pattern is leaf."
                        + " No need to check children. Returning true.");

            if (nodeMatches.size() == 0) {
                TreeGrepMatch<T> match = new TreeGrepMatch<>(currentPattern);
                if (verbose)
                    logger.info("Adding match between "
                            + match.getCurrentPatternNode().getLabel() + " and " + tree.getLabel());

                match.addMatch(tree);
                nodeMatches.add(match);

            } else {
                for (TreeGrepMatch<T> match : nodeMatches) {
                    if (verbose)
                        logger.info("Adding match between "
                                + match.getCurrentPatternNode().getLabel() + " and "
                                + tree.getLabel());

                    match.addMatch(tree);
                }
            }

            if (verbose) {
                logger.info("Current node match list after adding new match");

                logger.info(nodeMatches.toString());
            }

            return nodeMatches;
        }

        if (tree.isLeaf()) {
            if (verbose)
                logger.info("Tree is leaf, but pattern is not. Cannot match.");
            return new ArrayList<>();
        }

        // now that the labels match, match the children.
        // for now, it is naive search. Later, it can be made more efficient.

        List<Integer> treeChildMatchPositions = new ArrayList<>();

        List<List<TreeGrepMatch<T>>> currentNodeMatchList = new ArrayList<>();

        if (verbose)
            logger.info("Matching children of " + currentPattern.getLabel());

        // first check the "endOfChildrenString" case
        T lastChild = currentPattern.getChild(currentPattern.getNumberOfChildren() - 1).getLabel();
        T firstChild = currentPattern.getChild(0).getLabel();
        if (lastChild.toString().equals(endOfChildrenString)) {
            int treeChildId = tree.getNumberOfChildren() - currentPattern.getNumberOfChildren() + 1;

            if (treeChildId < 0)
                return new ArrayList<>();

            matchPatternChildren(tree, currentPattern, treeChildMatchPositions,
                    currentNodeMatchList, treeChildId, 0, currentPattern.getNumberOfChildren() - 1);
        } else if (firstChild.toString().equals(startOfChildrenString)) {
            int treeChildId = 0;

            matchPatternChildren(tree, currentPattern, treeChildMatchPositions,
                    currentNodeMatchList, treeChildId, 1, currentPattern.getNumberOfChildren());
        } else {

            for (int treeChildId = 0; treeChildId < tree.getNumberOfChildren()
                    - currentPattern.getNumberOfChildren() + 1; treeChildId++) {
                int start = 0;
                int end = currentPattern.getNumberOfChildren();

                matchPatternChildren(tree, currentPattern, treeChildMatchPositions,
                        currentNodeMatchList, treeChildId, start, end);

            }// end for each tree child
        }

        List<TreeGrepMatch<T>> newNodeMatches = new ArrayList<>();
        for (List<TreeGrepMatch<T>> currentNodeChildrenMatches : currentNodeMatchList) {
            if (verbose) {
                logger.info("Merging match");
                logger.info(currentNodeChildrenMatches.toString());
                logger.info(" with ");
                logger.info(nodeMatches.toString());
            }
            newNodeMatches.addAll(mergeMatches(nodeMatches, currentNodeChildrenMatches));
        }

        return newNodeMatches;
    }

    private void matchPatternChildren(Tree<T> tree, Tree<T> currentPattern,
            List<Integer> treeChildMatchPositions,
            List<List<TreeGrepMatch<T>>> currentNodeMatchList, int treeChildId, int start, int end) {

        boolean foundMatch = true;
        List<List<TreeGrepMatch<T>>> childrenMatches = new ArrayList<>();
        for (int patternChildId = start; patternChildId < end; patternChildId++, treeChildId++) {

            Tree<T> treeChildNode = tree.getChild(treeChildId);

            Tree<T> patternChildNode = currentPattern.getChild(patternChildId);

            List<TreeGrepMatch<T>> childMatches = new ArrayList<>();

            childMatches = doesNodeMatchPattern(treeChildNode, patternChildNode, childMatches);
            if (childMatches.size() == 0) {
                foundMatch = false;
                break;
            } // end if
            childrenMatches.add(childMatches);

            if (verbose) {
                logger.info(treeChildNode + " matches " + patternChildNode);
                logger.info(childMatches.toString());
            }

        } // end for each pattern node

        if (foundMatch) {
            if (verbose) {
                logger.info("Found match for children of " + currentPattern);
                logger.info(childrenMatches.toString());

            }
            // Add this new match to the existing one.

            // first make a new match for this root node
            TreeGrepMatch<T> currentNodeMatch = new TreeGrepMatch<>(currentPattern);
            currentNodeMatch.addMatch(tree);
            List<TreeGrepMatch<T>> currentNodeMatches = new ArrayList<>();
            currentNodeMatches.add(currentNodeMatch);

            // add all the children
            currentNodeMatches = mergeChildrenMatches(childrenMatches, currentNodeMatches);

            if (verbose) {
                logger.info("Matches for subtree at " + tree.getLabel());
                logger.info(currentNodeMatches.toString());
            }

            currentNodeMatchList.add(currentNodeMatches);

            // all the patternChildren have matched
            treeChildMatchPositions.add(treeChildId);
        }// if foundmatch
    }

    protected List<TreeGrepMatch<T>> mergeChildrenMatches(
            List<List<TreeGrepMatch<T>>> childrenMatches, List<TreeGrepMatch<T>> currentNodeMatches) {
        List<TreeGrepMatch<T>> newMatches = new ArrayList<>();

        for (List<TreeGrepMatch<T>> children : Permutations.crossProduct(childrenMatches)) {

            for (TreeGrepMatch<T> match : currentNodeMatches) {
                TreeGrepMatch<T> newMatch = new TreeGrepMatch<>(match);

                for (TreeGrepMatch<T> child : children)
                    newMatch.mergeMatches(child);

                newMatches.add(newMatch);

            }
        }
        return newMatches;

    }

    private List<TreeGrepMatch<T>> mergeMatches(List<TreeGrepMatch<T>> nodeOutsideMatches,
            List<TreeGrepMatch<T>> currentNodeChildrenMatches) {
        if (nodeOutsideMatches.size() == 0)
            return currentNodeChildrenMatches;
        List<TreeGrepMatch<T>> nodeMatches = new ArrayList<>();
        for (TreeGrepMatch<T> nodeOutsideMatch : nodeOutsideMatches) {
            for (TreeGrepMatch<T> nodeChildrenMatch : currentNodeChildrenMatches) {
                TreeGrepMatch<T> newMatch = new TreeGrepMatch<>(nodeOutsideMatch);

                newMatch.mergeMatches(nodeChildrenMatch);

                nodeMatches.add(newMatch);
            }
        }

        return nodeMatches;
    }

    @Override
    public String toString() {
        return this.getPattern().toString();
    }

    /**
     * @return the pattern
     */
    public Tree<T> getPattern() {
        return pattern;
    }

}
