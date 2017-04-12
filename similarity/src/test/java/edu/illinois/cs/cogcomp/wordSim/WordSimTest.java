package edu.illinois.cs.cogcomp.wordSim;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.illinois.cs.cogcomp.sim.MetricResponse;
import edu.illinois.cs.cogcomp.sim.WordSim;


public class WordSimTest {
	@Test
	public void test() {
		WordSim ws=new WordSim();
		MetricResponse m1=ws.compare("word","sentence","paragram");
		MetricResponse m2=ws.compare("man","wife","wordnet");
		System.out.println(m1.score+m1.reason);
		System.out.println(m2.score+m2.reason);
		
		assertTrue(m1.score > m2.score);
	}
}
