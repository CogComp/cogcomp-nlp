package edu.illinois.cs.cogcomp.entitySimilarity.compare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import edu.illinois.cs.cogcomp.nesim.utils.*;

/**
 * @author hhuang74
 */

public class LevensteinTester {
	
//	private static long time = 0;
	private static DameraoLevenstein leven = new DameraoLevenstein();
	
	public static void main (String[] args) {
		String queryFile = "test/NESimData.txt";
		String outputFile = "test/NESimData_levensteinOutput.txt";
		doComparison(queryFile, outputFile);
//		for (int i = 0; i < 100; i++) {
//			doComparison(queryFile, outputFile);
//		}
//		System.out.println(time/100.0);
	}
	
	
	private static void doComparison(String queryFile, String outputFile) {
		long startTime = System.currentTimeMillis();
		try {
			InputStream in = new FileInputStream(queryFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			PrintWriter outputWriter = new PrintWriter(outputFile);
			String strLine;
			int totalRight = 0, total = 0; // gives total cases correctly identified
			int positiveCases = 0, negativeCases = 0; // count total number of positive and negative tests
			int positiveRight = 0, negativeRight = 0; // cases correctly identified of each (+/-)
			while ((strLine = reader.readLine()) != null) {
				total++;
				String [] itemList = strLine.split("\\t+");
				String [] tokens = itemList[2].split("----", 2);
				int similar = Integer.parseInt(itemList[0]);
				if (similar == 1)
					positiveCases++;
				else
					negativeCases++;
				String first = tokens[0], second = tokens[1];
				String concat = first + second;
				float f1 = 1.0f - leven.score(first, second) / concat.length();
				String str1;
				str1 = String.format("%.4f", f1);
				outputWriter.println(str1);
				if (f1 >= 0.5f && similar == 1){ // total positive right
					totalRight++;
					positiveRight++;
				} else if (f1 < 0.5f && similar == 0) { // total negative right
					totalRight++;
					negativeRight++;
				}
			}
			System.out.println("Total: " + totalRight + "/" + total + " passed");
			System.out.println("Positive: " + positiveRight + "/" + positiveCases + " passed");
			System.out.println("Negative: " + negativeRight + "/" + negativeCases + " passed");

			reader.close();
			outputWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("done");
		System.out.println(System.currentTimeMillis() - startTime);
//		time += System.currentTimeMillis() - startTime;
	}
}
