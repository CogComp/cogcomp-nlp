/**
 * 
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import de.jollyday.Holiday;
import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;

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

	public Interval getHolidayInterval(int year, String timexStr) {
		String date = getHolidays(year, timexStr);
		String delims = "[-]";
		String[] tokens = date.split(delims);
		DateTime start = new DateTime(Integer.parseInt(tokens[0]),
				Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), 0, 0,
				0, 0);
		DateTime finish = new DateTime(Integer.parseInt(tokens[0]),
				Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), 23,
				59, 59, 59);
		Interval result = new Interval(start, finish);
		return result;
	}

	public static void main(String[] args) {
		String timexStr = "Christmas";
		MyHolidayManager holyMan = new MyHolidayManager();
		Interval interval = holyMan.getHolidayInterval(2000, timexStr);
	}
}
