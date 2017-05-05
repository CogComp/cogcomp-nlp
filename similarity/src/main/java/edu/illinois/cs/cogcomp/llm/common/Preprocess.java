/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.llm.common;

import java.io.IOException;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

public class Preprocess {
	public static void main(String[] args) throws IOException {
		String text1 = "Good afternoon, gentlemen. I am a HAL-9000 " + "computer. I was born in Urbana, Il. in 1992";

		String corpus = "2001_ODYSSEY";
		String textId = "001";

		// Create a TextAnnotation using the LBJ sentence splitter
		// and tokenizers.
		TextAnnotationBuilder tab;
		// don't split on hyphens, as NER models are trained this way
		boolean splitOnHyphens = false;
		tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));

		TextAnnotation ta = tab.createTextAnnotation(corpus, textId, text1);

		NERAnnotator co = new NERAnnotator(ViewNames.NER_CONLL);
		co.doInitialize();

		co.addView(ta);

		System.out.println(ta.getView(ViewNames.NER_CONLL));
	}

	public String getPhrase(String s, PhraseList list) {

		String[] tokens = s.split("\\s+");
		String prev = tokens[0];
		String ret = null;
		for (int i = 1; i < tokens.length; i++) {
			String phrase;
			if (prev != null) {
				phrase = prev + "_" + tokens[i];
			} else
				phrase = tokens[i];
			if (!list.contains(phrase)) {
				ret += prev + " " + tokens[i] + " ";
				prev = null;
			} else
				prev = phrase;

		}
		if (prev != null)
			ret += " " + prev;
		return ret;
	}

	public TextAnnotation runNER(String s) {
		TextAnnotationBuilder tab;
		// don't split on hyphens, as NER models are trained this way
		boolean splitOnHyphens = false;
		tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));

		TextAnnotation ta = tab.createTextAnnotation("001", "001", s);

		NERAnnotator co;
		try {
			co = new NERAnnotator(ViewNames.NER_CONLL);
			co.doInitialize();

			co.addView(ta);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ta;
	}
}
