/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.kernels.graph;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.GraphView;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of the Shortest Path Kernel for Graphs
 * <p>
 * Reference paper:
 * [1] K. M. Borgwardt and H.-P. Kriegel, “Shortest-Path Kernels on Graphs,” in
 * Proceedings of the Fifth IEEE International Conference on Data Mining, 2005, pp. 74–81.
 * <p>
 * Adopted from: https://github.com/SAG-KeLP/kelp-additional-kernels/blob/master/src/main/java/it/uniroma2/sag/kelp/kernel/graph/ShortestPathKernel.java
 *
 * @author Giovanni Da San Martino
 * @author Simone Filice
 * @author Daniel Khashabi
 */
public class ShortestPathKernel implements FeatureExtractor<Pair<GraphView, GraphView>> {
    @Override
    public Set<Feature> getFeatures(Pair<GraphView, GraphView> c) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<>();
        float result = kernelComputation(c.getFirst(), c.getSecond());
        features.add(new RealFeature(this.getName(), result));
        return features;
    }

    @Override
    public String getName() {
        return "shortest-path-kernel";
    }

    public float kernelComputation(GraphView exA, GraphView exB) {
        TObjectIntMap<String> labelDistancesA = getLabelDistances(exA);
        TObjectIntMap<String> labelDistancesB = getLabelDistances(exB);

        float sum = 0;

        TObjectIntMap<String> shortest;
        TObjectIntMap<String> longest;
        if (labelDistancesA.size() < labelDistancesB.size()) {
            shortest = labelDistancesA;
            longest = labelDistancesB;
        } else {
            shortest = labelDistancesB;
            longest = labelDistancesA;
        }
        for (TObjectIntIterator<String> it = shortest.iterator(); it.hasNext(); ) {
            it.advance();
            float shortestValue = it.value();
            float longestValue = longest.get(it.key());
            sum += shortestValue * longestValue;
        }
        return sum;
    }

    /**
     * the function that determines the keys of the map. The function counts occurances of this keys
     * you can override it with your favorite key
     */
    public static String createKey(int i, int j, GraphView graph) {
        return graph.getConstituents().get(i).getLabel() + graph.getConstituents().get(j).getLabel() + graph.getNodeDistance(i, j);
    }

    public static TObjectIntMap<String> getLabelDistances(GraphView graph) {
        TObjectIntMap<String> labelDistances = new TObjectIntHashMap<>();
        graph.computeNodeDistances();
        for (int i = 0; i < graph.getNumberOfConstituents(); i++) {
            for (int j = 0; j < graph.getNumberOfConstituents(); j++) {
                String key = createKey(i, j, graph);
                labelDistances.put(key, labelDistances.get(key) + 1);
            }
        }
        return labelDistances;
    }
}
