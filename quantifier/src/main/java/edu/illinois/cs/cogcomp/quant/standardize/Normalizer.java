/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.standardize;

import java.io.Serializable;

/**
 * Module responsible for complete normalization Calls modules for dates, ranges, dateranges,
 * numbers, in a particular order
 * 
 * @author subhroroy
 *
 */
public class Normalizer implements Serializable {

    private static final long serialVersionUID = -2076817050096378176L;

    public Normalizer() {
        Bounds.initialize();
        Date.initialize();
        DateRange.initialize();
        Numbers.initialize();
        Range.initialize();
        Ratio.initialize();
    }

    public Object parse(String phrase, String label) {
        phrase = phrase.toLowerCase();
        if (label.equals("DATE")) {
            DateRange dateRange = DateRange.extractDateRange(phrase);
            if (dateRange != null) {
                return dateRange;
            }
            Date date = Date.extractDate(phrase);
            if (date != null) {
                return date;
            }
        }
        if (label.equals("RANGE")) {
            Range range = Range.extractRange(phrase);
            if (range != null) {
                return range;
            }
        }
        if (label.equals("RATIO")) {
            Ratio ratio = Ratio.extractRatio(phrase);
            if (ratio != null) {
                return ratio;
            }
        }
        if (label.equals("NUMBER")) {
            Quantity quantity = Quantity.extractQuantity(phrase);
            if (quantity != null) {
                return quantity;
            }
        }
        return null;
    }

    public Object parse(String phrase) {
        phrase = phrase.toLowerCase();
        // System.out.println("Parsing : "+phrase);
        DateRange dateRange = DateRange.extractDateRange(phrase);
        if (dateRange != null) {
            return dateRange;
        }
        // System.out.println("DateRange : "+dateRange);
        Date date = Date.extractDate(phrase);
        if (date != null) {
            return date;
        }
        // System.out.println("Date: "+date);
        Range range = Range.extractRange(phrase);
        if (range != null) {
            return range;
        }
        // System.out.println("Range : "+range);
        Ratio ratio = Ratio.extractRatio(phrase);
        if (ratio != null) {
            return ratio;
        }
        // System.out.println("Ratio : "+ratio);
        Quantity quantity = Quantity.extractQuantity(phrase);
        if (quantity != null) {
            return quantity;
        }
        // System.out.println("Quantity : "+quantity);
        return null;
    }

    public static void main(String args[]) {
        Normalizer normalizer = new Normalizer();
        System.out.println(normalizer.parse("60 seconds"));
    }
}
