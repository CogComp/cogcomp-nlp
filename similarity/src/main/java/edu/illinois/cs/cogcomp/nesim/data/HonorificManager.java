/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.nesim.io.MappingReader;

public class HonorificManager {

	public static final String GENDER_EITHER = "either";
	public static final String GENDER_MALE = "male";
	public static final String GENDER_FEMALE = "female";

	private String honorificFileName;
	private Map<String, String> honorificList;

	/**
	 * Constructor to initialize a HonorificManager object, which is used to
	 * determine if a string or a pair of strings should be assigned type PER.
	 * 
	 * @param honorificFileName
	 *            File name to obtain list of honorific strings
	 * @throws IOException
	 */
	public HonorificManager(String honorificFileName) throws IOException {
		this.honorificFileName = honorificFileName;
		honorificList = new HashMap<String, String>();
		readHonorificFile();
	}

	/**
	 * Reads the file and stores the list of known honorific strings in a
	 * HashMap.
	 * 
	 * @throws IOException
	 */
	public void readHonorificFile() throws IOException {
		this.honorificList = MappingReader.readMapping(this.honorificFileName);
	}

	/**
	 * Returns true if the string parameter is a honorific.
	 * 
	 * @param str
	 *            String to be tested whether it is an honorific or not
	 * @return A boolean value (true/false) determining whether the string is an
	 *         honorific
	 */
	public boolean isHonorific(String str) {
		return honorificList.containsKey(str);
	}

	/**
	 * 
	 * 
	 * @param hono1
	 *            First honorific string
	 * @param hono2
	 *            Second honorific string
	 * @return A boolean value (true/false) determining whether the two
	 *         honorifics are the same with regards to gender and title
	 */
	public boolean isMatching(String hono1, String hono2) {
		String gender1 = honorificList.get(hono1);
		String gender2 = honorificList.get(hono2);
		if (gender1.equals(GENDER_EITHER) || gender2.equals(GENDER_EITHER))
			return true;
		else {
			if (!gender1.equals(gender2))
				return false;
			else
				return true;
		}
	}
}
