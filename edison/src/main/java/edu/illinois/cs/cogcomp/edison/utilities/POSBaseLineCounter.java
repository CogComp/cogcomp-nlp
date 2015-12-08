package edu.illinois.cs.cogcomp.edison.utilities;

import edu.illinois.cs.cogcomp.nlp.corpusreaders.PennTreebankPOSReader;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * A baseline counter simply builds a table by counting the number of times that each word
 * appears in training data associated with each possible part of speech tag.
 * Given a word, if it is observed, the counter will return the most frequent tag associated
 * with the word. 
 * 
 * @author Xinbo Wu
**/
public class POSBaseLineCounter {
	/**
	 * This map associates words with maps that associate POS tags with counts.
	 **/
	protected HashMap<String, TreeMap<String, Integer>> table;
	/**
	 * The name of corpus used for training
	 **/
	protected final String corpusName;

	public POSBaseLineCounter(String corpusName) {
		table = new HashMap<String, TreeMap<String, Integer>>();
		this.corpusName = corpusName;
	}

	/**
	 * A table is built from a given training corpus by simply counting the
	 * number of times that each word appears in training data associated with
	 * each possible part of speech tag.
	 * 
	 * @param fileName
	 *            fileName of the training corpus
	 * @throws Exception
	 **/
	public void buildTable(String fileName) throws Exception {
		PennTreebankPOSReader reader = new PennTreebankPOSReader(this.corpusName);
		reader.readFile(fileName);
		List<TextAnnotation> tas = reader.getTextAnnotations();
		for (TextAnnotation ta : tas) {
			for (int tokenId = 0; tokenId < ta.size(); tokenId++) {
				count(ta.getToken(tokenId), ((SpanLabelView) ta.getView(ViewNames.POS)).getLabel(tokenId));
			}
		}
	}

	public void count(String form, String tag) {
		TreeMap<String, Integer> counts = table.get(form);

		if (counts == null) {
			counts = new TreeMap<String, Integer>();
			table.put(form, counts);
		}

		String l = tag;
		Integer count = counts.get(l);
		if (count == null)
			count = 0;
		counts.put(l, count + 1);
	}

	/** Clears out the table to start fresh. */
	public void forget() {
		table.clear();
	}

	public String tag(int tokenId, TextAnnotation ta) {
		String form = ta.getToken(tokenId);
		TreeMap<String, Integer> counts = table.get(form);
		String l = null;

		if (counts == null) {
			if (form.equals(";"))
				l = ":";
			else if (looksLikeNumber(form))
				l = "CD";
			else
				l = "UNKNOWN";
		} else {
			int best = 0;

			for (Map.Entry<String, Integer> e : counts.entrySet()) {
				int c = e.getValue();
				if (c > best) {
					best = c;
					l = e.getKey();
				}
			}
		}

		return l;
	}

	/**
	 * Determines if the input word looks like a number of some sort.
	 *
	 * @param form
	 *            The form of the word.
	 * @return <code>true</code> iff the word contains only characters in ".,-"
	 *         and at least one digit.
	 **/
	public boolean looksLikeNumber(String form) {
		boolean containsDigit = false;

		for (int i = 0; i < form.length(); ++i) {
			if (Character.isDigit(form.charAt(i)))
				containsDigit = true;
			else if (".,-".indexOf(form.charAt(i)) == -1)
				return false;
		}

		return containsDigit;
	}

	/**
	 * Indicates whether the input word was observed while training this
	 * learner.
	 *
	 * @param form
	 *            The form of the word.
	 * @return <code>true</code> if this learner contains statistics for the
	 *         input word.
	 **/
	public boolean observed(String form) {
		return table.containsKey(form);
	}

	/** Returns the number of times the given form has been observed. */
	public int observedCount(String form) {
		if (!table.containsKey(form))
			return 0;
		int result = 0;
		for (Integer count : table.get(form).values())
			result += count;
		return result;
	}

	/**
	 * Returns the set of tags that the given word has been observed with.
	 *
	 * @param form
	 *            The form of the word.
	 * @return The set of tags observed in association with the given word.
	 **/
	public Set<String> allowableTags(String form) {
		if (!table.containsKey(form)) {
			HashSet<String> result = new HashSet<String>();
			if (form.equals(";"))
				result.add(":");
			else if (looksLikeNumber(form))
				result.add("CD");
			return result;
		}

		return table.get(form).keySet();
	}

	/** Return the name of the training corpus. */
	public String getCorpusName() {
		return this.corpusName;
	}
	
	public static String write(POSBaseLineCounter counter){
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(counter);
	}
	
	public static POSBaseLineCounter read(String json){
		return new Gson().fromJson(json, POSBaseLineCounter.class);
	}
}
