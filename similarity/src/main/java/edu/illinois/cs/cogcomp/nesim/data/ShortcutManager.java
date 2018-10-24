/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.data;

import java.io.IOException;
import java.util.*;

import com.wcohen.secondstring.JaroWinkler;
import com.wcohen.secondstring.SoftTFIDF;
import com.wcohen.secondstring.tokens.SimpleTokenizer;

import edu.illinois.cs.cogcomp.nesim.io.MappingReader;
import edu.illinois.cs.cogcomp.nesim.utils.DameraoLevenstein;
import edu.illinois.cs.cogcomp.nesim.utils.LSHUtils;

public class ShortcutManager {

	Map<String, List<String>> shortcutList;
	Map<String, List<String>> longformList;
	String shortcutFileName;
	JaroWinkler jrwk = new JaroWinkler();
	SimpleTokenizer tokenizer = new SimpleTokenizer(true, true);
	SoftTFIDF soft = new SoftTFIDF(tokenizer, jrwk, 0.92);
	DameraoLevenstein ed = new DameraoLevenstein();
	float score = 0.0f;

	/**
	 * Constructor to initialize a ShortcutManager object. Used to score strings
	 * of type LOC that may be shortcuts.
	 * 
	 * @param shortcutFileName
	 *            File name to obtain list of shortcut strings
	 * @throws IOException
	 */
	public ShortcutManager(String shortcutFileName) throws IOException {
		this.shortcutFileName = shortcutFileName;
		readShortcutFile();
	}

	/**
	 * Reads the given file and stores the shortcut and long form names in a
	 * map.
	 * 
	 * @return A Map containing all possible shortcuts for a particular long
	 *         form string
	 */
	private Map<String, List<String>> constructLongFormList() {
		Map<String, List<String>> mapping = new HashMap<String, List<String>>();
		String shortform, longform;
		List<String> storedShorts;
		for (Map.Entry<String, List<String>> entry : shortcutList.entrySet()) {
			shortform = entry.getKey();
			Iterator<String> listIter = entry.getValue().iterator();
			while (listIter.hasNext()) {
				longform = listIter.next();
				if (mapping.containsKey(longform)) {
					storedShorts = mapping.get(longform);
				} else {
					storedShorts = new ArrayList<String>();
				}
				storedShorts.add(shortform);
				mapping.put(longform, storedShorts);
			}
		}
		return mapping;
	}

	/**
	 * Reads the given file.
	 * 
	 * @throws IOException
	 */
	public void readShortcutFile() throws IOException {
		this.shortcutList = MappingReader.readListMapping(this.shortcutFileName, false);
		this.longformList = constructLongFormList();
	}

	/**
	 * Primary scoring function invoked by EntityComparison. Used to score
	 * strings of type LOC where one string may be a shortcut of the other
	 * string.
	 * 
	 * @param name1
	 *            First of the two strings to be scored
	 * @param name2
	 *            Second of the two strings to be scored
	 * @return A float value between 0 and 1 inclusive as the score
	 */
	public float scoring(String name1, String name2) {
		if (ed.score(name1, name2) <= 1.0f)
			return 1.0f;
		String longName = "";
		String shortName = "";
		if (name1.length() >= name2.length()) {
			longName = name1;
			shortName = name2;
		} else {
			longName = name2;
			shortName = name1;
		}
		shortName = shortName.replace(".", "");
		shortName = shortName.toUpperCase();
		longName = longName.toUpperCase();
		if (isShortcutPair(shortName, longName))
			score = 1.0f;
		else
			try {
				score = (float) soft.score(shortName, longName);
				boolean shouldIgnore = false;
				HashSet<String> keyWords = new HashSet<String>();
				keyWords.add("of");
				keyWords.add("in");
				keyWords.add("the");
				keyWords.add("at");
				String[] nameArr1 = name1.split("\\s+");
				String[] nameArr2 = name2.split("\\s+");
				if (nameArr1.length == nameArr2.length && nameArr1.length == 1) {
					score = oneWordScoring(name1, name2);
				}
				StringBuffer sb1 = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				int index = -1;
				String reducedName1;
				if (nameArr1.length > 1) {
					for (int i = 0; i < nameArr1.length; i++) {
						if (keyWords.contains(nameArr1[i])) {
							index = i;
						}
					}
					if (index != -1 && index != nameArr1.length - 1) {
						shouldIgnore = true;
						for (int i = index + 1; i < nameArr1.length; i++)
							sb1.append(nameArr1[i] + " ");
					}
					index = -1;
					reducedName1 = sb1.toString().trim();
				} else {
					reducedName1 = name1;
				}
				String reducedName2;
				if (nameArr2.length > 1) {
					for (int i = 0; i < nameArr2.length; i++) {
						if (keyWords.contains(nameArr2[i])) {
							index = i;
						}
					}
					if (index != -1 && index != nameArr2.length - 1) {
						shouldIgnore = true;
						for (int i = index + 1; i < nameArr2.length; i++)
							sb2.append(nameArr2[i] + " ");
					}
					reducedName2 = sb2.toString().trim();
				} else {
					reducedName2 = name2;
				}

				if (shouldIgnore) {
					float newScore = (float) LSHUtils.dice(reducedName1, reducedName2);
					float diffScore = ed.score(reducedName1, reducedName2);
					if (newScore < 0.85f) // adjust threshold as necessary for
											// optimal results
						score = 0.0f;
					else
						score = 1.0f;
					if (diffScore <= 1.0f) // allow for up to 1 misspelled
											// letter
						score = 1.0f;
					return score;
				}
				HashMap<String, String> reduced = removeSameWords(name1, name2);
				String shortName1 = reduced.get("FIRST"), shortName2 = reduced.get("SECOND");
				if (!shortName1.equalsIgnoreCase(name1) || !shortName2.equalsIgnoreCase(name2)) {
					HashSet<String> directions = new HashSet<String>();
					directions.add("north");
					directions.add("south");
					directions.add("east");
					directions.add("west");
					if (directions.contains(shortName1) && directions.contains(shortName2)) {
						if (shortName1.equalsIgnoreCase(shortName2))
							score = 1.0f;
						else
							score = 0.0f;
					}
					return score;
				}
			} catch (Exception e) {
				score = (float) jrwk.score(shortName, longName);
			}
		return score;
	}

	private float oneWordScoring(String name1, String name2) {
		if (name1.equalsIgnoreCase(name2))
			score = 1.0f;
		float typoScore = ed.score(name1, name2);
		if (name1.length() == name2.length() && typoScore <= 2.0f)
			score = 1.0f;
		if (name1.length() != name2.length() && typoScore >= 2.0f)
			score = 0.0f;
		return score;
	}

	private HashMap<String, String> removeSameWords(String name1, String name2) {
		HashMap<String, String> tmp = new HashMap<String, String>();
		ArrayList<String> tmp1 = new ArrayList<String>();
		ArrayList<String> tmp2 = new ArrayList<String>();
		ArrayList<String> sameWords = new ArrayList<String>();
		String[] tokens1 = name1.split("\\s+");
		String[] tokens2 = name2.split("\\s+");
		for (String a : tokens1)
			tmp1.add(a);
		for (String b : tokens2)
			tmp2.add(b);
		for (int i = 0; i < tmp1.size(); i++) {
			String a = tmp1.get(i);
			for (int j = 0; j < tmp2.size(); j++) {
				String b = tmp2.get(j);
				if (a.equalsIgnoreCase(b)) {
					tmp1.remove(i);
					tmp2.remove(j);
					if (!sameWords.contains(a))
						sameWords.add(a);
					i--;
					break;
				}
			}
		}
		for (int i = 0; i < tmp1.size(); i++) {
			if (sameWords.contains(tmp1.get(i))) {
				tmp1.remove(i);
				i--;
			}
		}
		for (int i = 0; i < tmp2.size(); i++) {
			if (sameWords.contains(tmp2.get(i))) {
				tmp2.remove(i);
				i--;
			}
		}
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0; i < tmp1.size(); i++) {
			sb1.append(tmp1.get(i) + " ");
		}
		for (int i = 0; i < tmp2.size(); i++) {
			sb2.append(tmp2.get(i) + " ");
		}
		name1 = (sb1.toString()).trim();
		name2 = (sb2.toString()).trim();
		tmp.put("FIRST", name1);
		tmp.put("SECOND", name2);
		// String [] shortened = {name1, name2};
		return tmp;
	}

	/**
	 * Returns true if the shorter string is a valid shortcut for the longer
	 * string.
	 * 
	 * @param shortName
	 *            The shorter of the two strings being compared
	 * @param longName
	 *            The longer of the two strings being compared
	 * @return A boolean value (true/false) determining whether the shorter
	 *         string is a shortcut of the longer string
	 */
	private boolean isShortcutPair(String shortName, String longName) {
		List<String> shortcuts = shortcutList.get(longName);
		if (shortcuts == null)
			return false;
		int check = 0;
		int n = shortcuts.size();
		for (int i = 0; i < n; i++) {
			String shortcut = shortcuts.get(i);
			if (shortcut.equals(shortName)) {
				check = 1;
				break;
			}
		}
		if (check == 0)
			return false;
		return true;
	}

	/**
	 * Finds all possible long forms for a given shortcut.
	 * 
	 * @param shortcut
	 *            The string that may be a shortcut
	 * @return A list containing the possible long forms for the given shortcut
	 */
	public List<String> getLongForm(String shortcut) {
		if (longformList.containsKey(shortcut))
			return longformList.get(shortcut);
		return new ArrayList<String>();
	}

	/**
	 * Finds all possible shortcuts for a given long form.
	 * 
	 * @param longform
	 *            The string that may be a long form
	 * @return A list containing the possible shortcuts for the given long form
	 */
	public List<String> getShortForm(String longform) {
		if (shortcutList.containsKey(longform))
			return shortcutList.get(longform);
		return new ArrayList<String>();
	}
}
