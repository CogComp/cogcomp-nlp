/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.*;

/**
 * This class handles special temporal phrases that contain ordinary terms like "yesterday", "tomorrow",
 * and "today", etc
 */
public class Ordinary {

	/**
	 * This functions deals with special terms like today, tomorrow, and yesterday
	 * @param start anchor time of the Phrase
	 * @param phrase
     * @return
     */
	public static TimexChunk Ordinaryrule(DateTime start, String phrase) {
		int numterm = 0;
		int i;
		int year;
		int month;
		int day;
		DateTime finish;
		String temp1;
		String temp2;
		Interval interval;
		interval = new Interval(start, start);
		phrase = phrase.toLowerCase();
		String patternStr = "(?:the)?\\s*(?:day)?\\s*(before|after|since)?\\s*(today|tomorrow|yesterday)\\s*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(phrase);
		boolean matchFound = matcher.find();
		TimexChunk tc = new TimexChunk();
		tc.addAttribute(TimexNames.type, TimexNames.DATE);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");

		if (matchFound) {
			for (i = 1; i <= 2; i++) {
				numterm++;
				if (matcher.group(i) == null) {
					numterm--;
				}
			}

			if (numterm == 2) {

				temp1 = matcher.group(1);
				temp2 = matcher.group(2);
				if (temp1.equals("before")) {
					if (temp2.equals("today")) {
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(0, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						tc.addAttribute(TimexNames.mod, TimexNames.BEFORE);
						return tc;
					}

					else if (temp2.equals("tomorrow")) {
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(0, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day+1, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						tc.addAttribute(TimexNames.mod, TimexNames.BEFORE);
						return tc;

					}

					else if (temp2.equals("yesterday")) {
						start = start.minusDays(1);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(0, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						tc.addAttribute(TimexNames.mod, TimexNames.BEFORE);
						return tc;
					}
				}

				else if (temp1.equals("after")) {
					if (temp2.equals("today")) {
						//start = start.minusDays(-1);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(start));
						tc.addAttribute(TimexNames.mod, TimexNames.AFTER);
						return tc;
					}

					else if (temp2.equals("tomorrow")) {
						start = start.minusDays(-1);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(start));
						tc.addAttribute(TimexNames.mod, TimexNames.AFTER);
						return tc;

					}

					else if (temp2.equals("yesterday")) {
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day-1, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(start));
						tc.addAttribute(TimexNames.mod, TimexNames.AFTER);
						return tc;
					}
				}

				else if (temp1.equals("since")) {
					if (temp2.equals("yesterday")) {
						start = start.minusDays(1);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(start));
						return tc;
					}

					else if (temp2.equals("today")) {
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(start));
						return tc;
					}

					else if (temp2.equals("tomorrow")) {
						start = start.minusDays(-1);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(start));
						return tc;
					}
				}
			}

			else if (numterm == 1) {

				temp1 = matcher.group(2);
				if (temp1.equals("today")) {
					day = start.getDayOfMonth();
					month = start.getMonthOfYear();
					year = start.getYear();
					start = new DateTime(year, month, day, 0, 0, 0, 0);
					finish = new DateTime(year, month, day, 23, 59, 59, 59);
					tc.addAttribute(TimexNames.value, fmt.print(finish));
					return tc;

				}

				else if (temp1.equals("tomorrow")) {
					start = start.minusDays(-1);
					day = start.getDayOfMonth();
					month = start.getMonthOfYear();
					year = start.getYear();
					start = new DateTime(year, month, day, 0, 0, 0, 0);
					finish = new DateTime(year, month, day, 23, 59, 59, 59);
					tc.addAttribute(TimexNames.value, fmt.print(finish));
					return tc;
				}

				else if (temp1.equals("yesterday")) {
					start = start.minusDays(1);
					day = start.getDayOfMonth();
					month = start.getMonthOfYear();
					year = start.getYear();
					start = new DateTime(year, month, day, 0, 0, 0, 0);
					finish = new DateTime(year, month, day, 23, 59, 59, 59);
					tc.addAttribute(TimexNames.value, fmt.print(finish));
					return tc;
				}
			}
		}
		return null;
	}

	public static void main(String args[]) {
		DateTime startTime = new DateTime(2000, 1, 1, 3, 3, 3, 3);
		String example = "3 days before yesterday";
		TimexChunk sample = Ordinaryrule(startTime, example);
		System.out.println(sample.toTIMEXString());
	}
}
