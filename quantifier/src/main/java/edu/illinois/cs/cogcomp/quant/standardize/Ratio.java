/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.standardize;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts ratios to standardized form
 * 
 * @author subhroroy
 *
 */
public class Ratio implements Serializable {
    private static final long serialVersionUID = 9205085634972499157L;
    public Quantity numerator, denominator;
    public String phrase;
    public static Pattern ratioRulePat, explicitRatioPat;

    Ratio(Quantity x, Quantity y) {
        numerator = x;
        denominator = y;
    }

    public String toString() {
        return "[ratio" + this.numerator.toString() + this.denominator.toString() + "]";
    }

    public static void initialize() {
        ratioRulePat =
                Pattern.compile(
                        "(\\s*(.+?)\\s*((?:out\\s*)?of\\s*(?:every\\s*)?(?:the\\s*)?)\\s*(.+))",
                        Pattern.CASE_INSENSITIVE);
        explicitRatioPat = Pattern.compile("((.+)\\s+to\\s+(.+))", Pattern.CASE_INSENSITIVE);
    }

    public static Ratio RatioRule(String str) {
        Matcher matcher = ratioRulePat.matcher(str);
        if (matcher.find()) {
            String arg1 = matcher.group(2);
            String arg2 = matcher.group(4);
            Quantity q1 = Quantity.extractQuantity(arg1.trim());
            if (q1 == null) {
                return null;
            }
            Quantity q2 = Quantity.extractQuantity(arg2.trim());
            if (q2 == null) {
                return null;
            }
            return new Ratio(q1, q2);
        }
        return null;
    }


    public static Ratio explicitRatio(String str) {
        Matcher matcher = explicitRatioPat.matcher(str);
        if (matcher.find()) {
            String arg1 = matcher.group(2);
            String arg2 = matcher.group(3);
            Quantity q1 = Quantity.extractQuantity(arg1.trim());
            if (q1 == null) {
                return null;
            }
            Quantity q2 = Quantity.extractQuantity(arg2.trim());
            if (q2 == null) {
                return null;
            }
            return new Ratio(q1, q2);
        }
        return null;
    }

    public static Ratio extractRatio(String phrase) {
        Ratio ratio;
        ratio = RatioRule(phrase);
        if (ratio != null) {
            ratio.phrase = phrase;
            return ratio;
        }
        ratio = explicitRatio(phrase);
        if (ratio != null) {
            ratio.phrase = phrase;
            return ratio;
        }
        return null;
    }
}
