/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wordSim;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ESASimTest {

	@Test
	public void test() throws IOException {
		String CONFIG = "config/test.configurations.properties";
		ResourceManager rm_ = new ResourceManager(CONFIG);
		File f1=new File(rm_.getString("memorybasedESA"));
		File f2=new File(rm_.getString("pageIDMapping"));
		MemoryBasedESA esa = new MemoryBasedESA(f1,f2);
		double score1 = esa.cosine("queen", "king");
		System.out.println(score1);
		double score2 = esa.cosine("queen", "word");
		System.out.println(score2);
		assertTrue(score1 > score2);
	}

}
