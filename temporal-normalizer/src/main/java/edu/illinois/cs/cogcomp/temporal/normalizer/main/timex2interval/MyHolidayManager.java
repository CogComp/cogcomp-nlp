/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
/**
 * 
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import java.util.Set;

import org.joda.time.DateTime;

import de.jollyday.Holiday;
import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author dxquang Jun 3, 2012
 */
public class MyHolidayManager {

	protected HolidayManager holyMan = null;

	/**
	 * 
	 */
	public MyHolidayManager() {
		holyMan = HolidayManager.getInstance(HolidayCalendar.UNITED_STATES);
	}

	protected String getHolidays(int year, String timexStr) {
		timexStr = timexStr.toLowerCase();
		Set<Holiday> setHolidays = holyMan.getHolidays(year);
		for (Holiday holiday : setHolidays) {
			String hday = holiday.toString().toLowerCase();
			if (hday.indexOf(timexStr) != -1) {
				int pos = hday.indexOf(' ');
				String date = hday.substring(0,pos);
				return date;
			}
		}
		return "";
	}

	public TimexChunk getHolidayInterval(int year, String timexStr) {
		String date = getHolidays(year, timexStr);
		String delims = "[-]";
		String[] tokens = date.split(delims);
		DateTime start = new DateTime(Integer.parseInt(tokens[0]),
				Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), 0, 0,
				0, 0);
		DateTime finish = new DateTime(Integer.parseInt(tokens[0]),
				Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), 23,
				59, 59, 59);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		TimexChunk tc = new TimexChunk();
		tc.setContent(timexStr);
		tc.addAttribute(TimexNames.type, TimexNames.DATE);
		tc.addAttribute(TimexNames.value, fmt.print(finish));
		return tc;
	}

	public static void main(String[] args) {
		String timexStr = "Christmas";
		MyHolidayManager holyMan = new MyHolidayManager();
		TimexChunk interval = holyMan.getHolidayInterval(2000, timexStr);
	}
}
