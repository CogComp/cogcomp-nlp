package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.*;

public class Period {
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

		String patternStr = "(?:the)?\\s*(\\d{1,4})(?:th|nd|st|rd)?\\s*(century|s$)\\s*";
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
		String example = "now";
		TimexChunk sample = Periodrule(startTime, example);
	}

}
