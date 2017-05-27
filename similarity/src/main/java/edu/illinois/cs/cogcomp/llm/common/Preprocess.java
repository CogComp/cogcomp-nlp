/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.llm.common;

import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

public class Preprocess {

	NERAnnotator co;

	public void initialize() {
		try {
			co = new NERAnnotator(ViewNames.NER_CONLL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPhrase(String s, PhraseList list) {
		return getContPhrase(getDiscontPhrase(s, list), list);
	}

	public String getContPhrase(String s, PhraseList list) {

		String[] tokens = s.split("\\s+");
		String ret = null;
		for (int i = 1; i < tokens.length;) {
			String phrase = tokens[i - 1] + " " + tokens[i];
			if (list.dict.contains(phrase)) {
				ret += tokens[i - 1] + "_" + tokens[i] + " ";
				i += 2;
			} else
				i = +1;
		}
		return ret;
	}

	public String getDiscontPhrase(String s, PhraseList list) {
		String[] tokens = s.split("\\s+");
		ArrayList<String> words = new ArrayList<String>();
		for (String ss : tokens)
			words.add(ss);
		for (int i = 0; i < words.size(); i++) {
			if (list.verb.contains(words.get(i))) {
				int j = i + 2;
				while (j < i + 5 && j < words.size()) {
					String phrase = words.get(i) + " " + words.get(j);
					if (list.verbPhrase.contains(phrase)) {
						words.set(i, words.get(i) + "_" + words.get(j));
						words.remove(j);
						break;
					}

				}
			}
		}
		String ret = "";
		for (int i = 0; i < words.size(); i++) {
			ret += words.get(i) + " ";
		}
		return ret;
	}

	public TextAnnotation runNER(String s) {
		TextAnnotationBuilder tab;
		// don't split on hyphens, as NER models are trained this way
		boolean splitOnHyphens = false;
		tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));

		TextAnnotation ta = tab.createTextAnnotation("001", "001", s);

		try {
			co.getView(ta);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ta;
	}
}
