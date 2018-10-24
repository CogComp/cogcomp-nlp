/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.standardize;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts ranges to standardized form
 * 
 * @author subhroroy
 *
 */
public class Range implements Serializable {

    private static final long serialVersionUID = 9176815857805433578L;
    public Quantity begins;
    public Quantity ends;
    public String phrase;
    static Map<String, Integer[]> vague = new HashMap<String, Integer[]>();
    public static Pattern rangeRulePat, vaguePat;

    Range(Quantity begins, Quantity ends) {
        this.begins = begins;
        this.ends = ends;
    }

    public String toString() {
        return "[range" + this.begins + this.ends + "]";
    }

    public static void initialize() {
        Integer vagueRanges[][] =
                { {12, 99}, {10, 99}, {100, 999}, {1000, 9999}, {100000, 999999},
                        {1000000, 999999999}, {100000000, 999999999}};
        vague.put("dozens", vagueRanges[0]);
        vague.put("tens", vagueRanges[1]);
        vague.put("hundreds", vagueRanges[2]);
        vague.put("thousands", vagueRanges[3]);
        vague.put("hundreds\\s*of\\s*thousands", vagueRanges[4]);
        vague.put("millions", vagueRanges[5]);
        vague.put("hundreds\\s*of\\s*millions", vagueRanges[6]);
        int vagueCount = 0;
        String VAGUE = "(";
        for (String key : vague.keySet()) {
            if (vagueCount != 0)
                VAGUE += "|";
            VAGUE += key;
            vagueCount++;
        }
        VAGUE += ")(.*)";
        vaguePat = Pattern.compile(VAGUE, Pattern.CASE_INSENSITIVE);
        rangeRulePat =
                Pattern.compile(
                        "(?:from|between|start(?:ing|s)?|begin(?:ing|s)?)(.+?)\\s(?:-|and|to|thru"
                                + "|through|until|and\\s*ending|finish[a-z]*|end[a-z]*)\\s(.+)",
                        Pattern.CASE_INSENSITIVE);
    }

    public static Range RangeRule(String str) {
        Matcher matcher = rangeRulePat.matcher(str);
        if (matcher.find()) {
            String arg1 = matcher.group(1);
            String arg2 = matcher.group(2);
            Quantity q1 = Quantity.extractQuantity(arg1.trim());
            if (q1 == null) {
                return null;
            }
            Quantity q2 = Quantity.extractQuantity(arg2.trim());
            if (q2 == null) {
                return null;
            }
            return new Range(q1, q2);
        }
        return null;
    }

    public static Range VagueNumericRange(String str) {
        Matcher matcher = vaguePat.matcher(str);
        if (matcher.find()) {
            String x = matcher.group(1);
            String y = matcher.group(2);
            int a = vague.get(x.trim().toLowerCase())[0];
            int b = vague.get(x.trim().toLowerCase())[1];
            if (y == null)
                return new Range(new Quantity("=", 1.0 * a, ""), new Quantity("=", 1.0 * b, ""));
            else
                return new Range(new Quantity("=", 1.0 * a, y), new Quantity("=", 1.0 * b, y));
        }
        return null;
    }


    public static Range extractRange(String phrase) {
        Range range = null;
        range = RangeRule(phrase);
        if (range != null) {
            range.phrase = phrase;
            return range;
        }
        range = VagueNumericRange(phrase);
        if (range != null) {
            range.phrase = phrase;
            return range;
        }
        return range;
    }
}
