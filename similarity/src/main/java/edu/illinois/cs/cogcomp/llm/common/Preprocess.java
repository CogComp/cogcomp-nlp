/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.llm.common;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

public class Preprocess {

	NERAnnotator co;

	public void initializeNER() {
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
		String ret = "";
		for (int i = 1; i < tokens.length;) {
			String phrase = tokens[i - 1] + " " + tokens[i];
			if (list.dict.contains(phrase)) {
				ret += tokens[i - 1] + "_" + tokens[i] + " ";
				i += 2;
			} else {
				ret += tokens[i - 1] + " ";
				i += 1;
			}
			// System.out.println(ret);
		}
		ret += tokens[tokens.length - 1];
		return ret;
	}

	public String getDiscontPhrase(String s, PhraseList list) {
		String[] tokens = s.split("\\s+");

		ArrayList<String> words = new ArrayList<String>();
		for (String ss : tokens) {
			words.add(ss);
		}
		for (int i = 0; i < words.size(); i++) {
			if (list.firstWord.contains(words.get(i))) {
				int j = i + 1;
				while (j < i + 5 && j < words.size()) {
					String phrase = words.get(i) + " " + words.get(j);
					if (list.discPhrase.contains(phrase)) {
						// System.out.println("disc phrase found!!!!! " +
						// phrase);
						words.set(i, words.get(i) + "_" + words.get(j));
						words.remove(j);
						break;
					}
					j++;
				}
			}
		}
		String ret = "";
		for (int i = 0; i < words.size(); i++) {
			if (i == words.size() - 1)
				ret += words.get(i);
			else
				ret += words.get(i) + " ";
		}
		return ret;
	}

	public TextAnnotation runNER(String s) {
		TextAnnotationBuilder tab;

		boolean splitOnHyphens = false;
		tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens, false));

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
