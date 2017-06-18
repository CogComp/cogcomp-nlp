package edu.illinois.cs.cogcomp.mrcs;

import org.junit.Ignore;
import org.junit.Test;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.sim.LLMStringSim;
import edu.illinois.cs.cogcomp.sim.Metric;

public class LlmSimTest {

	@Test
	public void llmSimTest(){
		String config = "config/configurations.properties";
		Metric llm =new LLMStringSim(config);
		String s1="please turn on the light";
		String s2="please turn off the TV";
		String s3="please close the door";
		double score=llm.compare(s1, s2).score;
		double score2=llm.compare(s1, s3).score;
		assert( score > score2);
	}
	
	
	@Test
	public void llmphraseSimTest(){
		String config = "config/test2.configurations.properties";
		Metric llm =new LLMStringSim(config);
		String s1="please turn off the light";
		String s2="please turn the light";
		String s3="please turn on the light";
		double score=llm.compare(s1, s2).score;
		double score2=llm.compare(s1, s3).score;
		assert( score < score2);

	}

	@Test 
	public void llmNERSimTest(){
		String config = "config/test.configurations.properties";
		Metric llm =new LLMStringSim(config);
		String s1="Donald Trump turn on the light";
		String s2="Trump turn off the light";
		String s3="He turn off the light";
		double score=llm.compare(s1, s2).score;
		double score2=llm.compare(s1, s3).score;
		System.out.println(score+" "+score2);
		assert( score > score2);
	}
	/**
	@Test
	public void caoxiaoyu() throws Exception{
		String config = "config/test.configurations.properties";
		LLMStringSim llm =new LLMStringSim(config);
		TextAnnotationBuilder tab;
		String s1="please turn on the light";
		String s2="please turn off the TV";	
		boolean splitOnHyphens = false;
		tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));
		TextAnnotation ta1 = tab.createTextAnnotation("001", "001", s1);
		TextAnnotation ta2 = tab.createTextAnnotation("001", "001", s2);

		ta1.addView("NER_CONLL", new View("1","1",ta1,1));
		ta2.addView("NER_CONLL", new View("1","1",ta1,1));
		double score=llm.llm.compareAnnotation(ta1,ta2);
		
		
	}
	**/
}
