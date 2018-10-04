/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by zhilifeng on 5/28/17.
 */
public class KnowledgeBase {
    public static List<String> timeUnitSet = new ArrayList<String>() {{
        add("seconds");
        add("minutes");
        add("hours");

        add("second");
        add("minute");
        add("hour");
    }};
    public static List<String> dateUnitSet = new ArrayList<String>() {{
        add("days");
        add("weeks");
        add("weekends");
        add("months");
        add("centuries");
        add("decades");

        add("day");
        add("week");
        add("weekend");
        add("month");
        add("year");
        add("years");
        add("century");
        add("decade");
    }};

    public static HashMap<String, String> hierarchy = new HashMap<String, String>(){{
        put("century", "decade");
        put("centuries", "decade");
        put("decades", "year");
        put("decade", "year");
        put("years", "month");
        put("year", "month");
        put("months", "day");
        put("month", "day");
        put("weeks", "day");
        put("week", "days");
        put("days", "hour");
        put("day", "hour");
        put("hours", "minute");
        put("hour", "minute");
        put("minute", "second");
        put("minutes", "second");

    }};

    public static HashMap<String, Integer> base = new HashMap<String, Integer>(){{
        put("century", 100);
        put("centuries", 100);
        put("decades", 10);
        put("decade", 10);
        put("years", 10);
        put("year", 10);
        put("months", 12);
        put("month", 12);
        put("weeks", 4);
        put("week", 4);
        put("days", 15);
        put("day", 15);
        put("hours", 24);
        put("hour", 24);
        put("minute", 60);
        put("minutes", 60);
        put("second", 60);
        put("seconds", 60);
    }};

    public static HashMap<String, String> unitMap = new HashMap<String, String>() {{
        put("day", "D");
        put("days", "D");
        put("week", "W");
        put("weeks", "W");
        put("month", "M");
        put("months", "M");
        put("year", "Y");
        put("years", "Y");
        put("quarter", "Q");
        put("quarters", "Q");
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

    }};

//
//    public static Set<String> freqSet = new HashSet<String>() {{
//        add()
//    }}
    public static Set<String> timeOfDaySet = new HashSet<String>() {{
        add("morning");
        add("noon");
        add("afternoon");
        add("evening");
        add("night");
    }};

    public static Set<String> setAdvSet = new HashSet<String>() {{
        add("everyday");
        add("weekly");
        add("biweekly");
        add("yearly");
        add("daily");
        add("anually");
        add("monthly");
    }};

    public static HashMap<String, Integer> freqMap = new HashMap<String, Integer>(){{
        put("once", 1);
        put("twice", 2);
        put("thrice", 3);
    }};


    public static String timePatternStr = "\\s*(\\d+)\\s*(:\\d+)?\\s*(:\\d+)?\\s*(am|a.m.|a.m|pm|p.m.|p.m)?";

    public static List<String> weekdaySet = new ArrayList<String>() {{
        add("monday");
        add("tuesday");
        add("wednesday");
        add("thursday");
        add("friday");
        add("saturday");
        add("sunday");
        add("mon");
        add("tue");
        add("wed");
        add("thur");
        add("fri");
        add("sat");
        add("sun");
    }};

    public static List<String> monthSet = new ArrayList<String>(){{
        add("january");
        add("february");
        add("march");
        add("april");
        add("june");
        add("august");
        add("september");
        add("october");
        add("november");
        add("december");

        add("jan");
        add("feb");
        add("mar");
        add("apr");
        add("may");
        add("jun");
        add("july");
        add("jul");
        add("aug");
        add("sept");
        add("oct");
        add("nov");
        add("dec");
    }};

    public static List<String> seasonSet = new ArrayList<String>() {{
        add("springs");
        add("summers");
        add("falls");
        add("autumns");
        add("winters");

        add("spring");
        add("summer");
        add("fall");
        add("autumn");
        add("winter");
    }};

    public static Set<String> modIndicatorSet = new HashSet<String>() {{
        add("early");
        add("earlier");
        add("late");
        add("later");
        add("begin");
        add("beginning");
    }};

    public static Set<String> shiftIndicatorSet = new HashSet<String>() {{
        add("last");
        add("next");
        add("upcoming");
        add("past");
        add("this");
        add("following");
        add("previous");
    }};

    public static Set<String> specialDayTermSet = new HashSet<String>() {{
        add("yesterday");
        add("today");
        add("tomorrow");
    }};

    public static Set<String> positionTermSet = new HashSet<String>() {{
        add("since");
        add("additional");
        add("before");
        add("after");
        add("in");
        add("during");
        add("prior to");
        add("preceding");
        add("from");
        add("these");
        add("recent");
        add("over");
    }};
    public static String positionTerm = StringUtils.join(positionTermSet, "|");

    public static String specialDayTerm = StringUtils.join(specialDayTermSet, "|");
    public static String shiftIndicator = StringUtils.join(shiftIndicatorSet, "|");
    public static String modIndicator = StringUtils.join(modIndicatorSet, "|");
    public static String freq = "\\s*(once|twice|thrice|\\d{1} time(?:s)?|\\d{1} day(?:s)?|\\d{1} week(?:s)?" +
            "|\\d{1} month(?:s)?|\\d{1} year(?:s)?)\\s*";

    public static String quant = "\\s*(every|each|per)\\s*";

    public static String monther = StringUtils.join(monthSet, "|");
    public static String weekday = StringUtils.join(weekdaySet, "|");
    public static String season = StringUtils.join(seasonSet, "|");
    public static String unit = "\\s*(" + StringUtils.join(timeOfDaySet, "|") + "|" +
            StringUtils.join(dateUnitSet, "|") + "|" + monther + "|" + weekday + ")\\s*";
    public static String adverb = "\\s*(?:" + StringUtils.join(setAdvSet, "|") + ")\\s*";

    public static String timeofDay =  StringUtils.join(timeOfDaySet, "|");
    public static String dateUnit = StringUtils.join(dateUnitSet, "|");
    public static String timeUnit = StringUtils.join(timeUnitSet, "|");

    public static String number = "(?:twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|fourty|fifth|sixty|seventy|eighty|ninety|hundred|thousand|million|billion|an|a|one|two|three|four|five|six|seven|eight|nine|ten|eleven|this)";
    public static String quantifier = "(?:several|few|a few|bunch|a bunch|set|a set)";
    public static void main(String[] args) {
        System.out.println(KnowledgeBase.weekday);
    }
}
