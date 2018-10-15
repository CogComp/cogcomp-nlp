/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.standardize;

import java.io.Serializable;
import java.util.regex.*;
import java.util.*;

/**
 * Converts pure numbers to normalized form Returns Quantity object
 * 
 * @author subhroroy
 *
 */

public class Numbers implements Serializable {

    private static final long serialVersionUID = 3879107958184862557L;
    // Map the string representation of a number to floating point
    static Map<String, Integer> str2num = new HashMap<String, Integer>();
    static Map<String, Integer> fractions = new HashMap<String, Integer>();
    // Orders of magnitude
    static List<String> Orders = new ArrayList<>();
    static Map<String, Integer> ordinals = new HashMap<String, Integer>();
    public static Pattern decimalPat, negDecimalPat, removeScientificPat, commaPat,
            anyNumberLikeThingPat, prefixUsdPat;
    public static String NUMBER, FRACTION, ORDINAL, DECIMAL, ANYNUMBERLIKETHING, NEG_DECIMAL;

    public static void initialize() {
        str2num.put("zero", 0);
        str2num.put("one", 1);
        str2num.put("two", 2);
        str2num.put("three", 3);
        str2num.put("four", 4);
        str2num.put("five", 5);
        str2num.put("six", 6);
        str2num.put("seven", 7);
        str2num.put("eight", 8);
        str2num.put("nine", 9);
        str2num.put("ten", 10);
        // the teens
        str2num.put("eleven", 11);
        str2num.put("twelve", 12);
        str2num.put("thirteen", 13);
        str2num.put("fourteen", 14);
        str2num.put("fifteen", 15);
        str2num.put("sixteen", 16);
        str2num.put("seventeen", 17);
        str2num.put("eighteen", 18);
        str2num.put("nineteen", 19);
        // multiples of ten
        str2num.put("twenty", 20);
        str2num.put("thirty", 30);
        str2num.put("forty", 40);
        str2num.put("fourty", 40);
        str2num.put("fifty", 50);
        str2num.put("sixty", 60);
        str2num.put("seventy", 70);
        str2num.put("eighty", 80);
        str2num.put("ninety", 90);
        // powers of ten
        str2num.put("hundred", 100);
        str2num.put("thousand", 1000);
        str2num.put("million", 1000000);
        str2num.put("mn", 1000000);
        str2num.put("billion", 1000000000);
        str2num.put("bn", 1000000000);
        // special phrases
        str2num.put("no", 0);
        str2num.put("single", 1);
        str2num.put("penny", 1);
        str2num.put("both", 2);
        str2num.put("pair", 2);
        str2num.put("duce", 2);
        str2num.put("nickel", 5);
        str2num.put("dime", 10);
        str2num.put("dozen", 12);
        str2num.put("dubs", 20);
        // other...
        str2num.put("once", 1);
        str2num.put("twice", 2);
        str2num.put("thrice", 3);

        fractions.put("halve", 2);
        fractions.put("middle", 2);
        fractions.put("half", 2);
        fractions.put("third", 3);
        fractions.put("fourth", 4);
        fractions.put("quarter", 4);
        fractions.put("fifth", 5);
        fractions.put("sixth", 6);
        fractions.put("seventh", 7);
        fractions.put("eighth", 8);
        fractions.put("nineth", 9);
        fractions.put("ninth", 9);
        fractions.put("tenth", 10);
        fractions.put("hundredth", 100);
        fractions.put("hundreth", 100);
        fractions.put("thousandth", 1000);
        fractions.put("millionth", 1000000);
        fractions.put("billionth", 1000000000);

        ordinals.put("zeroth", 0);
        ordinals.put("first", 1);
        ordinals.put("second", 2);
        ordinals.put("third", 3);
        ordinals.put("fourth", 4);
        ordinals.put("fifth", 5);
        ordinals.put("sixth", 6);
        ordinals.put("seventh", 7);
        ordinals.put("eighth", 8);
        ordinals.put("ninth", 9);
        ordinals.put("tenth", 10);
        ordinals.put("eleventh", 11);
        ordinals.put("twelfth", 12);
        ordinals.put("thirteenth", 13);
        ordinals.put("fourteenth", 14);
        ordinals.put("fifteenth", 15);
        ordinals.put("sixteenth", 16);
        ordinals.put("seventeenth", 17);
        ordinals.put("eighteenth", 18);
        ordinals.put("nineteenth", 19);
        ordinals.put("twentieth", 20);
        ordinals.put("thirtieth", 30);
        ordinals.put("fortieth", 40);
        ordinals.put("fiftieth", 50);
        ordinals.put("sixtieth", 60);
        ordinals.put("seventieth", 70);
        ordinals.put("eightieth", 80);
        ordinals.put("ninetieth", 90);
        ordinals.put("hundredth", 100);
        ordinals.put("thousandth", 1000);

        Orders.add("trillion");
        Orders.add("bn");
        Orders.add("billion");
        Orders.add("mn");
        Orders.add("million");
        Orders.add("thousand");
        Orders.add("hundred");
        Orders.add("dozen");

        int count;
        NUMBER = "(?:";
        count = 0;
        for (String key : str2num.keySet()) {
            if (count != 0)
                NUMBER += "|";
            NUMBER += key;
            count++;
        }
        NUMBER += ")";

        FRACTION = "(?:";
        count = 0;
        for (String key : fractions.keySet()) {
            if (count != 0)
                FRACTION += "|";
            FRACTION += key;
            count++;
        }
        FRACTION += ")";

        ORDINAL = "(?:";
        count = 0;
        for (String key : ordinals.keySet()) {
            if (count != 0)
                ORDINAL += "|";
            ORDINAL += key;
            count++;
        }
        ORDINAL += ")";
        DECIMAL = "(?:\\d*\\.\\d+|\\d+)";
        NEG_DECIMAL = "-" + DECIMAL;
        ANYNUMBERLIKETHING = "(?:" + NUMBER + "|" + FRACTION + "|" + ORDINAL + ")";
        decimalPat = Pattern.compile(DECIMAL);
        negDecimalPat = Pattern.compile(NEG_DECIMAL);
        removeScientificPat =
                Pattern.compile("([\\-0-9]+)(?:\\.([0-9]*))?e(\\-|\\+|)(\\d+)",
                        Pattern.CASE_INSENSITIVE);
        commaPat = Pattern.compile(",", Pattern.CASE_INSENSITIVE);
        anyNumberLikeThingPat = Pattern.compile(ANYNUMBERLIKETHING, Pattern.CASE_INSENSITIVE);
        prefixUsdPat = Pattern.compile("(US\\$|\\$)\\s*([\\d\\.]+)", Pattern.CASE_INSENSITIVE);
    }

    public static Quantity extractNumberFromTokenizedWords(List<String> tokens, boolean returnUnits) {
        Matcher matcher;
        // System.out.println("Input to extractNumberFromTokenizedWords : "+tokens);
        if (tokens.size() == 1) {
            matcher = decimalPat.matcher(tokens.get(0));
            if (matcher.find()) {
                return new Quantity(null, Double.parseDouble(matcher.group()), null);
            }
        }
        double num = 0;
        int indexOfLastOrder = -1;
        int indexOfLastNumber = -1;
        List<String> numberForOrder = new ArrayList<String>();
        for (String order : Orders) {
            numberForOrder.clear();
            for (int i = indexOfLastOrder + 1; i < tokens.size(); i++) {
                if (tokens.get(i).equals(order)) {
                    Quantity q = extractNumberFromTokenizedWords(numberForOrder, false);
                    if(q == null || q.value == null) continue;
                    num += q.value * str2num.get(order);
                    // System.out.println("Token : "+tokens.get(i)+" Num : "+num);
                    indexOfLastOrder = i;
                    indexOfLastNumber = i;
                    break;
                } else {
                    numberForOrder.add(tokens.get(i));
                }
            }
        }
        // Only other case of multiplication is three-fourths
        for (int i = indexOfLastOrder + 1; i < tokens.size(); i++) {
            if (tokens.get(i).equals("a")) {
                tokens.set(i, "one");
            }
            if (str2num.containsKey(tokens.get(i))
                    && i < tokens.size() - 1 // three-forths
                    && fractions.containsKey(tokens.get(i + 1))
                    && (str2num.get(tokens.get(i)) % 10 != 0 || str2num.get(tokens.get(i)) < fractions
                            .get(tokens.get(i + 1)))) {
                num += str2num.get(tokens.get(i)) * 1.0 / fractions.get(tokens.get(i + 1));
                indexOfLastNumber = i + 1;
                break;
            } else if (str2num.containsKey(tokens.get(i)) && i < tokens.size() - 1
                    && ordinals.containsKey(tokens.get(i + 1))) {
                num += str2num.get(tokens.get(i)) + ordinals.get(tokens.get(i + 1));
                indexOfLastNumber = i + 1;
                break;
            } else if (str2num.containsKey(tokens.get(i))) {
                num += str2num.get(tokens.get(i));
                indexOfLastNumber = i;
            } else if (fractions.containsKey(tokens.get(i))) {
                num += 1.0 / fractions.get(tokens.get(i));
                indexOfLastNumber = i;
            }
            // System.out.println("Token : "+tokens.get(i)+" Num : "+num);
        }
        if (num == 0.0) {
            return null;
        }
        // Dont have to find units if not the first call
        if (returnUnits) {
            String unit = "";
            for (int i = indexOfLastNumber + 1; i < tokens.size(); ++i) {
                unit += tokens.get(i) + " ";
            }
            return new Quantity(null, num, unit.trim());
        } else {
            return new Quantity(null, num, "");
        }
    }

    // Assumption : Number either completely in words or in numbers
    public static Quantity extractNumber(String phrase) {
        // System.out.println("Extract number: "+phrase);
        phrase = preProcessing(phrase);
        // System.out.println("Extract number: "+phrase);
        Matcher matcher;
        Quantity quantity = null;
        matcher = anyNumberLikeThingPat.matcher(phrase);
        if (matcher.find()) {
            quantity = extractNumberFromWords(phrase.trim());
            if (quantity != null) {
                return postProcessing(quantity);
            }
        }
        matcher = negDecimalPat.matcher(phrase);
        if (matcher.find()) {
            quantity =
                    new Quantity(null, Double.parseDouble(matcher.group()),
                            phrase.substring(matcher.end()));
            // System.out.println("Extract number: "+quantity.value);
            return postProcessing(quantity);
        }
        matcher = decimalPat.matcher(phrase);
        if (matcher.find()) {
            quantity =
                    new Quantity(null, Double.parseDouble(matcher.group()),
                            phrase.substring(matcher.end()));
            return postProcessing(quantity);
        }
        return postProcessing(quantity);
    }

    public static String preProcessing(String phrase) {
        phrase = removeScientificNotation(phrase);
        phrase = replaceCommas(phrase);
        phrase = prefixUsd(phrase);
        return phrase;
    }

    public static Quantity postProcessing(Quantity quantity) {
        return quantity;
    }

    public static Quantity extractNumberFromWords(String phrase) {
        String splitUp[] = phrase.split("[-\\s]");
        return extractNumberFromTokenizedWords(Arrays.asList(splitUp), true);
    }

    public static String prefixUsd(String phrase) {
        Matcher matcher = prefixUsdPat.matcher(phrase);
        if (matcher.find()) {
            phrase = phrase.replace(matcher.group(1), "");
            phrase += "US$";
        }
        return phrase;
    }

    public static String removeScientificNotation(String str) {
        Matcher matcher = removeScientificPat.matcher(str);
        if (matcher.find()) {
            String lead_digit = matcher.group(1);
            String trail_digits = matcher.group(2);
            String sign = matcher.group(3);
            String exponent = matcher.group(4);
            trail_digits = "0";
            if (trail_digits != null)
                if (trail_digits.length() > 0)
                    trail_digits = matcher.group(2);
            String zeros = "";
            int exp = (int) Double.parseDouble(exponent);
            for (int i = 0; i < exp - 1; i++)
                zeros += "0";
            if (sign.equals("-"))
                return str.replace(matcher.group(), "0." + zeros + lead_digit + trail_digits);
            else {
                trail_digits += zeros;
                return str.replace(matcher.group(), lead_digit + trail_digits.substring(0, exp)
                        + "." + trail_digits.substring(exp, trail_digits.length()));
            }
        }
        return str;
    }

    public static String replaceCommas(String phrase) {
        Matcher matcher = commaPat.matcher(phrase);
        while (matcher.find()) {
            phrase = phrase.replace(matcher.group(), "");
        }
        return phrase;
    }

    public static void main(String args[]) {
        Numbers.initialize();
        System.out.println(extractNumber("a hundred and forty and a half dollars"));
        System.out.println(extractNumber("two fifth century"));
        System.out.println(extractNumber("2.3e5"));
        System.out.println(extractNumber("50,000 dollars"));
        System.out.println(extractNumber("70 cents"));
    }


}
