/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.math;

import edu.illinois.cs.cogcomp.core.stats.Counter;
import edu.illinois.cs.cogcomp.core.utilities.ArrayUtilities;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PermutationsTest {

    @Test
    public void testGetAllBinaryCombinations() {
        for (int numElements : new int[] {3, 4}) {

            List<int[]> combs = Permutations.getAllBinaryCombinations(numElements);
            assertEquals((int) (Math.pow(2, numElements)), combs.size());

            // There must be 1 array with all zeros, numElements with one 1, ...
            Counter<Integer> numOnes = new Counter<>();

            for (int[] a : combs) {

                for (int i : a) {
                    assertEquals(true, i == 0 || i == 1);
                }
                int sum = ArrayUtilities.sum(a);
                numOnes.incrementCount(sum);
            }

            for (int i = 0; i < numElements; i++) {
                assertEquals((double) MathUtilities.binomialCoeffs(numElements, i),
                        numOnes.getCount(i), 0.0);
            }
        }

    }

}
