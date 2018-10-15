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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval.KnowledgeBase.*;

/**
 * Created by zhilifeng on 2/19/17.
 * This class provides method to normalize a duration of time
 */
public class Duration {


    public static double fracToDec(String frac) {
        if (!frac.contains("/")) {
            return Double.parseDouble(frac);
        }

        String []list = frac.split("/");
        double numerator = Double.parseDouble(list[0]);
        double denominator = Double.parseDouble(list[1]);
        return numerator/denominator;

    }

    /**
     * convert a string that represents a fraction to fraction format: half ==> 1/2, etc
     * @param input
     * @return
     */
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

    /**
     * Maps some strings to TIMEX3 MOD
     */
    public static HashMap<String, String> modMap = new HashMap<String, String>(){
        {
            put(TimexNames.LESS_THAN, "(almost|less than)");
            put(TimexNames.MORE_THAN, "(more than|over|exceed(?:s)?)");
            put(TimexNames.EQUAL_OR_LESS, "(up to|nearly|no more than|within)");
            put(TimexNames.EQUAL_OR_MORE, "(at least|no less than)");
        }
    };

    /**
     * Main method in the class. Given a temporal string and DCT, normalize using TIMEX3 format.
     * The temporal string is supposed to contain a duration of time, for details check TIMEX3 standard
     * about the definition of DURATION
     * @param start
     * @param phrase
     * @return
     */
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



//        String fracPatternStr = "(\\s*(\\d+(?:[./]\\d+)*)\\s*(?:(year(?:s)?|month(?:s)?|day(?:s)?|" +
//                "weekend(?:s)?|week(?:s)?|decade(?:s)?|tenure(?:s)?|centur(?:y)?(?:ies)?|hour(?:s)?|" +
//                "minute(?:s)?|second(?:s)?))*)+";
        String fracPatternStr = "(\\s*(\\d+(?:[./]\\d+)*)\\s*(?:(" + dateUnit + "|" + timeUnit  + "))*)+";
        Pattern fracPattern = Pattern.compile(fracPatternStr);
        String canonicalPhrase = Duration.converter(phrase);
        boolean containsFrac = canonicalPhrase.contains("/");
        Matcher fracMatcher = fracPattern.matcher(canonicalPhrase);
        String prevQuant = null;
        String prevUnit = null;
        String fracRes = "P";
        boolean isFirstUnit = true;
        while (containsFrac && fracMatcher.find()) {
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
                    if (timeUnitSet.contains(unit)) {
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
                if (temp2.equals("years") || temp2.equals("year")) {
                    tc.addAttribute(TimexNames.value, "P" + amount + "Y");
                    return tc;
                } else if (temp2.equals("day") || temp2.equals("days")) {
                    tc.addAttribute(TimexNames.value, "P" + amount + "D");
                    return tc;
                } else if (temp2.equals("month")
                        || temp2.equals("months")) {
                    tc.addAttribute(TimexNames.value, "P" + amount + "M");
                    return tc;
                } else if (temp2.equals("weekend") || temp2.equals("weekends")) {
                    tc.addAttribute(TimexNames.value, "P" + (2*Integer.parseInt(amount)) + "D");
                    return tc;
                } else if (temp2.equals("week") || temp2.equals("weeks")) {
                    tc.addAttribute(TimexNames.value, "P" + amount + "W");
                    return tc;
                }  else if (temp2.equals("decade")
                        || temp2.equals("decades")) {
                    amount = amount.equals("X")?amount:String.valueOf(Integer.parseInt(amount)*10);
                    tc.addAttribute(TimexNames.value, "P" + amount + "Y");
                    return tc;
                } else if (temp2.equals("century")
                        || temp2.equals("centuries")) {
                    amount = amount.equals("X")?amount:String.valueOf(Integer.parseInt(amount)*100);
                    tc.addAttribute(TimexNames.value, "P" + amount + "Y");
                    return tc;
                } else if (temp2.equals("quarter")
                        || temp2.equals("quarters")) {
                    tc.addAttribute(TimexNames.value, "P" + amount + "Q");
                    return tc;
                } else if (temp2.equals("hour") || temp2.equals("hours")) {
                    tc.addAttribute(TimexNames.value, "PT" + amount + "H");
                    return tc;
                } else if (temp2.equals("minute")
                        || temp2.equals("minutes")) {
                    tc.addAttribute(TimexNames.value, "PT" + amount + "M");
                    return tc;
                } else if (temp2.equals("second")
                        || temp2.equals("seconds")) {
                    tc.addAttribute(TimexNames.value, "PT" + amount + "S");
                    return tc;
                }

            }

        }

        // Here we capture single vague durations like "days", "years"
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
