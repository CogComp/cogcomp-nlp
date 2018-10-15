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

import edu.illinois.cs.cogcomp.nesim.io.MappingReader;
import edu.illinois.cs.cogcomp.nesim.utils.DameraoLevenstein;

public class AcronymManager {
	// Variables
	Map<String, List<String>> acronymList;
	Map<String, List<String>> longformList;
	String acronymFileName;
	JaroWinkler jrwk;
	DameraoLevenstein ed = new DameraoLevenstein();
	private boolean specificCase = false;

	/**
	 * Constructor to initialize an AcronymManager object, which is used to
	 * determine if a string or a pair of strings should be assigned type ORG.
	 * 
	 * @param acronymFileName
	 *            File name to obtain list of acronyms
	 * @throws IOException
	 */
	public AcronymManager(String acronymFileName) throws IOException {
		this.acronymFileName = acronymFileName;
		jrwk = new JaroWinkler();
		readAcronymFile();
	}

	/**
	 * Helper scoring function used to score strings designated ORG.
	 * 
	 * @param name1
	 *            First of two type ORG strings to be compared
	 * @param name2
	 *            Second of two type ORG strings to be compared
	 * @return Float value between 0 and 1 inclusive as the score
	 */
	public float acronymScoring(String name1, String name2) {
		String longName = "";
		String shortName = "";
		if (name1.length() >= name2.length()) {
			longName = name1;
			shortName = name2;
		} else {
			longName = name2;
			shortName = name1;
		}
		String[] tokensLong = longName.split("\\s+");
		String[] tokensShort = shortName.split("\\s+");
		if (tokensShort.length == 1 && tokensLong.length > 1) {
			for (String s : tokensLong) {
				if (ed.score(s, shortName) <= 1.0f)
					return 1.0f;
			}
		}
		if (!longName.contains(" ") && !shortName.contains(" ")) {
			specificCase = true;
			if (longName.charAt(0) != shortName.charAt(0))
				return 0.0f;
			HashMap<String, String> differences = removeSameChar(longName, shortName);
			String longReduced = differences.get("FIRST"), shortReduced = differences.get("SECOND");
			float letterScore = ed.score(longReduced, shortReduced);
			if (letterScore < 1.0f)
				return 1.0f;
			else
				return 0.0f;
		}
		if (tokensLong.length < 2)
			return 0.0f;
		float score = 0.0f;
		if (isAcronymPair(shortName, longName))
			score = 1.0f;
		else if (isAcronymFirstLetters(shortName, longName))
			score = 1.0f;
		else {
			List<String> fullNames = acronymList.get(shortName);
			if (fullNames != null) {
				int n = fullNames.size();
				JaroWinkler jrwk = new JaroWinkler();
				float maxScore = 0.0f;
				for (int i = 0; i < n; i++) {
					String fullName = fullNames.get(i);
					score = (float) jrwk.score(fullName, longName);
					if (score > maxScore)
						maxScore = score;
				}
				if (maxScore > 0)
					score = maxScore;
			}
		}
		return score;
	}

	public boolean isSpecificCase() {
		return specificCase;
	}

	private HashMap<String, String> removeSameChar(String name1, String name2) {
		HashMap<String, String> hm = new HashMap<String, String>();
		StringBuffer sbLong = new StringBuffer();
		StringBuffer sbShort = new StringBuffer();
		String longName = name1;
		String shortName = name2;
		char[] arr1 = longName.toCharArray();
		char[] arr2 = shortName.toCharArray();
		for (int i = 0; i < arr2.length; i++) {
			if (arr2[i] != arr1[i]) {
				sbLong.append(arr1[i]);
				sbShort.append(arr2[i]);
			}
		}
		for (int i = arr2.length; i < arr1.length; i++)
			sbLong.append(arr1[i]);
		hm.put("FIRST", sbLong.toString());
		hm.put("SECOND", sbShort.toString());
		return hm;
	}

	/**
	 * Helper scoring function used to score strings designated ORG.
	 * 
	 * @param candidates1
	 *            ArrayList<String> for first string
	 * @param candidates2
	 *            ArrayList<String> for second string
	 * @return Float value between 0 and 1 inclusive as the score
	 */
	public float acronymScoring(ArrayList<String> candidates1, ArrayList<String> candidates2) {
		int n = candidates1.size();
		int m = candidates2.size();
		float maxScore = 0.0f;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				float score = acronymScoring(candidates1.get(i), candidates2.get(j));
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		return maxScore;
	}

	/**
	 * Primary scoring function called by EntityComparison. It scores pairs of
	 * strings of type ORG.
	 * 
	 * @param name1Info
	 *            First NameInfo object to be compared
	 * @param name2Info
	 *            Second NameInfo object to be compared
	 * @return Float value between 0 and 1 inclusive as the score
	 */
	public float scoring(NameInfo name1Info, NameInfo name2Info) {
		String name1 = name1Info.getOriginalName();
		String name2 = name2Info.getOriginalName();
		float acronymScore = acronymScoring(name1, name2);
		float candidateAcronymScore = acronymScoring(name1Info.getOriginalCandidates(),
				name2Info.getOriginalCandidates());
		float score = 0.0f;
		if (specificCase)
			return acronymScore;
		if (acronymScore > 0.0f || candidateAcronymScore > 0.0f)
			score = (acronymScore > candidateAcronymScore) ? acronymScore : candidateAcronymScore;
		else
			score = (float) jrwk.score(name1, name2);
		return score;
	}

	/**
	 * Finds the possible acronyms of a long string (presumably multiple words)
	 * with and without words like "of, at, the" included in the abbreviated
	 * form.
	 * 
	 * @param longName
	 *            The longer of two strings being compared
	 * @return A string representation of the possible acronym(s) that longName
	 *         could be
	 */
	public String findAcronym(String longName) {
		return findAcronym(longName, true);
	}

	private String findAcronym(String longName, boolean ignoreStopWords) {
		HashSet<String> stopWords = new HashSet<String>();
		stopWords.add("of");
		stopWords.add("at");
		stopWords.add("the");

		String tokens[] = longName.split("\\s+");
		if (tokens.length < 2)
			return "";
		int n = tokens.length;
		String newShortName = "";
		for (int i = 0; i < n; i++) {
			String token = tokens[i];
			if ((token.length() > 0) && !(ignoreStopWords && stopWords.contains(token)))
				newShortName += token.charAt(0);
		}
		if (newShortName.length() != 0)
			return newShortName;
		return "";
	}

	/**
	 * Returns true if the shorter string is an abbreviation, or a variant of
	 * acceptable ones, of the longer string.
	 * 
	 * @param shortName
	 *            Shorter of two strings being compared
	 * @param longName
	 *            Longer of two strings being compared
	 * @return A boolean value (true/false) determining whether shortName is a
	 *         possible acronym of longName
	 */
	public boolean isAcronymFirstLetters(String shortName, String longName) {
		String newShortName = findAcronym(longName, false);
		String newShortNameMod = findAcronym(longName, true);

		if (newShortName.equals("") || newShortNameMod.equals(""))
			return false;
		if (newShortName.length() != 0 && newShortName.toLowerCase().equals(shortName.toLowerCase()))
			return true;
		if (newShortNameMod.length() != 0 && newShortNameMod.toLowerCase().equals(shortName.toLowerCase()))
			return true;
		return false;
	}

	/**
	 * More rigorously defined than isAcronymFirstLetters; returns true if the
	 * shorter string is exactly an acronym of the longer string.
	 * 
	 * @param acronym
	 *            Shorter of the two strings
	 * @param fullName
	 *            Longer of the two strings
	 * @return A boolean value (true/false) determining whether the acronym is
	 *         an exactly acceptable abbreviation for the longer string
	 */
	public boolean isAcronymPair(String acronym, String fullName) {
		List<String> fullNames = acronymList.get(acronym);
		if (fullNames == null)
			return false;
		int check = 0;
		int n = fullNames.size();
		for (int i = 0; i < n; i++) {
			String name = fullNames.get(i);
			if (name.equals(fullName)) {
				check = 1;
				break;
			}
		}
		if (check == 0)
			return false;
		return true;
	}

	// ==============
	private Map<String, List<String>> constructLongFormList() {
		Map<String, List<String>> mapping = new HashMap<String, List<String>>();
		String acronym, longform;
		List<String> storedAcronyms;
		for (Map.Entry<String, List<String>> entry : acronymList.entrySet()) {
			acronym = entry.getKey();
			Iterator<String> listIter = entry.getValue().iterator();
			while (listIter.hasNext()) {
				longform = listIter.next();
				if (mapping.containsKey(longform)) {
					storedAcronyms = mapping.get(longform);
				} else {
					storedAcronyms = new ArrayList<String>();
				}
				storedAcronyms.add(acronym);
				mapping.put(longform, storedAcronyms);
			}
		}
		return mapping;
	}

	// ==============
	public void readAcronymFile() throws IOException {
		this.acronymList = MappingReader.readListMapping(acronymFileName, false);
		this.longformList = constructLongFormList();
	}

	// ==============
	public List<String> getLongForm(String acronym) {
		if (longformList.containsKey(acronym))
			return longformList.get(acronym);
		return new ArrayList<String>();
	}

	// ==============
	public List<String> getShortForm(String longform) {
		if (acronymList.containsKey(longform))
			return acronymList.get(longform);
		return new ArrayList<String>();
	}
}
