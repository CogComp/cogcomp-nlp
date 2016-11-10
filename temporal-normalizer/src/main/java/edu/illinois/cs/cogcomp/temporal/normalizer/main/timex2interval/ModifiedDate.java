package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Interval;


//Define the time line
//Morning 7:00-11:00 Noon 11:00-2:00 Afternoon 2:00-6:00 Evening 6:00-9:00 Night 9:00-12:00
public class ModifiedDate {

	public static Interval ModifiedRule(DateTime start, String phrase) {
		phrase = phrase.trim();
		// System.out.println(phrase);

		// ###################################################preprocess
		// 1################################################
		String preprocess = "\\A(morning|afternoon|noon|evening|night|early|earlier|later|late)\\s*(mon(?:day)?|tues(?:day)?|wed(?:nesday)?|thur(?:sday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)\\Z";
		Pattern pattern11 = Pattern.compile(preprocess);
		Matcher matcher11 = pattern11.matcher(phrase);
		boolean matcher11Found = matcher11.find();

		if (matcher11Found) {
			String secondphrase = new String();
			// split the phrase into two parts
			String firstphrase = "this" + matcher11.group(2);
			if (matcher11.group(1).contains("early")
					|| matcher11.group(1).contains("earlier")) {

				secondphrase = "this morning";
			}

			else if (matcher11.group(1).contains("late")
					|| matcher11.group(1).contains("later")) {
				secondphrase = "this afternoon";
			}

			else {
				secondphrase = "this" + matcher11.group(1);
			}

			Interval keypoint = ModifiedDate.ModifiedRule(start, firstphrase);
			DateTime starttime = keypoint.getStart();
			Interval result = ModifiedDate
					.ModifiedRule(starttime, secondphrase);
			return result;

		}

		// ###################################################preprocess
		// 2################################################

		String ownprocess = "\\A(mon(?:day)?|tues(?:day)?|wed(?:nesday)?|thur(?:sday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)\\s*(morning|afternoon|noon|evening|night)\\Z";
		Pattern pattern2 = Pattern.compile(ownprocess);
		Matcher matcher2 = pattern2.matcher(phrase);
		boolean matcher2Found = matcher2.find();
		if (matcher2Found) {
			// split the phrase into two parts
			String firstphrase = "this" + matcher2.group(1);
			String secondphrase = "this" + matcher2.group(2);
			Interval keypoint = ModifiedDate.ModifiedRule(start, firstphrase);
			DateTime starttime = keypoint.getStart();
			Interval result = ModifiedDate
					.ModifiedRule(starttime, secondphrase);
			return result;

		}

		String monther = "(?:jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may?|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sept(?:ember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?|year)";
		String specialterm = "\\A(?:morning|afternoon|noon|evening|night|spring|fall|summer|winter|mon(?:day)?|tues(?:day)?|wed(?:nesday)?|thur(?:sday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)\\Z";
		Pattern onepattern = Pattern.compile(specialterm);

		Matcher onematcher = onepattern.matcher(phrase);
		boolean onematchFound = onematcher.find();
		if (onematchFound) {
			phrase = "this " + phrase;
		}

		int numterm;
		DateMapping temp = new DateMapping();
		int i;
		int dayweek;
		int year;
		int month = 1;
		int day;
		int flagbefore = -1;
		int flagafter = -1;
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
		String spepatternStr = "(morning|afternoon|noon|evening|night|spring|fall|summer|winter|mon(?:day)?|tues(?:day)?|wed(?:nesday)?|thur(?:sday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)\\s*(last|past|next|upcoming|this|following|previous)\\s*(week|month|year)";
		Pattern spepattern = Pattern.compile(spepatternStr);
		Matcher spematcher = spepattern.matcher(phrase);
		boolean spematchFound = spematcher.find();
		if (spematchFound) {
			String group1 = spematcher.group(1);
			String group2 = spematcher.group(2);
			// String group3 = spematcher.group(3);

			phrase = group2 + " " + group1;

		}
		String patternStr = "\\s*(last|past|next|upcoming|this|following|previous|yesterday|tomorrow|today|early|late|earlier|later)\\s*(morning|afternoon|noon|evening|night|month|mon(?:day)?|tues(?:day)?|wed(?:nesday)?|thur(?:sday)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?|weekend|week|century|year|day|hour|minute|second|spring|fall|summer|winter|"
				+ monther + ")";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(phrase);
		boolean matchFound = matcher.find();
		if (matchFound) {
			// System.out.println(matcher.group(1));
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
				// System.out.println(matcher.group(0));
				// group(0) returns the whole detected string which matches the
				// pattern
				phrase = phrase.replaceAll(matcher.group(0), "");
				phrase = phrase.trim();
				// System.out.println("lalal"+phrase);
				String prepatternStr = monther;
				Pattern prepattern = Pattern.compile(prepatternStr);
				Matcher prematcher = prepattern.matcher(phrase);
				boolean prematchFound = prematcher.find();
				if (prematchFound) {
					month = Integer.parseInt(temp.hm.get(phrase));

					preflag = 1;
				}

				else {
					String backPatt = "(early|earlier)";

					Pattern backPa = Pattern.compile(backPatt);
					Matcher backmatcher = backPa.matcher(phrase);
					boolean backFound = backmatcher.find();

					if (backFound) {
						// 2 means early
						preflag = 2;
					}

					else {
						// System.out.print("hello" + phrase);
						String backPatter = "(late|later)";
						Pattern backPater = Pattern.compile(backPatter);
						Matcher backmatch = backPater.matcher(phrase);
						boolean backFounder = backmatch.find();
						// System.out.print(backFounder);
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
								start = new DateTime(0000, 1, 1, 0, 0, 0, 0);
								interval = new Interval(start, finish);
								return interval;
							}

							else if (flagafter == 1) {
								finish = new DateTime(year, month, 1, 0, 0, 0,
										0);
								finish = finish.dayOfMonth().withMaximumValue();
								finish = finish.minusDays(-1);
								start = new DateTime(9999, 12, 31, 23, 59, 59,
										59);
								interval = new Interval(finish, start);
								return interval;
							}
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							interval = new Interval(start, finish);
							return interval;
						}

						else if (preflag == 2) {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 3, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;
						}

						else if (preflag == 3) {
							start = new DateTime(year, 10, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;
						}

						else {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;
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
						if (preflag == 2) {
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							finish = new DateTime(year, month, 10, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}

						else if (preflag == 3) {
							start = new DateTime(year, month, day - 10, 0, 0,
									0, 0);
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}
						return interval;
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
						if (preflag == 2) {
							finish = start.minusDays(-2);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}

						else if (preflag == 3) {

							start = start.minusDays(-4);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							interval = new Interval(start, finish);

						}
						return interval;
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
						interval = new Interval(start, finish);
						return interval;
					} else if (temp2.equals("hour")) {
						finish = start.minusHours(1);
						interval = new Interval(finish, start);
						return interval;
					}

					else if (temp2.equals("minute")) {
						finish = start.minusMinutes(1);
						interval = new Interval(finish, start);
						return interval;
					}

					else if (temp2.equals("second")) {
						finish = start.minusSeconds(1);
						interval = new Interval(finish, start);
						return interval;
					}

					else if (temp2.equals("century")) {
						finish = start.minusYears(100);
						year = finish.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("mon") || temp2.equals("monday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 6);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("tues") || temp2.equals("tuesday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 5);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("wed") || temp2.equals("wednesday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 4);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("thur") || temp2.equals("thursday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 3);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("fri") || temp2.equals("friday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 2);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("sat") || temp2.equals("saturday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek + 1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("sun") || temp2.equals("sunday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("morning")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 7, 0, 0, 0);
						finish = new DateTime(year, month, day, 10, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("noon")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 11, 0, 0, 0);
						finish = new DateTime(year, month, day, 13, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("afternoon")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 14, 0, 0, 0);
						finish = new DateTime(year, month, day, 17, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("evening")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 18, 0, 0, 0);
						finish = new DateTime(year, month, day, 20, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("night")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 21, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("day")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("spring")) {
						finish = start.minusYears(1);
						year = finish.getYear();
						start = new DateTime(year, 3, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 5, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("summer")) {
						finish = start.minusYears(1);
						year = finish.getYear();
						start = new DateTime(year, 6, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 8, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("fall")) {
						finish = start.minusYears(1);
						year = finish.getYear();
						start = new DateTime(year, 9, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 11, 30, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("winter")) {
						finish = start.minusYears(2);
						year = finish.getYear();
						start = new DateTime(year, 12, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 1, 2, 28, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

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
							interval = new Interval(start, finish);
							if (preflag == 2) {
								finish = new DateTime(year, month, 10, 23, 59,
										59, 59);
								start = new DateTime(year, month, 1, 0, 0, 0, 0);
								interval = new Interval(start, finish);
							}

							else if (preflag == 3) {
								start = new DateTime(year, month, 20, 0, 0, 0,
										0);
								interval = new Interval(start, finish);
							}
							return interval;
						} else {
							return null;
						}

					}
				}

				else if (temp1.equals("early") || temp1.equals("earlier")) {

					if (temp2.equals("century")) {

						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 20, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("year")) {
						year = start.getYear();

						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 3, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;

					}

					else if (temp2.equals("month")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						finish = start.dayOfMonth().withMaximumValue();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, 10, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
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
						interval = new Interval(start, finish);
						return interval;
					}

					else {
						String patternStr1 = monther;
						Pattern pattern1 = Pattern.compile(patternStr1);
						Matcher matcher1 = pattern1.matcher(temp2);
						boolean matchFound1 = matcher1.find();
						if (matchFound1) {
							month = Integer.parseInt(temp.hm.get(temp2));
							year = start.getYear();
							finish = new DateTime(year, month, 10, 23, 59, 59,
									59);
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							interval = new Interval(start, finish);
							return interval;
						} else {
							return null;
						}

					}

				}

				else if (temp1.equals("late") || temp1.equals("later")) {
					if (temp2.equals("century")) {

						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year + 80, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("year")) {
						year = start.getYear();

						start = new DateTime(year, 10, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;

					}

					else if (temp2.equals("month")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						finish = start.dayOfMonth().withMaximumValue();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day - 10, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else {
						String patternStr1 = monther;
						Pattern pattern1 = Pattern.compile(patternStr1);
						Matcher matcher1 = pattern1.matcher(temp2);
						boolean matchFound1 = matcher1.find();
						if (matchFound1) {
							month = Integer.parseInt(temp.hm.get(temp2));
							year = start.getYear();
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month,
									finish.getDayOfMonth() - 10, 0, 0, 0, 0);
							interval = new Interval(start, finish);
							return interval;
						} else {
							return null;
						}

					}
				}

				else if (temp1.equals("this") || temp1.equals("today")) {

					if (temp2.equals("century")) {

						year = start.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("year")) {
						year = start.getYear();
						if (preflag == 1) {
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							interval = new Interval(start, finish);
							return interval;
						}

						else if (preflag == 2) {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 3, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;
						}

						else if (preflag == 3) {
							start = new DateTime(year, 10, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;
						} else {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;
						}
					}

					else if (temp2.equals("month")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						finish = start.dayOfMonth().withMaximumValue();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, 1, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);

						if (preflag == 2) {
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							finish = new DateTime(year, month, 10, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}

						else if (preflag == 3) {
							start = new DateTime(year, month, day - 10, 0, 0,
									0, 0);
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}
						return interval;
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
						interval = new Interval(start, finish);
						if (preflag == 2) {
							finish = start.minusDays(-2);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}

						else if (preflag == 3) {

							start = start.minusDays(-4);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							interval = new Interval(start, finish);

						}
						return interval;
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
						interval = new Interval(start, finish);
						return interval;

					} else if (temp2.equals("mon") || temp2.equals("monday")) {

						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 1);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("tues") || temp2.equals("tuesday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 2);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("wed") || temp2.equals("wednesday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 3);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("thur") || temp2.equals("thursday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 4);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("fri") || temp2.equals("friday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 5);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("sat") || temp2.equals("saturday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 6);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("sun") || temp2.equals("sunday")) {
						dayweek = start.getDayOfWeek();
						start = start.minusDays(dayweek - 7);
						day = start.getDayOfMonth();
						year = start.getYear();
						month = start.getMonthOfYear();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("morning")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 7, 0, 0, 0);
						finish = new DateTime(year, month, day, 10, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("noon")) {

						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 11, 0, 0, 0);
						finish = new DateTime(year, month, day, 13, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("afternoon")) {

						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 14, 0, 0, 0);
						finish = new DateTime(year, month, day, 17, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("evening")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 18, 0, 0, 0);
						finish = new DateTime(year, month, day, 20, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("night")) {
						year = start.getYear();
						month = start.getMonthOfYear();
						day = start.getDayOfMonth();
						start = new DateTime(year, month, day, 21, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("spring")) {
						year = start.getYear();
						start = new DateTime(year, 3, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 5, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("summer")) {
						year = start.getYear();
						start = new DateTime(year, 6, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 8, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("fall")) {
						year = start.getYear();
						start = new DateTime(year, 9, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 11, 30, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("winter")) {
						finish = start.minusDays(1);
						year = finish.getYear();
						start = new DateTime(year, 12, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 1, 2, 28, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else {
						String patternStr1 = monther;
						Pattern pattern1 = Pattern.compile(patternStr1);
						Matcher matcher1 = pattern1.matcher(temp2);
						boolean matchFound1 = matcher1.find();
						if (matchFound1) {
							month = Integer.parseInt(temp.hm.get(temp2));
							year = start.getYear();
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							interval = new Interval(start, finish);
							if (preflag == 2) {
								finish = new DateTime(year, month, 10, 23, 59,
										59, 59);
								start = new DateTime(year, month, 1, 0, 0, 0, 0);
								interval = new Interval(start, finish);
							}

							else if (preflag == 3) {
								start = new DateTime(year, month, 20, 0, 0, 0,
										0);
								interval = new Interval(start, finish);
							}
							return interval;
						}

						else {
							return null;
						}

					}
				}

				else if (temp1.equals("next") || temp1.equals("upcoming")
						|| temp1.equals("following")
						|| temp1.equals("tomorrow")) {
					if (temp2.equals("year")) {
						finish = start.minusYears(-1);
						year = finish.getYear();
						if (preflag == 1) {
							finish = new DateTime(year, month, 1, 23, 59, 59,
									59);
							finish = finish.dayOfMonth().withMaximumValue();
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							interval = new Interval(start, finish);
							return interval;
						}

						else if (preflag == 2) {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 3, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;

						}

						else if (preflag == 3) {
							start = new DateTime(year, 10, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;
						} else {
							start = new DateTime(year, 1, 1, 0, 0, 0, 0);
							finish = new DateTime(year, 12, 31, 23, 59, 59, 59);
							interval = new Interval(start, finish);
							return interval;
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
						interval = new Interval(start, finish);
						if (preflag == 2) {
							start = new DateTime(year, month, 1, 0, 0, 0, 0);
							finish = new DateTime(year, month, 10, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}

						else if (preflag == 3) {
							start = new DateTime(year, month, day - 10, 0, 0,
									0, 0);
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}
						return interval;
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
						interval = new Interval(start, finish);
						if (preflag == 2) {
							finish = start.minusDays(-2);
							year = finish.getYear();
							month = finish.getMonthOfYear();
							day = finish.getDayOfMonth();
							finish = new DateTime(year, month, day, 23, 59, 59,
									59);
							interval = new Interval(start, finish);
						}

						else if (preflag == 3) {

							start = start.minusDays(-4);
							year = start.getYear();
							month = start.getMonthOfYear();
							day = start.getDayOfMonth();
							start = new DateTime(year, month, day, 0, 0, 0, 0);
							interval = new Interval(start, finish);

						}

						return interval;
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
						interval = new Interval(start, finish);
						return interval;
					} else if (temp2.equals("hour")) {
						finish = start.minusHours(-1);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("minute")) {
						finish = start.minusMinutes(-1);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("second")) {
						finish = start.minusSeconds(-1);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("century")) {
						finish = start.minusYears(-100);
						year = finish.getCenturyOfEra();
						year = year * 100;
						start = new DateTime(year, 1, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 99, 12, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("day")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("mon") || temp2.equals("monday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 8);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("tues") || temp2.equals("tuesday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 9);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("wed") || temp2.equals("wednesday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 10);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("thur") || temp2.equals("thursday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 11);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("fri") || temp2.equals("friday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 12);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("sat") || temp2.equals("saturday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 13);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("sun") || temp2.equals("sunday")) {
						dayweek = start.getDayOfWeek();
						finish = start.minusDays(dayweek - 14);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 0, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("morning")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 7, 0, 0, 0);
						finish = new DateTime(year, month, day, 10, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("noon")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 11, 0, 0, 0);
						finish = new DateTime(year, month, day, 13, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("afternoon")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 14, 0, 0, 0);
						finish = new DateTime(year, month, day, 17, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("evening")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 18, 0, 0, 0);
						finish = new DateTime(year, month, day, 20, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("night")) {
						finish = start.minusDays(-1);
						year = finish.getYear();
						month = finish.getMonthOfYear();
						day = finish.getDayOfMonth();
						start = new DateTime(year, month, day, 21, 0, 0, 0);
						finish = new DateTime(year, month, day, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("spring")) {
						finish = start.minusYears(-1);
						year = finish.getYear();
						start = new DateTime(year, 3, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 5, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("summer")) {
						finish = start.minusYears(-1);
						year = finish.getYear();
						start = new DateTime(year, 6, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 8, 31, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("fall")) {
						finish = start.minusYears(-1);
						year = finish.getYear();
						start = new DateTime(year, 9, 1, 0, 0, 0, 0);
						finish = new DateTime(year, 11, 30, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
					}

					else if (temp2.equals("winter")) {
						year = start.getYear();
						start = new DateTime(year, 12, 1, 0, 0, 0, 0);
						finish = new DateTime(year + 1, 2, 28, 23, 59, 59, 59);
						interval = new Interval(start, finish);
						return interval;
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
							interval = new Interval(start, finish);

							if (preflag == 2) {
								finish = new DateTime(year, month, 10, 23, 59,
										59, 59);
								start = new DateTime(year, month, 1, 0, 0, 0, 0);
								interval = new Interval(start, finish);
							}

							else if (preflag == 3) {
								start = new DateTime(year, month, 20, 0, 0, 0,
										0);
								interval = new Interval(start, finish);
							}
							return interval;
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
		DateTime startTime = new DateTime(2001, 3, 14, 3, 3, 3, 3);
		String example = "early September";
		Interval sample = ModifiedRule(startTime, example);
	}
}
