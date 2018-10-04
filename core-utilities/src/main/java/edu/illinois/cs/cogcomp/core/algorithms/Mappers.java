/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods to map over {@link Iterable}s and {@link Tree}s. Each method also takes as input
 * an {@link ITransformer} which transforms an individual node of the iterable or the tree into a
 * new object of a (possibly) different type. The methods then return a new iterable or tree
 * containing the new objects.
 *
 * @author Vivek Srikumar
 */
public class Mappers {
    public static <T, S> List<S> map(final Iterable<T> enumeration, final ITransformer<T, S> f) {
        ArrayList<S> items = new ArrayList<>();
        for (T item : enumeration) {
            items.add(f.transform(item));
        }
        return items;
    }

    public static <T, S> Tree<S> mapTree(final Tree<T> tree,
            final ITransformer<Tree<T>, S> transformer) {
        Tree<S> t = new Tree<>(transformer.transform(tree));

        for (Tree<T> treeChild : tree.getChildren()) {
            t.addSubtree(mapTree(treeChild, transformer));
        }
        return t;
    }

    public static <T, S> Tree<S> mapTree(final Tree<T> tree,
            final ITransformer<Tree<T>, S> nodeTransformer, final ITransformer<T, S> edgeTransformer) {
        Tree<S> t = new Tree<>(nodeTransformer.transform(tree));

        for (int i = 0; i < tree.getNumberOfChildren(); i++) {
            Tree<T> treeChild = tree.getChild(i);
            T edgeLabel = tree.getChildrenEdgeLabels().get(i);

            t.addSubtree(mapTree(treeChild, nodeTransformer, edgeTransformer),
                    edgeTransformer.transform(edgeLabel));
        }
        return t;
    }

    public static <T, S> Tree<S> conditionalMapTree(Tree<T> tree,
            ITransformer<Tree<T>, S> transformer, ITransformer<Tree<T>, Boolean> predicate) {
        Tree<S> t = new Tree<>(transformer.transform(tree));

        for (Tree<T> treeChild : tree.getChildren()) {
            if (predicate.transform(treeChild))
                t.addSubtree(conditionalMapTree(treeChild, transformer, predicate));
        }
        return t;
    }

    public static <T, S> Tree<S> mapTreePostfix(Tree<T> tree, ITransformer<Tree<T>, S> transformer) {

        List<Tree<S>> children = new ArrayList<>();

        for (Tree<T> child : tree.getChildren()) {
            children.add(mapTreePostfix(child, transformer));
        }

        Tree<S> newTree = new Tree<>(transformer.transform(tree));
        for (Tree<S> child : children) {
            newTree.addSubtree(child);
        }
        return newTree;
    }

    public static <T, S> Tree<S> mapTreePostfixConditional(Tree<T> tree,
            ITransformer<Tree<T>, S> transformer, ITransformer<Tree<T>, Boolean> predicate) {
        List<Tree<S>> children = new ArrayList<>();

        for (Tree<T> child : tree.getChildren()) {
            children.add(mapTreePostfixConditional(child, transformer, predicate));
        }

        Tree<S> newTree = new Tree<>(transformer.transform(tree));
        for (int i = 0; i < children.size(); i++) {
            Tree<S> child = children.get(i);
            Tree<T> originalChild = tree.getChild(i);
            if (predicate.transform(originalChild))
                newTree.addSubtree(child);
        }
        return newTree;
    }

}
