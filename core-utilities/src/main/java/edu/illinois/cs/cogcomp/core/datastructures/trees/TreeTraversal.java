/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.trees;

import java.util.*;

public class TreeTraversal {


    // /**
    // * Walk through the tree in the post order:
    // * @param <T> The type of the data content of a node
    // * @param tree
    // * @return Sequence of subtrees in post order
    // */
    // public static <T> Iterable<Tree<T>> postorder(Tree<T> tree)
    // {
    // ArrayList<Tree<T>> postOrder = new ArrayList<Tree<T>>();
    //
    // if(tree == null)
    // return postOrder;
    //
    // for(Tree<T> child: tree.childrenIterator())
    // {
    // for(Tree<T> p: postorder(child))
    // {
    // postOrder.add(p);
    // }
    // }
    // postOrder.add(tree);
    // return postOrder;
    // }
    //
    //
    // /**
    // * Walk through the tree in preorder
    // * @param <T> the data type of the content of a node
    // * @param tree
    // * @return Sequence of subtrees in preorder
    // */
    //
    // public static <T> Iterable<Tree<T>> preorder(Tree<T> tree)
    // {
    // ArrayList<Tree<T>> preOrder = new ArrayList<Tree<T>>();
    //
    // if(tree == null)
    // return preOrder;
    //
    // preOrder.add(tree);
    // for(Tree<T> child: tree.childrenIterator())
    // {
    // for(Tree<T> p: preorder(child))
    // {
    // preOrder.add(p);
    // }
    // }
    // return preOrder;
    // }
    //


    /**
     * Get the nodes of the tree in depth first order.
     *
     * @return Iterator over subtrees in dfs
     */
    public static <T> List<Tree<T>> depthFirstTraversal(Tree<T> tree) {
        Stack<Tree<T>> treeStack = new Stack<>();
        treeStack.push(tree);

        ArrayList<Tree<T>> dfs = new ArrayList<>();

        while (!treeStack.isEmpty()) {
            Tree<T> t = treeStack.pop();

            dfs.add(t);

            // We want to do a left to right traversal of the children. Adding
            // the children to the stack just gives a left to right ordering. To fix it,
            // there is a temporary stack.

            Stack<Tree<T>> tmpStack = new Stack<>();

            for (Tree<T> child : t.childrenIterator()) {
                tmpStack.add(child);
            }

            while (!tmpStack.isEmpty()) {
                treeStack.add(tmpStack.pop());
            }
        }
        return dfs;
    }


    /**
     * Do a breadth first traversal over the tree.
     * <p>
     * This does a standard queue based implemenation of the traversal
     *
     * @return An iterable object over the subtrees in breadth first order
     */
    public static <T> List<Tree<T>> breadthFirstTraversal(Tree<T> tree) {
        Queue<Tree<T>> nodeQueue = new LinkedList<>();
        nodeQueue.offer(tree);

        ArrayList<Tree<T>> bfs = new ArrayList<>();

        while (!nodeQueue.isEmpty()) {
            Tree<T> t = nodeQueue.remove();
            bfs.add(t);
            for (Tree<T> child : t.childrenIterator()) {
                nodeQueue.offer(child);
            }

        }

        return bfs;


    }


    /**
     * Get all the nodes of the tree in depth first fashion, limited to a given depth.
     * <p>
     * The depth computation is a little bit ugly, because the tree gives height instead of depths.
     */
    public static <T> List<Tree<T>> depthLimitedTraversal(Tree<T> tree, int depth) {

        Stack<Tree<T>> treeStack = new Stack<>();
        treeStack.push(tree);

        int treeHeight = tree.getHeight();

        ArrayList<Tree<T>> dfs = new ArrayList<>();

        while (!treeStack.isEmpty()) {
            Tree<T> t = treeStack.pop();

            dfs.add(t);

            // We want to do a left to right traversal of the children. Adding
            // the children to the stack just gives a left to right ordering. To fix it,
            // there is a temporary stack.

            Stack<Tree<T>> tmpStack = new Stack<>();

            for (Tree<T> child : t.childrenIterator()) {
                tmpStack.add(child);
            }

            while (!tmpStack.isEmpty()) {
                Tree<T> tmpTree = tmpStack.pop();

                if (treeHeight - tmpTree.getHeight() < depth)
                    treeStack.add(tmpStack.pop());
            }
        }
        return dfs;
    }


    public static <T> List<Tree<T>> bottomUpBreadthFirstTraversal(Tree<T> tree) {
        List<Tree<T>> traversal = new ArrayList<>();

        Queue<Tree<T>> queue = new LinkedList<>();
        queue.add(tree);

        while (!queue.isEmpty()) {
            Tree<T> t = queue.remove();
            traversal.add(t);

            for (int i = t.getNumberOfChildren() - 1; i >= 0; i--) {
                queue.add(t.getChild(i));
            }
        }
        Collections.reverse(traversal);
        return traversal;
    }


}
