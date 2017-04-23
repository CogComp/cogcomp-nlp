package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhilifeng on 2/19/17.
 */
public class Duration {

    public static HashMap<String, String> modMap = new HashMap<String, String>(){
        {
            put(TimexNames.LESS_THAN, "(almost|less than)");
            put(TimexNames.MORE_THAN, "(more than|over|exceed(?:s)?)");
            put(TimexNames.EQUAL_OR_LESS, "(up to|nearly|no more than|within)");
            put(TimexNames.EQUAL_OR_MORE, "(at least|no less than)");
        }
    };

    public static TimexChunk DurationRule(DateTime start, String phrase){
        start = new DateTime(0,1,1,0,0,0);
        phrase = phrase.trim();
        Interval interval;
        interval = new Interval(start, start);
        DateTime finish;
        String temp1;
        String temp2;
        String temp3;
        String amount;
        int numterm;
        int flag_ago = 0;
        int i;
        int year;
        int month;
        int day;
        phrase = phrase.toLowerCase();
        phrase = phrase.trim().replace(" +", " ");
        TimexChunk tc = new TimexChunk();
        tc.setContent(phrase);
        tc.addAttribute(TimexNames.type, TimexNames.DURATION);


        for (Map.Entry<String, String> entry: modMap.entrySet()) {
            String mod = entry.getKey();
            String patternStr = entry.getValue();
            Pattern modPattern = Pattern.compile(patternStr);
            Matcher modMatcher = modPattern.matcher(phrase);
            boolean modFound = modMatcher.find();
            if (modFound) {
                tc.addAttribute(TimexNames.mod, mod);
            }
        }
        // This captures strings like (some number)(some unit)
        String patternStr = "\\s*((?:(?:\\d+|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|" +
                "eighteen|nineteen|twenty|thirty|fourty|fifth|sixty|seventy|eighty|ninety|" +
                "hundred(?:s)?|thousand(?:s)?|million(?:s)?|billion(?:s)?|an|a|one|two|three|" +
                "four|five|six|seven|eight|nine|ten|eleven|this|several|few|recent|a few)\\s*))(?:\\s|-|more|less)*(?:\\s*(year(?:s)?|" +
                "day(?:s)?|month(?:s)?|week(?:s)?|decade(?:s)?|tenure(?:s)?|centur(?:y)?(?:ies)?|hour(?:s)?|" +
                "minute(?:s)?|second(?:s)?))\\s*\\w*";

        // Just a special term, which means 5 years
        if (phrase.equals("tenure")) {
            tc.addAttribute(TimexNames.value, "P5Y");
            return tc;
        }
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(phrase);
        boolean matchFound = matcher.find();
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

                temp1 = matcher.group(1);
                // System.out.println(matcher.group(2));
                temp2 = matcher.group(2);
                temp3 = RelativeDate.converter(temp1);
                if (temp3 == null) {
                    if (temp2.charAt(temp2.length()-1)=='s')
                        amount = "X";
                    else
                        amount = "1";
                }
                else {
                    amount = temp3;
                }
                // System.out.println(amount);
                if (temp2.equals("years") || temp2.equals("year")) {
//                    finish = start.plusYears(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "P" + amount + "Y");
                    return tc;
                } else if (temp2.equals("day") || temp2.equals("days")) {
//                    finish = start.plusDays(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "P" + amount + "D");
                    return tc;
                } else if (temp2.equals("month")
                        || temp2.equals("months")) {
//                    finish = start.plusMonths(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "P" + amount + "M");
                    return tc;
                } else if (temp2.equals("week") || temp2.equals("weeks")) {
//                    finish = start.plusWeeks(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "P" + amount + "W");
                    return tc;
                } else if (temp2.equals("decade")
                        || temp2.equals("decades")) {

//                    finish = start.plusYears(amount * 10);
//                    interval = new Interval(start, finish);
//                    return interval;
                    amount = amount.equals("X")?amount:String.valueOf(Integer.parseInt(amount)*10);
                    tc.addAttribute(TimexNames.value, "P" + amount + "Y");
                    return tc;
                } else if (temp2.equals("century")
                        || temp2.equals("centuries")) {
//                    finish = start.plusYears(amount * 100);
//                    interval = new Interval(start, finish);
//                    return interval;
                    amount = amount.equals("X")?amount:String.valueOf(Integer.parseInt(amount)*100);
                    tc.addAttribute(TimexNames.value, "P" + amount + "Y");
                    return tc;
                } else if (temp2.equals("hour") || temp2.equals("hours")) {
//                    finish = start.plusHours(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "PT" + amount + "H");
                    return tc;
                } else if (temp2.equals("minute")
                        || temp2.equals("minutes")) {
//                    finish = start.plusMinutes(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "PT" + amount + "M");
                    return tc;
                } else if (temp2.equals("second")
                        || temp2.equals("seconds")) {
//                    finish = start.plusSeconds(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "PT" + amount + "S");
                    return tc;
                }

            }

        }

        // Here we capture single vague durations like "days", "years"
        // System.out.println(amount);
        if (phrase.charAt(phrase.length()-1)=='s')
            amount = "X";
        else
            amount = "1";
        if (phrase.matches("years|year")) {
            tc.addAttribute(TimexNames.value, "P" + amount + "Y");
            return tc;
        } else if (phrase.matches("days|day")) {
            tc.addAttribute(TimexNames.value, "P" + amount + "D");
            return tc;
        } else if (phrase.matches("months|month")) {
            tc.addAttribute(TimexNames.value, "P" + amount + "M");
            return tc;
        } else if (phrase.matches("weeks|week")) {
            tc.addAttribute(TimexNames.value, "P" + amount + "W");
            return tc;
        } else if (phrase.matches("decades|decade")) {
            tc.addAttribute(TimexNames.value, "P" + amount + "Y");
            return tc;
        } else if (phrase.matches("centuries|century")) {
            tc.addAttribute(TimexNames.value, "P" + amount + "Y");
            return tc;
        } else if (phrase.matches("hours|hour")) {
            tc.addAttribute(TimexNames.value, "PT" + amount + "H");
            return tc;
        } else if (phrase.matches("minutes|minute")) {
            tc.addAttribute(TimexNames.value, "PT" + amount + "M");
            return tc;
        } else if (phrase.matches("seconds|second")) {
            tc.addAttribute(TimexNames.value, "PT" + amount + "S");
            return tc;
        }



        return null;
    }

    public static void main(String[] args) {
        String temp = "weeks";
        TimexChunk interval = DurationRule(new DateTime(), temp);
        System.out.println(interval.toTIMEXString());
    }

}
