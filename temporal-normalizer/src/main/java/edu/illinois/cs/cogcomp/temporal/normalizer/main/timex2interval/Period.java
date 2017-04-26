package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;

public class Period {
	public static Set<String> dateUnit = new HashSet<String>() {{
		add("day");
		add("week");
		add("month");
		add("year");
		add("century");
		add("decade");
		add("quarter");
	}};

	public static HashMap<String, String> unitMap = new HashMap<String, String>(){
		{
			put("day", "D");
			put("days", "D");
			put("week", "W");
			put("weeks", "W");
			put("month", "M");
			put("months", "M");
			put("year", "Y");
			put("years", "Y");
			put("century", "00Y");
			put("centuries", "00Y");
			put("decade", "0Y");
			put("decades", "0Y");
			put("second", "S");
			put("seconds", "S");
			put("minute", "M");
			put("minutes", "M");
			put("hour", "H");
			put("hours", "H");
			put("time", "X");
			put("timex", "X");
			put("morning", "MO");
			put("noon", "12:00");
			put("afternoon", "AF");
			put("evening", "EV");
			put("night", "NI");

		}
	};

	public static TimexChunk Periodrule(DateTime start, String phrase) {

		int year;
		DateTime finish;
		String temp1;
		String temp2;
		Interval interval;
		interval = new Interval(start, start);
		phrase = phrase.toLowerCase();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");

		int modiword = 0;// 0 :no modified words 1:early,ealier 2:late,later
		// Handle some special cases
		TimexChunk tc = new TimexChunk();
		tc.addAttribute(TimexNames.type, TimexNames.DATE);
		if (phrase.contains("now") || phrase.contains("currently")
				|| phrase.contains("current") || phrase.contains("today")) {
			DateTime virtualStart = interval.getStart();
			virtualStart = new DateTime(virtualStart.getYear(),
					virtualStart.getMonthOfYear(),
					virtualStart.getDayOfMonth(), virtualStart.getHourOfDay(),
					virtualStart.getMinuteOfHour(),
					virtualStart.getSecondOfMinute(),
					virtualStart.getMillisOfSecond() + 1);
			//interval = new Interval(virtualStart, virtualStart);
			tc.addAttribute(TimexNames.value, TimexNames.PRESENT_REF);
			return tc;
		}
		if (phrase.contains("early") || phrase.contains("earlier")) {
			modiword = 1;
		}
		if (phrase.contains("late") || phrase.contains("later")) {
			modiword = 2;
		}

		String units = "";

		for (String unitStr: dateUnit) {
			units = units + unitStr + "|";
		}
		units += "s$";

		String patternStr = "(?:the)?\\s*(\\d{1,4})(?:th|nd|st|rd)\\s*(" + units + ")\\s*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(phrase);
		boolean matchFound = matcher.find();
		if (matchFound) {
			temp1 = matcher.group(1);
			temp2 = matcher.group(2);

			if (temp2.equals("century")) {
				year = (Integer.parseInt(temp1) - 1) * 100;
				start = new DateTime(year, 1, 1, 0, 0, 0, 0);
				finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
				//interval = new Interval(start, finish);
				tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()));
				return tc;
			}

			else if (temp2.equals("decade")) {
				// e.g.: 3rd decade (of this century)
				// first we get this century is 20, then the 3rd decade is 203
				int anchorCentury = start.getCenturyOfEra();
				String val = String.valueOf(anchorCentury*10+temp1);
				tc.addAttribute(TimexNames.value, String.valueOf(val));
				return tc;
			}

			else if (temp2.equals("year")) {
				int anchorCentury = start.getCenturyOfEra();
				String val = String.valueOf(anchorCentury*100+temp1);
				tc.addAttribute(TimexNames.value, String.valueOf(val));
				return tc;
			}

			else if (temp2.equals("quarter")) {
				int anchorYear = start.getYear();
				String val = String.valueOf(anchorYear)+"-Q"+temp1;
				tc.addAttribute(TimexNames.value, String.valueOf(val));
				return tc;
			}

			else if (temp2.equals("month")) {
				int anchorYear = start.getYear();
				String monthStr = Integer.parseInt(temp1)<10?"0"+temp1:temp1;
				String val = String.valueOf(anchorYear)+"-"+monthStr;
				tc.addAttribute(TimexNames.value, String.valueOf(val));
				return tc;
			}

			else if (temp2.equals("day")) {
				int anchorYear = start.getYear();
				int anchorMonth = start.getMonthOfYear();
				String monthStr = anchorMonth<10?"0"+anchorMonth:String.valueOf(anchorMonth);
				String dayStr = Integer.parseInt(temp1)<10?"0"+temp1:temp1;
				String val = String.valueOf(anchorYear)+"-"+monthStr+"-"+dayStr;
				tc.addAttribute(TimexNames.value, String.valueOf(val));
				return tc;
			}

			else if (temp2.equals("s")) {

				if (Integer.parseInt(temp1) < 100) {
					year = start.getCenturyOfEra();
					year = year * 100 + Integer.parseInt(temp1);
					if (modiword == 0) {

						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 9, 12, 31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.value, String.valueOf(year/10));
					}

					else if (modiword == 1) {
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 3, 12, 31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.value, String.valueOf(year/10));
						tc.addAttribute(TimexNames.mod, TimexNames.START);
					}

					else if (modiword == 2) {
						start = new DateTime(year + 7, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 9, 12, 31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.value, String.valueOf(year/10));
						tc.addAttribute(TimexNames.mod, TimexNames.END);
					}

					return tc;
				}

				else {
					if (modiword == 0) {
						start = new DateTime(Integer.parseInt(temp1), 1, 1, 0,
								0, 0, 0);
						finish = new DateTime(Integer.parseInt(temp1) + 9, 12,
								31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getYear()/10));
					}

					else if (modiword == 1) {
						start = new DateTime(Integer.parseInt(temp1), 1, 1, 0,
								0, 0, 0);
						finish = new DateTime(Integer.parseInt(temp1) + 3, 12,
								31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getYear()/10));
						tc.addAttribute(TimexNames.mod, TimexNames.START);
					}

					else if (modiword == 2) {
						start = new DateTime(Integer.parseInt(temp1) + 7, 1, 1,
								0, 0, 0, 0);
						finish = new DateTime(Integer.parseInt(temp1) + 9, 12,
								31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getYear()/10));
						tc.addAttribute(TimexNames.mod, TimexNames.END);
					}
					return tc;

				}
			}

		}
		return null;
	}

	public static void main(String args[]) {
		DateTime startTime = new DateTime(1900, 3, 14, 3, 3, 3, 3);
		String example = "90 days";
		TimexChunk sample = Periodrule(startTime, example);
		System.out.println(sample.toTIMEXString());
	}

}
