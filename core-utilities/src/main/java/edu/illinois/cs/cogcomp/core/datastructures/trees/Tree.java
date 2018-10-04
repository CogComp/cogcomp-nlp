/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.datastructures.trees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class Tree<T> implements Serializable {

    private static final long serialVersionUID = 5888625065211632698L;
    // statistics about the tree
    private int numNodes;
    private int height;
    boolean heightChanged = true;
    List<Tree<T>> yield;
    boolean yieldChanged = true;

    // information about the current node
    private T label;

    // structure of the tree
    private List<T> childrenEdgeLabels;
    private List<Tree<T>> children;
    Tree<T> parent;

    /**
     * Default constructor for a tree.
     * <p>
     * The constructed tree will not have any label, children.
     */
    public Tree() {
        height = 0;
        children = new ArrayList<>();
        childrenEdgeLabels = new ArrayList<>();
        parent = null;
        numNodes = 1;
    }

    /**
     * Creates a tree with just the root.
     *
     * @param label : The data stored in the root of the tree.
     */
    public Tree(T label) {
        this.label = label;
        height = 0;
        children = new ArrayList<>();
        childrenEdgeLabels = new ArrayList<>();
        parent = null;
        numNodes = 1;
    }

    public void addSubtreeAt(Tree<T> tree, T edgeLabel, int position) {
        tree.parent = this;
        children.add(position, tree);
        childrenEdgeLabels.add(position, edgeLabel);

        // update statistics
        numNodes += tree.numNodes;
        if (this.parent != null)
            this.parent.updateNumNodes();

        // update flags
        yieldChanged = true;
        heightChanged = true;

    }

    public void addSubtreeAt(Tree<T> tree, int position) {
        addSubtreeAt(tree, null, position);
    }

    public void addSubtree(Tree<T> tree, T edgeLabel) {
        // this could be written as a specific instance of addSubtreeAt, but the
        // insertion at a specific position is O(n), while what is done here is
        // O(1)

        tree.parent = this;
        children.add(tree);
        childrenEdgeLabels.add(edgeLabel);

        // update statistics
        numNodes += tree.numNodes;
        if (this.parent != null)
            this.parent.updateNumNodes();

        // update flags
        yieldChanged = true;
        heightChanged = true;
    }

    public void addSubtree(Tree<T> tree) {
        addSubtree(tree, null);
    }

    public void addSubtrees(Iterable<Tree<T>> subtrees, Iterable<T> edgeLabels) {
        Iterator<Tree<T>> treeIter = subtrees.iterator();
        Iterator<T> edgeIter = edgeLabels.iterator();

        while (treeIter.hasNext() && edgeIter.hasNext()) {
            this.addSubtree(treeIter.next(), edgeIter.next());
        }
    }

    public void addSubtrees(Iterable<Tree<T>> subtrees) {
        for (Tree<T> tree : subtrees) {
            this.addSubtree(tree);
        }
    }

    public void addLeaf(T leafLabel, T edgeLabel) {
        addSubtree(new Tree<>(leafLabel), edgeLabel);
    }

    public void addLeaf(T leafLabel) {
        addSubtree(new Tree<>(leafLabel));
    }

    /**
     * Read a tree from a string parenthesis representation
     */
    public static Tree<String> readTreeFromString(String treeString) {
        return (new TreeParser<>(new INodeReader<String>() {

            public String parseNode(String string) {
                return string;
            }
        })).parse(treeString);
    }

    /*
     * -------------------------------------------------------------------------- -------- GETTERS
     * ---------------------------------------------------------- ------------------------
     */

    public Tree<T> getChild(int position) {
        return children.get(position);
    }

    public T getLabel() {
        return label;
    }

    public Iterable<Tree<T>> childrenIterator() {
        return children;
    }

    public Iterable<T> childrenEdgeLabelIterator() {
        return this.childrenEdgeLabels;
    }

    public List<Tree<T>> getChildren() {
        return children;
    }

    public List<T> getChildrenEdgeLabels() {
        return this.childrenEdgeLabels;
    }

    public T getEdgeLabel(int childId) {
        return this.childrenEdgeLabels.get(childId);
    }

    public int size() {
        return numNodes;
    }

    public List<Tree<T>> getYield() {

        if (yieldChanged)
            computeYield();
        return yield;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public int getNumberOfChildren() {
        return children.size();
    }

    public Tree<T> getParent() {
        return this.parent;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public int getHeight() {
        if (heightChanged)
            computeHeight();
        return height;
    }

    public int getPositionAmongParentsChildren() {
        if (this.isRoot())
            return 0;

        return this.parent.getChildren().indexOf(this);
    }

    @Override
    public String toString() {
        String s = toString(true, 0);

        if (this.isLeaf())
            s = "(" + s + ")";
        return s;

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException {
        Tree<T> cloneTree = new Tree<>(this.label);
        // for (Tree<T> child : children)
        for (int i = 0; i < this.getNumberOfChildren(); i++) {
            Tree<T> child = children.get(i);
            T edgeLabel = childrenEdgeLabels.get(i);

            cloneTree.addSubtree((Tree<T>) (child.clone()), edgeLabel);
        }
        return cloneTree;
    }

    /*
     * -------------------------------------------------------------------------- -------- EQUALITY,
     * HASH AND THE LIKE --------------------------------------
     * --------------------------------------------
     */

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
            Tree<T> otherTree = (Tree<T>) obj;
            if (!(this.label.equals(otherTree.label)))
                return false;

            if (this.getNumberOfChildren() != otherTree.getNumberOfChildren())
                return false;

            for (int childId = 0; childId < children.size(); childId++) {
                if (!(children.get(childId).equals(otherTree.children.get(childId))))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /*
     * -------------------------------------------------------------------------- -------- TREE
     * MODIFIERS -------------------------------------------------- --------------------------------
     */

    void makeParentNull() {
        this.parent = null;
    }

    public void removeChildAt(int position) {
        this.numNodes -= this.getChild(position).numNodes;
        this.children.remove(position);
        this.heightChanged = true;
        this.yieldChanged = true;
    }

    public boolean hasEdgeLabels() {
        if (this.parent == null) {
            return this.parent.childrenEdgeLabels != null;
        } else {
            return !this.isLeaf() && this.childrenEdgeLabels != null;
        }
    }

    /*
     * -------------------------------------------------------------------------- --------
     * MISCELLANEOUS HELPER FUNCTIONS ----------------------------------
     * ------------------------------------------------
     */

    private String toString(boolean firstChild, int spaces) {
        StringBuilder treeString = new StringBuilder();
        if (!firstChild) {
            for (int i = 0; i < spaces; i++) {
                treeString.append(" ");
            }
        }

        if (!this.isLeaf())
            treeString.append("(");

        int position = this.getPositionAmongParentsChildren();

        if (this.parent != null && this.parent.childrenEdgeLabels != null
                && this.parent.childrenEdgeLabels.get(position) != null) {
            treeString.append(":LABEL:")
                    .append(this.parent.childrenEdgeLabels.get(position))
                    .append(" ");
        }

        treeString.append(label.toString());

        if (children.size() > 0)
            treeString.append(" ");

        int len = treeString.length();

        boolean first = true;
        int index = 0;
        for (Iterator<Tree<T>> iterator = children.iterator(); iterator.hasNext();) {
            Tree<T> child = iterator.next();

            treeString.append(child.toString(first, len));

            first = false;

            if (iterator.hasNext())
                treeString.append("\n");
            index++;
        }

        if (!this.isLeaf())
            treeString.append(")");

        return treeString.toString();

    }

    private void computeYield() {
        yield = new ArrayList<>();
        if (this.isLeaf())
            yield.add(this);
        else
            for (Tree<T> t : children)
                yield.addAll(t.getYield());

    }

    private void updateNumNodes() {
        int n = 1;

        for (Tree<T> child : children) {
            n += child.numNodes;
        }
        this.numNodes = n;
        if (this.parent != null)
            this.parent.updateNumNodes();
    }

    private void computeHeight() {
        int max = 0;
        for (Tree<T> child : children) {
            if (child.getHeight() > max) {
                max = child.getHeight();
            }
        }
        this.height = max + 1;
    }

}
