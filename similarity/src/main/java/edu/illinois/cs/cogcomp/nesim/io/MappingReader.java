/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.io;

import edu.illinois.cs.cogcomp.core.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MappingReader {

	public static Map<String, String> readMapping(String fileName) throws IOException {
		BufferedReader reader = getReader(fileName);
		Map<String, String> mapping = new HashMap<String, String>();
		String line;
		while ((line = reader.readLine()) != null) {

			line = line.trim();
			if (line.length() == 0)
				continue;
			String[] parts = line.split("\t+");
			if (parts.length != 2)
				continue;
			String honorific = parts[0];
			honorific = honorific.toLowerCase();
			String gender = parts[1];
			gender = gender.toLowerCase();
			if (!mapping.containsKey(honorific))
				mapping.put(honorific, gender);
		}

		reader.close();

		return mapping;
	}

	public static BufferedReader getReader(String fileName) throws IOException {
		// InputStream inStrm = ClassLoader.getSystemResourceAsStream( fileName
		// );
		InputStream inStrm;
		try {
			inStrm = IOUtils.lsResources(MappingReader.class, fileName).get(0).openStream();
		} catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}

		// InputStream is = this.getClass().getResourceAsStream( name_ );

		if (inStrm == null) {
			// try with a leading slash
			inStrm = ClassLoader.getSystemResourceAsStream("/" + fileName);
			if (inStrm == null) // try as absolute or correct local path
				inStrm = new FileInputStream(fileName);

		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(inStrm, "UTF-8"));

		return reader;
	}

	public static Map<String, List<String>> readListMapping(String fileName) throws IOException {
		return readListMapping(fileName, true);
	}

	public static Map<String, List<String>> readListMapping(String fileName, boolean makeLowerCase) throws IOException {
		Map<String, List<String>> mapping = new HashMap<String, List<String>>();

		// InputStream stopwordStrm = null;
		//
		// try {
		// stopwordStrm = AcronymManager.class.getResourceAsStream( fileName );
		//
		// if (stopwordStrm == null)
		// {
		// IllegalArgumentException e =
		// new IllegalArgumentException( "ERROR: couldn't find resource " +
		// fileName );
		// throw e;
		// }
		//
		BufferedReader reader = getReader(fileName);

		String line;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			String[] parts = line.split("\t+");
			if (parts.length < 2)
				continue;
			String acronym = parts[0];
			if (makeLowerCase) // VG
				acronym = acronym.toLowerCase();
			String fullName = parts[1];
			String[] fullNames = fullName.split("#+");
			List<String> names = new LinkedList<String>();
			int n = fullNames.length;
			for (int i = 0; i < n; i++) {
				String lowerCaseFullName = fullNames[i];
				if (makeLowerCase) // VG
					lowerCaseFullName = lowerCaseFullName.toLowerCase();
				lowerCaseFullName = lowerCaseFullName.replaceAll("\\s\\s+", " ");
				names.add(lowerCaseFullName);
			}
			addListMappingEntry(mapping, acronym, names);
		}

		return mapping;
	}

	private static void addListMappingEntry(Map<String, List<String>> mapping, String acronym, List<String> names) {
		if (mapping.containsKey(acronym)) {
			List<String> storedNames = mapping.get(acronym);
			int m = names.size();
			for (int i = 0; i < m; i++) {
				storedNames.add(names.get(i));
			}
			mapping.put(acronym, storedNames);
		} else {
			mapping.put(acronym, names);
		}
	}
}
