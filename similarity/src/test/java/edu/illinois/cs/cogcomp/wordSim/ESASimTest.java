package edu.illinois.cs.cogcomp.wordSim;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.wsim.esa.MemoryBasedESA;


public class ESASimTest {

	@Test
	public void test() throws IOException {
		String CONFIG="configurations";
		ResourceManager rm_=new ResourceManager(CONFIG); 
		MemoryBasedESA esa = new MemoryBasedESA(rm_);
		double score1=esa.cosin("queen", "king");
		double score2=esa.cosin("queen","word");
		assertTrue(score1>score2);
	}

}
