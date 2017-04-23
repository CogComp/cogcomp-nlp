package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

/**
 * Created by zhilifeng on 3/21/17.
 * This class normalizes TIMEX3 SET: "every day", "annually", "daily", etc.
 */
// TODO: every monday: <TIMEX3 tid="t5" type="SET" value="XXXX-WXX-1" quant="EVERY" freq="1W">every monday</TIMEX3> notice the freq


public class SetRule {
    public static String freq = "\\s*(once|twice|thrice|\\d{1} time(?:s)?|\\d{1} day(?:s)?|\\d{1} week(?:s)?" +
            "|\\d{1} month(?:s)?|\\d{1} year(?:s)?)\\s*";

    public static String quant = "\\s*(every|each|per)\\s*";
    public static String monther = "jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may?|jun(?:e)?|jul(?:y)?" +
            "|aug(?:ust)?|sept(?:ember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?";
    public static String weekday = "mon(?:day)?|tues(?:day)?|wed(?:nesday)?|thur(?:sday)?|fri(?:day)?" +
            "|sat(?:urday)?|sun(?:day)?";
    public static String unit = "\\s*(day(?:s)?|month(?:s)?|week(?:s)?|year(?:s)?|decade(?:s)?|century|centuries" +
            "|morning|noon|afternoon|evening|night|" + monther + "|" + weekday + ")\\s*";
    public static String adverb = "\\s*(?:everyday|weekly|biweekly|yearly|daily|annually|monthly)\\s*";

    public static String timeofDay = "(morning|noon|afternoon|evening|night)";
    public static HashMap<String, Integer> freqMap = new HashMap<String, Integer>(){
        {
            put("once", 1);
            put("twice", 2);
            put("thrice", 3);
        }
    };

    public static Set<String> timeSet = new HashSet<String>(){
        {
            add("minute");
            add("minutes");
            add("second");
            add("seconds");
            add("hour");
            add("hours");
        }
    };
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
            put("decades", "Y");
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

    public static String normalizeFreq(String freqStr) {
        int freqNum;
        String res = "";
        if (freqMap.containsKey(freqStr)) {
            freqNum = freqMap.get(freqStr);
            res = freqNum+"X";
        }
        else {
            freqStr = freqStr.trim();
            String []freqArr = freqStr.split(" ");

            freqNum = Integer.parseInt(freqArr[0]);
            String freqUnit = unitMap.get(freqArr[1]);
            freqUnit = freqUnit.equals("X")?"X":freqUnit.toLowerCase();
            res = freqNum + freqUnit;
        }
        return res;
    }

    public static String normalizeValue(String numStr, String unitStr){
        int numNum;
        String res = "";
        if (numStr.matches("a|an")) {
            numNum = 1;
        }
        else {
            numNum = Integer.parseInt(numStr);
        }

        if (unitMap.containsKey(unitStr)) {
            String timexUnit = unitMap.get(unitStr);
            if (timeSet.contains(unitStr))
                res = "PT"+numNum+timexUnit;
            else
                res = "P"+numNum+timexUnit;
        }

        String weekdayPatternStr = "(" + weekday + ")";
        Pattern weekdayPattern = Pattern.compile(weekdayPatternStr);
        Matcher weekdayPatternMatcher = weekdayPattern.matcher(unitStr);
        Boolean weekdayPatternMatchFound = weekdayPatternMatcher.find();
        if (weekdayPatternMatchFound) {
            DateMapping weekdayMap = new DateMapping();
            res = "XXXX-WXX-"+weekdayMap.hm.get(weekdayPatternMatcher.group(1));
        }

        String monthPatternStr = "(" + monther + ")";
        Pattern monthPattern = Pattern.compile(monthPatternStr);
        Matcher monthPatternMatcher = monthPattern.matcher(unitStr);
        Boolean monthPatternMatchFound = monthPatternMatcher.find();
        if (monthPatternMatchFound) {
            DateMapping monthMap = new DateMapping();
            String mapRes = monthMap.hm.get(monthPatternMatcher.group(1));
            mapRes = mapRes.length()==1?"0"+mapRes:mapRes;
            res = "XXXX-"+mapRes;
        }

        String todPatternStr = "(" + timeofDay + ")";
        Pattern todPattern = Pattern.compile(todPatternStr);
        Matcher todPatternMatcher = todPattern.matcher(unitStr);
        Boolean todPatternMatchFound = todPatternMatcher.find();
        if (todPatternMatchFound) {
            String mapRes = unitMap.get(todPatternMatcher.group(1));
            res = "XXXX-XX-XXT"+mapRes;
        }
        return res;
    }

    public static TimexChunk normalizeSpecialAdverb(String phrase) {
        TimexChunk tc = new TimexChunk();
        tc.addAttribute(TimexNames.type, TimexNames.SET);
        if (phrase.matches("everyday|daily")) {
            if (phrase.matches("daily")) {
                tc.addAttribute(TimexNames.quant, "EVERY");
            }
            tc.addAttribute(TimexNames.value, "P1D");
            tc.addAttribute(TimexNames.freq, "1X");
        }
        else if (phrase.matches("annually|yearly")) {
            tc.addAttribute(TimexNames.value, "XXXX");
            tc.addAttribute(TimexNames.freq, "1X");
        }
        else if (phrase.matches("monthly")) {
            tc.addAttribute(TimexNames.value, "XXXX-XX");
            tc.addAttribute(TimexNames.freq, "1X");
        }
        else if (phrase.matches("weekly")) {
            tc.addAttribute(TimexNames.value, "P1W");
            tc.addAttribute(TimexNames.freq, "1X");
        }
        else {
            return null;
        }
        return tc;
    }

    public static TimexChunk SetRule(TemporalPhrase temporalPhrase) {
        String phrase = temporalPhrase.getPhrase();
        phrase = phrase.toLowerCase();
        phrase = phrase.trim();
        int i;
        int numterm;


        TimexChunk adverbChunk = normalizeSpecialAdverb(phrase);
        if (adverbChunk!=null) {
            return adverbChunk;
        }
        // This pattern captures any phrase with (freq)(number)(unit): once a day
        String freqUnitPatternStr = freq + "(a|an|\\d{1})*" + unit;

        // Captures (freq)(quant)(number)(unit): once every two days
        String freqQuantUnitPatternStr = freq + quant + "(a|an|\\d{1})*" + unit;

        // Captures (quant)(number)(unit) : every (one) day
        String quantUnitPatternStr = quant + "(a|an|\\d{1})*" + unit;

        Pattern freqUnitPattern = Pattern.compile(freqUnitPatternStr);
        Matcher freqUnitMatcher = freqUnitPattern.matcher(phrase);

        TimexChunk tc = new TimexChunk();
        tc.addAttribute(TimexNames.type, TimexNames.SET);

        boolean freqUnitMatchFound = freqUnitMatcher.find();
        if (freqUnitMatchFound) {
            for ( i = 1; i <= 3; i++) {
                if (freqUnitMatcher.group(i) == null) {
                    i--;
                    break;
                }
            }
            if (i == 4) {
                i--;
            }
            numterm = i;
            if (numterm==3) {
                String freqStr = freqUnitMatcher.group(1);
                String numStr = freqUnitMatcher.group(2);
                String unitStr = freqUnitMatcher.group(3);

                tc.addAttribute(TimexNames.freq, SetRule.normalizeFreq(freqStr));
                tc.addAttribute(TimexNames.value, SetRule.normalizeValue(numStr, unitStr));

                return tc;

            }
        }




        Pattern freqQuantUnitPattern = Pattern.compile(freqQuantUnitPatternStr);
        Matcher freqQuantUnitMatcher = freqQuantUnitPattern.matcher(phrase);

        boolean freqQuantUnitMatchFound = freqQuantUnitMatcher.find();
        if (freqQuantUnitMatchFound) {
            for ( i = 1; i <= 4; i++) {
                if (freqQuantUnitMatcher.group(i) == null) {
                    i--;
                    break;
                }
            }
            if (i == 5) {
                i--;
            }
            numterm = i;
            if (numterm==4) {
                String freqStr = freqQuantUnitMatcher.group(1);
                String quantStr = freqQuantUnitMatcher.group(2);
                String numStr = freqQuantUnitMatcher.group(3);
                String unitStr = freqQuantUnitMatcher.group(4);

                tc.addAttribute(TimexNames.freq, SetRule.normalizeFreq(freqStr));
                tc.addAttribute(TimexNames.value, SetRule.normalizeValue(numStr, unitStr));
                tc.addAttribute(TimexNames.quant, quantStr.toUpperCase());
                return tc;
            }

            // e.g "(3 days)(each)(week)"
            if (numterm == 2 && freqQuantUnitMatcher.group(4)!=null) {
                String freqStr = freqQuantUnitMatcher.group(1);
                String quantStr = freqQuantUnitMatcher.group(2);
                String numStr = "1";
                String unitStr = freqQuantUnitMatcher.group(4);

                tc.addAttribute(TimexNames.freq, SetRule.normalizeFreq(freqStr));
                tc.addAttribute(TimexNames.value, SetRule.normalizeValue(numStr, unitStr));
                tc.addAttribute(TimexNames.quant, quantStr.toUpperCase());
                return tc;
            }
        }

        Pattern quantUnitPattern = Pattern.compile(quantUnitPatternStr);
        Matcher quantUnitMatcher = quantUnitPattern.matcher(phrase);
        boolean quantUnitMatchFound = quantUnitMatcher.find();
        if (quantUnitMatchFound) {
            for ( i = 1; i <= 3; i++) {
                if (quantUnitMatcher.group(i) == null) {
                    i--;
                    break;
                }
            }
            if (i == 4) {
                i--;
            }
            numterm = i;
            if (numterm==3) {
                String quantStr = quantUnitMatcher.group(1);
                String numStr = quantUnitMatcher.group(2);
                String unitStr = quantUnitMatcher.group(3);

                tc.addAttribute(TimexNames.value, SetRule.normalizeValue(numStr, unitStr));
                tc.addAttribute(TimexNames.quant, quantStr.toUpperCase());
                return tc;
            }

            if (numterm==1 && quantUnitMatcher.group(3)!=null) {
                String quantStr = quantUnitMatcher.group(1);
                String numStr = "1";
                String unitStr = quantUnitMatcher.group(3);

                tc.addAttribute(TimexNames.value, SetRule.normalizeValue(numStr, unitStr));
                tc.addAttribute(TimexNames.quant, quantStr.toUpperCase());
                return tc;
            }
        }
        return null;
    }

    public static void main (String[] args){
        String test = "everyday";
        TimexChunk tc = SetRule.SetRule(new TemporalPhrase(test)) ;
        System.out.println(tc.toTIMEXString());
    }

}
