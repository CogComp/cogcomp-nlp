/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import static edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.KnowledgeBase.*;

/**
 * This class provides method to normalize a group of temporal phrases that are modified by
 * words like "before", "since", etc
 * Define the time line
 * Morning 7:00-11:00 Noon 11:00-2:00 Afternoon 2:00-6:00 Evening 6:00-9:00 Night 9:00-12:00
 */
public class ModifiedDate {

	/**
	 *
	 * @param start the start point (anchor time) of the Phrase
	 * @param temporalPhrase
     * @return
     */
	public static TimexChunk ModifiedRule(DateTime start, TemporalPhrase temporalPhrase) {
		String phrase = temporalPhrase.getPhrase();
		phrase = phrase.trim();

		// ###################################################preprocess
		// 1################################################
		String preprocess = "\\A(" + timeofDay + "|" + modIndicator + ")\\s*(" + weekday + ")\\Z";
		Pattern pattern11 = Pattern.compile(preprocess);
		Matcher matcher11 = pattern11.matcher(phrase);
		boolean matcher11Found = matcher11.find();

		if (matcher11Found) {
			TimexChunk tc = new TimexChunk();
			tc.addAttribute(TimexNames.type, TimexNames.DATE);
			String secondphrase = new String();
			// split the Phrase into two parts
			// TODO: write converter to convert TIMEX3 to datepoint
			String firstphrase = "this" + matcher11.group(2);
			tc = ModifiedDate.ModifiedRule(start, new TemporalPhrase(firstphrase, temporalPhrase.getTense()));
			if (matcher11.group(1).contains("early")
					|| matcher11.group(1).contains("earlier")) {

				tc.addAttribute(TimexNames.mod, TimexNames.START);
			}

			else if (matcher11.group(1).contains("late")
					|| matcher11.group(1).contains("later")) {
				tc.addAttribute(TimexNames.mod, TimexNames.END);
			}

			else {
				secondphrase = matcher11.group(1);
				String revertStr = firstphrase + " " + secondphrase;
				temporalPhrase.setPhrase(revertStr);
				return ModifiedDate.ModifiedRule(start, temporalPhrase);
			}
			return tc;

		}

		// ###################################################preprocess
		// 2################################################

		String ownprocess = "\\A(" + weekday + ")\\s*(" + timeofDay + ")\\Z";
		Pattern pattern2 = Pattern.compile(ownprocess);
		Matcher matcher2 = pattern2.matcher(phrase);
		boolean matcher2Found = matcher2.find();
		if (matcher2Found) {
			// split the Phrase into two parts
			String firstphrase = "this" + matcher2.group(1);
			String secondphrase = "this" + matcher2.group(2);
			TimexChunk keypoint = ModifiedDate.ModifiedRule(start, new TemporalPhrase(firstphrase, temporalPhrase.getTense()));
			String normVal = keypoint.getAttributes().get(TimexNames.value);
			DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime starttime = fmt.parseDateTime(normVal);
			TimexChunk result = ModifiedDate
					.ModifiedRule(starttime, new TemporalPhrase(secondphrase, temporalPhrase.getTense()));
			return result;

		}

		//String monther = "(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may?|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sept(?:ember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?|year)";
		String monther = "(" + KnowledgeBase.monther + ")";
		//String specialterm = "\\A(?:morning|afternoon|noon|evening|night|spring|fall|summer|winter|mon(?:day)?|tues(?:day)?|wed(?:nesday)?|thur(?:sday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)\\Z";
		String specialterm = "\\A(" + timeofDay + "|" + season + "|" + weekday + ")\\Z";
		Pattern onepattern = Pattern.compile(specialterm);

		Matcher onematcher = onepattern.matcher(phrase);
		boolean onematchFound = onematcher.find();
		if (onematchFound) {
			phrase = "this " + onematcher.group(0);
		}

		int numterm;
		DateMapping temp = DateMapping.getInstance();
		int i;
		int dayweek;
		int year;
		int month = 1;
		int day;
		int flagbefore = -1;
		int flagafter = -1;
		String thisTimeOfDay="";
		int prematchDay=-1;
		DateTime finish;
		String temp1;
		String temp2;
		Interval interval;
		interval = new Interval(start, start);
		phrase = phrase.toLowerCase();
		int preflag = -1;
		if (phrase.equals("tonight")) {
			phrase = "today night";
		}
		if (phrase.contains("before")) {

			phrase = phrase.replaceAll("before", "");
			phrase = phrase.trim();
			flagbefore = 1;
		}
		if (phrase.contains("\\Aafter\\Z")) {
			phrase = phrase.replaceAll("after", "");
			phrase = phrase.trim();
			flagafter = 1;
		}

		String spepatternStr = "(" + timeofDay + "|" + season + "|" + weekday + ")\\s*(" + shiftIndicator + ")\\s*(" + dateUnit + ")";
		Pattern spepattern = Pattern.compile(spepatternStr);
		Matcher spematcher = spepattern.matcher(phrase);
		boolean spematchFound = spematcher.find();
		if (spematchFound) {
			String group1 = spematcher.group(1);
			String group2 = spematcher.group(2);
			// String group3 = spematcher.group(3);

			phrase = group2 + " " + group1;

		}

		String patternStr = "\\s*(" + shiftIndicator + "|" + modIndicator + "|" + specialDayTerm + ")\\s*(" +
				timeofDay + "|" + dateUnit + "|" +weekday + "|" + season + "|" +  KnowledgeBase.monther  + ")";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(phrase);
		boolean matchFound = matcher.find();
		TimexChunk tc = new TimexChunk();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTimeFormatter ymFmt = DateTimeFormat.forPattern("yyyy-MM");
		DateTimeFormatter yFmt = DateTimeFormat.forPattern("yyyy");
		if (matchFound) {
			for (i = 1; i <= 2; i++) {
				if (matcher.group(i) == null) {
					i--;
					break;
				}
			}
			if (i == 3) {
				i--;
			}
			numterm = i;
			if (numterm == 2) {
				// group(0) returns the whole detected string which matches the
				// pattern
				String origPhrase = phrase;
				phrase = phrase.replaceAll(matcher.group(0), "");
				phrase = phrase.trim();
				String prepatternStr = "(" + KnowledgeBase.monther + "|year)";
				Pattern prepattern = Pattern.compile(prepatternStr);
				Matcher prematcher = prepattern.matcher(phrase);
				boolean prematchFound = prematcher.find();
				if (prematchFound) {
					if (temp.hm.containsKey(prematcher.group(1))) {
						month = Integer.parseInt(temp.hm.get(prematcher.group(1)));

						preflag = 1;
					}
				}

				prepatternStr = "(\\d+)";
				prepattern = Pattern.compile(prepatternStr);
				prematcher = prepattern.matcher(phrase);
				prematchFound = prematcher.find();
				if (prematchFound) {
					int dayInt = Integer.parseInt(prematcher.group(1));
					if (dayInt > 0 && dayInt < 32) {
						prematchDay = dayInt;
						preflag = 1;
					}
				}

				prepatternStr = "(" + weekday + ")";
				prepattern = Pattern.compile(prepatternStr);
				prematcher = prepattern.matcher(phrase);
				prematchFound = prematcher.find();
				if (prematchFound) {
					if (temp.hm.containsKey(prematcher.group(1))) {
						prematchDay = Integer.parseInt(temp.hm.get(prematcher.group(1)));
						preflag = 1;
					}
				}

				prepatternStr = "(" + timeofDay + ")";
				prepattern = Pattern.compile(prepatternStr);
				prematcher = prepattern.matcher(phrase);
				prematchFound = prematcher.find();
				if (prematchFound) {
					if (unitMap.containsKey(prematcher.group(1))) {
						thisTimeOfDay = unitMap.get(prematcher.group(1));
						preflag = 1;
					}
				}

				else {
					String backPatt = "(early|earlier|begin(?:ing)?|start)";

					Pattern backPa = Pattern.compile(backPatt);
					Matcher backmatcher = backPa.matcher(phrase);
					boolean backFound = backmatcher.find();

					if (backFound) {
						// 2 means early
						preflag = 2;
					}

					else {
						String backPatter = "(late|later|end)";
						Pattern backPater = Pattern.compile(backPatter);
						Matcher backmatch = backPater.matcher(phrase);
						boolean backFounder = backmatch.find();
						if (backFounder) {
							// 3 means late
							preflag = 3;
						}

					}

				}

				temp1 = matcher.group(1);
				temp2 = matcher.group(2);

				if (temp1.equals("last") || temp1.equals("past")
						|| temp1.equals("previous")
						|| temp1.equals("yesterday")) {

					if (temp2.equals("year")) {
						finish = start.minusYears(1);

						year = finish.getYear();
						if (preflag == 1) {

							if (flagbefore == 1) {

								finish = new DateTime(year, month, 1, 23, 59,
										59, 59);
								finish = finish.minusDays(1);
								String normVal = fmt.print(finish);
								tc.addAttribute(TimexNames.type, TimexNames.DATE);
								tc.addAttribute(TimexNames.value, normVal);
								tc.addAttribute(TimexNames.mod, TimexNames.BEFORE);
								return tc;
							}

							else if (flagafter == 1) {
								finish = new DateTime(year, month, 1, 0, 0, 0,
										0);
								finish = finish.dayOfMonth().withMaximumValue();
								finish = finish.minusDays(-1);
								String normVal = fmt.print(finish);
								tc.addAttribute(TimexNames.type, TimexNames.DATE);
								tc.addAttribute(TimexNames.value, normVal);
								tc.addAttribute(TimexNames.mod, TimexNames.AFTER);
								return tc;
							}
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							String normVal = ymFmt.print(finish);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, normVal);
							return tc;
						}

						else if (preflag == 2) {

							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, String.valueOf(year));
							tc.addAttribute(TimexNames.mod, TimexNames.START);
							return tc;
						}

						else if (preflag == 3) {
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, String.valueOf(year));
							tc.addAttribute(TimexNames.mod, TimexNames.END);
							return tc;
						}

						else {
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, String.valueOf(year));
							return tc;
						}
					}


					else if (temp2.equals("month")) {

						finish = start.minusMonths(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						finish = finish.dayOfMonth().withMaximumValue();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, ymFmt.print(finish));
						if (preflag == 1) {
							finish = new DateTime(year, month, prematchDay, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, fmt.print(finish));
						}
						else if (preflag == 2) {
							tc.addAttribute(TimexNames.value, fmt.print(finish));
							tc.addAttribute(TimexNames.mod, TimexNames.START);
						}

						else if (preflag == 3) {
							start = new DateTime(year, month, day - 10, 0, 0,
									0, 0);
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
							tc.addAttribute(TimexNames.value, fmt.print(finish));
							tc.addAttribute(TimexNames.mod, TimexNames.END);
						}
						return tc;
					}

					else if (temp2.equals("week")) {
						finish = start.minusWeeks(1);
						dayweek = finish.getDayOfWeek();
						finish = finish.minusDays(dayweek - 1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = finish.minusDays(-6);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						if (preflag == 1) {
							String weekIdx = String.valueOf(finish.getWeekOfWeekyear());
							weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
							tc.addAttribute(TimexNames.value,
									String.valueOf(year)+"-W"+weekIdx + "-" + String.valueOf(prematchDay));
						}
						if (preflag == 2) {
							finish = start.minusDays(-2);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							String weekIdx = String.valueOf(finish.getWeekOfWeekyear());
							weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
							tc.addAttribute(TimexNames.value, String.valueOf(year)+"-W"+weekIdx);
							tc.addAttribute(TimexNames.mod, TimexNames.START);
						}

						else if (preflag == 3) {
							start = start.minusDays(-4);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(year, month, day, 0, 0, 0, 0);

							String weekIdx = String.valueOf(start.getWeekOfWeekyear());
							weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
							tc.addAttribute(TimexNames.value, String.valueOf(year)+"-W"+weekIdx);
							tc.addAttribute(TimexNames.mod, TimexNames.END);

						}
						else
						{
							String weekIdx = String.valueOf(start.getWeekOfWeekyear());
							tc.addAttribute(TimexNames.value, String.valueOf(year)+"-W"+weekIdx);
						}
						return tc;
					}

					else if (temp2.equals("weekend")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = start.minusDays(-1);
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String weekIdx = String.valueOf(finish.getWeekOfWeekyear());
						weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
						tc.addAttribute(TimexNames.value, String.valueOf(year) + "-W" + weekIdx + "-WE");
						return tc;

						/* TODO: I commented out these three cases for now because they don't make sense
						 using TIMEX3 standard
						 */
					}
//					 else if (temp2.equals("hour")) {
//						finish = start.minusHours(1);
//						interval = new Interval(finish, start);
//						return interval;
//					}
//
//					else if (temp2.equals("minute")) {
//						finish = start.minusMinutes(1);
//						interval = new Interval(finish, start);
//						return interval;
//					}
//
//					else if (temp2.equals("second")) {
//						finish = start.minusSeconds(1);
//						interval = new Interval(finish, start);
//						return interval;
//					}

					else if (temp2.equals("century")) {
						finish = start.minusYears(100);
						year = finish.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()));
						return tc;
					}

					else if (temp2.equals("decade")) {
						finish = start.minusYears(10);
						year = finish.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 9, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()*10));
						return tc;
					}

					else if (temp2.equals("mon") || temp2.equals("monday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 6);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("tues") || temp2.equals("tuesday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 5);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("wed") || temp2.equals("wednesday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 4);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("thur") || temp2.equals("thursday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 3);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("fri") || temp2.equals("friday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 2);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("sat") || temp2.equals("saturday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("sun") || temp2.equals("sunday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("morning")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 7, 0, 0, 0);
						finish = new DateTime(year, month, day, 10, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.MORNING);
						return tc;
					}

					else if (temp2.equals("noon")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 11, 0, 0, 0);
						finish = new DateTime(year, month, day, 13, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.NOON);
						return tc;
					}

					else if (temp2.equals("afternoon")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 14, 0, 0, 0);
						finish = new DateTime(year, month, day, 17, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.AFTERNOON);
						return tc;
					}

					else if (temp2.equals("evening")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 18, 0, 0, 0);
						finish = new DateTime(year, month, day, 20, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.EVENING);
						return tc;
					}

					else if (temp2.equals("night")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 21, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.NIGHT);
						return tc;
					}

					else if (temp2.equals("day")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						return tc;
					}

					else if (temp2.equals("spring")) {
						finish = start.minusYears(1);
						year = finish.getYear();
						start = new DateTime(year, 3, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 5, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-" + TimexNames.SPRING);
						return tc;
					}

					else if (temp2.equals("summer")) {
						finish = start.minusYears(1);
						year = finish.getYear();
						start = new DateTime(year, 6, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 8, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-" + TimexNames.SUMMER);
						return tc;
					}

					else if (temp2.equals("fall") || temp2.equals("autumn")) {
						finish = start.minusYears(1);
						year = finish.getYear();
						start = new DateTime(year, 9, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 11, 30, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-" + TimexNames.FALL);
						return tc;
					}

					else if (temp2.equals("winter")) {
						finish = start.minusYears(2);
						year = finish.getYear();
						start = new DateTime(year, 12, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 1, 2, 28, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-" + TimexNames.WINTER);
						return tc;
					}

					// If we have (last|previous|...)(month)
					else {
						String patternStr1 = monther;
						Pattern pattern1 = Pattern.compile(patternStr1);
						Matcher matcher1 = pattern1.matcher(temp2);
						boolean matchFound1 = matcher1.find();
						if (matchFound1) {
							month = Integer.parseInt(temp.hm.get(temp2));
							finish = start.minusYears(1);
							year = finish.getYear();
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
							if (preflag == 2) {
								finish = new DateTime(year, month, 10, 23, 59,
										59, 59);
								start = new DateTime(year, month, 1, 0, 0, 0, 0);
								tc.addAttribute(TimexNames.mod, TimexNames.START);
							}

							else if (preflag == 3) {
								start = new DateTime(year, month, 20, 0, 0, 0,
										0);
								tc.addAttribute(TimexNames.mod, TimexNames.END);
							}
							return tc;
						} else {
							return null;
						}

					}
				}

				else if (temp1.equals("early") || temp1.equals("earlier")) {
					tc.addAttribute(TimexNames.mod, TimexNames.START);
					if (temp2.equals("century")) {

						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 20, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()));
						return tc;
					}
					else if (temp2.equals("decade")) {
						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 2, 12, 31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()*10));
						return tc;
					}

					else if (temp2.equals("year")) {
						year = start.getYear();

						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 3, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, yFmt.print(finish));
						return tc;

					}

					else if (temp2.equals("month")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						finish = start.dayOfMonth().withMaximumValue();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, 10, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, ymFmt.print(finish));
						return tc;
					}

					else if (temp2.equals("week")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 1);
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = start.minusDays(-2);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String weekIdx = String.valueOf(finish.getWeekOfWeekyear());
						weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, String.valueOf(year)+"-W"+weekIdx);
						return tc;
					}

					else {
						// This part only focus on "early" + "[month]"
						// Modified by Zhili: if the sentence is past tense, and the month mentioned is
						// after DCT, then subtract 1 from year
						String tense = temporalPhrase.getTense();
						String patternStr1 = monther;
						Pattern pattern1 = Pattern.compile(patternStr1);
						Matcher matcher1 = pattern1.matcher(temp2);
						boolean matchFound1 = matcher1.find();
						if (matchFound1) {
							month = Integer.parseInt(temp.hm.get(temp2));
							year = start.getYear();
							if (tense.equals("past") && start.getMonthOfYear()<month) {
									year-=1;
							}
							else if (!tense.equals("past") && start.getMonthOfYear()>month) {
								year+=1;
							}
							finish = new DateTime(year, month, 10, 23, 59, 59,
									59);
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.type, TimexNames.DATE);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
							return tc;
						} else {
							return null;
						}

					}

				}

				else if (temp1.equals("late") || temp1.equals("later")) {
					tc.addAttribute(TimexNames.mod, TimexNames.END);
					tc.addAttribute(TimexNames.type, TimexNames.DATE);
					if (temp2.equals("century")) {

						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year + 80, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()));
						return tc;
					}

					else if (temp2.equals("decade")) {
						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year+8, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 9, 12, 31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()*10));
						return tc;
					}

					else if (temp2.equals("year")) {
						year = start.getYear();

						start = new DateTime(year, 10, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.value, yFmt.print(finish));
						return tc;

					}

					else if (temp2.equals("month")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						finish = start.dayOfMonth().withMaximumValue();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day - 10, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, ymFmt.print(finish));
						return tc;
					}

					else {
						String patternStr1 = monther;
						Pattern pattern1 = Pattern.compile(patternStr1);
						Matcher matcher1 = pattern1.matcher(temp2);
						boolean matchFound1 = matcher1.find();
						if (matchFound1) {
							month = Integer.parseInt(temp.hm.get(temp2));
							year = start.getYear();
							String tense = temporalPhrase.getTense();
							if (tense.equals("past") && start.getMonthOfYear()<month) {
								year-=1;
							}
							else if (!tense.equals("past") && start.getMonthOfYear()>month) {
								year+=1;
							}
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month,
									finish.getDayOfMonth() - 10, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
							return tc;
						} else {
							return null;
						}

					}
				}

				else if (temp1.equals("this") || temp1.equals("today")) {
					tc.addAttribute(TimexNames.type, TimexNames.DATE);
					if (temp2.equals("century")) {

						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()));
						return tc;
					}

					else if (temp2.equals("decade")) {
						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 9, 12, 31, 23, 59, 59, 59);
						//interval = new Interval(start, finish);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()*10));
						return tc;
					}

					else if (temp2.equals("year")) {
						year = start.getYear();
						if (preflag == 1) {
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
							return tc;
						}

						else if (preflag == 2) {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 3, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, yFmt.print(finish));
							tc.addAttribute(TimexNames.mod, TimexNames.START);
							return tc;
						}

						else if (preflag == 3) {
							start = new DateTime(year, 10, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, yFmt.print(finish));
							tc.addAttribute(TimexNames.mod, TimexNames.END);
							return tc;
						} else {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, yFmt.print(finish));
							return tc;
						}
					}

					else if (temp2.equals("month")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						finish = start.dayOfMonth().withMaximumValue();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, ymFmt.print(finish));
						if (preflag == 1) {
							finish = new DateTime(year, month, prematchDay, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, fmt.print(finish));
						}
						else if (preflag == 2) {
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							finish = new DateTime(year, month, 10, 23, 59, 59,
									59);
							tc.addAttribute(TimexNames.mod, TimexNames.START);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
						}

						else if (preflag == 3) {
							start = new DateTime(year, month, day - 10, 0, 0,
									0, 0);
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							tc.addAttribute(TimexNames.mod, TimexNames.END);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));

						}
						return tc;
					}

					else if (temp2.equals("week")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 1);
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = start.minusDays(-6);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String weekIdx = String.valueOf(finish.getWeekOfWeekyear());
						weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-W" + weekIdx);
						if (preflag == 1) {
							weekIdx = String.valueOf(finish.getWeekOfWeekyear());
							weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
							tc.addAttribute(TimexNames.value,
									String.valueOf(year)+"-W"+weekIdx + "-" + String.valueOf(prematchDay));
						}
						else if (preflag == 2) {
							finish = start.minusDays(-2);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							tc.addAttribute(TimexNames.mod, TimexNames.START);
						}

						else if (preflag == 3) {

							start = start.minusDays(-4);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.mod, TimexNames.END);

						}
						return tc;
					}

					else if (temp2.equals("weekend")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 6);
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = start.minusDays(-1);
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String weekIdx = String.valueOf(finish.getWeekOfWeekyear());
						weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
						tc.addAttribute(TimexNames.value, String.valueOf(year) + "-W" + weekIdx + "-WE");
						return tc;

					} else if (temp2.equals("mon") || temp2.equals("monday")) {

						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 1);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String tense = temporalPhrase.getTense();
						if (tense.equals("past") && dayweek<1) {
							finish = finish.minusDays(7);
						}
						else if (!tense.equals("past") && dayweek>1) {
							finish = finish.minusDays(-7);
						}
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("tues") || temp2.equals("tuesday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 2);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String tense = temporalPhrase.getTense();
						if (tense.equals("past") && dayweek<2) {
							finish = finish.minusDays(7);
						}
						else if (!tense.equals("past") && dayweek>2) {
							finish = finish.minusDays(-7);
						}
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("wed") || temp2.equals("wednesday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 3);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String tense = temporalPhrase.getTense();
						if (tense.equals("past") && dayweek<3) {
							finish = finish.minusDays(7);
						}
						else if (!tense.equals("past") && dayweek>3) {
							finish = finish.minusDays(-7);
						}
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("thur") || temp2.equals("thursday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 4);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String tense = temporalPhrase.getTense();
						if (tense.equals("past") && dayweek<4) {
							finish = finish.minusDays(7);
						}
						else if (!tense.equals("past") && dayweek>4) {
							finish = finish.minusDays(-7);
						}
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("fri") || temp2.equals("friday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 5);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String tense = temporalPhrase.getTense();
						if (tense.equals("past") && dayweek<5) {
							finish = finish.minusDays(7);
						}
						else if (!tense.equals("past") && dayweek>5) {
							finish = finish.minusDays(-7);
						}
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("sat") || temp2.equals("saturday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 6);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String tense = temporalPhrase.getTense();
						if (tense.equals("past") && dayweek<6) {
							finish = finish.minusDays(7);
						}
						else if (!tense.equals("past") && dayweek>6) {
							finish = finish.minusDays(-7);
						}
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("sun") || temp2.equals("sunday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 7);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String tense = temporalPhrase.getTense();
						if (tense.equals("past") && dayweek<7) {
							finish = finish.minusDays(7);
						}
						else if (!tense.equals("past") && dayweek>7) {
							finish = finish.minusDays(-7);
						}
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("morning")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 7, 0, 0, 0);
						finish = new DateTime(year, month, day, 10, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.MORNING);
						return tc;
					}

					else if (temp2.equals("noon")) {

						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 11, 0, 0, 0);
						finish = new DateTime(year, month, day, 13, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.NOON);
						return tc;
					}

					else if (temp2.equals("afternoon")) {

						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 14, 0, 0, 0);
						finish = new DateTime(year, month, day, 17, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.AFTERNOON);
						return tc;
					}

					else if (temp2.equals("evening")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 18, 0, 0, 0);
						finish = new DateTime(year, month, day, 20, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.EVENING);
						return tc;
					}

					else if (temp2.equals("night")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 21, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish) + TimexNames.NIGHT);
						return tc;
					}

					else if (temp2.equals("spring")) {
						year = start.getYear();
						start = new DateTime(year, 3, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 5, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-" + TimexNames.SPRING);
						return tc;
					}

					else if (temp2.equals("summer")) {
						year = start.getYear();
						start = new DateTime(year, 6, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 8, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-" + TimexNames.SUMMER);
						return tc;
					}

					else if (temp2.equals("fall")) {
						year = start.getYear();
						start = new DateTime(year, 9, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 11, 30, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-" + TimexNames.FALL);
						return tc;
					}

					else if (temp2.equals("winter")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						start = new DateTime(year, 12, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 1, 2, 28, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, yFmt.print(finish) + "-" + TimexNames.WINTER);
						return tc;
					}

					else {
						String patternStr1 = monther;
						Pattern pattern1 = Pattern.compile(patternStr1);
						Matcher matcher1 = pattern1.matcher(temp2);
						boolean matchFound1 = matcher1.find();
						if (matchFound1) {
							month = Integer.parseInt(temp.hm.get(temp2));
							year = start.getYear();
							int referenceMonth = start.getMonthOfYear();
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							String tense = temporalPhrase.getTense();
							if (tense.equals("past") && referenceMonth<month) {
								finish = finish.minusYears(1);
							}
							else if (!tense.equals("past") && referenceMonth>month) {
								finish = finish.minusYears(-1);
							}
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));

							if (preflag == 2) {
								finish = new DateTime(year, month, 10, 23, 59,
										59, 59);
								start = new DateTime(year, month, 1, 0, 0, 0, 0);
								tc.addAttribute(TimexNames.mod, TimexNames.START);
							}

							else if (preflag == 3) {
								start = new DateTime(year, month, 20, 0, 0, 0,
										0);
								tc.addAttribute(TimexNames.mod, TimexNames.END);
							}
							return tc;
						}

						else {
							return null;
						}

					}
				}

				else if (temp1.equals("next") || temp1.equals("upcoming")
						|| temp1.equals("following")
						|| temp1.equals("tomorrow")) {
					tc.addAttribute(TimexNames.type, TimexNames.DATE);
					if (temp2.equals("year")) {
						finish = start.minusYears(-1);
						year = finish.getYear();
						if (preflag == 1) {
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
							return tc;
						}

						else if (preflag == 2) {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 3, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
							tc.addAttribute(TimexNames.mod, TimexNames.START);
							return tc;

						}

						else if (preflag == 3) {
							start = new DateTime(year, 10, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));
							tc.addAttribute(TimexNames.mod, TimexNames.END);
							return tc;
						} else {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, yFmt.print(finish));
							return tc;
						}
					}

					else if (temp2.equals("month")) {
						finish = start.minusMonths(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						finish = finish.dayOfMonth().withMaximumValue();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, ymFmt.print(finish));
						if (preflag == 1) {
							finish = new DateTime(year, month, prematchDay, 23, 59, 59, 59);
							tc.addAttribute(TimexNames.value, fmt.print(finish));
						}
						else if (preflag == 2) {
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							finish = new DateTime(year, month, 10, 23, 59, 59,
									59);
							tc.addAttribute(TimexNames.mod, TimexNames.START);
						}

						else if (preflag == 3) {
							start = new DateTime(year, month, day - 10, 0, 0,
									0, 0);
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							tc.addAttribute(TimexNames.mod, TimexNames.END);
						}
						return tc;
					}

					else if (temp2.equals("week")) {
						finish = start.minusWeeks(-1);
						dayweek = finish.getDayOfWeek();
						finish = finish.minusDays(dayweek - 1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = start.minusDays(-6);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String weekIdx = String.valueOf(finish.getWeekOfWeekyear());
						weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
						tc.addAttribute(TimexNames.value, String.valueOf(year)+"-W"+weekIdx);
						if (preflag == 1) {
							weekIdx = String.valueOf(finish.getWeekOfWeekyear());
							weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
							tc.addAttribute(TimexNames.value,
									String.valueOf(year)+"-W"+weekIdx + "-" + String.valueOf(prematchDay));
						}
						else if (preflag == 2) {
							finish = start.minusDays(-2);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							tc.addAttribute(TimexNames.mod, TimexNames.START);
						}

						else if (preflag == 3) {

							start = start.minusDays(-4);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.mod, TimexNames.END);

						}

						return tc;
					}

					else if (temp2.equals("weekend")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 13);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = start.minusDays(-1);
						day = finish.getDayOfMonth();
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						String weekIdx = String.valueOf(finish.getWeekOfWeekyear());
						weekIdx = weekIdx.length()==1?"0"+weekIdx:weekIdx;
						tc.addAttribute(TimexNames.value, String.valueOf(year)+"-W"
								+weekIdx+"-WE");
						return tc;
					} else if (temp2.equals("hour")) {
						finish = start.minusHours(-1);
						tc.addAttribute(TimexNames.value, TimexNames.FUTURE_REF);
						return tc;
					}

					else if (temp2.equals("minute")) {
						finish = start.minusMinutes(-1);
						tc.addAttribute(TimexNames.value, TimexNames.FUTURE_REF);
						return tc;
					}

					else if (temp2.equals("second")) {
						finish = start.minusSeconds(-1);
						tc.addAttribute(TimexNames.value, TimexNames.FUTURE_REF);
						return tc;
					}

					else if (temp2.equals("century")) {
						finish = start.minusYears(-100);
						year = finish.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()));
						return tc;
					}
					else if (temp2.equals("decade")) {
						finish = start.minusYears(-10);
						year = finish.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 9, 12, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.DATE);
						tc.addAttribute(TimexNames.value, String.valueOf(finish.getCenturyOfEra()*10));
						return tc;
					}
					else if (temp2.equals("day")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						return tc;
					}

					else if (temp2.equals("mon") || temp2.equals("monday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 8);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("tues") || temp2.equals("tuesday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 9);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("wed") || temp2.equals("wednesday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 10);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("thur") || temp2.equals("thursday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 11);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("fri") || temp2.equals("friday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 12);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("sat") || temp2.equals("saturday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 13);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("sun") || temp2.equals("sunday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 14);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish));
						if (preflag == 1) {
							tc.addAttribute(TimexNames.value,
									tc.getAttribute(TimexNames.value) + "T"+thisTimeOfDay);
						}
						return tc;
					}

					else if (temp2.equals("morning")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 7, 0, 0, 0);
						finish = new DateTime(year, month, day, 10, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ TimexNames.MORNING);
						return tc;
					}

					else if (temp2.equals("noon")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 11, 0, 0, 0);
						finish = new DateTime(year, month, day, 13, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ TimexNames.NOON);
						return tc;
					}

					else if (temp2.equals("afternoon")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 14, 0, 0, 0);
						finish = new DateTime(year, month, day, 17, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ TimexNames.AFTERNOON);
						return tc;
					}

					else if (temp2.equals("evening")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 18, 0, 0, 0);
						finish = new DateTime(year, month, day, 20, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ TimexNames.EVENING);
						return tc;
					}

					else if (temp2.equals("night")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 21, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.type, TimexNames.TIME);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ TimexNames.NIGHT);
						return tc;
					}

					else if (temp2.equals("spring")) {
						finish = start.minusYears(-1);
						year = finish.getYear();
						start = new DateTime(year, 3, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 5, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ "-" + TimexNames.SPRING);
						return tc;
					}

					else if (temp2.equals("summer")) {
						finish = start.minusYears(-1);
						year = finish.getYear();
						start = new DateTime(year, 6, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 8, 31, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ "-" + TimexNames.MORNING);
						return tc;
					}

					else if (temp2.equals("fall")) {
						finish = start.minusYears(-1);
						year = finish.getYear();
						start = new DateTime(year, 9, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 11, 30, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ "-" + TimexNames.FALL);
						return tc;
					}

					else if (temp2.equals("winter")) {
						year = start.getYear();
						start = new DateTime(year, 12, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 1, 2, 28, 23, 59, 59, 59);
						tc.addAttribute(TimexNames.value, fmt.print(finish)+ "-" + TimexNames.WINTER);
						return tc;
					}

					else {
						String patternStr1 = monther;
						Pattern pattern1 = Pattern.compile(patternStr1);
						Matcher matcher1 = pattern1.matcher(temp2);
						boolean matchFound1 = matcher1.find();
						if (matchFound1) {
							month = Integer.parseInt(temp.hm.get(temp2));
							finish = start.minusYears(-1);
							year = finish.getYear();
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							tc.addAttribute(TimexNames.value, ymFmt.print(finish));

							if (preflag == 2) {
								finish = new DateTime(year, month, 10, 23, 59,
										59, 59);
								start = new DateTime(year, month, 1, 0, 0, 0, 0);
								tc.addAttribute(TimexNames.mod, TimexNames.START);
							}

							else if (preflag == 3) {
								start = new DateTime(year, month, 20, 0, 0, 0,
										0);
								tc.addAttribute(TimexNames.mod, TimexNames.END);
							}
							return tc;
						}

						else {
							return null;
						}

					}

				}

			}

		}
		return null;

	}

	public static void main(String args[]) {
		DateTime startTime = new DateTime(2001, 1, 14, 3, 3, 3, 3);
		String example = "this week";
		TimexChunk sample = ModifiedRule(startTime, new TemporalPhrase(example));
		System.out.println(sample.toTIMEXString());
	}
}
