/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nesim.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Soundex algorithm for phonetic correction.
 */
public class Soundex {

	private String ERROR = "A000";

	Soundex() {
	}

	/**
	 * Returns the soundex code of @str. For details about the soundex algorithm
	 * and its utility, read: {1} http://en.wikipedia.org/wiki/Soundex {2}
	 * http://nlp.stanford.edu/IR-book/html/htmledition/phonetic-correction-1.html
	 * This function handles all kinds of bad inputs i.e. strings with special
	 * characters. It treats all special characters as vowels. Modify this
	 * appropriately if you don't want that. Returns ERROR in case of empty
	 * strings (or strings with just spaces).
	 */
	public String getCode(String str) {
		try {
			str = str.trim().toUpperCase();
			if (str.length() == 0)
				return ERROR;
			char firstLetter = str.charAt(0);
			StringBuilder code = new StringBuilder();
			for (int i = 0; i < str.length(); i++)
				if (str.charAt(i) != 'H' && str.charAt(i) != 'W')
					code.append(str.charAt(i));
			str = code.toString();
			code.setLength(0);
			int size = str.length();
			int arr[] = new int[size];
			for (int i = 0; i < size; i++) {
				if (str.charAt(i) == 'B' || str.charAt(i) == 'F' || str.charAt(i) == 'P' || str.charAt(i) == 'V')
					arr[i] = 1;
				else if (str.charAt(i) == 'C' || str.charAt(i) == 'G' || str.charAt(i) == 'J' || str.charAt(i) == 'K'
						|| str.charAt(i) == 'Q' || str.charAt(i) == 'S' || str.charAt(i) == 'X' || str.charAt(i) == 'Z')
					arr[i] = 2;
				else if (str.charAt(i) == 'D' || str.charAt(i) == 'T')
					arr[i] = 3;
				else if (str.charAt(i) == 'L')
					arr[i] = 4;
				else if (str.charAt(i) == 'M' || str.charAt(i) == 'N')
					arr[i] = 5;
				else if (str.charAt(i) == 'R')
					arr[i] = 6;
				else
					arr[i] = 0;
			}
			code.append(firstLetter);
			if (arr.length > 0) {
				int curr = arr[0];
				int i = 1;
				while (i < size) {
					if (arr[i] != curr) {
						curr = arr[i];
						if (curr > 0)
							code.append((char) ('0' + curr));
					}
					i++;
				}
			}
			while (code.length() < 4)
				code.append('0');
			return code.toString().substring(0, 4);
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR;
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
				String code = getCode(line);
				System.out.println(code);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Soundex sound = new Soundex();
			sound.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
