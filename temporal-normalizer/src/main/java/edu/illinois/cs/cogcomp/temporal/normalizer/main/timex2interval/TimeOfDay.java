package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhilifeng on 3/22/17.
 */
public class TimeOfDay {
    public static String timePatternStr = "\\s*(\\d+)\\s*(:\\d+)?\\s*(:\\d+)?\\s*(am|a.m.|a.m|pm|p.m.|p.m)?";
    public static TimexChunk timeRule(DateTime start, TemporalPhrase temporalPhrase) {
        String phrase = temporalPhrase.getPhrase();
        phrase = phrase.trim().toLowerCase().replace(" +", " ");


        TimexChunk tc = new TimexChunk();
        tc.addAttribute(TimexNames.type, TimexNames.TIME);

        Pattern timePattern = Pattern.compile(timePatternStr);
        Matcher timePatternMatch = timePattern.matcher(phrase);
        boolean timeMatchFound = timePatternMatch.find();
        if (timeMatchFound) {
            String whole = timePatternMatch.group(0);
            String hour = timePatternMatch.group(1);
            String minute = timePatternMatch.group(2);
            String second = timePatternMatch.group(3);
            String amPm = timePatternMatch.group(4);

            if (hour!=null && hour.length()==1){
                hour = "0" + hour;
            }
            if (minute!=null && minute.length()==1){
                minute = "0" + minute;
            }
            if (second!=null && second.length()==1){
                second = "0" + second;
            }

            String res = "";

            if (hour==null) {
                return null;
            }

            else if (minute==null) {
                if (amPm==null) {
                    return null;
                }
                else {
                    amPm = amPm.replace(".", "");
                    if (amPm.equals("am")) {
                        res = "T" + hour;
                    }
                    else {
                        res = "T" + (12 + Integer.parseInt(hour));
                    }
                }
            }

            else if (second==null) {
                if (amPm==null) {
                    res = "T" + hour+minute;
                }
                else {
                    amPm = amPm.replace(".", "");
                    if (amPm.equals("am")) {
                        res = "T" + hour+minute;
                    }
                    else {
                        res = "T" + (12 + Integer.parseInt(hour))+minute;
                    }
                }
            }

            else{
                if (amPm==null) {
                    res = "T" + hour+minute+second;
                }
                else {
                    amPm = amPm.replace(".", "");
                    if (amPm.equals("am")) {
                        res = "T" + hour+minute+second;
                    }
                    else {
                        res = "T" + (12 + Integer.parseInt(hour))+minute+second;
                    }
                }
            }

            String residual = StringUtils.difference(whole, phrase);
            if (residual.length()>0) {
                residual = residual.trim().replace(" +", " ").toLowerCase();
                TimexChunk date = ModifiedDate.ModifiedRule(start,
                        new TemporalPhrase(residual, temporalPhrase.getTense()));
                if (date != null) {
                    res = date.getAttribute(TimexNames.value) + res;
                }
            }
            else {
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
                String dateStr = fmt.print(start);
                res = dateStr + res;
            }
            tc.addAttribute(TimexNames.value, res);
            return tc;
        }

        return null;

    }

    public static void main(String[] args) {
        TemporalPhrase temporalPhrase = new TemporalPhrase("10 p.m. ", "past");
        TimexChunk tc = TimeOfDay.timeRule(new DateTime(), temporalPhrase);
        System.out.println(tc.toTIMEXString());
    }

}
