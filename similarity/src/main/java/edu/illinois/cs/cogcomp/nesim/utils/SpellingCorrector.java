/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class SpellingCorrector {

	private ArrayList<String>[] hash;
	private List<String> sort1, sort2;
	private int OFFSET = 3;

	public SpellingCorrector() {
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
				int cmp = str.substring(str.length() - OFFSET, str.length()).compareTo(word);
				if (cmp > 0)
					e = mid;
				else if (cmp < 0)
					b = mid + 1;
				else
					e = mid;
			}
			String str = sort2.get(b);
			if (str.substring(str.length() - OFFSET, str.length()).compareTo(word) == 0)
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
				int cmp = str.substring(str.length() - OFFSET, str.length()).compareTo(word);
				if (cmp > 0)
					e = mid - 1;
				else if (cmp < 0)
					b = mid;
				else
					b = mid;
			}
			String str = sort2.get(b);
			if (str.substring(str.length() - OFFSET, str.length()).compareTo(word) == 0)
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
			return str1.substring(0, OFFSET).compareTo(str2.substring(0, OFFSET));
		}
	}

	private class customComp2 implements Comparator<String> {
		public int compare(String str1, String str2) {
			int n1 = str1.length(), n2 = str2.length();
			return (str1.substring(n1 - OFFSET, n1).compareTo(str2.substring(n2 - OFFSET, n2)));
		}
	}

	/**
	 * Generates a hash table for all entities present in "DbpediaPerson.txt"
	 * and "GeoPlaces.txt". The hashing function used is the soundex function.
	 * Also, generates generates two list containing all the above entities. The
	 * first list is sorted by the first OFFSET characters while the second list
	 * is sorted by the last OFFSET characters. These lists are used for fast
	 * retrieval in the function @correctSpelling.
	 */
	@SuppressWarnings("unchecked")
	private void generateHash() {
		try {
			InputStream in = SpellingCorrector.class
					.getResourceAsStream("/edu/illinois/cs/cogcomp/nesim/files/DbpediaPersons.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			hash = (ArrayList<String>[]) new ArrayList[26000];
			for (int i = 0; i < 26000; i++)
				hash[i] = new ArrayList<String>();
			sort1 = new ArrayList<String>();
			sort2 = new ArrayList<String>();
			Soundex sex = new Soundex();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.length() == 0)
					continue;
				String[] parts = line.split("_");
				if (parts.length < 1)
					continue;
				parts[0] = parts[0].trim();
				if (parts[0].length() == 0)
					continue;
				String code = sex.getCode(parts[0]);
				int index = (code.charAt(0) - 'A') * 1000 + (code.charAt(1) - '0') * 100 + (code.charAt(2) - '0') * 10
						+ (code.charAt(3) - '0');
				hash[index].add(line);
				sort1.add(line);
				sort2.add(line);
				if (line.length() < OFFSET)
					System.out.println(line);
			}
			in = SpellingCorrector.class.getResourceAsStream("/edu/illinois/cs/cogcomp/nesim/files/GeoPlaces.txt");
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.length() == 0)
					continue;
				String[] parts = line.split("_");
				if (parts.length < 1)
					continue;
				parts[0] = parts[0].trim();
				if (parts[0].length() == 0)
					continue;
				String code = sex.getCode(parts[0]);
				int index = (code.charAt(0) - 'A') * 1000 + (code.charAt(1) - '0') * 100 + (code.charAt(2) - '0') * 10
						+ (code.charAt(3) - '0');
				hash[index].add(line);
				sort1.add(line);
				sort2.add(line);
				if (line.length() < OFFSET)
					System.out.println(line);
			}
			Collections.sort(sort1, new customComp1());
			Collections.sort(sort2, new customComp2());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Corrects spelling of @word i.e. returns a set of possible corrections
	 * for @word. For e.g. ["packistan"] => [{"pakestan", "pakistan"}] It
	 * currently returns all entities that are 1 edit distance away from @word +
	 * all entities that are 2 edit distance away from @word and whose soundex
	 * code matches with @word The edit distance used is DameraoLevenstein. If
	 * the list is empty, it returns the entity that is closest from @word. This
	 * spelling correction currently handles entities of types PER and LOC. It
	 * uses the list of entities present in the files "DbpediaPersons.txt" and
	 * "GeoPlaces.txt" as a Knowledge Base.
	 * 
	 * @param word
	 * @return
	 * @author ashwink
	 */
	public HashSet<String> correctSpelling(String word) {
		try {
			HashSet<String> can = new HashSet<String>();
			word = word.trim().toLowerCase();
			if (word.length() < OFFSET)
				return can;
			String[] parts = word.split("\\s+");
			Soundex sound = new Soundex();
			DameraoLevenstein ped = new DameraoLevenstein();
			String code = sound.getCode(parts[0]);
			int index = (code.charAt(0) - 'A') * 1000 + (code.charAt(1) - '0') * 100 + (code.charAt(2) - '0') * 10
					+ (code.charAt(3) - '0');
			String bestpossword = "";
			float bestscore = 100.0F;
			for (int i = 0; i < hash[index].size(); i++) {
				String possword = new String(hash[index].get(i));
				float score = Math.abs(ped.score(possword, word));
				if (score <= 2.0F)
					can.add(possword.replace("_", " "));
				if (score < bestscore) {
					bestscore = score;
					bestpossword = possword.replace("_", " ");
				}
			}
			int beginIndex, endIndex;
			beginIndex = bsearchBegin1(word.substring(0, OFFSET));
			if (beginIndex >= 0) {
				endIndex = bsearchEnd1(word.substring(0, OFFSET));
				for (int i = beginIndex; i <= endIndex; i++) {
					float score = Math.abs((float) ped.score(sort1.get(i), word));
					if (score <= 1.0F)
						can.add(sort1.get(i).replace("_", " "));
					if (score < bestscore) {
						bestscore = score;
						bestpossword = sort1.get(i).replace("_", " ");
					}
				}
			}
			beginIndex = bsearchBegin2(word.substring(word.length() - OFFSET, word.length()));
			if (beginIndex >= 0) {
				endIndex = bsearchEnd2(word.substring(word.length() - OFFSET, word.length()));
				for (int i = beginIndex; i <= endIndex; i++) {
					float score = Math.abs((float) ped.score(sort2.get(i), word));
					if (score <= 1.0F)
						can.add(sort2.get(i).replace("_", " "));
					if (score < bestscore) {
						bestscore = score;
						bestpossword = sort2.get(i).replace("_", " ");
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.length() == 0)
					continue;
				if (line.equals("end"))
					break;
				HashSet<String> can = correctSpelling(line);
				if (can.size() == 0)
					System.out.println("not found");
				else {
					ArrayList<String> sortCan = new ArrayList<String>();
					for (String str : can)
						sortCan.add(str);
					Collections.sort(sortCan);
					for (int i = 0; i < sortCan.size(); i++)
						System.out.print(sortCan.get(i) + "|");
					System.out.println();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			SpellingCorrector sc = new SpellingCorrector();
			sc.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
