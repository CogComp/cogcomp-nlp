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

public class PeopleManager {

	Set<String> setValidPeopleName = new HashSet<String>();

	/**
	 * Constructor to initialize a PeopleManager object. Used to identify
	 * strings that could be of type PER.
	 * 
	 * @param peopleFileName
	 *            File name to obtain list of people strings
	 * @throws IOException
	 */
	public PeopleManager(String peopleFileName) throws IOException {
		BufferedReader reader = MappingReader.getReader(peopleFileName);
		String line = null;

		while (null != (line = reader.readLine())) {
			line = line.toLowerCase();
			String tokens[] = line.split("\\s+");
			for (String token : tokens) {
				setValidPeopleName.add(token);
			}
		}
		reader.close();
	}

	/**
	 * Returns true if the string parameter is a valid name and should be
	 * designated type PER.
	 * 
	 * @param token
	 *            String to be checked for type PER
	 * @return A boolean value (true/false) determining whether the string
	 *         should be designated type PER
	 */
	public boolean isValidPeopleName(String token) {
		return setValidPeopleName.contains(token);
	}
}
