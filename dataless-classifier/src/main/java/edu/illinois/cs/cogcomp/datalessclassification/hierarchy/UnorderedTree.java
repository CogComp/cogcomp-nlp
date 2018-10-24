/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.hierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.google.common.collect.ImmutableSet;

import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * A Generic Tree Implementation, largely based on Jung's OrderedKAryTree (edu.uci.ics.jung.graph)
 * -- adapted largely to discount the order of nodes, and to support arbitrary number of children --
 * allows some data to be associated with Edges, which was not immediately required but is kind of a
 * forward planning for the project
 * 
 * @author shashank
 */

public class UnorderedTree<V extends Serializable, E extends Serializable> extends
        AbstractTypedGraph<V, E> implements Tree<V, E> {

    private static final long serialVersionUID = 1L;

    protected Map<E, Pair<V>> edge_vpairs;
    protected Map<V, VertexData> vertex_data;
    protected int height;
    protected V root;

    public UnorderedTree() {
        super(EdgeType.DIRECTED);
        this.height = -1;
        this.edge_vpairs = new HashMap<>();
        this.vertex_data = new HashMap<>();
    }

    /**
     * @param vertex the vertex whose number of children is to be returned
     * @return the number of children that the {@code vertex} has
     */
    @Override
    public int getChildCount(V vertex) {
        if (!containsVertex(vertex))
            return 0;

        Set<E> edges = vertex_data.get(vertex).child_edges;

        if (edges == null)
            return 0;

        return edges.size();
    }

    @Override
    public Set<E> getChildEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;

        Set<E> edges = vertex_data.get(vertex).child_edges;

        if (edges == null)
            return Collections.emptySet();
        else
            return new ImmutableSet.Builder<E>().addAll(edges).build();
    }

    /**
     * Returns a set of vertex's child vertices. If the vertex has no children then an empty set
     * will be returned.
     */
    @Override
    public Set<V> getChildren(V vertex) {
        if (!containsVertex(vertex))
            return null;

        Set<E> edges = vertex_data.get(vertex).child_edges;

        if (edges == null)
            return Collections.emptySet();

        Set<V> children = new HashSet<V>(edges.size());

        for (E edge : edges)
            children.add(this.getOpposite(vertex, edge));

        return new ImmutableSet.Builder<V>().addAll(children).build();
    }

    /**
     * @return the depth of the vertex in this tree, or -1 if the vertex is not present in this tree
     */
    @Override
    public int getDepth(V vertex) {
        if (!containsVertex(vertex))
            return -1;

        return vertex_data.get(vertex).depth;
    }

    /**
     * Returns the height of the tree, or -1 if the tree is empty.
     */
    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public V getParent(V vertex) {
        if (!containsVertex(vertex))
            return null;
        else if (vertex.equals(root))
            return null;

        return edge_vpairs.get(vertex_data.get(vertex).parent_edge).getFirst();
    }

    @Override
    public E getParentEdge(V vertex) {
        if (!containsVertex(vertex))
            return null;

        return vertex_data.get(vertex).parent_edge;
    }

    @Override
    public V getRoot() {
        return root;
    }

    @Override
    public Collection<Tree<V, E>> getTrees() {
        Collection<Tree<V, E>> forest = new ArrayList<>(1);
        forest.add(this);

        return forest;
    }

    /**
     * Adds the specified {@code child} vertex and edge {@code e} to the graph with the specified
     * parent vertex {@code parent}.
     * 
     * @param e the edge to add
     * @param parent the source of the edge to be added
     * @param child the destination of the edge to be added
     * @return {@code true} if the graph has been modified
     */
    @Override
    public boolean addEdge(E e, V parent, V child) {
        if (e == null || child == null || parent == null)
            throw new IllegalArgumentException("Inputs must not be null");

        if (!containsVertex(parent))
            throw new IllegalArgumentException("Tree must already include parent: " + parent);

        if (containsVertex(child))
            throw new IllegalArgumentException("Tree must not already include child: " + child);

        if (parent.equals(child))
            throw new IllegalArgumentException("Input vertices must be distinct");

        Pair<V> endpoints = new Pair<>(parent, child);

        if (containsEdge(e)) {
            if (!endpoints.equals(edge_vpairs.get(e)))
                throw new IllegalArgumentException("Tree already includes edge" + e
                        + " with different endpoints " + edge_vpairs.get(e));
            else
                return false;
        }

        VertexData parent_data = vertex_data.get(parent);
        Set<E> outedges = parent_data.child_edges;

        if (outedges == null) {
            parent_data.child_edges = new HashSet<>();
            outedges = parent_data.child_edges;
        }

        outedges.add(e);

        // initialize VertexData for child; leave child's child_edges null for now
        VertexData child_data = new VertexData(e, parent_data.depth + 1);
        vertex_data.put(child, child_data);

        height = child_data.depth > height ? child_data.depth : height;
        edge_vpairs.put(e, endpoints);

        return true;
    }

    @Override
    public boolean addEdge(E e, V v1, V v2, EdgeType edge_type) {
        this.validateEdgeType(edge_type);

        return addEdge(e, v1, v2);
    }

    @Override
    public boolean addEdge(E edge, Pair<? extends V> endpoints, EdgeType edgeType) {
        if (edge == null || endpoints == null)
            throw new IllegalArgumentException("inputs must not be null");

        return addEdge(edge, endpoints.getFirst(), endpoints.getSecond(), edgeType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addEdge(E edge, Collection<? extends V> vertices, EdgeType edge_type) {
        if (edge == null || vertices == null)
            throw new IllegalArgumentException("inputs must not be null");

        if (vertices.size() != 2)
            throw new IllegalArgumentException("'vertices' must contain "
                    + "exactly 2 distinct vertices");

        this.validateEdgeType(edge_type);

        Pair<V> endpoints;

        if (vertices instanceof Pair)
            endpoints = (Pair<V>) vertices;
        else
            endpoints = new Pair<>(vertices);

        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();

        if (v1.equals(v2))
            throw new IllegalArgumentException("Input vertices must be distinct");

        return addEdge(edge, v1, v2);
    }

    @Override
    public boolean addVertex(V vertex) throws UnsupportedOperationException {
        if (root == null) {
            this.root = vertex;
            vertex_data.put(vertex, new VertexData(null, 0));
            this.height = 0;
            return true;
        }

        else {
            throw new UnsupportedOperationException("Unless you are setting "
                    + "the root, use addEdge() or addChild()");
        }
    }

    @Override
    public V getDest(E directed_edge) {
        if (!containsEdge(directed_edge))
            return null;

        return edge_vpairs.get(directed_edge).getSecond();
    }

    @Override
    public Pair<V> getEndpoints(E edge) {
        if (!containsEdge(edge))
            return null;

        return edge_vpairs.get(edge);
    }

    @Override
    public Set<E> getInEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        else if (vertex.equals(root))
            return Collections.emptySet();
        else
            return Collections.singleton(getParentEdge(vertex));
    }

    @Override
    public V getOpposite(V vertex, E edge) {
        if (!containsVertex(vertex) || !containsEdge(edge))
            return null;

        Pair<V> endpoints = edge_vpairs.get(edge);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();

        return v1.equals(vertex) ? v2 : v1;
    }

    @Override
    public Set<E> getOutEdges(V vertex) {
        return getChildEdges(vertex);
    }

    /**
     * @return 0 if <code>vertex</code> is the root, -1 if the vertex is not an element of this
     *         tree, and 1 otherwise
     */
    @Override
    public int getPredecessorCount(V vertex) {
        if (!containsVertex(vertex))
            return -1;

        return vertex.equals(root) ? 0 : 1;
    }

    /**
     * @return Empty Set if the <code>vertex</code> is the root, null if the vertex is not an
     *         element of this tree, and the Parent wrapper in a set otherwise
     */
    @Override
    public Set<V> getPredecessors(V vertex) {
        if (!containsVertex(vertex))
            return null;

        if (vertex.equals(root))
            return Collections.emptySet();

        return Collections.singleton(getParent(vertex));
    }

    @Override
    public V getSource(E directed_edge) {
        if (!containsEdge(directed_edge))
            return null;

        return edge_vpairs.get(directed_edge).getFirst();
    }

    @Override
    public int getSuccessorCount(V vertex) {
        return getChildCount(vertex);
    }

    @Override
    public Set<V> getSuccessors(V vertex) {
        return getChildren(vertex);
    }

    @Override
    public int inDegree(V vertex) {
        if (!containsVertex(vertex))
            return 0;

        if (vertex.equals(root))
            return 0;

        return 1;
    }

    @Override
    public boolean isDest(V vertex, E edge) {
        if (!containsEdge(edge) || !containsVertex(vertex))
            return false;

        return edge_vpairs.get(edge).getSecond().equals(vertex);
    }

    /**
     * Returns <code>true</code> if <code>vertex</code> is a leaf of this tree, i.e., if it has no
     * children.
     * 
     * @param vertex the vertex to be queried
     */
    public boolean isLeaf(V vertex) {
        if (!containsVertex(vertex))
            return false;

        return outDegree(vertex) == 0;
    }

    /**
     * Returns true iff <code>v1</code> is the parent of <code>v2</code>. Note that if
     * <code>v2</code> is the root and <code>v1</code> is <code>null</code>, this method returns
     * <code>true</code>.
     */
    @Override
    public boolean isPredecessor(V v1, V v2) {
        if (!containsVertex(v2))
            return false;

        return getParent(v2).equals(v1);
    }

    /**
     * Returns <code>true</code> if the <code>vertex</code> is the root of this tree
     * 
     * @param vertex the vertex to be queried
     */
    public boolean isRoot(V vertex) {
        if (root == null)
            return false;

        return root.equals(vertex);
    }

    @Override
    public boolean isSource(V vertex, E edge) {
        if (!containsEdge(edge) || !containsVertex(vertex))
            return false;

        return edge_vpairs.get(edge).getFirst().equals(vertex);
    }

    /**
     * Returns true iff <code>v1</code> is the child of <code>v2</code>. Note that if
     * <code>v2</code> is a leaf node and <code>v1</code> is <code>null</code>, this method returns
     * <code>true</code>.
     */
    @Override
    public boolean isSuccessor(V v1, V v2) {
        if (!containsVertex(v2))
            return false;

        if (containsVertex(v1))
            return getParent(v1).equals(v2);

        return isLeaf(v2) && v1 == null;
    }

    @Override
    public int outDegree(V vertex) {
        if (!containsVertex(vertex))
            return 0;

        Set<E> out_edges = vertex_data.get(vertex).child_edges;

        if (out_edges == null)
            return 0;

        return out_edges.size();
    }

    @Override
    public boolean isIncident(V vertex, E edge) {
        if (!containsVertex(vertex) || !containsEdge(edge))
            return false;

        return edge_vpairs.get(edge).contains(vertex);
    }

    @Override
    public boolean isNeighbor(V v1, V v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            return false;

        return getNeighbors(v1).contains(v2);
    }

    @Override
    public boolean containsEdge(E edge) {
        return edge_vpairs.containsKey(edge);
    }

    @Override
    public boolean containsVertex(V vertex) {
        return vertex_data.containsKey(vertex);
    }

    @Override
    public E findEdge(V v1, V v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;

        VertexData v1_data = vertex_data.get(v1);

        if (edge_vpairs.get(v1_data.parent_edge).getFirst().equals(v2))
            return v1_data.parent_edge;

        Set<E> edges = v1_data.child_edges;

        if (edges == null)
            return null;

        for (E edge : edges)
            if (edge_vpairs.get(edge).getSecond().equals(v2))
                return edge;

        return null;
    }

    @Override
    public Set<E> findEdgeSet(V v1, V v2) {
        E edge = findEdge(v1, v2);

        if (edge == null)
            return Collections.emptySet();
        else
            return Collections.singleton(edge);
    }

    @Override
    public int getEdgeCount() {
        return edge_vpairs.size();
    }

    @Override
    public Set<E> getEdges() {
        return new ImmutableSet.Builder<E>().addAll(edge_vpairs.keySet()).build();
    }

    @Override
    public int getIncidentCount(E edge) {
        return 2; // all tree edges have 2 incident vertices
    }

    public Set<E> getIncidentEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;

        Set<E> edges = new HashSet<>();
        VertexData v_data = vertex_data.get(vertex);

        if (v_data.parent_edge != null)
            edges.add(v_data.parent_edge);

        if (v_data.child_edges != null) {
            edges.addAll(v_data.child_edges);
        }

        if (edges.isEmpty())
            return Collections.emptySet();

        return new ImmutableSet.Builder<E>().addAll(edges).build();
    }

    @Override
    public Collection<V> getIncidentVertices(E edge) {
        return edge_vpairs.get(edge);
    }

    @Override
    public int getNeighborCount(V vertex) {
        if (!containsVertex(vertex))
            return 0;

        return (vertex.equals(root) ? 0 : 1) + this.getChildCount(vertex);
    }

    @Override
    public Set<V> getNeighbors(V vertex) {
        if (!containsVertex(vertex))
            return null;

        Set<V> vertices = new HashSet<>();
        VertexData v_data = vertex_data.get(vertex);

        if (v_data.parent_edge != null)
            vertices.add(edge_vpairs.get(v_data.parent_edge).getFirst());

        if (v_data.child_edges != null) {
            for (E edge : v_data.child_edges)
                vertices.add(edge_vpairs.get(edge).getSecond());
        }

        if (vertices.isEmpty())
            return Collections.emptySet();

        return new ImmutableSet.Builder<V>().addAll(vertices).build();
    }

    @Override
    public int getVertexCount() {
        return vertex_data.size();
    }

    @Override
    public Set<V> getVertices() {
        return new ImmutableSet.Builder<V>().addAll(vertex_data.keySet()).build();
    }

    @Override
    public boolean removeEdge(E edge) {
        if (!containsEdge(edge))
            return false;

        removeVertex(edge_vpairs.get(edge).getSecond());
        edge_vpairs.remove(edge);

        return true;
    }

    @Override
    public boolean removeVertex(V vertex) {
        if (!containsVertex(vertex))
            return false;

        // recursively remove all of vertex's children
        for (V v : getChildren(vertex))
            removeVertex(v);

        E parent_edge = getParentEdge(vertex);
        edge_vpairs.remove(parent_edge);

        Set<E> edges = vertex_data.get(vertex).child_edges;

        if (edges != null)
            for (E edge : edges)
                edge_vpairs.remove(edge);

        vertex_data.remove(vertex);

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnorderedTree<?, ?>))
            return false;

        UnorderedTree<V, E> that = (UnorderedTree<V, E>) o;

        if (this.edge_vpairs.size() != that.edge_vpairs.size())
            return false;

        for (E edge : this.edge_vpairs.keySet()) {
            Pair<V> thisPair = this.edge_vpairs.get(edge);

            if (!that.edge_vpairs.containsKey(edge))
                return false;

            Pair<V> thatPair = that.edge_vpairs.get(edge);

            if (!thisPair.equals(thatPair))
                return false;
        }

        if (this.vertex_data.size() != that.vertex_data.size())
            return false;

        for (V vertex : this.vertex_data.keySet()) {
            VertexData thisData = this.vertex_data.get(vertex);

            if (!that.vertex_data.containsKey(vertex))
                return false;

            VertexData thatData = that.vertex_data.get(vertex);

            if (!thisData.equals(thatData))
                return false;
        }

        if (this.height != that.height)
            return false;

        if (!this.root.equals(that.root))
            return false;

        return true;
    }

    protected class VertexData implements Serializable {
        private static final long serialVersionUID = 1L;

        Set<E> child_edges;
        E parent_edge;
        int depth;

        VertexData(E parent_edge, int depth) {
            this.parent_edge = parent_edge;
            this.depth = depth;
        }

        public boolean equals(VertexData that) {
            if (this.child_edges.size() != that.child_edges.size())
                return false;

            for (E edge : this.child_edges) {
                if (!that.child_edges.contains(edge))
                    return false;
            }

            if (!this.parent_edge.equals(that.parent_edge))
                return false;

            if (this.depth != that.depth)
                return false;

            return true;
        }
    }
}
