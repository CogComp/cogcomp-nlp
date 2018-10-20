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

import static edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.KnowledgeBase.*;

/**
 * This class provides normalizer for temporal phrases that are anchored to certain dates (may not be DCT).
 * For example, "after xxx"
 */
public class RelativeDate {

	public static DateMapping temp = DateMapping.getInstance();

	/**
	 * This function converts phrase that contains a relative date: more than xxx, etc
	 * @param start anchor time
	 * @param phrase
     * @return
     */
	public static TimexChunk Relativerule(DateTime start, String phrase) {
		int numterm;
		int flag_ago = 0;
		int i;
		int year;
		int month;
		int day;
		DateTime finish;
		String temp1;
		String temp2;
		String temp3;
		int amount;
		Interval interval;
		interval = new Interval(start, start);
		phrase = phrase.toLowerCase();
		phrase = phrase.trim();
		TimexChunk tc = new TimexChunk();

		if (phrase.equals("recently") || phrase.equals("recent") || phrase.equals("past") || phrase.equals("at time")
				|| phrase.equals("previously")) {
			tc.addAttribute(TimexNames.type, TimexNames.DATE);
			tc.addAttribute(TimexNames.value, TimexNames.PAST_REF);
			return tc;
		}

		if (phrase.contains("future")) {
			tc.addAttribute(TimexNames.type, TimexNames.DATE);
			tc.addAttribute(TimexNames.value, TimexNames.FUTURE_REF);
			return tc;
		}

		// Handle some special cases
		if (phrase.endsWith("earlier")) {
			phrase = phrase.replace("earlier", "ago");
			phrase = phrase.trim();
		}

		if (phrase.contains("ago")) {
			phrase = phrase.replaceAll("ago", "");
			phrase = "last " + phrase;
			flag_ago = 1;
		}

		if (phrase.contains("later")) {
			phrase = phrase.replaceAll("later", "");
			phrase = "after " + phrase;
		}

		if (phrase.contains("after")) {
			phrase = phrase.replaceAll("after", "");
			phrase = "after " + phrase;
		}

		if (phrase.contains("more than")) {
			phrase = phrase.replaceAll("more than", "after");
		}

		if (phrase.contains("less than")) {
			phrase = phrase.replaceAll("less than", "in");
		}
		if (phrase.contains("last decade")) {
			phrase = phrase.replaceAll("last decade", "last ten years");
		}

		if (phrase.contains("next decade")) {
			phrase = phrase.replaceAll("next decade", "next ten years");

		}

		String patternStr = "\\s*(" + positionTerm + "|" + shiftIndicator + ")\\s*((?:\\d{1,4}|" + number + ")\\s*(?:"
				+ number + ")?)\\s*(" + dateUnit +"|" + timeUnit + ")\\s*\\w*";

		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(phrase);
		boolean matchFound = matcher.find();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTimeFormatter ymFmt = DateTimeFormat.forPattern("yyyy-MM");
		DateTimeFormatter yFmt = DateTimeFormat.forPattern("yyyy");
		if (matchFound) {
			for (i = 1; i <= 3; i++) {
				if (matcher.group(i) == null) {
					i--;
					break;
				}
			}
			if (i == 4) {
				i--;
			}
			numterm = i;
			if (numterm == 3) {

				temp1 = matcher.group(1);
				temp2 = matcher.group(2);
				temp3 = matcher.group(3);
				amount = Integer.parseInt(converter(temp2));
				if (temp1.equals("last") || temp1.equals("past")
						|| temp1.equals("previous") || temp1.equals("since")
						|| temp1.equals("this") || temp1.equals("recent")) {
					if (flag_ago == 0) {
						if (temp3.equals("years") || temp3.equals("year")) {
							finish = start.minusYears(amount);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(start.getYear(),
									start.getMonthOfYear(),
									start.getDayOfMonth(), 0, 0, 0, 0);
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "P" + amount + "Y");
							return tc;
						}

						else if (temp3.equals("day") || temp3.equals("days")) {
							finish = start.minusDays(amount);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(start.getYear(),
									start.getMonthOfYear(),
									start.getDayOfMonth(), 0, 0, 0, 0);
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "P" + amount + "D");
							return tc;

						}

						else if (temp3.equals("month")
								|| temp3.equals("months")) {
							finish = start.minusMonths(amount);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(), 0, 0, 0, 0);
							finish = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "P" + amount + "M");
							return tc;
						}

						else if (temp3.equals("week") || temp3.equals("weeks")) {
							finish = start.minusWeeks(amount);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(), 0, 0, 0, 0);
							finish = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "P" + amount + "W");
							return tc;
						}

						else if (temp3.equals("decade")
								|| temp3.equals("decades")) {

							finish = start.minusYears(amount * 10);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(), 0, 0, 0, 0);
							finish = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "P" + amount + "DE");
							return tc;
						}

						else if (temp3.equals("century")
								|| temp3.equals("centuries")) {
							finish = start.minusYears(amount * 100);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(), 0, 0, 0, 0);
							finish = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "P" + amount + "CE");
							return tc;
						}

						else if (temp3.equals("hour") || temp3.equals("hours")) {
							finish = start.minusHours(amount);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "PT" + amount + "H");
							return tc;
						}

						else if (temp3.equals("minute")
								|| temp3.equals("minutes")) {
							finish = start.minusMinutes(amount);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "PT" + amount + "M");
							return tc;
						}

						else if (temp3.equals("second")
								|| temp3.equals("seconds")) {
							finish = start.minusSeconds(amount);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "PT" + amount + "S");
							return tc;
						}

					}

					else {
						flag_ago = 0;
						if (temp3.equals("years") || temp3.equals("year")) {
							finish = start.minusYears(amount);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, yFmt.print(finish));
							return tc;
						}

						else if (temp3.equals("day") || temp3.equals("days")) {
							finish = start.minusDays(amount);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, fmt.print(finish));
							return tc;

						}

						else if (temp3.equals("month")
								|| temp3.equals("months")) {
							finish = start.minusMonths(amount);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.dayOfMonth().getMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
							return tc;
						}

						else if (temp3.equals("week") || temp3.equals("weeks")) {
							finish = start.minusWeeks(amount);
							start = finish.minusWeeks(amount + 1);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							finish = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(), 23, 59, 59, 59);
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, yFmt.print(finish)+"-W"+String.valueOf(finish.getWeekOfWeekyear()));
							return tc;
						}

						else if (temp3.equals("decade")
								|| temp3.equals("decades")) {

							finish = start.minusYears(amount * 10);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, String.valueOf(finish.getYear()/10)+"X");
							return tc;
						}

						else if (temp3.equals("century")
								|| temp3.equals("centuries")) {
							finish = start.minusYears(amount * 100);
							year = finish.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()));
							return tc;
						}

						else if (temp3.equals("hour") || temp3.equals("hours")) {
							finish = start.minusHours(amount);
							start = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(),
									finish.getHourOfDay(), 0, 0, 0);
							finish = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(),
									finish.getHourOfDay(), 59, 59, 59);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "PT" + amount + "H");
							return tc;
						}

						else if (temp3.equals("minute")
								|| temp3.equals("minutes")) {
							finish = start.minusMinutes(amount);
							start = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(),
									finish.getHourOfDay(), finish
											.minuteOfHour().get(), 0, 0);
							finish = new DateTime(finish.getYear(),
									finish.getMonthOfYear(),
									finish.getDayOfMonth(),
									finish.getHourOfDay(), finish
											.minuteOfHour().get(), 59, 59);
							tc.addAttribute(TimexNames.type, TimexNames.DURATION);
							tc.addAttribute(TimexNames.value, "PT" + amount + "M");
							return tc;
						}

					}

				}

				else if (temp1.equals("in") || temp1.equals("upcoming")
						|| temp1.equals("next") || temp1.equals("from")
						|| temp1.equals("following") || temp1.equals("during")
						|| temp1.equals("additional")) {
					if (temp3.equals("years") || temp3.equals("year")) {
						finish = start.minusYears((-1) * amount);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(start.getYear(),
								start.getMonthOfYear(), start.getDayOfMonth(),
								23, 59, 59, 59);
						start = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(finish, start);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("day") || temp3.equals("days")) {
						finish = start.minusDays((-1) * amount);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(start.getYear(),
								start.getMonthOfYear(), start.getDayOfMonth(),
								23, 59, 59, 59);
						start = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "D");
						return tc;

					}

					else if (temp3.equals("month") || temp3.equals("months")) {
						finish = start.minusMonths((-1) * amount);
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(finish.getYear(),
								finish.getMonthOfYear(),
								finish.getDayOfMonth(), 23, 59, 59, 59);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "M");
						return tc;
					}

					else if (temp3.equals("week") || temp3.equals("weeks")) {
						finish = start.minusWeeks((-1) * amount);
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(finish.getYear(),
								finish.getMonthOfYear(),
								finish.getDayOfMonth(), 23, 59, 59, 59);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "W");
						return tc;
					}

					else if (temp3.equals("decade") || temp3.equals("decades")) {
						finish = start.minusYears((-1) * amount * 10);
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(finish.getYear(),
								finish.getMonthOfYear(),
								finish.getDayOfMonth(), 23, 59, 59, 59);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						amount = amount*10;
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("century")
							|| temp3.equals("centuries")) {
						finish = start.minusYears((-1) * amount * 100);
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(finish.getYear(),
								finish.getMonthOfYear(),
								finish.getDayOfMonth(), 23, 59, 59, 59);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						amount*=100;
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("hour") || temp3.equals("hours")) {
						finish = start.minusHours((-1) * amount);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "H");
						return tc;
					}

					else if (temp3.equals("minute") || temp3.equals("minutes")) {
						finish = start.minusMinutes((-1) * amount);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "M");
						return tc;
					}

					else if (temp3.equals("second") || temp3.equals("seconds")) {
						finish = start.minusSeconds((-1) * amount);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "S");
						return tc;
					}

				}

				else if (temp1.equals("before") || temp1.equals("prior to")
						|| temp1.equals("preceding")) {
					if (temp3.equals("years") || temp3.equals("year")) {
						finish = start.minusYears(amount);
						finish = finish.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("day") || temp3.equals("days")) {

						finish = start.minusDays(amount);
						finish = finish.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "D");
						return tc;

					}

					else if (temp3.equals("month") || temp3.equals("months")) {
						finish = start.minusMonths(amount);
						finish = finish.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "M");
						return tc;
					}

					else if (temp3.equals("week") || temp3.equals("weeks")) {
						finish = start.minusWeeks(amount);
						finish = finish.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "W");
						return tc;
					}

					else if (temp3.equals("decade") || temp3.equals("decades")) {
						finish = start.minusYears(amount * 10);
						finish = finish.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						amount*=10;
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("century")
							|| temp3.equals("centuries")) {
						finish = start.minusYears(amount * 100);
						finish = finish.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						amount*=100;
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("hour") || temp3.equals("hours")) {
						finish = start.minusHours(amount);
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "H");
						return tc;
					}

					else if (temp3.equals("minute") || temp3.equals("minutes")) {
						finish = start.minusMinutes(amount);
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "M");
						return tc;
					}

					else if (temp3.equals("second") || temp3.equals("seconds")) {
						finish = start.minusSeconds(amount);
						start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "S");
						return tc;
					}

				}

				else if (temp1.equals("after") || temp1.equals("over")) {
					if (temp3.equals("years") || temp3.equals("year")) {
						finish = start.minusYears((-1) * amount);
						finish = finish.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 0, 0, 0, 0);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("day") || temp3.equals("days")) {
						finish = start.minusDays((-1) * amount);
						finish = finish.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 0, 0, 0, 0);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "D");
						return tc;

					}

					else if (temp3.equals("month") || temp3.equals("months")) {
						finish = start.minusMonths((-1) * amount);
						finish = finish.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 0, 0, 0, 0);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "M");
						return tc;
					}

					else if (temp3.equals("week") || temp3.equals("weeks")) {
						finish = start.minusWeeks((-1) * amount);
						finish = finish.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 0, 0, 0, 0);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "W");
						return tc;
					}

					else if (temp3.equals("decade") || temp3.equals("decades")) {
						finish = start.minusYears((-1) * amount * 10);
						finish = finish.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 0, 0, 0, 0);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						amount*=10;
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("century")
							|| temp3.equals("centuries")) {
						finish = start.minusYears((-1) * amount * 100);
						finish = finish.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 0, 0, 0, 0);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(finish, start);
						amount*=100;
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "P" + amount + "Y");
						return tc;
					}

					else if (temp3.equals("hour") || temp3.equals("hours")) {
						finish = start.minusHours((-1) * amount);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(finish, start);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "H");
						return tc;
					}

					else if (temp3.equals("minute") || temp3.equals("minutes")) {
						finish = start.minusMinutes((-1) * amount);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(finish, start);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "M");
						return tc;
					}

					else if (temp3.equals("second") || temp3.equals("seconds")) {
						finish = start.minusSeconds((-1) * amount);
						start = new DateTime(9999, 12, 31, 23, 59, 59, 59);
						interval = new Interval(finish, start);
						tc.addAttribute(TimexNames.type, TimexNames.DURATION);
						tc.addAttribute(TimexNames.value, "PT" + amount + "S");
						return tc;
					}

				}
			}
		}
		return null;
	}

	public static String converter(String phrase) {
		String temp1;
		String temp2;
		String tempresult;
		int onepart;
		int secondpart;
		int result;
		String patternStr = "\\d{1,4}\\s*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(phrase);
		boolean matchFound = matcher.find();
		if (matchFound) {

			return phrase.trim();
		}

		else {
			patternStr = quantifier;
			pattern = Pattern.compile(patternStr);
			matcher = pattern.matcher(phrase);
			matchFound = matcher.find();
			if (matchFound) {
				return "X";
			}

			patternStr = "(\\d{1,4})\\s*(" + number + ")";
			pattern = Pattern.compile(patternStr);
			matcher = pattern.matcher(phrase);
			matchFound = matcher.find();
			if (matchFound) {
				temp1 = matcher.group(1);
				temp2 = matcher.group(2);
				onepart = Integer.parseInt(temp1);
				temp2 = temp.hm.get(temp2);
				secondpart = Integer.parseInt(temp2);
				result = onepart * secondpart;
				tempresult = Integer.toString(result);
				return tempresult;
			}

			else {
				patternStr = "(" + number + ")\\s*(" + number + ")";
				pattern = Pattern.compile(patternStr);
				matcher = pattern.matcher(phrase);
				matchFound = matcher.find();
				if (matchFound) {
					temp1 = matcher.group(1);
					temp2 = matcher.group(2);
					temp1 = temp.hm.get(temp1);
					onepart = Integer.parseInt(temp1);
					temp2 = temp.hm.get(temp2);
					secondpart = Integer.parseInt(temp2);
					if (matcher.group(2).equals("hundred")
							|| matcher.group(2).equals("thousand")
							|| matcher.group(2).equals("million")
							|| matcher.group(2).equals("billion")) {

						result = onepart * secondpart;
					} else {
						result = onepart + secondpart;

					}
					tempresult = Integer.toString(result);
					return tempresult;
				}

				else {

					patternStr = "(" + number + ")\\s*";
					pattern = Pattern.compile(patternStr);
					matcher = pattern.matcher(phrase);
					matchFound = matcher.find();
					if (matchFound) {

						temp1 = matcher.group(1);
						temp1 = temp.hm.get(temp1);
						return temp1;


					}
				}

			}
		}
		return null;
	}

	public static void main(String args[]) {
		DateTime startTime = new DateTime(2001, 3, 14, 3, 3, 3, 3);
		String example = "one day ago";
		TimexChunk sample = Relativerule(startTime, example);

		System.out.println(RelativeDate.converter("one hundred days"));
	}
}
