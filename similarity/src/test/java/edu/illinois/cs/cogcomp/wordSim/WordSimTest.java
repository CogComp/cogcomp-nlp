package edu.illinois.cs.cogcomp.wordSim;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.illinois.cs.cogcomp.sim.MetricResponse;
import edu.illinois.cs.cogcomp.sim.WordSim;


public class WordSimTest {
	

	@Test
	public void test() {
		WordSim ws=new WordSim();
		System.out.println("paragram similarity :"+ws.compare("word","sentence","paragram"));
		System.out.println("paragram similarity :"+ws.compare("word","sentence","esa"));
		System.out.println("paragram similarity :"+ws.compare("word","sentence","word2vec"));
		System.out.println("paragram similarity :"+ws.compare("word","sentence","glove"));
		System.out.println("paragram similarity :"+ws.compare("word","sentence","wordnet"));
		MetricResponse m1=ws.compare("word","sentence","paragram");
		MetricResponse m2=ws.compare("man","wife","wordnet");
		System.out.println(m1.score+m1.reason);
		System.out.println(m2.score+m2.reason);
		
		assertTrue(m1.score > m2.score);
	}
}
