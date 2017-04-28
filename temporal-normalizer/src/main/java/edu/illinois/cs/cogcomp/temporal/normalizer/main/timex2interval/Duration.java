package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhilifeng on 2/19/17.
 */
public class Duration {

    public static Set<String> timeUnit = new HashSet<String>() {{
        add("second");
        add("seconds");
        add("minute");
        add("minutes");
        add("hour");
        add("hours");
    }};
    public static Set<String> dateUnit = new HashSet<String>() {{
        add("day");
        add("days");
        add("week");
        add("weeks");
        add("month");
        add("months");
        add("year");
        add("years");
        add("century");
        add("centuries");
        add("decade");
        add("decades");
    }};

    public static HashMap<String, String> hierarchy = new HashMap<String, String>(){
        {
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

        }
    };

    public static HashMap<String, Integer> base = new HashMap<String, Integer>(){
        {
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

        }
    };

    public static double fracToDec(String frac) {
        if (!frac.contains("/")) {
            return Double.parseDouble(frac);
        }

        String []list = frac.split("/");
        double numerator = Double.parseDouble(list[0]);
        double denominator = Double.parseDouble(list[1]);
        return numerator/denominator;

    }

    // convert to fraction: half==>1/2
    public static String converter(String input) {
        HashMap<String, String> fracMap = new HashMap<>();
        fracMap.put("half", "1/2");
        fracMap.put("third", "1/3");
        fracMap.put("quarter", "1/4");
        fracMap.put("fifth", "1/5");
        fracMap.put("a", "1");


        input = input.replace(" and ", " ");
        input = input.replace(" a ", " ");
        input = input.replace(" an ", " ");
        String []list = input.split(" ");
        for (int i = 0; i < list.length; i++) {
            if (fracMap.containsKey(list[i])) {
                list[i] = fracMap.get(list[i]);
            }
        }

        List<String> canonicalList = new ArrayList<>();
        for (int i = 0; i < list.length-1; i++) {
            canonicalList.add(list[i]);
            char start = list[i+1].charAt(0);
            if (start>='0' && start<='9') {
                canonicalList.add("and");
            }
        }
        canonicalList.add(list[list.length-1]);
        String res = String.join(" ", canonicalList);
        return res;
    }

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



        String fracPatternStr = "(\\s*(\\d+(?:[./]\\d+)*)\\s*(?:(year(?:s)?|month(?:s)?|day(?:s)?|" +
                "weekend(?:s)?|week(?:s)?|decade(?:s)?|tenure(?:s)?|centur(?:y)?(?:ies)?|hour(?:s)?|" +
                "minute(?:s)?|second(?:s)?))*)+";
        Pattern fracPattern = Pattern.compile(fracPatternStr);
        String canonicalPhrase = Duration.converter(phrase);
        Matcher fracMatcher = fracPattern.matcher(canonicalPhrase);
        String prevQuant = null;
        String prevUnit = null;
        String fracRes = "P";
        boolean isFirstUnit = true;
        while (fracMatcher.find()) {
            String quantity = fracMatcher.group(2);
            String unit = fracMatcher.group(3);
            if (unit==null) {
                if (prevUnit == null) {
                    prevQuant = quantity;
                    continue;
                }
                else {
                    String lowerUnit = hierarchy.get(prevUnit);
                    int lowerUnitBase = base.get(lowerUnit);
                    double fracQuant = fracToDec(quantity);
                    int remainder = (int) (fracQuant * lowerUnitBase);
                    fracRes = fracRes + remainder + unitMap.get(lowerUnit);
                    prevUnit = null;
                }
            }
            else {
                prevUnit = unit;
                if (isFirstUnit) {
                    if (timeUnit.contains(unit)) {
                        fracRes += "T";
                        isFirstUnit = false;
                    }
                }
                String suffix = unitMap.get(unit);
                if (prevQuant != null) {
                    fracRes = fracRes + prevQuant + suffix;
                    String lowerUnit = hierarchy.get(unit);
                    int lowerUnitBase = base.get(lowerUnit);
                    double fracQuant = fracToDec(quantity);
                    int remainder = (int) (fracQuant * lowerUnitBase);
                    fracRes = fracRes + remainder + unitMap.get(lowerUnit);
                    prevQuant = null;
                }
                else {
                    double fracQuant = fracToDec(quantity);
                    if (fracQuant <1) {
                        String lowerUnit = hierarchy.get(unit);
                        int lowerUnitBase = base.get(lowerUnit);
                        int remainder = (int) (fracQuant * lowerUnitBase);
                        fracRes = fracRes + remainder + unitMap.get(lowerUnit);
                    }
                    else {
                        fracRes = fracRes + quantity + suffix;
                    }
                }

            }
        }

        if (fracRes.length()>1) {
            tc.addAttribute(TimexNames.value, fracRes);
            return tc;
        }

        // This captures strings like (some number)(some unit)
        String patternStr = "\\s*((?:(?:\\d+|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|" +
                "eighteen|nineteen|twenty|thirty|fourty|fifth|sixty|seventy|eighty|ninety|" +
                "hundred(?:s)?|thousand(?:s)?|million(?:s)?|billion(?:s)?|an|a|one|two|three|" +
                "four|five|six|seven|eight|nine|ten|eleven|this|several|few|recent|a few)\\s*))(?:\\s|-|more|less)*(?:\\s*(year(?:s)?|" +
                "day(?:s)?|month(?:s)?|weekend(?:s)?|week(?:s)?|quarter(?:s)?|decade(?:s)?|tenure(?:s)?|centur(?:y)?(?:ies)?|hour(?:s)?|" +
                "minute(?:s)?|second(?:s)?))\\s*\\w*";

        // Just a special term, which means 5 years
//        if (phrase.equals("tenure")) {
//            tc.addAttribute(TimexNames.value, "P5Y");
//            return tc;
//        }
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
                } else if (temp2.equals("weekend") || temp2.equals("weekends")) {
//                    finish = start.plusWeeks(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "P" + (2*Integer.parseInt(amount)) + "D");
                    return tc;
                } else if (temp2.equals("week") || temp2.equals("weeks")) {
//                    finish = start.plusWeeks(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "P" + amount + "W");
                    return tc;
                }  else if (temp2.equals("decade")
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
                } else if (temp2.equals("quarter")
                        || temp2.equals("quarters")) {
//                    finish = start.plusMinutes(amount);
//                    interval = new Interval(start, finish);
//                    return interval;
                    tc.addAttribute(TimexNames.value, "P" + amount + "Q");
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
        } else if (phrase.matches("weekends|weekend")) {
            if (amount.equals("1")) {
                amount = String.valueOf((2*Integer.parseInt(amount)));
            }
            tc.addAttribute(TimexNames.value, "P" + amount + "D");
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
        } else if (phrase.matches("quarter|quarters")) {
            tc.addAttribute(TimexNames.value, "P" + amount + "Q");
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
        String temp = "a minute and 1/2";
        TimexChunk interval = DurationRule(new DateTime(), temp);
        System.out.println(interval.toTIMEXString());
    }

}
