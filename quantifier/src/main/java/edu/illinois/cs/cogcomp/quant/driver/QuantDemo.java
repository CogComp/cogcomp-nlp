/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.driver;

import edu.illinois.cs.cogcomp.quant.standardize.DateRange;
import edu.illinois.cs.cogcomp.quant.standardize.Quantity;
import edu.illinois.cs.cogcomp.quant.standardize.Range;
import edu.illinois.cs.cogcomp.quant.standardize.Ratio;

public class QuantDemo {

    public static String getJSONFromQuantity(Quantity q) {
        return "{\n\t\"bound\" : \"" + q.bound + "\",\n\t\"value\" : " + q.value
                + ",\n\t\"unit\" : \"" + q.units + "\"\n}\n";
    }

    public static String getJSONFromRange(Range r) {
        return "{\n\t\"begin\" : " + getJSONFromQuantity(r.begins) + ",\n\t\"end\" : "
                + getJSONFromQuantity(r.ends) + "\n}\n";
    }

    public static String getJSONFromDate(edu.illinois.cs.cogcomp.quant.standardize.Date d) {
        return "{\n\t\"mm\" : " + d.month + ",\n\t\"dd\" : " + d.day + ",\n\t\"yy\" : " + d.year
                + "\n}\n";
    }

    public static String getJSONFromDateRange(DateRange dr) {
        return "{\n\t\"begin\" : " + getJSONFromDate(dr.begins) + ",\n\t\"end\" : "
                + getJSONFromDate(dr.ends) + "\n}\n";
    }

    public static String getJSONFromRatio(Ratio r) {
        return "{\n\t\"begin\" : " + getJSONFromQuantity(r.numerator) + ",\n\t\"end\" : "
                + getJSONFromQuantity(r.denominator) + "\n}\n";
    }

    public static void main(String args[]) throws Exception {
        Quantifier quantifier = new Quantifier();
        System.out.println(quantifier.getSpans(
                "March oil down 1.9%, to settle at $51.16/bbl on Nymex.", true, null));
    }
}
