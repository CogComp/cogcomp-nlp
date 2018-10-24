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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Date implements Serializable {

    private static final long serialVersionUID = 742632977601215490L;
    public int month, day, year;
    public String phrase;
    public boolean missing_day = true, missing_month = true, missing_year = true;
    public String bound;

    // 3-letter months
    public static Map<String, Integer> month2num = new HashMap<String, Integer>();
    public static String MONTH = "(?:jan(?:uary)?|febr?(?:uary)?|mar(?:ch)?|apr(?:il)?"
            + "|may|june?|july?|aug(?:ust)?|sept?(?:ember)?|oct(?:ober)?|nov(?:ember)?|"
            + "dec(?:ember)?)\\.?";
    public static Pattern yearMonthDatePat, dateMonthYearPat, monthDatePat, monthYearPat,
            specialDayPat, monthDateYearPat, dateMonthPat;
    public static Calendar presentDate;

    public Date() {}

    public Date(Integer year, Integer month, Integer day) {
        if (year != null) {
            this.year = year;
            missing_year = false;
        }
        if (month != null) {
            this.month = month;
            missing_month = false;
        }
        if (day != null) {
            this.day = day;
            missing_day = false;
        }
        bound = "=";
    }

    public Date(Integer year, Integer month, Integer day, String bound) {
        this(year, month, day);
        this.bound = bound;
    }

    public String toString() {
        String str = "[" + bound + " Date(";
        if (missing_month)
            str += "XX/";
        else
            str += String.format("%02d/", month);
        if (missing_day)
            str += "XX/";
        else
            str += String.format("%02d/", day);
        if (missing_year)
            str += "XXXX)";
        else
            str += String.format("%04d)", year);
        return str + "]";
    }

    public static void initialize() {

        month2num.put("jan", 1);
        month2num.put("feb", 2);
        month2num.put("mar", 3);
        month2num.put("apr", 4);
        month2num.put("may", 5);
        month2num.put("jun", 6);
        month2num.put("jul", 7);
        month2num.put("aug", 8);
        month2num.put("sep", 9);
        month2num.put("oct", 10);
        month2num.put("nov", 11);
        month2num.put("dec", 12);

        // Date - Month - Year
        // Month(Number) - Date - Year
        // Year(2-digit) - Month- Date
        dateMonthYearPat =
                Pattern.compile("([\\dX]{1,2})"
                        + "(?:(?:\\s*[/\\-\\.]\\s*)|(?:\\s*(?:st|nd|rd|th)?\\s+(?:of)?\\s*)|\\s+)"
                        + "([\\dX]{1,2}|" + MONTH + ")" + "(?:\\s*(?:[/\\-\\.,'])\\s*|\\s+)"
                        + "([\\dX]{2,4})", Pattern.CASE_INSENSITIVE);

        // Date - Month(words)
        dateMonthPat =
                Pattern.compile("([\\dX]{1,2})"
                        + "(?:\\s*(?:[/\\-\\.]|(?:(?:st|nd|rd|th)?\\s*(?:of)?))\\s*|\\s+)" + "("
                        + MONTH + ")", Pattern.CASE_INSENSITIVE);

        // Month(words) - Date - Year
        monthDateYearPat =
                Pattern.compile("(" + MONTH + ")" + "(?:\\s*-\\s*|\\s+)"
                        + "([\\dX]{1,2})(?:st|nd|rd|th)?" + "(?:\\s*[-,']\\s*|\\s+)"
                        + "([\\dX]{2,4})", Pattern.CASE_INSENSITIVE);

        // Month(words) - Year
        monthYearPat =
                Pattern.compile("(" + MONTH + ")" + "(?:(?:\\s*[,']\\s*([\\dX]{2,4}))|"
                        + "(?:\\s+([\\dX]{4})))", Pattern.CASE_INSENSITIVE);


        // Month(words) - Date?
        monthDatePat =
                Pattern.compile("(" + MONTH + ")" + "(?:(?:\\s*-\\s*|\\s+)"
                        + "([\\dX]{1,2})(?:st|nd|rd|th)?)?", Pattern.CASE_INSENSITIVE);

        // Year - Month - Date
        yearMonthDatePat =
                Pattern.compile("([\\dX]{4})" + "(?:\\s*[/\\-\\.,]\\s*|\\s+)" + "([\\dX]{1,2}|"
                        + MONTH + ")" + "(?:\\s*(?:[/\\-\\.])\\s*|\\s+)" + "([\\dX]{1,2})",
                        Pattern.CASE_INSENSITIVE);

        presentDate = Calendar.getInstance();
    }

    public static Date extractDate(String phrase) {
        Date date;
        date = yearMonthDate(phrase);
        // System.out.println("After yearMonthDate :"+date);
        if (date != null) {
            return date;
        }
        date = dateMonthYear(phrase);
        // System.out.println("After dateMonthYear :"+date);
        if (date != null) {
            return date;
        }
        date = monthDateYear(phrase);
        // System.out.println("After monthDateYear :"+date);
        if (date != null) {
            return date;
        }
        return date;
    }

    public static Date getDate(int yr, int mth, int day) {
        if (mth < 1) {
            yr = yr - (int) ((Math.abs(mth) / 12) + 1);
            mth = mth + (int) ((Math.abs(mth) / 12) + 1) * 12;
        }
        if (mth > 12) {
            yr = yr + (mth / 12);
            mth = mth - (mth / 12) * 12;
        }
        // This library seems to have a problem with number of days in February
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, Math.max(1, mth));
        cal.set(Calendar.YEAR, Math.max(1, yr));
        // System.err.println(cal.toString());
        cal.add(Calendar.DAY_OF_MONTH, day - 1);
        // System.err.println(cal.toString());
        return new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
    }

    public static Date getRelativeDate(String units, int n, Date dt) {
        // System.err.println("Relative units: "+units+" "+n+" "+dt.repr());
        units = units.substring(0, 3);
        Integer d = dt.day;
        Integer m = dt.month;
        Integer y = dt.year;

        if (units.equals("yea"))
            y = y + n;
        else if (units.equals("cen"))
            y = y + n * 100;
        else if (units.equals("dec"))
            y = y + n * 10;
        else if (units.equals("mon"))
            m = m + n;
        else if (units.equals("for"))
            d = d + 14 * n;
        else if (units.equals("wee"))
            d = d + 7 * n;
        else if (units.equals("day"))
            d = d + n;
        // System.err.println("Relativeuns: "+y+" "+m+" "+d);
        return getDate(y, m, d);
    }

    public static Integer handleTwoDigitYear(Integer x) {
        if (x == null || x >= 100) {
            return x;
        }
        if (x <= (Calendar.getInstance().get(Calendar.YEAR)) % 100) {
            return 2000 + x;
        } else {
            return 1900 + x;
        }
    }

    public static Date dateMonthYear(String str) {
        Matcher matcher = dateMonthYearPat.matcher(str);
        Integer dd = null, mm = null, yy = null, temp;
        if (matcher.find()) {
            String d = matcher.group(1);
            String m = matcher.group(2);
            String y = matcher.group(3);
            // System.out.println(d + "|" + m + "|" +y);
            // If month is a word, convert
            if (m.length() >= 3) {
                String mmm = m.substring(0, 3).toLowerCase();
                if (month2num.containsKey(mmm)) {
                    mm = month2num.get(mmm);
                }
            } else {
                mm = Integer.parseInt(m);
            }
            if (y != null) {
                yy = Integer.parseInt(y);
                yy = handleTwoDigitYear(yy);
            }
            dd = Integer.parseInt(d);

            // invalid date
            if (mm > 12 && dd > 31) {
                return null;
            }
            // Change only when both day and month were numbers
            if (mm <= 31 && dd <= 12 && m.length() < 3) {
                temp = mm;
                mm = dd;
                dd = temp;
            }
            if (dd > 31 && y != null && yy <= 31) {
                temp = yy;
                yy = dd;
                dd = temp;
            }
            return new Date(yy, mm, dd);
        }
        matcher = dateMonthPat.matcher(str);
        if (matcher.find()) {
            String d = matcher.group(1);
            String m = matcher.group(2);
            // System.out.println(d+"|"+m);
            // If month is a word, convert
            String mmm = m.substring(0, 3).toLowerCase();
            if (month2num.containsKey(mmm)) {
                mm = month2num.get(mmm);
            }
            dd = Integer.parseInt(d);
            return new Date(yy, mm, dd);
        }
        return null;
    }

    public static Date monthDateYear(String str) {
        Integer dd = null, mm = null, yy = null;
        Matcher matcher = monthDateYearPat.matcher(str);
        if (matcher.find()) {
            // System.out.println("MonthDateYear matched");
            String m = matcher.group(1);
            String d = matcher.group(2);
            String y = matcher.group(3);
            mm = Date.month2num.get(m.substring(0, 3).toLowerCase());
            yy = handleTwoDigitYear(Integer.parseInt(y));
            dd = Integer.parseInt(d);
            return new Date(yy, mm, dd);
        }
        matcher = monthYearPat.matcher(str);
        if (matcher.find()) {
            // System.out.println("MonthYear matched");
            String m = matcher.group(1);
            String y1 = matcher.group(2);
            String y2 = matcher.group(3);
            mm = Date.month2num.get(m.substring(0, 3).toLowerCase());
            if (y1 != null) {
                yy = handleTwoDigitYear(Integer.parseInt(y1));
            }
            if (y2 != null) {
                yy = handleTwoDigitYear(Integer.parseInt(y2));
            }
            return new Date(yy, mm, dd);
        }
        matcher = monthDatePat.matcher(str);
        if (matcher.find()) {
            // System.out.println("MonthDate matched");
            String m = matcher.group(1);
            String d = matcher.group(2);
            mm = Date.month2num.get(m.substring(0, 3).toLowerCase());
            if (d != null) {
                dd = Integer.parseInt(d);
            }
            return new Date(yy, mm, dd);
        }
        return null;
    }

    public static Date yearMonthDate(String str) {
        Integer dd = null, mm = null, yy = null;
        Matcher matcher = yearMonthDatePat.matcher(str);
        if (matcher.find()) {
            String d = matcher.group(3);
            String m = matcher.group(2);
            String y = matcher.group(1);
            if (m.length() >= 3) {
                mm = Date.month2num.get(m.substring(0, 3).toLowerCase());
            } else {
                mm = Integer.parseInt(m);
            }
            dd = Integer.parseInt(d);
            yy = handleTwoDigitYear(Integer.parseInt(y));
            return new Date(yy, mm, dd);
        }
        return null;
    }

    public static void main(String args[]) {
        Date.initialize();
        System.out.println(Date.extractDate("4th March, '03"));
        // System.out.println(Date.extractDate("July 7"));
    }
}
