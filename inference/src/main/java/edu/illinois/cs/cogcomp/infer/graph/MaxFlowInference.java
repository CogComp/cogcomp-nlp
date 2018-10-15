/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.infer.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.EdmondsKarpMaximumFlow;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import edu.illinois.cs.cogcomp.infer.Inference;

public class MaxFlowInference<Node> implements Inference<Set<Node>> {

    protected SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph;

    private Map<Node, Double> scores = new HashMap<Node, Double>();

    private Node source;
    private Node sink;

    public MaxFlowInference() {
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

    public void setSink(Node sink) {
        this.sink = sink;
    }

    protected Double getNodeScore(Node t) {
        if (scores.containsKey(t))
            return scores.get(t);
        else
            return 0d;
    }

    public void addEdge(Node s, Node t, double score) {
        DefaultWeightedEdge e = graph.addEdge(s, t);
        double sc = getNodeScore(t) + score;

        graph.setEdgeWeight(e, sc);

    }

    public void updateEdgeWeight(Node s, Node t, double score) {
        DefaultWeightedEdge e = graph.getEdge(s, t);
        double sc = getNodeScore(t) + score;

        graph.setEdgeWeight(e, sc);

    }

    @Override
    public Set<Node> runInference() throws Exception {
        EdmondsKarpMaximumFlow<Node, DefaultWeightedEdge> algo =
                new EdmondsKarpMaximumFlow<Node, DefaultWeightedEdge>(graph);

        algo.calculateMaximumFlow(source, sink);
        Map<DefaultWeightedEdge, Double> flow = algo.getMaximumFlow();
        Set<Node> nodes = new HashSet<Node>();

        for (Entry<DefaultWeightedEdge, Double> entry : flow.entrySet()) {
            if (entry.getValue() > 0) {
                DefaultWeightedEdge key = entry.getKey();
                nodes.add(graph.getEdgeSource(key));
                nodes.add(graph.getEdgeTarget(key));
            }
        }

        return nodes;

    }

}
