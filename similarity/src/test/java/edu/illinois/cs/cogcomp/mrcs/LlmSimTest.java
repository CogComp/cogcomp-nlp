/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
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
	public void llmSimTest() {
		String config = "config/configurations.properties";
		Metric llm = new LLMStringSim(config);
		String s1 = "please turn on the light";
		String s2 = "please turn off the TV";
		String s3 = "please close the door";
		double score = llm.compare(s1, s2).score;
		double score2 = llm.compare(s1, s3).score;
		assert (score > score2);
	}

	@Ignore
	@Test
	public void llmphraseSimTest() {
		String config = "config/test2.configurations.properties";
		Metric llm = new LLMStringSim(config);
		String s1 = "please turn off the light";
		String s2 = "please turn the light";
		String s3 = "please turn on the light";
		double score = llm.compare(s1, s2).score;
		double score2 = llm.compare(s1, s3).score;
		assert (score < score2);

	}

	@Ignore
	@Test
	public void llmNERSimTest() {
		String config = "config/test.configurations.properties";
		Metric llm = new LLMStringSim(config);
		String s1 = "Donald Trump turn off the light";
		String s2 = "Trump turn off the light";
		String s3 = "Shaoshi turn off the light";
		double score = llm.compare(s1, s2).score;
		double score2 = llm.compare(s1, s3).score;
		System.out.println(score + " " + score2);
		assert (score > score2);
	}

}
