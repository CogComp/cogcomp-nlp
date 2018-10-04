/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.wikifier.utils.spelling.ashwin;

import edu.illinois.cs.cogcomp.wikifier.utils.spelling.AbstractSurfaceQueryEngine;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ashwink
 *
 */

public class AshwinSpellChecker extends AbstractSurfaceQueryEngine {

	private static final Logger logger = LoggerFactory
			.getLogger(AshwinSpellChecker.class);
	private ArrayList<String> titleIndex;
	private ArrayList<String>[] hash;
	private List<String> sort1, sort2;
	private int OFFSET = 3;

	@SuppressWarnings("unchecked")
	public AshwinSpellChecker() {
		titleIndex = new ArrayList<String>();
		hash = (ArrayList<String>[]) new ArrayList[26000];
		for (int i = 0; i < 26000; i++)
			hash[i] = new ArrayList<String>();
		sort1 = new ArrayList<String>();
		sort2 = new ArrayList<String>();
		generateHash();
	}

	private int bsearchBegin1(String word) {
		try {
			int b = 0, e = sort1.size() - 1;
			while (b < e) {
				int mid = (b + e) / 2;
				int cmp = sort1.get(mid).substring(0, OFFSET).compareTo(word);
				if (cmp > 0)
					e = mid;
				else if (cmp < 0)
					b = mid + 1;
				else
					e = mid;
			}
			if (sort1.get(b).substring(0, OFFSET).compareTo(word) == 0)
				return b;
			else
				return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	private int bsearchEnd1(String word) {
		try {
			int b = 0, e = sort1.size() - 1;
			while (b < e) {
				int mid = (b + e + 1) / 2;
				int cmp = sort1.get(mid).substring(0, OFFSET).compareTo(word);
				if (cmp > 0)
					e = mid - 1;
				else if (cmp < 0)
					b = mid;
				else
					b = mid;
			}
			if (sort1.get(b).substring(0, OFFSET).compareTo(word) == 0)
				return b;
			else
				return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	private int bsearchBegin2(String word) {
		try {
			int b = 0, e = sort2.size() - 1;
			while (b < e) {
				int mid = (b + e) / 2;
				String str = sort2.get(mid);
				int cmp = str.substring(str.length() - OFFSET, str.length())
						.compareTo(word);
				if (cmp > 0)
					e = mid;
				else if (cmp < 0)
					b = mid + 1;
				else
					e = mid;
			}
			String str = sort2.get(b);
			if (str.substring(str.length() - OFFSET, str.length()).compareTo(
					word) == 0)
				return b;
			else
				return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	private int bsearchEnd2(String word) {
		try {
			int b = 0, e = sort2.size() - 1;
			while (b < e) {
				int mid = (b + e + 1) / 2;
				String str = sort2.get(mid);
				int cmp = str.substring(str.length() - OFFSET, str.length())
						.compareTo(word);
				if (cmp > 0)
					e = mid - 1;
				else if (cmp < 0)
					b = mid;
				else
					b = mid;
			}
			String str = sort2.get(b);
			if (str.substring(str.length() - OFFSET, str.length()).compareTo(
					word) == 0)
				return b;
			else
				return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	private class customComp1 implements Comparator<String> {
		public int compare(String str1, String str2) {
			return str1.substring(0, OFFSET).compareTo(
					str2.substring(0, OFFSET));
		}
	}

	private class customComp2 implements Comparator<String> {
		public int compare(String str1, String str2) {
			int n1 = str1.length(), n2 = str2.length();
			return (str1.substring(n1 - OFFSET, n1).compareTo(str2.substring(n2
					- OFFSET, n2)));
		}
	}

	@SuppressWarnings("unused")
	private void readTitleIndexFromKB() {
		try {
			FileInputStream in = new FileInputStream(new File("data/TACKB.xml"));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.length() == 0)
					continue;
				String[] tokens = line.split("\\s+");
				if (tokens.length != 4)
					continue;
				String title = tokens[1].substring(7, tokens[1].length() - 1);
				titleIndex.add(title);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void readTitleIndexFromMyList() {
		try {
			FileInputStream in = new FileInputStream(new File(
					"data/DbpediaPersons.txt"));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.length() == 0)
					continue;
				titleIndex.add(line);
			}
			reader.close();
			in = new FileInputStream(new File("data/GeoPlaces.txt"));
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.length() == 0)
					continue;
				titleIndex.add(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void generateHash() {
		try {
			logger.info("spelling corrector loading...");
			long startTime = System.nanoTime();
			logger.info("preprocessing...please wait");
			// readTitleIndexFromKB();
			readTitleIndexFromMyList();
			Soundex sex = new Soundex();
			for (int i = 0; i < titleIndex.size(); i++) {
				String line = titleIndex.get(i);
				String[] tokens = line.split("_,");
				if (tokens.length < 1)
					continue;
				tokens[0] = tokens[0].trim();
				if (tokens[0].length() == 0)
					continue;
				String code = sex.getCode(tokens[0]);
				int index = (code.charAt(0) - 'A') * 1000
						+ (code.charAt(1) - '0') * 100 + (code.charAt(2) - '0')
						* 10 + (code.charAt(3) - '0');
				if (line.length() >= OFFSET) {
					hash[index].add(line.replace('_', ' '));
					sort1.add(line.replace('_', ' '));
					sort2.add(line.replace('_', ' '));
				}
			}
			Collections.sort(sort1, new customComp1());
			Collections.sort(sort2, new customComp2());
			logger.info("preprocessing...complete");
			long endTime = System.nanoTime();
			logger.info("preprocessing took " + (endTime - startTime)
					/ 1000000000 + " seconds");
			logger.info("spelling corrector loaded");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashSet<String> correctSpelling(String word) {
		try {
			HashSet<String> can = new HashSet<String>();
			word = word.trim().toLowerCase();
			if (word.length() == 0)
				return can;
			String[] parts = word.split("\\s+");
			Soundex sound = new Soundex();
			DameraoLevenstein ped = new DameraoLevenstein();
			String code = sound.getCode(parts[0]);
			int index = (code.charAt(0) - 'A') * 1000 + (code.charAt(1) - '0')
					* 100 + (code.charAt(2) - '0') * 10
					+ (code.charAt(3) - '0');
			String bestpossword = "";
			float bestscore = 100.0F;
			for (int i = 0; i < hash[index].size(); i++) {
				String possword = new String(hash[index].get(i));
				float score = Math.abs(ped.score(possword, word));
				if (score <= 2.0F)
					can.add(possword);
				if (score < bestscore) {
					bestscore = score;
					bestpossword = possword;
				}
			}
			int beginIndex, endIndex;
			beginIndex = bsearchBegin1(word.substring(0, OFFSET));
			if (beginIndex >= 0) {
				endIndex = bsearchEnd1(word.substring(0, OFFSET));
				for (int i = beginIndex; i <= endIndex; i++) {
					float score = Math
							.abs((float) ped.score(sort1.get(i), word));
					if (score <= 1.0F)
						can.add(sort1.get(i));
					if (score < bestscore) {
						bestscore = score;
						bestpossword = sort1.get(i);
					}
				}
			}
			beginIndex = bsearchBegin2(word.substring(word.length() - OFFSET,
					word.length()));
			if (beginIndex >= 0) {
				endIndex = bsearchEnd2(word.substring(word.length() - OFFSET,
						word.length()));
				for (int i = beginIndex; i <= endIndex; i++) {
					float score = Math
							.abs((float) ped.score(sort2.get(i), word));
					if (score <= 1.0F)
						can.add(sort2.get(i));
					if (score < bestscore) {
						bestscore = score;
						bestpossword = sort2.get(i);
					}
				}
			}
			if (can.isEmpty() && bestscore < 100.0F)
				can.add(bestpossword);
			return can;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashSet<String>();
		}
	}

	private void play() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in, "UTF-8"));
			logger.info("keep entering strings (enter END to stop)");
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.length() == 0)
					continue;
				if (line.equals("end"))
					break;
				HashSet<String> can = correctSpelling(line);
				if (can.size() == 0)
					logger.info("not found");
				else {
					ArrayList<String> sortCan = new ArrayList<String>();
					for (String str : can)
						sortCan.add(str);
					Collections.sort(sortCan);
					for (int i = 0; i < sortCan.size(); i++)
						logger.info(sortCan.get(i) + "|");
					logger.info("\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		AshwinSpellChecker sc = new AshwinSpellChecker();
		String[] ans;
		// ans = sc.query("Rep Civil War"); // rafa villar
		// ans = sc.query("Bye Rog"); // icefrog
		// ans = sc.query("Teodor Obiang Nguema"); //
		// ans = sc.query("Bagdahd"); //
		// ans = sc.query("Democrat"); //
		// ans = sc.query("Dubia"); //
		// ans = sc.query("Swedin"); //
		// ans = sc.query("Czech Republick"); //
		// ans = sc.query("Qutar"); //
		ans = sc.query("Engerland"); //

		for (String a : ans) {
			logger.info(a);
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] query(String word) throws IOException {
		HashSet<String> candidates = correctSpelling(word);
		String[] ans = new String[candidates.size()];
		return candidates.toArray(ans);
	}
}