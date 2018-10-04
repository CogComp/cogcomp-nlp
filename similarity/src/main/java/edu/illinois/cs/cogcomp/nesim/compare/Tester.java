/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.compare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * @author hhuang74
 */

public class Tester {

	// private static long time = 0;

	public static void main(String[] args) {
		String queryFile = "test/NESimData.txt";
		String outputFile = "test/NESimData_output.txt";
		String reasonFile = "test/NESimData_reason.txt";
		doComparison(queryFile, outputFile, reasonFile);
		// for (int i = 0; i < 100; i++) {
		// doComparison(queryFile, outputFile, reasonFile);
		// }
		// System.out.println(time/100.0);
	}

	private static void doComparison(String queryFile, String outputFile, String reasonFile) {
		long startTime = System.currentTimeMillis();
		EntityComparison ec = null;
		try {
			ec = new EntityComparison();
			InputStream in = new FileInputStream(queryFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			PrintWriter outputWriter = new PrintWriter(outputFile);
			PrintWriter reasonWriter = new PrintWriter(reasonFile);
			String strLine;
			int totalRight = 0, total = 0; // gives total cases correctly
											// identified
			int positiveCases = 0, negativeCases = 0; // count total number of
														// positive and negative
														// tests
			int positiveRight = 0, negativeRight = 0; // cases correctly
														// identified of each
														// (+/-)
			while ((strLine = reader.readLine()) != null) {
				total++;
				String[] itemList = strLine.split("\\t+");
				String[] tokens = itemList[2].split("----", 2);
				int similar = Integer.parseInt(itemList[0]);
				if (similar == 1)
					positiveCases++;
				else
					negativeCases++;
				HashMap<String, String> items = new HashMap<String, String>();
				items.put("FIRST_STRING", tokens[0]);
				items.put("SECOND_STRING", tokens[1]);
				HashMap<String, String> result = ec.compare(items);
				float f1 = Float.parseFloat(result.get("SCORE"));
				String str1;
				str1 = String.format("%.4f", f1);
				outputWriter.println(str1);
				reasonWriter.println(result.get("REASON"));
				if (f1 >= 0.5f && similar == 1) { // total right
					totalRight++;
					positiveRight++;
				} else if (f1 < 0.5f && similar == 0) { // total right
					totalRight++;
					negativeRight++;
				}
			}
			System.out.println("Total: " + totalRight + "/" + total + " passed");
			System.out.println("Positive: " + positiveRight + "/" + positiveCases + " passed");
			System.out.println("Negative: " + negativeRight + "/" + negativeCases + " passed");

			reader.close();
			outputWriter.close();
			reasonWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done");
		System.out.println(System.currentTimeMillis() - startTime);
		// time += System.currentTimeMillis() - startTime;
	}
}
