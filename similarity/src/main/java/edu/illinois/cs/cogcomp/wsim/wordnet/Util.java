/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wsim.wordnet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Util {

	public static ArrayList<String> readLines(String fileName) {
		BufferedReader reader;
		try {
			reader = openReader(fileName);
		} catch (IOException e1) {
			System.out.println("Couldn't read file " + fileName);
			return null;
			// e1.printStackTrace();
		}
		String line;
		ArrayList<String> content = new ArrayList<String>();
		try {
			while ((line = reader.readLine()) != null) {
				content.add(line);
			}

			reader.close();

			return content;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedReader openReader(String fname) throws IOException {
		BufferedReader reader;

		InputStream is = ClassLoader.getSystemResourceAsStream(fname);

		if (is == null) {
			// try with a leading slash
			is = ClassLoader.getSystemResourceAsStream("/" + fname);

			if (is == null)
				is = new FileInputStream(fname);

		}

		reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		return reader;

	}

}
