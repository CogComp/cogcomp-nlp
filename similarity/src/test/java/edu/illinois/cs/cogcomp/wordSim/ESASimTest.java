/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.wordSim;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ESASimTest {
	@Ignore
	@Test
	public void test() throws IOException {
		String CONFIG = "config/configurations.properties";
		ResourceManager rm_ = new ResourceManager(CONFIG);
		MemoryBasedESA esa = new MemoryBasedESA(rm_);
		double score1 = esa.cosin("queen", "king");
		double score2 = esa.cosin("queen", "word");
		assertTrue(score1 > score2);
	}

}
