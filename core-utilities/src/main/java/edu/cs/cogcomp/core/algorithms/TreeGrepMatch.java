/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.core.algorithms;

import edu.cs.cogcomp.core.datastructures.trees.Tree;
import edu.cs.cogcomp.core.datastructures.trees.TreeTraversal;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the result of a single match for
 * {@link edu.illinois.cs.cogcomp.core.algorithms.TreeGrep}. For every pattern node, it can be used
 * to recover the matching node in the tree that is being searched.
 * <p>
 * <b>Usage:</b> Suppose <code>match</code> is an instance of TreeGrepMatch{@literal <T>} that is
 * generated by searching through a tree <code>t</code> for some pattern <code>p</code>.
 * <p>
 * Then, the <code>match</code> object provides three methods to access the matching elements
 * between the tree and the pattern.
 * <p>
 * <p>
 * <ul>
 * <li> {@link edu.illinois.cs.cogcomp.core.algorithms.TreeGrepMatch#getPatternDFSMatches()}: This
 * gives a list of matches for the nodes of the pattern, assuming that the pattern is traversed in
 * depth first order. Use {@link TreeTraversal#depthFirstTraversal(Tree)} for depth first traversal
 * of the pattern.
 * <li> {@link edu.illinois.cs.cogcomp.core.algorithms.TreeGrepMatch#getPatternLeafMatches()}: This
 * gives a list of matches for just the leaves of the pattern.
 * <li> {@link edu.illinois.cs.cogcomp.core.algorithms.TreeGrepMatch#getRootMatch()}: This gets the
 * node in the tree that matches the root of the pattern.
 * </ul>
 * <p>
 *
 * @author Vivek Srikumar
 * @see edu.illinois.cs.cogcomp.core.algorithms.TreeGrep
 * @see TreeTraversal#depthFirstTraversal(Tree)
 */
public class TreeGrepMatch<T> {
    /*
     * By convention, the pattern tree is traversed depth first while searching. This traversal also
     * gives a unique identifier for each pattern node, which is used for storing matches.
     */

    protected Tree<T>[] dfs;
    int currentPosition;
    protected List<Tree<T>> matches;
    protected List<Tree<T>> patternLeafMatches;
    private final Tree<T> pattern;

    public TreeGrepMatch(TreeGrepMatch<T> t) {
        dfs = t.dfs;
        matches = new ArrayList<>(t.matches);
        patternLeafMatches = new ArrayList<>(t.patternLeafMatches);
        currentPosition = t.currentPosition;
        this.pattern = t.pattern;
    }

    @SuppressWarnings("unchecked")
    public TreeGrepMatch(Tree<T> pattern) {
        this.pattern = pattern;
        dfs = new Tree[pattern.size()];
        matches = new ArrayList<>();
        patternLeafMatches = new ArrayList<>();
        int i = 0;
        for (Tree<T> item : TreeTraversal.depthFirstTraversal(pattern)) {
            dfs[i++] = item;
        }
        currentPosition = 0;
    }

    void addMatch(Tree<T> match) {
        if (dfs[currentPosition].isLeaf())
            patternLeafMatches.add(match);

        matches.add(match);
        currentPosition++;
    }

    void mergeMatches(TreeGrepMatch<T> newMatch) {
        for (Tree<T> match : newMatch.matches) {
            addMatch(match);
        }
    }

    /**
     * Get the nodes in the most recently searched tree which match the leaves of the pattern.
     */
    public List<Tree<T>> getPatternLeafMatches() {
        return patternLeafMatches;
    }

    /**
     * Get nodes in the most recently searched tree, which match those of the pattern when traversed
     * depth first.
     */
    public List<Tree<T>> getPatternDFSMatches() {
        return matches;
    }

    /**
     * Get the node in the most recently searched tree that matches that root of the pattern.
     */
    public Tree<T> getRootMatch() {
        return matches.get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");

        for (Tree<T> d : dfs) {

            sb.append(d.getLabel()).append(". ");
        }
        sb.append("}\t{");
        for (Tree<T> d : this.matches) {
            sb.append(" ").append(d).append(".");

        }

        sb.append("}");
        return sb.toString();
    }

    Tree<T> getCurrentPatternNode() {
        return this.dfs[this.currentPosition];
    }

    public Tree<T> getPattern() {
        return this.pattern;
    }
}
