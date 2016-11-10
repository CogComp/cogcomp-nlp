package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import java.util.regex.*;

public class Ordinary {

	public static Interval Ordinaryrule(DateTime start, String phrase) {
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
						start = start.minusDays(1);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(0, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("tomorrow")) {
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(0, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;

					}

					else if (temp2.equals("yesterday")) {
						start = start.minusDays(2);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(0, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}
				}

				else if (temp1.equals("after")) {
					if (temp2.equals("today")) {
						start = start.minusDays(-1);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("tomorrow")) {
						start = start.minusDays(-2);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;

					}

					else if (temp2.equals("yesterday")) {
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
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
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("today")) {
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("tomorrow")) {
						start = start.minusDays(-1);
						day = start.getDayOfMonth();
						month = start.getMonthOfYear();
						year = start.getYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
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
					interval = new Interval(start, finish);
					return interval;

				}

				else if (temp1.equals("tomorrow")) {
					start = start.minusDays(-1);
					day = start.getDayOfMonth();
					month = start.getMonthOfYear();
					year = start.getYear();
					start = new DateTime(year, month, day, 0, 0, 0, 0);
					finish = new DateTime(year, month, day, 23, 59, 59, 59);
					interval = new Interval(start, finish);
					return interval;
				}

				else if (temp1.equals("yesterday")) {
					start = start.minusDays(1);
					day = start.getDayOfMonth();
					month = start.getMonthOfYear();
					year = start.getYear();
					start = new DateTime(year, month, day, 0, 0, 0, 0);
					finish = new DateTime(year, month, day, 23, 59, 59, 59);
					interval = new Interval(start, finish);
					return interval;
				}
			}
		}
		return null;
	}

	public static void main(String args[]) {
		DateTime startTime = new DateTime(2000, 1, 1, 3, 3, 3, 3);
		String example = "the day before yesterday";
		Interval sample = Ordinaryrule(startTime, example);
	}
}
