/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 *
 */
package edu.illinois.cs.cogcomp.core.search;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vivek Srikumar May 1, 2009
 */
public class SearchTest {

    IStateManager<Pair<Integer, Integer>> stateManager =
            new IStateManager<Pair<Integer, Integer>>() {

                public boolean goalTest(Pair<Integer, Integer> data) {
                    return (data.getFirst() + data.getSecond()) >= 4;
                }

                public List<Pair<Integer, Integer>> nextStates(Pair<Integer, Integer> currentState) {

                    List<Pair<Integer, Integer>> list = new ArrayList<>();

                    list.add(new Pair<>(currentState.getFirst() + 1, currentState.getSecond()));
                    list.add(new Pair<>(currentState.getFirst(), currentState.getSecond() + 1));
                    return list;
                }
            };

    public Integer score(Pair<Integer, Integer> element) {
        return Math.max(element.getFirst(), element.getSecond());
    }

    @Test
    public final void testDepthFirstSearch() {
        DepthFirstSearch<Pair<Integer, Integer>> dfs = new DepthFirstSearch<>();

        Pair<Integer, Integer> result = (dfs.search(new Pair<>(0, 0), stateManager));

        assertEquals(result, new Pair<>(0, 4));
    }

    @Test
    public final void testBreadthFirstSearch() {
        GraphSearch<Pair<Integer, Integer>> dfs = new BreadthFirstSearch<>();

        Pair<Integer, Integer> result = (dfs.search(new Pair<>(0, 0), stateManager));

        assertEquals(result, new Pair<>(4, 0));
    }

    @Test
    public final void testUniformCostSearch() {
        GraphSearch<Pair<Integer, Integer>> dfs =
                new UniformCostSearch<>(new Comparator<Pair<Integer, Integer>>() {
                    public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                        return -score(o1).compareTo(score(o2));
                    }
                });

        Pair<Integer, Integer> result = (dfs.search(new Pair<>(0, 0), stateManager));
        assertEquals(result, new Pair<>(4, 0));
    }

    @Test
    public final void testBeamSearch() {
        GraphSearch<Pair<Integer, Integer>> dfs =
                new BeamSearch<>(4, new Comparator<Pair<Integer, Integer>>() {
                    public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                        return -score(o1).compareTo(score(o2));
                    }
                });

        Pair<Integer, Integer> result = (dfs.search(new Pair<>(0, 0), stateManager));

        assertEquals(result, new Pair<>(4, 0));
    }

}
