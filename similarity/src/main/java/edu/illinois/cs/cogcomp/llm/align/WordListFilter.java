/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.llm.align;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.mrcs.align.ListFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.io.LineIO;

public class WordListFilter implements ListFilter<String> {
	public static final java.lang.String STOPWORD_FILE = "stopwordFile";

	private static final String NAME = WordListFilter.class.getCanonicalName();
	private Logger logger = LoggerFactory.getLogger(WordListFilter.class);
	private String m_stopwordFile;
	private Set<String> m_stopwords;

	public WordListFilter(ResourceManager rm_) throws IOException {
		m_stopwordFile = rm_.getString(STOPWORD_FILE);
		loadStopwords();
	}

	/**
	 * filter non-content words from an input array of string
	 * 
	 * @param elements_
	 *            a list of input words
	 * @return an array of string containing only non-stopwords
	 */

	@Override
	public String[] filter(String[] elements_) {
		String[] filteredElts = new String[elements_.length];

		for (int i = 0; i < elements_.length; ++i) {
			String lcTok = elements_[i].toLowerCase();

			if (!m_stopwords.contains(lcTok))
				filteredElts[i] = elements_[i];
			else
				filteredElts[i] = null;

			logger.debug(
					(null == filteredElts[i] ? "FILTERED" : "DID NOT FILTER") + " element '" + elements_[i] + "'.");
		}

		return filteredElts;
	}

	protected void loadStopwords() throws IOException {

		ArrayList<String> lines = LineIO.readFromClasspath(m_stopwordFile);
		m_stopwords = new HashSet<String>();

		for (String line : lines) {
			m_stopwords.add(line.toLowerCase());
		}

	}

}
