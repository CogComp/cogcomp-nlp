/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import de.jollyday.Holiday;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import org.joda.time.DateTime;

/**
 * This class converts a holiday to a date
 */
public class Jollyday {
	static int[] list = new int[100];
	static int j = 0;

	public static TimexChunk test(String aim, String temp, int year) {
		Jollyday oTest = new Jollyday();
		ArrayList<String> lHolidays = new ArrayList<>();
		// Print out all holiday dates of each year.
		for (int i = year; i <= year; i++) {
			lHolidays = oTest.getHolidays(i, aim, temp);
			// System.out.println(lHolidays.size());
		}
		if (lHolidays.size() == 0) {
			return null;
		}
		String tempone = lHolidays.get(0);
		// System.out.println(tempone);
		String temptwo = lHolidays.get(lHolidays.size() - 1);
		String delims = "[-]";
		String[] token = tempone.split(delims);
		DateTime start = new DateTime(Integer.parseInt(token[0]),
				Integer.parseInt(token[1]), Integer.parseInt(token[2]), 0, 0,
				0, 0);
		String[] tokens = temptwo.split(delims);
		DateTime finish = new DateTime(Integer.parseInt(tokens[0]),
				Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), 23,
				59, 59, 59);
		TimexChunk tc = new TimexChunk();
		//TODO: remember to check the purpose of tempone and temptwo
		tc.setContent(aim);
		tc.addAttribute(TimexNames.type, TimexNames.DATE);
		tc.addAttribute(TimexNames.value, tempone);
		return tc;

	}

	/**
	 * Get a list of holiday dates of a given year.
	 * 
	 * @param iYear
	 * @return List of holiday dates of the whole year.
	 */
	public ArrayList<String> getHolidays(int iYear, String phrase, String temp) {
		ArrayList<String> oSortedHolidays = new ArrayList<>();
		int index;
		try {

			for (HolidayCalendar c : HolidayCalendar.values()) {
				HolidayManager oManager = HolidayManager.getInstance(c);

				Set<Holiday> oHolidays = oManager.getHolidays(iYear);
				for (Holiday oHoliday : oHolidays) {
					index = (((oHoliday.toString())).toLowerCase())
							.indexOf(phrase);
					if (index != -1) {
						String tempstr = "Country:" + c + " "
								+ (oHoliday.toString()).toLowerCase();

						if (tempstr.contains(temp)) {
							String intValue = tempstr.replaceAll(
									"[a-zA-Z():_.']", "");
							intValue = intValue.trim();
							oSortedHolidays.add(intValue);

						}
					}
				}

				// Sorted holiday dates.
				Collections.sort(oSortedHolidays);
			}
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		HashSet<String> h = new HashSet<>(oSortedHolidays);
		oSortedHolidays.clear();
		oSortedHolidays.addAll(h);
		Collections.sort(oSortedHolidays);
		return oSortedHolidays;
	}

	public static int getMax(int[] numbers) {
		int max = list[0];
		int maxnum = 0;
		int i;
		for (i = 0; i < j; i++) {
			if (numbers[i] > max) {
				max = numbers[i];
				maxnum = i;
			}
		}

		return maxnum;
	}

	public static void main(String[] args) {
		String phrase1 = "christmas";
		String temp2 = "UNITED_STATES";
		int time = 2007;
		phrase1 = phrase1.toLowerCase();
		TimexChunk usually = Jollyday.test(phrase1, temp2, time);
		System.out.println(usually);
		System.out.println(usually.toTIMEXString());
	}

}
