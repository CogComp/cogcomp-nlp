package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhilifeng on 3/22/17.
 */
public class TimeOfDay {
    // Here we get rid of timezone phrases, currently didn't deal with the offset
    // TODO: add support to offset
    public static String converter(String phrase) {
        Set<String> zoneIds = DateTimeZone.getAvailableIDs();

        for(String zoneId:zoneIds) {
            String longName = TimeZone.getTimeZone(zoneId).getDisplayName();
            phrase = phrase.replace(longName.toLowerCase(), "");
            String[] zoneList = zoneId.split("/");
            // Because we have zone like UTC/GMT+1
            for (String zone: zoneList) {
                phrase = phrase.replace(zone.toLowerCase(), "");
            }
        }
        // EST wasn't in the canonical ID list in Joda time
        phrase = phrase.replace("est", "");
        phrase = phrase.trim().replaceAll(" +", " ");
        return phrase;
    }

    public static String timePatternStr = "\\s*(\\d+)\\s*(:\\d+)?\\s*(:\\d+)?\\s*(am|a.m.|a.m|pm|p.m.|p.m)?";
    public static TimexChunk timeRule(DateTime start, TemporalPhrase temporalPhrase) {
        String phrase = temporalPhrase.getPhrase();
        phrase = phrase.trim().toLowerCase().replace(" +", " ");
        phrase = converter(phrase);

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
        TemporalPhrase temporalPhrase = new TemporalPhrase(" 4 a.m. eastern standard time saturday", "past");
        TimexChunk tc = TimeOfDay.timeRule(new DateTime(), temporalPhrase);
        System.out.println(tc.toTIMEXString());
    }

}
