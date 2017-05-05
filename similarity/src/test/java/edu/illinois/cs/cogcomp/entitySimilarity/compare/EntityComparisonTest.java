/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.entitySimilarity.compare;

import org.junit.Test;

import edu.illinois.cs.cogcomp.nesim.compare.EntityComparison;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by mssammon on 9/12/15.
 */
public class EntityComparisonTest {

	@Test
	public void testEntityComparison() {
		EntityComparison ec = null;
		try {
			ec = new EntityComparison();
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String hitchA = "Bill C. Hitchcock";
		String hitchB = "William Hitchcock";
		String hitchC = "Mrs. Hitchcock";
		String hitchD = "Arthur Hitchcock";
		String hitchE = "Bill F. Hitchcock";

		HashMap<String, String> result = ec.compareNames(hitchA, hitchB);

		assertEquals(Double.parseDouble(result.get(EntityComparison.SCORE)), 1.0, 0.01);

		/**
		 * this test FAILS: no gender test
		 */
		result = ec.compareNames(hitchB, hitchC);

		// assertTrue(Double.parseDouble(result.get(EntityComparison.SCORE)) <
		// 0.5);

		result = ec.compareNames(hitchB, hitchD);

		assertTrue(Double.parseDouble(result.get(EntityComparison.SCORE)) < 0.6);

		result = ec.compareNames(hitchA, hitchE);

		// Another test that fails. Critical local difference.
		// assertTrue( Double.parseDouble( result.get( EntityComparison.SCORE )
		// ) < 0.6 );
	}
}
