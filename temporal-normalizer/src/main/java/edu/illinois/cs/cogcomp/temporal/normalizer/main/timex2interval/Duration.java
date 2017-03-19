package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhilifeng on 2/19/17.
 */
public class Duration {

    public static Interval DurationRule(DateTime start, String phrase){
        start = new DateTime(0,1,1,0,0,0);
        phrase = phrase.trim();
        Interval interval;
        interval = new Interval(start, start);
        DateTime finish;
        String temp1;
        String temp2;
        String temp3;
        int amount;
        int numterm;
        int flag_ago = 0;
        int i;
        int year;
        int month;
        int day;

        phrase = phrase.toLowerCase();

        String patternStr = "\\s*((?:(?:\\d+|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|" +
                "eighteen|nineteen|twenty|thirty|fourty|fifth|sixty|seventy|eighty|ninety|" +
                "hundred(?:s)?|thousand(?:s)?|million(?:s)?|billion(?:s)?|an|a|one|two|three|" +
                "four|five|six|seven|eight|nine|ten|eleven|this)\\s*)*)\\s*(?:\\s*(year(?:s)?|" +
                "day(?:s)?|month(?:s)?|week(?:s)?|decade(?:s)?|centur(?:y)?(?:ies)?|hour(?:s)?|" +
                "minute(?:s)?|second(?:s)?))\\s*\\w*";

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
                    return null;
                }
                amount = Integer.parseInt(temp3);
                // System.out.println(amount);
                if (temp2.equals("years") || temp2.equals("year")) {
                    finish = start.plusYears(amount);
                    interval = new Interval(start, finish);
                    return interval;
                } else if (temp2.equals("day") || temp2.equals("days")) {
                    finish = start.plusDays(amount);
                    interval = new Interval(start, finish);
                    return interval;

                } else if (temp2.equals("month")
                        || temp2.equals("months")) {
                    finish = start.plusMonths(amount);
                    interval = new Interval(start, finish);
                    return interval;
                } else if (temp2.equals("week") || temp2.equals("weeks")) {
                    finish = start.plusWeeks(amount);
                    interval = new Interval(start, finish);
                    return interval;
                } else if (temp2.equals("decade")
                        || temp2.equals("decades")) {

                    finish = start.plusYears(amount * 10);
                    interval = new Interval(start, finish);
                    return interval;
                } else if (temp2.equals("century")
                        || temp2.equals("centuries")) {
                    finish = start.plusYears(amount * 100);
                    interval = new Interval(start, finish);
                    return interval;
                } else if (temp2.equals("hour") || temp2.equals("hours")) {
                    finish = start.plusHours(amount);
                    interval = new Interval(start, finish);
                    return interval;
                } else if (temp2.equals("minute")
                        || temp2.equals("minutes")) {
                    finish = start.plusMinutes(amount);
                    interval = new Interval(start, finish);
                    return interval;
                } else if (temp2.equals("second")
                        || temp2.equals("seconds")) {
                    finish = start.plusSeconds(amount);
                    interval = new Interval(start, finish);
                    return interval;
                }

            }

        }
        return null;
    }

    public static void main(String[] args) {
        String temp = "ten thousand years";
        Interval interval = DurationRule(new DateTime(), temp);
        System.out.println(interval);
    }

}
