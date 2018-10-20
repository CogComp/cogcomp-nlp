/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import java.util.*;

public class ConvertWordToNumber {

	private static String[] ordinals = { "first", "second", "third", "fourth", "fifth",
			"sixth", "seventh", "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth",
			"fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth",
			"twentieth", "thirtieth", "fortieth", "fiftieth", "sixtieth", "seventieth", "eightieth",
			"ninetieth"};

	private static String[] numerals = { "zero", "one", "first", "two",
			"second", "three", "third", "four", "fourth", "fifth", "five",
			"sixth", "six", "seven", "seventh", "eighth", "eight", "ninth",
			"nine", "ten", "tenth", "eleven", "eleventh", "twelve", "twelfth",
			"thirteen", "thirteenth", "fourteenth", "fourteen", "fifteenth",
			"fifteen", "sixteenth", "sixteen", "seventeenth", "seventeen",
			"eighteenth", "eighteen", "nineteenth", "nineteen", "twenty",
			"twentieth", "twenties", "thirty", "thirtieth", "thirties",
			"fortieth", "forty", "forties", "fiftieth", "fifty", "fifties",
			"sixty", "sixtieth", "sixties", "seventieth", "seventy",
			"seventies", "eightieth", "eighty", "eighties", "ninetieth",
			"ninety", "nineties", "hundred" };

	private static long[] values = { 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7,
			7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 16,
			16, 17, 17, 18, 18, 19, 19, 20, 20, 20, 30, 30, 30, 40, 40, 40, 50,
			50, 50, 60, 60, 60, 70, 70, 70, 80, 80, 80, 90, 90, 90, 100 };

	private static ArrayList<String> list = new ArrayList<String>(
			Arrays.asList(numerals));

	/**
	 * This function parses individual digit of a numerical string
	 * @param text string we want to parse numerical
	 * @return long that should be a number
	 * @throws Exception
     */
	public static long parseNumerals(String text) throws Exception {
		long value = 0;
		String[] words = text.replaceAll(" and ", " ").split("\\s");
		for (String word : words) {
			if (!list.contains(word)) {
				return -1;
			}

			long subval = getValueOf(word);
			if (subval == 100) {
				if (value == 0)
					value = 100;
				else
					value *= 100;
			} else
				value += subval;
		}

		return value;
	}

	private static long getValueOf(String word) {
		return values[list.indexOf(word)];
	}

	private static String[] words = { "trillion", "billion", "million",
			"thousand" };

	private static long[] digits = { 1000000000000L, 1000000000L, 1000000L,
			1000L };

	/**
	 * This function utilizes parseNumerical()
	 * @param text string we want to parse
	 * @return long
	 * @throws Exception
     */
	public static long parse(String text) throws Exception {
		text = text.toLowerCase().replaceAll("-", " ");
		long totalValue = 0;
		boolean processed = false;
		for (int n = 0; n < words.length; n++) {
			int index = text.indexOf(words[n]);
			if (index >= 0) {
				String text1 = text.substring(0, index).trim();
				String text2 = text.substring(index + words[n].length()).trim();

				if (text1.equals(""))
					text1 = "one";

				if (text2.equals(""))
					text2 = "zero";

				totalValue = parseNumerals(text1) * digits[n] + parse(text2);
				processed = true;
				break;
			}
		}

		if (processed)
			return totalValue;
		else
			return parseNumerals(text);
	}

	/**
	 * This function converts numerical strings like "ten" to arabic numbers 10.
	 * Notice we distinguish between "one" and "first".
	 * @param phrase
	 * @return string that contains arabic numbers
	 * @throws Exception
     */
	public static String ConvertWTN(String phrase) throws Exception {
		for (String ordinal: ordinals) {
			phrase = phrase.replaceAll(ordinal, ordinal+" th");
		}
		phrase = phrase.replaceAll("(\\d+)st", "$1 th");
		phrase = phrase.replaceAll("(\\d+)nd", "$1 th");
		phrase = phrase.replaceAll("(\\d+)rd", "$1 th");
		phrase = phrase.replaceAll("(\\d+)th", "$1 th");

		int startflag = -1;
		String delims = " ";
		String newphrase = new String();
		String tophrase = new String();
		String[] token = phrase.split(delims);
		for (int i = 0; i < token.length; i++) {
			String numberWordsText = token[i];
			if (ConvertWordToNumber.parse(numberWordsText) != -1) {

				if (!newphrase.equals("")) {
					newphrase = newphrase + "-" + token[i];
					token[i] = "NOT";
				}

				else {
					newphrase = token[i];
					token[i] = "NOT";
				}
			}
		}
		newphrase = newphrase.trim();

		delims = "-";

		String[] setoken = newphrase.split(delims);

		if (setoken.length == 1) {
			newphrase = "" + (ConvertWordToNumber.parse(newphrase));
		}

		else {
			newphrase = "";

			long pre = ConvertWordToNumber.parse(setoken[0]);
			if (pre >= 11 && pre <= 19) {
				pre = pre * 100;
			}

			for (int i = 1; i < setoken.length; i++) {
				if (i == 1) {
					newphrase = setoken[1];
				}

				else {
					newphrase = newphrase + "-" + setoken[i];
				}

				newphrase = newphrase.trim();

			}
			long result = pre + ConvertWordToNumber.parse(newphrase);
			newphrase = "" + result;

		}

		for (int i = 0; i < token.length; i++) {
			if (token[i].equals("NOT")) {
				if (startflag == -1) {
					tophrase = tophrase + " " + newphrase;
					startflag = 1;
				}

			}

			else {
				tophrase = tophrase + " " + token[i];
			}
		}

		tophrase = tophrase.replaceAll(" th ", "th ");
		return tophrase;
	}

	public static void main(String []args) throws Exception {
		System.out.println(ConvertWordToNumber.ConvertWTN("first day"));
	}

}