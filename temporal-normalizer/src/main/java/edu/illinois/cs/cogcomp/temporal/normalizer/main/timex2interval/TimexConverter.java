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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhilifeng on 3/18/17.
 * This class serves the purpose of converting TIMEX3 normalization string to time intervals, for now
 */

public class TimexConverter {

    /**
     * Convert a timex3 format to a range/interval
     * @param tc
     * @return
     */
    public static Interval timeConverter(TimexChunk tc) {
        String patternStr = "\\s*([\\w]*)[-]([\\w]*)[-](.*)";
        Pattern pattern = Pattern.compile(patternStr);
        HashMap<String, String> attrMap = tc.getAttributes();
        if (attrMap.get("type")!="TIME") {
            System.out.println("Couldn't convert type other than TIME");
            return null;
        }
        String normString = attrMap.get("value");
        Matcher matcher = pattern.matcher(normString);
        boolean matchFound = matcher.find();
        if (matchFound) {
            String year = matcher.group(1);
            String month = matcher.group(2);
            String day = matcher.group(3);

            // We will not deal with any vague norm value; if such exists, return [0/1/1-9999/1/1]
            // and always mark as correct in normalization, as long as our system didn't return null
            if (year.contains("X") | month.contains("X") | day.contains("X")) {
                DateTime start = new DateTime(0, 1, 1, 0, 0, 0, 0);
                DateTime finish = new DateTime(9999, 1, 1, 0, 0, 0, 0);
                return new Interval(start, finish);
            }
            int yearInt = Integer.parseInt(year);
            int monthInt = Integer.parseInt(month);
            String time = "";

            if (day.indexOf("T") != -1) {
                int pos = day.indexOf("T");
                time = day.substring(pos + 1);
                day = day.substring(0, pos);
            }
            int dayInt = Integer.parseInt(day);
            if (time.length() == 0) {
                DateTime start = new DateTime(yearInt, monthInt, dayInt, 0, 0, 0, 0);
                DateTime finish = new DateTime(yearInt, monthInt, dayInt, 23, 59, 59, 0);
                return new Interval(start, finish);
            } else if (time == "MO") {
                DateTime start = new DateTime(yearInt, monthInt, dayInt, 7, 0, 0, 0);
                DateTime finish = new DateTime(yearInt, monthInt, dayInt, 10, 59, 59, 59);
                return new Interval(start, finish);
            } else if (time == "MD") {
                DateTime start = new DateTime(yearInt, monthInt, dayInt, 11, 0, 0, 0);
                DateTime finish = new DateTime(yearInt, monthInt, dayInt, 13, 59, 59, 59);
                return new Interval(start, finish);
            } else if (time == "AF") {
                DateTime start = new DateTime(yearInt, monthInt, dayInt, 14, 0, 0, 0);
                DateTime finish = new DateTime(yearInt, monthInt, dayInt, 17, 59, 59, 59);
                return new Interval(start, finish);
            } else if (time == "EV") {
                DateTime start = new DateTime(yearInt, monthInt, dayInt, 18, 0, 0, 0);
                DateTime finish = new DateTime(yearInt, monthInt, dayInt, 20, 59, 59, 59);
                return new Interval(start, finish);
            } else if (time == "NI") {
                DateTime start = new DateTime(yearInt, monthInt, dayInt, 21, 0, 0, 0);
                DateTime finish = new DateTime(yearInt, monthInt, dayInt, 23, 59, 59, 59);
                return new Interval(start, finish);
            }

            String hmsPatternStr = "\\s*(\\d{2})(?:[:])*(\\d{2})*(?:[:])*(\\d{2})*";
            Pattern hmsPattern = Pattern.compile(hmsPatternStr);
            Matcher hmsMatcher = hmsPattern.matcher(time);
            boolean hmsMatchFound = hmsMatcher.find();
            if (hmsMatchFound) {
                // System.out.println(matcher.group(1));
                int i;
                for (i = 1; i <= 3; i++) {
                    if (hmsMatcher.group(i) == null) {
                        i--;
                        break;
                    }
                }
                if (i == 4) {
                    i--;
                }
                int numterm = i;

                // This means we have all HH:MM:SS
                if (numterm == 3) {
                    int hour = Integer.parseInt(hmsMatcher.group(1));
                    int minute = Integer.parseInt(hmsMatcher.group(2));
                    int second = Integer.parseInt(hmsMatcher.group(3));
                    DateTime start = new DateTime(yearInt, monthInt, dayInt, hour, minute, second, 0);
                    DateTime finish = new DateTime(yearInt, monthInt, dayInt, hour, minute, second, 59);
                    return new Interval(start, finish);
                }

                // Only have HH:MM
                else if (numterm == 2) {
                    int hour = Integer.parseInt(hmsMatcher.group(1));
                    int minute = Integer.parseInt(hmsMatcher.group(2));
                    DateTime start = new DateTime(yearInt, monthInt, dayInt, hour, minute, 0, 0);
                    DateTime finish = new DateTime(yearInt, monthInt, dayInt, hour, minute, 59, 59);
                    return new Interval(start, finish);
                }

                // Only have HH
                else if (numterm == 1) {
                    int hour = Integer.parseInt(hmsMatcher.group(1));
                    DateTime start = new DateTime(yearInt, monthInt, dayInt, hour, 0, 0, 0);
                    DateTime finish = new DateTime(yearInt, monthInt, dayInt, hour, 59, 59, 59);
                    return new Interval(start, finish);
                }
            }
            else
            {
                DateTime start = new DateTime(yearInt, monthInt, dayInt, 0, 0, 0, 0);
                DateTime finish = new DateTime(yearInt, monthInt, dayInt, 23, 59, 59, 59);
                return new Interval(start, finish);
            }
        }

        return null;

    }

    public static void main(String []args) {
        TimexChunk tc = new TimexChunk();
        HashMap<String, String> attrs = new HashMap<>();
        tc.setContent("15:00 GMT Saturday");
        tc.addAttribute("type", "TIME");
        tc.addAttribute("value", "2013-03-23T15:00");

        Interval temp = TimexConverter.timeConverter(tc);
        System.out.println(temp);
    }

}
