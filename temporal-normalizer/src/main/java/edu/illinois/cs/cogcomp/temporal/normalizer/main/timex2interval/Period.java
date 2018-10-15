/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;
import java.util.regex.*;

import static edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.KnowledgeBase.*;

public class Period {

	public static List<String> normTimexToList(String timex) {
		String []list = timex.split("\\-");
		int year = 0;
		int month = 1;
		int day = 1;
		ArrayList<String> res = new ArrayList<String>();

		if (!StringUtils.isNumeric(list[0])) {
			return null;
		}

		String yearStr = list[0];
		// This the case where we have century
		if (yearStr.length()<=2) {
			//int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour
			year = Integer.parseInt(yearStr)*100;
		}
		//decades, 1960s==>196
		else if (yearStr.length()==3) {
			year = Integer.parseInt(yearStr)*10;
		}
		else if (yearStr.length()==4) {
			year = Integer.parseInt(yearStr);
		}

		res.add(String.valueOf(year));
		if (list.length==1) {
			return res;
		}

		// either 1994-W12, or 1994-04
		res.add(list[1]);
		return res;

	}

	/**
	 * This function deals with a period of time with format the xxth day/month, etc
	 * @param start the anchor time
	 * @param temporalPhrase
     * @return
     */
	public static TimexChunk Periodrule(DateTime start, TemporalPhrase temporalPhrase) {

		int year;
		DateTime finish;
		String temp1;
		String temp2;
		Interval interval;
		interval = new Interval(start, start);
		String phrase = temporalPhrase.getPhrase();
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

		for (String unitStr: dateUnitSet) {
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

			String residual = StringUtils.difference(matcher.group(0), phrase);
			String anchorStr = "";
			if (residual.length()>0) {
				TemporalPhrase anchorPhrase = new TemporalPhrase(residual, temporalPhrase.getTense());
				TimexChunk anchorTimex = TimexNormalizer.normalize(anchorPhrase);
				if (anchorTimex!=null) {
					anchorStr = anchorTimex.getAttribute(TimexNames.value);
				}
			}

			if (temp2.equals("century")) {
				year = (Integer.parseInt(temp1) - 1) * 100;
				start = new DateTime(year, 1, 1, 0, 0, 0, 0);
				finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
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
				String val = "";
				if (anchorStr.length()>0) {
					List<String> normTimexList = Period.normTimexToList(anchorStr);
					String anchorYear = normTimexList.get(0);
					String anchorDate;
					String anchorMonth;
					if (normTimexList.size()==1 || Integer.parseInt(temp1)>31) {
						anchorMonth = "01";
					}
					else {
						anchorMonth = normTimexList.get(1);
					}
					DateTime normDateTime = new DateTime(Integer.parseInt(anchorYear),
							Integer.parseInt(anchorMonth), 1, 0, 0);
					normDateTime = normDateTime.minusDays(-1*Integer.parseInt(temp1));
					anchorYear = String.valueOf(normDateTime.getYear());
					anchorMonth = String.valueOf(normDateTime.getMonthOfYear());
					anchorDate = String.valueOf(normDateTime.getDayOfMonth());
					anchorMonth = anchorMonth.length()==1?"0"+anchorMonth:anchorMonth;
					anchorDate = anchorDate.length()==1?"0"+anchorDate:anchorDate;
					val = anchorYear + "-" + anchorMonth + "-" + anchorDate;

				}
				else {
					int month = Integer.parseInt(temp1)>31?1:start.getMonthOfYear();
					DateTime normDateTime = new DateTime(start.getYear(), month, 1, 0, 0);
					normDateTime = normDateTime.minusDays(-1*Integer.parseInt(temp1));
					String anchorYear = String.valueOf(normDateTime.getYear());
					String anchorMonth = String.valueOf(normDateTime.getMonthOfYear());
					String anchorDate = String.valueOf(normDateTime.getDayOfMonth());
					anchorMonth = anchorMonth.length()==1?"0"+anchorMonth:anchorMonth;
					anchorDate = anchorDate.length()==1?"0"+anchorDate:anchorDate;
					val = String.valueOf(anchorYear) + "-" + anchorMonth + "-" + anchorDate;
				}
				tc.addAttribute(TimexNames.value, String.valueOf(val));
				return tc;
			}

			else if (temp2.equals("s")) {

				if (Integer.parseInt(temp1) < 100) {
					year = start.getCenturyOfEra();
					year = year * 100 + Integer.parseInt(temp1);
					if (modiword == 0) {
						tc.addAttribute(TimexNames.value, String.valueOf(year/10));
					}

					else if (modiword == 1) {
						tc.addAttribute(TimexNames.value, String.valueOf(year/10));
						tc.addAttribute(TimexNames.mod, TimexNames.START);
					}

					else if (modiword == 2) {
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
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getYear()/10));
					}

					else if (modiword == 1) {
						start = new DateTime(Integer.parseInt(temp1), 1, 1, 0,
								0, 0, 0);
						finish = new DateTime(Integer.parseInt(temp1) + 3, 12,
								31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getYear()/10));
						tc.addAttribute(TimexNames.mod, TimexNames.START);
					}

					else if (modiword == 2) {
						start = new DateTime(Integer.parseInt(temp1) + 7, 1, 1,
								0, 0, 0, 0);
						finish = new DateTime(Integer.parseInt(temp1) + 9, 12,
								31, 23, 59, 59, 59);
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
		String example = "the 99th day of the year";
		TimexChunk sample = Periodrule(startTime, new TemporalPhrase(example, "past"));
		System.out.println(sample.toTIMEXString());
	}

}
