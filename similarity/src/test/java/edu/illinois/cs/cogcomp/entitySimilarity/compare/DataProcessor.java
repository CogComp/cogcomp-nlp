/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.entitySimilarity.compare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import edu.illinois.cs.cogcomp.nesim.compare.EntityComparison;

public class DataProcessor {

	private static Set<String> allLines = new HashSet<String>();

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		String inputFile = "test/NESimData_2.txt";
		String outputFile = "test/NESimData.txt";
		try {
			EntityComparison ec = new EntityComparison();
			HashMap<String, String> items = new HashMap<String, String>();
			InputStream in = new FileInputStream(inputFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			PrintWriter out = new PrintWriter(outputFile);
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\\t+");
				int score = Integer.parseInt(tokens[0]);
				String[] names = tokens[2].split("----", 2);
				if (names[0].equalsIgnoreCase(names[1]))
					out.println(1 + "\t" + tokens[1] + "\t" + tokens[2]);
				else
					out.println(line);
			}
			reader.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done\n" + (System.currentTimeMillis() - startTime));
	}

	private static boolean hasSpecialChar(String name) {
		Pattern p = Pattern.compile("[^a-zA-Z ]");
		boolean hasSpecialChar = p.matcher(name).find();
		return hasSpecialChar;
	}
}
