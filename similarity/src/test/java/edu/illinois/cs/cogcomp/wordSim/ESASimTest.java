package edu.illinois.cs.cogcomp.wordSim;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;
import edu.illinois.cs.cogcomp.wsim.esa.ResourcesConfig;

public class ESASimTest {

	@Test
	public void test() {
		ResourcesConfig config=new ResourcesConfig();
		MemoryBasedESA esa = new MemoryBasedESA(config);
		double score1=esa.cosin("queen", "king");
		double score2=esa.cosin("queen","word");
		assertTrue(score1>score2);
	}

}
