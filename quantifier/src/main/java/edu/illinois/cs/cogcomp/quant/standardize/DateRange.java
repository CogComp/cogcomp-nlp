/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.standardize;

import java.io.Serializable;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateRange implements Serializable {

    private static final long serialVersionUID = -5569548960218363583L;
    public Date begins;
    public Date ends;
    public String phrase;
    public static Pattern periodPat, thisNextDatePat, pastLastDatePat, fromToDatePat, fromDatePat,
            justYearPat1, justYearPat2;

    public DateRange() {}

    public DateRange(Date begins, Date ends) {
        this.begins = begins;
        this.ends = ends;
    }

    public String toString() {
        return "[daterange" + this.begins + this.ends + "]";
    }

    public static void initialize() {
        thisNextDatePat =
                Pattern.compile("(this|next)\\s*"
                        + "(week|fort\\W*nights?|centur(?:ys?|ies)|decades?|months?"
                        + "|weeks?|days?|years?)", Pattern.CASE_INSENSITIVE);
        periodPat =
                Pattern.compile("(\\d{4}|\\d{2})\\s*(?:st|nd|rd|th|)\\W*?(century|cent|s)",
                        Pattern.CASE_INSENSITIVE);
        pastLastDatePat =
                Pattern.compile("(past|last|since)\\s*(\\d+|a)?\\s*(fort\\W*nights?|centur"
                        + "(?:ys?|ies)|decades?|months?|weeks?|days?|years?)",
                        Pattern.CASE_INSENSITIVE);
        fromToDatePat =
                Pattern.compile("(?:from|between|start(?:ing|s)?|begin(?:ing|s)?)" + "(.+?)\\s"
                        + "(?:-|and|to|thru|through|until|and\\s*ending|finish[a-z]*|end[a-z]*)\\s"
                        + "(.+)", Pattern.CASE_INSENSITIVE);
        fromDatePat =
                Pattern.compile("(?:since|from|start(?:ing|s)?|begin(?:ing|s)?)" + "(.+?)",
                        Pattern.CASE_INSENSITIVE);
        justYearPat1 = Pattern.compile("([\\dX]{4})-([\\dX]{4})", Pattern.CASE_INSENSITIVE);
        justYearPat2 = Pattern.compile("([\\dX]{4})-([\\dX]{2})", Pattern.CASE_INSENSITIVE);
    }

    public static DateRange thisNextDate(String str) {
        /*
         * Modified dates examples: this weekend, this monday Does not support double modifiers:
         * weekend after next... months: this March
         */
        Matcher matcher = thisNextDatePat.matcher(str);
        Date present =
                new Date(Date.presentDate.get(Calendar.YEAR), Date.presentDate.get(Calendar.MONTH),
                        Date.presentDate.get(Calendar.DAY_OF_MONTH));
        if (matcher.find()) {
            String a = matcher.group(1);
            String b = matcher.group(2);
            if (a.equals("this")) {
                return new DateRange(Date.getRelativeDate(b, -1, present), present);
            }
            if (a.equals("next")) {
                return new DateRange(present, Date.getRelativeDate(b, 1, present));
            }
        }
        return null;
    }

    public static DateRange Periods(String str) {
        // Periods of time: the 1600's, 90's
        Matcher matcher = periodPat.matcher(str);
        if (matcher.find()) {
            String digit = matcher.group(1);
            String unit = matcher.group(2);
            // System.err.println(chunk+"|"+digit+"|"+ordinal+"|"+unit);
            int d = Integer.parseInt(digit);
            if (unit.equals("s") && matcher.end() == str.length()) {
                // handle the 80's, 90's
                if (digit.length() == 4 && digit.substring(digit.length() - 2).equals("00"))
                    return new DateRange(new Date(d, 1, 1), new Date(d + 99, 12, 31));
                else if (digit.substring(digit.length() - 1).equals("0")) {
                    d = Date.handleTwoDigitYear(d);
                    return new DateRange(new Date(d, 1, 1), new Date(d + 9, 12, 31));
                }
            }
            if (unit.toLowerCase().equals("century") || unit.toLowerCase().equals("cent")) {
                if (digit.length() <= 2) {
                    int begins = (d - 1) * 100 + 1;
                    int ends = d * 100;
                    return new DateRange(new Date(begins, 1, 1), new Date(ends, 12, 31));
                }
            }
        }
        return null;
    }

    public static DateRange pastLastDate(String str) {
        Matcher matcher = pastLastDatePat.matcher(str);
        if (matcher.find()) {
            String num = matcher.group(2);
            String unit = matcher.group(3);
            int nm;
            try {
                nm = Integer.parseInt(num);
            } catch (NumberFormatException e) {
                nm = 1;
            }
            return new DateRange(Date.getRelativeDate(
                    unit,
                    -nm,
                    new Date(Date.presentDate.get(Calendar.YEAR), Date.presentDate
                            .get(Calendar.MONTH), Date.presentDate.get(Calendar.DAY_OF_MONTH))),
                    new Date(Date.presentDate.get(Calendar.YEAR), Date.presentDate
                            .get(Calendar.MONTH), Date.presentDate.get(Calendar.DAY_OF_MONTH)));
        }
        return null;
    }

    public static DateRange fromToDate(String str) {
        Matcher matcher = fromToDatePat.matcher(str);
        if (matcher.find()) {
            Date date1 = Date.extractDate(matcher.group(1));
            if (date1 == null) {
                return null;
            }
            Date date2 = Date.extractDate(matcher.group(2));
            if (date2 == null) {
                return null;
            }
            return new DateRange(date1, date2);
        }
        return null;
    }

    public static DateRange fromDate(String str) {
        Matcher matcher = fromDatePat.matcher(str);
        if (matcher.find()) {
            Date date = Date.extractDate(matcher.group(1));
            if (date == null) {
                return null;
            }
            return new DateRange(date, new Date(Date.presentDate.get(Calendar.YEAR),
                    Date.presentDate.get(Calendar.MONTH),
                    Date.presentDate.get(Calendar.DAY_OF_MONTH)));
        }
        return null;
    }

    public static DateRange justYear(String str) {
        Matcher matcher = justYearPat1.matcher(str);
        Integer y1, y2;
        if (matcher.find()) {
            y1 = Integer.parseInt(matcher.group(1));
            y2 = Integer.parseInt(matcher.group(2));
            return new DateRange(new Date(y1, 1, 1), new Date(y2, 12, 31));
        }
        matcher = justYearPat2.matcher(str);
        if (matcher.find() && matcher.end() == str.trim().length()) {
            y1 = Integer.parseInt(matcher.group(1));
            y2 = Integer.parseInt(matcher.group(1).substring(0, 2) + matcher.group(2));
            return new DateRange(new Date(y1, 1, 1), new Date(y2, 12, 31));
        }
        str = str.trim();
        if (str.length() == 4) {
            try {
                int num = Integer.parseInt(str);
                if (num >= 1000 && num <= 3000) {
                    return new DateRange(new Date(num, 1, 1), new Date(num, 12, 31));
                }
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    public static DateRange extractDateRange(String phrase) {
        DateRange dateRange;
        dateRange = pastLastDate(phrase);
        if (dateRange != null) {
            return dateRange;
        }
        dateRange = Periods(phrase);
        if (dateRange != null) {
            return dateRange;
        }
        dateRange = thisNextDate(phrase);
        if (dateRange != null) {
            return dateRange;
        }
        dateRange = fromToDate(phrase);
        if (dateRange != null) {
            return dateRange;
        }
        dateRange = fromDate(phrase);
        if (dateRange != null) {
            return dateRange;
        }
        dateRange = justYear(phrase);
        if (dateRange != null) {
            return dateRange;
        }
        return dateRange;
    }

    public static void main(String args[]) {
        DateRange.initialize();
        System.out.println(DateRange.extractDateRange("the 80s"));
    }
}
