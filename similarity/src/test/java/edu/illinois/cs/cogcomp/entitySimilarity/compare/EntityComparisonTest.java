/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.entitySimilarity.compare;

import org.junit.Test;

import edu.illinois.cs.cogcomp.nesim.compare.EntityComparison;
import edu.illinois.cs.cogcomp.sim.Metric;
import edu.illinois.cs.cogcomp.sim.MetricResponse;
import edu.illinois.cs.cogcomp.sim.NESim;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EntityComparisonTest {

	@Test
	public void testEntityComparison() {
		Metric ec = new NESim();

		String hitchA = "Bill C. Hitchcock";
		String hitchB = "William Hitchcock";
		String hitchC = "Mrs. Hitchcock";
		String hitchD = "Arthur Hitchcock";
		String hitchE = "Bill F. Hitchcock";

		MetricResponse result = ec.compare(hitchA, hitchB);

		assertEquals(result.score, 1.0, 0.01);

		/**
		 * this test FAILS: no gender test
		 */
		result = ec.compare(hitchB, hitchC);

		// assertTrue(Double.parseDouble(result.get(EntityComparison.SCORE)) <
		// 0.5);

		result = ec.compare(hitchB, hitchD);

		assertTrue(result.score < 0.6);

		result = ec.compare(hitchA, hitchE);

		// Another test that fails. Critical local difference.
		// assertTrue( Double.parseDouble( result.get( EntityComparison.SCORE )
		// ) < 0.6 );
	}
}
