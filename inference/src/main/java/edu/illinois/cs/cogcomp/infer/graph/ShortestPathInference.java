/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import edu.illinois.cs.cogcomp.infer.Inference;

public class ShortestPathInference<Node> implements Inference<List<Node>> {

    protected SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph;

    private Map<Node, Double> scores = new HashMap<Node, Double>();

    private Node source;
    private Node target;

    private List<Node> shortestPath;

    protected boolean negativeWeights = false;

    public ShortestPathInference() {
        graph =
                new SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge>(
                        DefaultWeightedEdge.class);
    }

    public void addNode(Node node, double score) {
        graph.addVertex(node);
        if (score != 0)
            scores.put(node, score);
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public void setTarget(Node target) {
        this.target = target;
    }

    protected Double getNodeScore(Node t) {
        if (scores.containsKey(t))
            return scores.get(t);
        else
            return 0d;
    }

    public List<Node> runInference() {
        List<DefaultWeightedEdge> path;

        if (negativeWeights) {
            path = BellmanFordShortestPath.findPathBetween(graph, source, target);
        } else {
            path = DijkstraShortestPath.findPathBetween(graph, source, target);
        }

        shortestPath = new ArrayList<Node>();

        if (path == null) {
            System.out.println(graph);
        }

        DefaultWeightedEdge firstEdge = path.get(0);
        assert firstEdge != null;

        shortestPath.add(graph.getEdgeSource(firstEdge));

        for (DefaultWeightedEdge e : path) {
            shortestPath.add(graph.getEdgeTarget(e));
        }

        return shortestPath;
    }

    public void addEdge(Node s, Node t, double score) {
        DefaultWeightedEdge e = graph.addEdge(s, t);
        double sc = getNodeScore(t) + score;

        graph.setEdgeWeight(e, sc);

        if (sc < 0)
            negativeWeights = true;
    }

    public void updateEdgeWeight(Node s, Node t, double score) {
        DefaultWeightedEdge e = graph.getEdge(s, t);
        double sc = getNodeScore(t) + score;

        graph.setEdgeWeight(e, sc);

        if (sc < 0)
            negativeWeights = true;
    }

    public List<Node> getShortestPath() {
        if (shortestPath == null)
            throw new RuntimeException("Inference not run yet!");
        return shortestPath;
    }

}
