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

public class OrganizationManager {

	Set<String> setValidOrg = new HashSet<String>();
	Set<String> setValidOrg2 = new HashSet<String>();
	LocationManager locMan;

	/**
	 * Constructor to initialize an OrganizationManager object. Used to identify
	 * strings that could be of type ORG and subsequently score them.
	 * 
	 * @param fileName
	 *            File name to obtain list of organization strings
	 * @throws IOException
	 */
	public OrganizationManager(String fileName) throws IOException {
		BufferedReader reader = MappingReader.getReader(fileName);
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.toLowerCase();
			String[] tokens_tab = line.split("\t");
			String[] tokens = tokens_tab[1].split("\\s+");
			for (String token : tokens) {
				setValidOrg.add(token);
			}
			setValidOrg2.add(tokens_tab[1]);
		}
		reader.close();

		locMan = new LocationManager("locations.txt");
	}

	/**
	 * Returns true if the string parameter should be of type ORG.
	 * 
	 * @param token
	 *            One of the two strings to be checked for type ORG
	 * @return A boolean value (true/false) determining whether the string
	 *         should be designated type ORG
	 */
	public boolean isValidOrg(String token) {
		token = token.toLowerCase();
		if (setValidOrg2.contains(token))
			return true;

		HashSet<String> stopWords = new HashSet<String>();
		stopWords.add("of");
		stopWords.add("at");
		stopWords.add("the");
		String[] tokens = token.split("\\s+");
		int stopWordIndex = -1;
		boolean loc = false;
		for (int i = 0; i < tokens.length; i++) {
			if (stopWords.contains(tokens[i])) {
				stopWordIndex = i;
				break;
			}
		}
		if (stopWordIndex != -1) {
			for (int j = stopWordIndex + 1; j < tokens.length; j++) {
				if (locMan.isValidLocation(tokens[j]))
					loc = true;
			}
		}
		if (setValidOrg.contains(token))
			return true;
		else
			return loc;
	}
}
