package edu.illinois.cs.cogcomp.edison.utilities;

import junit.framework.TestCase;

public class ConcretenessScorerTest extends TestCase {

	public void testGetRating() throws Exception {
		TestCase.assertEquals(4.93, ConcretenessScorer.getRating("human"));
	}
}