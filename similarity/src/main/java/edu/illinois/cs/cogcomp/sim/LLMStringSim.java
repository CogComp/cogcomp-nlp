/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.sim;

import java.io.IOException;

import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.llm.common.PhraseList;
import edu.illinois.cs.cogcomp.llm.common.Preprocess;
import edu.illinois.cs.cogcomp.llm.comparators.LlmStringComparator;

/**
 * 
 * Lexical Level Matching Metric
 * 
 * @author shaoshi
 *
 */
public class LLMStringSim implements Metric<String> {
	public LlmStringComparator llm;
	ResourceManager rm_;
	Preprocess preprocess;
	PhraseList list;

	public LLMStringSim(String config) {
		try {
			rm_ = new ResourceManager(config);
			llm = new LlmStringComparator(rm_);
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResourceManager fullRm = new SimConfigurator().getConfig(rm_);
		preprocess = new Preprocess();
		if (fullRm.getBoolean(SimConfigurator.USE_NE_COMPARISON.key)) {
			preprocess.initializeNER();
		}
		String phrases = fullRm.getString(SimConfigurator.PHRASE_DICT.key);
		list = new PhraseList(phrases);
	}

	@Override
	public MetricResponse compare(String arg1, String arg2) throws IllegalArgumentException {
		String reason = "";
		try {
			ResourceManager fullRm = new SimConfigurator().getConfig(rm_);
			double score;
			if (fullRm.getBoolean(SimConfigurator.USE_PHRASE_COMPARISON.key)) {
				System.out.println("using phrase representations");
				arg1 = preprocess.getPhrase(arg1, list);
				arg2 = preprocess.getPhrase(arg2, list);
				// System.out.println(arg1+"||"+arg2);
			}
			if (fullRm.getBoolean(SimConfigurator.USE_NE_COMPARISON.key)) {
				System.out.println("using NER annotator");
				TextAnnotation ta1 = preprocess.runNER(arg1);
				TextAnnotation ta2 = preprocess.runNER(arg2);
				score = llm.compareAnnotation(ta1, ta2);
			} else
				score = llm.compareStrings_(arg1, arg2);
			return new MetricResponse(score, reason);
		} catch (Exception e) {

		}
		return null;

	}

}
