/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.nesim.io.MappingReader;

public class CountryLanguageManager {
	// Variables
	private Map<Integer, List<String>> mapCountryLang;
	private HashMap<String, List<Integer>> mapInvertCountryLang;
	private String countryLangFileName;

	/**
	 * Constructor to initialize a CountryLanguageManager object, which is used
	 * to determine if a string or a pair of strings should be assigned type
	 * LOC.
	 * 
	 * @param countryLangFileName
	 *            File name to obtain list of country-language strings
	 * @throws IOException
	 */
	public CountryLanguageManager(String countryLangFileName) throws IOException {
		this.countryLangFileName = countryLangFileName;
		mapCountryLang = new HashMap<Integer, List<String>>();
		mapInvertCountryLang = new HashMap<String, List<Integer>>();
		readCountryLang();
	}

	/**
	 * Reads the provided file and stores the list of known country-language
	 * strings in a HashMap.
	 * 
	 * @throws IOException
	 */
	private void readCountryLang() throws IOException {
		BufferedReader reader = MappingReader.getReader(countryLangFileName);
		int index = 0;
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			line = line.toLowerCase();
			String[] tokens = line.split("\\t+");
			int n = tokens.length;
			if (n < 2)
				continue;
			List<String> arrTokens = new ArrayList<String>();
			for (int i = 0; i < n; i++) {
				String token = tokens[i];
				if (mapInvertCountryLang.containsKey(token)) {
					List<Integer> arrIndex = mapInvertCountryLang.get(token);
					arrIndex.add(new Integer(index));
					mapInvertCountryLang.put(token, arrIndex);
				} else {
					ArrayList<Integer> arrIndex = new ArrayList<Integer>();
					arrIndex.add(index);
					mapInvertCountryLang.put(token, arrIndex);
				}
				arrTokens.add(tokens[i]);
			}
			mapCountryLang.put(new Integer(index), arrTokens);
			index++;
		}
	}

	/**
	 * Primary scoring function called by EntityComparison. It scores pairs of
	 * strings of type LOC.
	 * 
	 * @param str1
	 *            First of two strings being compared
	 * @param str2
	 *            Second of two strings being compared
	 * @return Float value between 0 and 1 inclusive as the score for comparing
	 *         two strings designated type LOC
	 */
	public float scoring(String str1, String str2) {
		float score = 0.0f;
		if (isCountryLang(str1, str2))
			score = 1.0f;
		return score;
	}

	/**
	 * Returns true if the two strings compared are a country-language pair.
	 * Only used on strings designated type LOC.
	 * 
	 * @param str1
	 *            First of two strings being compared
	 * @param str2
	 *            Second of two strings being compared
	 * @return A boolean value (true/false) determining whether the pair of
	 *         strings being compared is a country-language pair
	 */
	public boolean isCountryLang(String str1, String str2) {
		str1 = str1.toLowerCase();
		str1 = str1.replaceAll("[^A-Za-z]", "");
		str2 = str2.toLowerCase();
		str2 = str2.replaceAll("[^A-Za-z]", "");
		List<Integer> arrIndexStr1 = mapInvertCountryLang.get(str1);
		List<Integer> arrIndexStr2 = mapInvertCountryLang.get(str2);
		if (arrIndexStr1 == null || arrIndexStr2 == null) {
			return false;
		}
		int n = arrIndexStr1.size();
		int m = arrIndexStr2.size();
		for (int i = 0; i < n; i++) {
			int index1 = arrIndexStr1.get(i).intValue();
			for (int j = 0; j < m; j++) {
				int index2 = arrIndexStr2.get(j).intValue();
				if (index1 == index2)
					return true;
			}
		}
		return false;
	}
}
