/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.StringStatisticsUtils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParsePosition;

public class MyString {
    public static String cleanPunctuation(String s) {
        StringBuilder res = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c))
                res.append(c);
        }
        return res.toString();
    }

    public static String normalizeDigitsForFeatureExtraction(String s) {
        String form = s;
        if (MyString.isDate(form))
            form = "*DATE*";
        if (MyString.hasDigits(form))
            form = MyString.normalizeDigits(form);
        return form;
    }

    /** fast date formatter for identifying date instances. */
    static final FastDateFormat df = FastDateFormat.getInstance();

    /** we will reuse this class to avoid the allocation. */
    static final ParsePosition pp = new ParsePosition(0);

    /**
     * Tests the string for a date starting at position zero.
     * 
     * @param s the date string.
     * @return true if it is a date, or false if it is not.
     */
    public static boolean isDate(String s) {
        pp.setIndex(0);
        return df.parse(s, pp) != null;
    }

    public static String collapseDigits(String s) {
        StringBuilder res = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                while (i < s.length() && Character.isDigit(s.charAt(i)))
                    i++;
                res.append("*D*");
                if (i < s.length())
                    res.append(s.charAt(i));
            } else {
                res.append(s.charAt(i));
            }
        }
        return res.toString();
    }

    public static String normalizeDigits(String s) {
        StringBuilder res = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                res.append("*D*");
            } else {
                res.append(s.charAt(i));
            }
        }
        return res.toString();
    }

    public static boolean hasDigits(String s) {
        for (int i = 0; i < s.length(); i++)
            if (Character.isDigit(s.charAt(i)))
                return true;
        return false;
    }

}
