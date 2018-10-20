/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wordSim;

import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.comparators.LlmStringComparator;
import edu.illinois.cs.cogcomp.sim.MetricResponse;
import edu.illinois.cs.cogcomp.sim.WordSim;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class WordSimTest {

	static ResourceManager rm_;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rm_ = new SimConfigurator().getDefaultConfig();
	}

	@Test
	public void testWordNet() {
		WordSim ws = new WordSim(rm_, "wordnet");
		MetricResponse m1 = ws.compare("word", "sentence", "wordnet");
		MetricResponse m2 = ws.compare("word", "wife", "wordnet");
		assertTrue(m1.score > m2.score);
	}

	@Test
	public void testParagram() throws Exception {
		WordSim ws = new WordSim(rm_, "paragram");
		MetricResponse m1 = ws.compare("word", "sentence", "paragram");
		MetricResponse m2 = ws.compare("word", "wife", "paragram");
		assertTrue(m1.score > m2.score);
	}

}
