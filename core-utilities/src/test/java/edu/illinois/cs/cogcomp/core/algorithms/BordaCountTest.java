/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Narender Gupta
 */
public class BordaCountTest {
    private List<List<String>> rankings;
    private List<String> aggregate;

    @Before
    public void init() throws Exception {
        this.rankings = new ArrayList<>();
        List<String> ranking1 = new ArrayList<>();
        ranking1.add("A");
        ranking1.add("B");
        ranking1.add("C");
        ranking1.add("D");
        this.rankings.add(ranking1);
        List<String> ranking2 = new ArrayList<>();
        ranking2.add("D");
        ranking2.add("B");
        ranking2.add("C");
        ranking2.add("A");
        this.rankings.add(ranking2);
        List<String> ranking3 = new ArrayList<>();
        ranking3.add("D");
        ranking3.add("B");
        ranking3.add("A");
        ranking3.add("C");
        this.rankings.add(ranking3);
        List<String> ranking4 = new ArrayList<>();
        ranking4.add("C");
        ranking4.add("B");
        ranking4.add("D");
        ranking4.add("A");
        this.rankings.add(ranking4);

        this.aggregate = new ArrayList<>();
        this.aggregate.add("B");
        this.aggregate.add("D");
        this.aggregate.add("C");
        this.aggregate.add("A");
    }

    @Test
    public void testSimpleAggregateRanking() {
        List<String> aggregate = BordaCount.getAggregatedRanking(this.rankings);
        assert (aggregate != null);
        assert (aggregate.equals(this.aggregate));
    }
}
