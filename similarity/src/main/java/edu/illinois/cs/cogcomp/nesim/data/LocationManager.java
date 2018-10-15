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
import java.util.HashSet;
import java.util.Set;

import edu.illinois.cs.cogcomp.nesim.io.MappingReader;

public class LocationManager {

	Set<String> setValidLocation = new HashSet<String>();
	Set<String> setValidLocation2 = new HashSet<String>();

	/**
	 * Constructor to initialize a LocationManager object, which is used to
	 * determine if a string or a pair of strings should be assigned type LOC.
	 * 
	 * @param fileName
	 *            File name to obtain list of location strings
	 * @throws IOException
	 */
	public LocationManager(String fileName) throws IOException {
		BufferedReader reader = MappingReader.getReader(fileName);
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.toLowerCase();
			String tokens[] = line.split("\\s+");
			for (String token : tokens) {
				setValidLocation.add(token);
			}
			setValidLocation2.add(line);
		}
		reader.close();
	}

	/**
	 * Returns true if the string parameter is a location and will be assigned
	 * type LOC in EntityComparison.
	 * 
	 * @param token
	 *            One of the entire strings to be compared
	 * @return A boolean value (true/false) determining whether the string
	 *         should be scored as type LOC
	 */
	public boolean isValidLocation(String token) {
		HashSet<String> keyWords = new HashSet<String>();
		keyWords.add("district");
		keyWords.add("states");
		keyWords.add("state");
		keyWords.add("province");
		keyWords.add("isle");
		keyWords.add("republic");
		int keyWordIndex = -1;
		String[] tokens = token.split("\\s+");
		for (int i = 0; i < tokens.length; i++) {
			if (keyWords.contains(tokens[i])) {
				keyWordIndex = i;
				break;
			}
		}
		if (keyWordIndex != -1) {
			return true;
		} else {
			for (String word : tokens) {
				if (setValidLocation.contains(word) || setValidLocation2.contains(word))
					return true;

			}
			return false;
		}
	}
}
