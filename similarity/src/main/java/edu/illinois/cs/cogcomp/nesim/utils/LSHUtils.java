/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.utils;

import java.io.FileWriter;
import java.util.*;

public class LSHUtils {

	public static List<char[]> bigrams(String input) {
		ArrayList<char[]> bigram = new ArrayList<char[]>();
		for (int i = 0; i < input.length() - 1; i++) {
			char[] chars = new char[2];
			chars[0] = input.charAt(i);
			chars[1] = input.charAt(i + 1);
			bigram.add(chars);
		}
		return bigram;
	}

	/**
	 * CURRENTLY A CASE SENSITIVE BIGRAM FIND Todo : Ignore Case when getting
	 * bigrams ?????
	 */
	public static List<String> bigramsAsstrings(String input) {
		ArrayList<String> bigram = new ArrayList<String>();
		for (int i = 0; i < input.length() - 1; i++) {
			char[] chars = new char[2];
			chars[0] = input.charAt(i);
			chars[1] = input.charAt(i + 1);
			bigram.add(new String(chars));
		}
		return bigram;
	}

	public static double dice(String s1, String s2) { // List<char[]> bigram1,
														// List<char[]> bigram2)
														// {
		List<char[]> bigram1 = bigrams(s1);
		List<char[]> bigram2 = bigrams(s2);

		List<char[]> copy = new ArrayList<char[]>(bigram2);
		int matches = 0;
		for (int i = bigram1.size(); --i >= 0;) {
			char[] bigram = bigram1.get(i);
			for (int j = copy.size(); --j >= 0;) {
				char[] toMatch = copy.get(j);
				if (bigram[0] == toMatch[0] && bigram[1] == toMatch[1]) {
					copy.remove(j);
					matches += 2;
					break;
				}
			}
		}
		return (double) matches / (bigram1.size() + bigram2.size());
	}

	public static double dice(List<char[]> bigram1, List<char[]> bigram2) {
		List<char[]> copy = new ArrayList<char[]>(bigram2);
		int matches = 0;
		for (int i = bigram1.size(); --i >= 0;) {
			char[] bigram = bigram1.get(i);
			for (int j = copy.size(); --j >= 0;) {
				char[] toMatch = copy.get(j);
				if (bigram[0] == toMatch[0] && bigram[1] == toMatch[1]) {
					copy.remove(j);
					matches += 2;
					break;
				}
			}
		}
		return (double) matches / (bigram1.size() + bigram2.size());
	}

	public static HashSet<String> BigramCounter(List<String[]> strings) {
		HashSet<String> bigrams = new HashSet<String>();
		for (String[] s : strings) {
			String st = s[0].trim();
			bigrams.addAll(bigramsAsstrings(st));
		}
		return bigrams;
	}

	public static HashMap<String, Integer> BigramToIntMap(List<String[]> strings) {
		int intmapkey = 0;
		HashMap<String, Integer> bigram_int_map = new HashMap<String, Integer>();
		for (String[] s : strings) {
			String st = s[0].trim();
			List<String> bigrams = LSHUtils.bigramsAsstrings(st);
			for (String bigram : bigrams) {
				if (!bigram_int_map.containsKey(bigram)) {
					bigram_int_map.put(bigram, intmapkey++);
				}
			}
		}
		return bigram_int_map;
	}

	public static void print_bigrams(HashSet<String> bigrams) {
		for (String bi : bigrams)
			System.out.println(bi);
	}

	public static StringBuilder writeBigramBinaryVectors(List<String[]> head_docids) {
		HashMap<String, Integer> bigram_int_map = LSHUtils.BigramToIntMap(head_docids);
		int num_bigrams = bigram_int_map.keySet().size();

		StringBuilder output = new StringBuilder();

		for (String[] head_doc : head_docids) {
			String head = head_doc[0].trim();
			String docid = head_doc[1].trim();

			List<String> head_bigrams = LSHUtils.bigramsAsstrings(head);

			// Binary Array with size equal to the number of bigrams in corpus.
			// Stores the number of
			// existing background in the mention.
			String[] bigram_counts = new String[num_bigrams];
			Arrays.fill(bigram_counts, "0");

			for (String s : head_bigrams) {
				if (!bigram_int_map.containsKey(s)) {
					System.err.println("Error!!!! : " + s);
					System.exit(0);
				}
				int intkey = bigram_int_map.get(s);
				bigram_counts[intkey] = Integer.toString(Integer.parseInt(bigram_counts[intkey]) + 1);
			}

			head = head.replaceAll(" ", "_");
			output.append(head + "_" + docid + " ");
			output.append(String.join(" ", bigram_counts));
			output.append("\n");
		}
		return output;
	}

	public static void writeBigramBinaryVectors(List<String[]> head_docids, String filepath) throws Exception {
		FileWriter fw = new FileWriter(filepath);
		fw.write(writeBigramBinaryVectors(head_docids).toString());
		fw.close();
	}

	public static void main(String[] args) { // used for testing
		System.out.println(LSHUtils.dice("canadian", "canada"));
	}
}
