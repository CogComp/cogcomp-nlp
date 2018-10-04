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

import static edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.KnowledgeBase.*;

/**
 * Created by zhilifeng on 3/21/17.
 * This class normalizes TIMEX3 SET: "every day", "annually", "daily", etc.
 */


public class SetRule {

    /**
     *
     * @param freqStr
     * @return
     */
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

    /**
     * Convert a frequency to timex3 format
     * @param numStr how often an event happens (3)
     * @param unitStr the unit of the above frequency (days a week)
     * @return
     */
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
            if (timeUnitSet.contains(unitStr))
                res = "PT"+numNum+timexUnit;
            else
                res = "P"+numNum+timexUnit;
        }

        String weekdayPatternStr = "(" + weekday + ")";
        Pattern weekdayPattern = Pattern.compile(weekdayPatternStr);
        Matcher weekdayPatternMatcher = weekdayPattern.matcher(unitStr);
        Boolean weekdayPatternMatchFound = weekdayPatternMatcher.find();
        if (weekdayPatternMatchFound) {
            DateMapping weekdayMap = DateMapping.getInstance();
            res = "XXXX-WXX-"+weekdayMap.hm.get(weekdayPatternMatcher.group(1));
        }

        String monthPatternStr = "(" + monther + ")";
        Pattern monthPattern = Pattern.compile(monthPatternStr);
        Matcher monthPatternMatcher = monthPattern.matcher(unitStr);
        Boolean monthPatternMatchFound = monthPatternMatcher.find();
        if (monthPatternMatchFound) {
            DateMapping monthMap = DateMapping.getInstance();
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

    /**
     * This function normalizes special recurrent adverbs like weekly
     * @param phrase
     * @return
     */
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

    /**
     * This function converts a recurrent time: every day to timex3 format
     * @param temporalPhrase
     * @return
     */
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
        // This pattern captures any Phrase with (freq)(number)(unit): once a day
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
        String test = "every morning";
        TimexChunk tc = SetRule.SetRule(new TemporalPhrase(test)) ;
        System.out.println(tc.toTIMEXString());
    }

}
