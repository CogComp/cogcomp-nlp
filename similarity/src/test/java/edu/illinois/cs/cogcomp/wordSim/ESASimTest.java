package edu.illinois.cs.cogcomp.wordSim;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


public class ESASimTest {

	@Test
	public void test() throws IOException {
		String CONFIG="config/configurations.properties";
		ResourceManager rm_=new ResourceManager(CONFIG);
		MemoryBasedESA esa = new MemoryBasedESA(rm_);
		double score1=esa.cosin("queen", "king");
		double score2=esa.cosin("queen","word");
		assertTrue(score1>score2);
	}

}
