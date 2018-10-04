/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.standardize;

import java.util.regex.*;
import java.util.*;

/**
 * Collection of regex es which are not currently used The functions are not expected to work now,
 * kept only as an archive
 * 
 * @author subhroroy
 *
 */

public class Archive {

    public static Pattern dollarRewritePat, removesPat, inAPat, ofPat, dateCheckPat,
            yuckyHyphenPat, teenagerPat, stNdPat, percentPat, secondsPat, perSecondPat,
            secondPerPat, mostPat, severalPat, ofOfPat, monthOfPat;

    public static Pattern fracToDoublePat, aOrAnThenNumberPat, numberWordHyphenationPat,
            ordinalDanglersPat, danglersPat, danglingAddPat, danglingMultPat, usdPat, feetPat,
            inchPat, separateSuffixPat, allPat, mForMillionPat, prefixUsdPat, prefixGbpPat,
            prefixEuroPat, aAnPat, digitPat, notSurePat, anAPerPat, justAnAPerPat, frac1Pat;

    public static Pattern intWillBeIntPat, removeScientificPat, standardizeDecimalPat,
            pryNumbersFromLettersPat, pryNumbersFromSymbolsPat, commaPat, dontFloatUnlessFloatPat,
            tightenDanglingSPat, tightenSpacePat;

    public static Pattern wordSplitPat[];

    public static void initialize() {


        wordSplitPat = new Pattern[25];
        // TODO : Accommodate negative numbers and nesting
        wordSplitPat[0] = Pattern.compile("\\-\\-*");
        wordSplitPat[1] = Pattern.compile("\\'\\'([^\\'\\w]|$)");
        wordSplitPat[2] = Pattern.compile("(^|[^\\'\\w\\.\\,\\:\\;\\!\\?])\\'\\'");
        wordSplitPat[3] = Pattern.compile("([^\\w\\s\\`])([^\\w\\s\\`])");
        wordSplitPat[4] = Pattern.compile("(^|\\W)(\\')(\\w)");
        wordSplitPat[5] = Pattern.compile("(\\w)(\\')(\\W|$)");
        wordSplitPat[6] = Pattern.compile("(\\w)\\s*'\\s*s(\\W|$)");
        wordSplitPat[7] = Pattern.compile("(\\S)([^\\w\\s\\`\\.\\,\\-$])");
        wordSplitPat[8] = Pattern.compile("([^\\w\\s\\`\\'\\.\\,\\-])(\\S)");
        wordSplitPat[9] = Pattern.compile("([^\\`])(\\`)");
        wordSplitPat[10] = Pattern.compile("(\\`)([^\\`])");
        wordSplitPat[11] = Pattern.compile("(^|\\s)\\`\\`\\`");
        wordSplitPat[12] = Pattern.compile("(\\S)(\\-)(\\s|$)");
        wordSplitPat[13] = Pattern.compile("(^|\\s)(\\-)(\\S)");
        dollarRewritePat =
                Pattern.compile(
                        "((?:U.?S.?s*(?:dollars?|dlrs?|US\\$)|dlrs?|dollars?|US\\s*\\$USD|USD))",
                        Pattern.CASE_INSENSITIVE);

        removesPat =
                Pattern.compile("^\\s*(?:a|1)\\s+|metric|[\"\',{}]|(?:\\W|^)th(?:\\W|$)",
                        Pattern.CASE_INSENSITIVE);

        inAPat = Pattern.compile("\\s+(in\\s+/)\\s+", Pattern.CASE_INSENSITIVE);

        ofPat = Pattern.compile("^of\\s", Pattern.CASE_INSENSITIVE);

        dateCheckPat =
                Pattern.compile("((\\d{1,4}|" + Date.MONTH + ")\\s*[/\\-\\.]\\s*(\\d\\d?|"
                        + Date.MONTH + ")\\s*[/\\-\\.]\\s*(\\d{1,4}|" + Date.MONTH + "))",
                        Pattern.CASE_INSENSITIVE);

        yuckyHyphenPat =
                Pattern.compile("(.*?)(\\d+.*?)(?:%u2013|-)(.*?\\d+.*?)", Pattern.CASE_INSENSITIVE);

        teenagerPat = Pattern.compile("teenager?s?", Pattern.CASE_INSENSITIVE);

        stNdPat = Pattern.compile("\\s*<sup>(st|nd|rd|th)</sup>", Pattern.CASE_INSENSITIVE);

        percentPat = Pattern.compile("(per cent|\\%)", Pattern.CASE_INSENSITIVE);

        secondsPat = Pattern.compile("(\\d+\\s*)(seconds?)", Pattern.CASE_INSENSITIVE);

        perSecondPat = Pattern.compile("(per\\s*)(seconds?)", Pattern.CASE_INSENSITIVE);

        secondPerPat = Pattern.compile("(seconds?)(\\s*per)", Pattern.CASE_INSENSITIVE);

        mostPat = Pattern.compile("^\\s*most\\s*(.*)", Pattern.CASE_INSENSITIVE);

        severalPat = Pattern.compile("([Ss]everal)", Pattern.CASE_INSENSITIVE);

        ofOfPat = Pattern.compile("\\s+of\\s*of\\s+", Pattern.CASE_INSENSITIVE);

        monthOfPat =
                Pattern.compile("(" + Date.MONTH + ")\\s*of\\s*(\\d{4}|\\d{2})",
                        Pattern.CASE_INSENSITIVE);
        fracToDoublePat =
                Pattern.compile("(?:(" + Numbers.NUMBER + ")\\s*\\-?\\s*(" + Numbers.FRACTION
                        + ")(s?)|(?:\\W|^)(?:an|a|one)\\s*-?\\s*(" + Numbers.FRACTION + "))",
                        Pattern.CASE_INSENSITIVE);

        aOrAnThenNumberPat =
                Pattern.compile("(?:\\W|^)(an|a)\\s*(" + Numbers.ANYNUMBERLIKETHING + ")(?:\\W|$)",
                        Pattern.CASE_INSENSITIVE);

        numberWordHyphenationPat =
                Pattern.compile("(\\s*(" + Numbers.NUMBER + "|" + Numbers.FRACTION + ")\\s*-\\s*("
                        + Numbers.NUMBER + "|" + Numbers.FRACTION + "|" + Numbers.ORDINAL
                        + ")\\s*)", Pattern.CASE_INSENSITIVE);

        ordinalDanglersPat =
                Pattern.compile("(([^\\s]|^)\\s+(" + Numbers.ORDINAL + ")[^s](.*))",
                        Pattern.CASE_INSENSITIVE);

        danglersPat =
                Pattern.compile("(([^a-z]|^)(" + Numbers.NUMBER + "|" + Numbers.FRACTION
                        + ")(\\W|$))", Pattern.CASE_INSENSITIVE);

        danglingAddPat =
                Pattern.compile("(" + Numbers.DECIMAL + ")\\s+(" + Numbers.DECIMAL + ")",
                        Pattern.CASE_INSENSITIVE);

        danglingMultPat =
                Pattern.compile("(" + Numbers.DECIMAL + ")\\s+(" + Numbers.DECIMAL + ")",
                        Pattern.CASE_INSENSITIVE);

        usdPat =
                Pattern.compile("(USD|US\\$|\\$US|US dlrs\\.?|dollars?|dlrs?\\.?|\\$)",
                        Pattern.CASE_INSENSITIVE);

        feetPat = Pattern.compile("(\\d)\\s*'", Pattern.CASE_INSENSITIVE);

        inchPat = Pattern.compile("(\\d)\\s*\"", Pattern.CASE_INSENSITIVE);

        separateSuffixPat =
                Pattern.compile("(" + Numbers.NUMBER + "|" + Numbers.FRACTION + ")s",
                        Pattern.CASE_INSENSITIVE);

        allPat = Pattern.compile("^\\s*all\\s*(.*)", Pattern.CASE_INSENSITIVE);

        // convert special case of a/an
        aAnPat = Pattern.compile("(and|&|^)\\s+(a|an)(\\W+)", Pattern.CASE_INSENSITIVE);

        digitPat = Pattern.compile("\\d", Pattern.CASE_INSENSITIVE);

        // Rearrange prefixed units of currency
        prefixUsdPat = Pattern.compile("(US\\$|\\$)\\s*([\\d\\.]+)", Pattern.CASE_INSENSITIVE);
        prefixEuroPat = Pattern.compile("Euros\\s*([\\d\\.]+)", Pattern.CASE_INSENSITIVE);
        prefixGbpPat = Pattern.compile("GBP\\s*([\\d\\.]+)", Pattern.CASE_INSENSITIVE);

        // The little 'm' must mean 'millions' in this case..
        mForMillionPat = Pattern.compile("(US\\$|Euros|GBP) m(\\W|$)", Pattern.CASE_INSENSITIVE);

        notSurePat = Pattern.compile("(\\d+\\.\\d+)\\.(?=[^\\d])", Pattern.CASE_INSENSITIVE);

        anAPerPat = Pattern.compile("(\\W|^)(an|a|per)\\s+([^0-9])", Pattern.CASE_INSENSITIVE);

        justAnAPerPat = Pattern.compile("(an|a|per|-)", Pattern.CASE_INSENSITIVE);

        frac1Pat =
                Pattern.compile(
                        "(?:\\s*(\\d+/\\d+/\\d+)\\s*|(?=[^/\\.\\-0-9])\\s+(\\d+\\.\\d+|\\d+)"
                                + "\\s*/\\s*(\\d+)(?:\\W+|$))", Pattern.CASE_INSENSITIVE);

        intWillBeIntPat = Pattern.compile("\\d+\\.\\d+", Pattern.CASE_INSENSITIVE);
        removeScientificPat =
                Pattern.compile("([\\-0-9]+)(?:\\.([0-9]*))?e(\\-|\\+|)(\\d+)",
                        Pattern.CASE_INSENSITIVE);
        standardizeDecimalPat =
                Pattern.compile("(\\d),(\\d\\d)([^\\d]|$)", Pattern.CASE_INSENSITIVE);

        pryNumbersFromLettersPat = Pattern.compile("(\\d+)([a-zA-Z]+)", Pattern.CASE_INSENSITIVE);
        pryNumbersFromSymbolsPat =
                Pattern.compile("([^\\d,\\./]+)(\\d+)", Pattern.CASE_INSENSITIVE);
        commaPat = Pattern.compile(",", Pattern.CASE_INSENSITIVE);

        // don't float unless its float
        dontFloatUnlessFloatPat = Pattern.compile("([\\d]+)\\.0(\\W|$)", Pattern.CASE_INSENSITIVE);
        // tighten up dangling s
        tightenDanglingSPat = Pattern.compile("([\\d\\.]+)\\s*s(\\W|$)", Pattern.CASE_INSENSITIVE);
        // tighten up spaces
        tightenSpacePat = Pattern.compile("(^\\s+|\\s+|\\s+$)", Pattern.CASE_INSENSITIVE);
    }



    public static String Frac1(String str) {
        // non-num NUM/NUM
        Matcher matcher = frac1Pat.matcher(str);
        if (matcher.find()) {
            if (matcher.group(1) != null)
                return str.replace(matcher.group(), matcher.group(1));
            else
                return str.replace(
                        matcher.group(),
                        " "
                                + (Float.parseFloat(matcher.group(2).trim()) * 1.0 / Float
                                        .parseFloat(matcher.group(3).trim())) + " ");
        }
        return str;
    }

    public static String Frac2double(String str) {
        str = str.trim();
        Matcher matcher = fracToDoublePat.matcher(str);
        if (matcher.find()) {
            if (matcher.group(4) != null)
                if (matcher.group(4).length() > 0)
                    return str.replace(matcher.group(),
                            " " + (1.0 / Numbers.fractions.get(matcher.group(4).toLowerCase())));
            if (matcher.group(3).equals("") == false) {
                if (matcher.group(1).equals("")) {
                    return str.replace(matcher.group(),
                            " " + (1.0 / Numbers.fractions.get(matcher.group(2).toLowerCase())));
                }
                return str
                        .replace(
                                matcher.group(),
                                ""
                                        + (Numbers.str2num.get(matcher.group(1).toLowerCase()) * 1.0 / Numbers.fractions
                                                .get(matcher.group(2).toLowerCase())));
            } else {
                int a = Numbers.str2num.get(matcher.group(1).toLowerCase());
                int b = Numbers.fractions.get(matcher.group(2).toLowerCase());
                if (a > b && a >= 20)
                    return str.replace(matcher.group(), "" + (a + b));
                else
                    return str.replace(matcher.group(), "" + (a * 1.0 / b));
            }
        }
        return str;
    }

    // e.g., "a thousand dollars' => 'one thousand dollars'
    public static String AorAnThenNumber(String str) {
        Matcher matcher = aOrAnThenNumberPat.matcher(str);
        if (matcher.find()) {
            return str.replace(matcher.group(), " one " + matcher.group(2) + " ");
        }
        return str;
    }

    public static String NumberWordHyphenation(String str) {
        Matcher matcher = numberWordHyphenationPat.matcher(str);
        int x = 0, y = 0;
        if (matcher.find()) {
            // System.err.println( matcher.group(2)+" "+matcher.group(3));
            if (Numbers.str2num.containsKey(matcher.group(2).toLowerCase()))
                x = Numbers.str2num.get(matcher.group(2).toLowerCase());
            else if (Numbers.fractions.containsKey(matcher.group(2).toLowerCase()))
                x = Numbers.fractions.get(matcher.group(2).toLowerCase());
            if (Numbers.Orders.contains(matcher.group(3).toLowerCase()))
                return str.replace(matcher.group(), matcher.group(1));
            if (Numbers.str2num.containsKey(matcher.group(3).toLowerCase()))
                y = Numbers.str2num.get(matcher.group(3).toLowerCase());
            else if (Numbers.fractions.containsKey(matcher.group(3).toLowerCase()))
                y = Numbers.fractions.get(matcher.group(3).toLowerCase());
            else if (Numbers.ordinals.containsKey(matcher.group(3).toLowerCase()))
                y = Numbers.ordinals.get(matcher.group(3).toLowerCase());
            return str.replace(matcher.group(), " " + (x + y) + " ");
        }
        return str;
    }


    public static String OrdinalDanglers(String str) {

        Matcher matcher = ordinalDanglersPat.matcher(str);
        if (matcher.find()) {
            String chunk = matcher.group(1);
            String before = matcher.group(2);
            String x = matcher.group(3);
            String after = matcher.group(4);
            Matcher m = justAnAPerPat.matcher(chunk);

            if (x.toLowerCase().equals("second") == false)
                return str.replace(matcher.group(),
                        before + " " + Numbers.ordinals.get(x.toLowerCase()) + " " + after + " ");
            else if (after.trim().length() > 0 && m.find() == false)
                return str.replace(matcher.group(),
                        before + " " + Numbers.ordinals.get(x.toLowerCase()) + " " + after + " ");
            else
                return str.replace(matcher.group(), chunk);

        }
        return str;

    }


    public static String Danglers(String str) {
        // Converts numbers or fractions just sitting by themselves
        Matcher matcher = danglersPat.matcher(str);
        if (matcher.find()) {

            String chunk = matcher.group(1);
            String junkA = matcher.group(2);
            String x = matcher.group(3);
            String junkB = matcher.group(4);

            if (Numbers.str2num.containsKey(x.toLowerCase()))
                return str.replace(matcher.group(), junkA + Numbers.str2num.get(x.toLowerCase())
                        + junkB);
            else if (Numbers.fractions.containsKey(x.toLowerCase()))
                return str.replace(matcher.group(),
                        junkA + (1 / Numbers.fractions.get(x.toLowerCase())) + junkB);
            else
                return str.replace(matcher.group(), chunk);
        }
        return str;
    }


    public static String DanglingAddition(String str) {

        Matcher matcher = danglingAddPat.matcher(str);
        if (matcher.find()) {
            Double x = Double.parseDouble(matcher.group(1));
            Double y = Double.parseDouble(matcher.group(2));
            if (addibleNeighbors(x, y))
                return str.replace(matcher.group(), "" + (x + y) + "");
            else
                return str.replace(matcher.group(), x + " " + y);
        }
        return str;
    }


    // I have no clue why this function is same as the previous one
    public static String DanglingMultiplication(String str) {

        Matcher matcher = danglingMultPat.matcher(str);
        if (matcher.find()) {
            Double x = Double.parseDouble(matcher.group(1));
            Double y = Double.parseDouble(matcher.group(2));
            if (Math.floor(Math.log(x)) != Math.floor(Math.log(y)))
                return str.replace(matcher.group(), "" + (x * y));
            else
                return str.replace(matcher.group(), "" + ((1.0 / x) * y));

        }
        return str;
    }


    public static boolean safe_double(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    public static boolean addibleNeighbors(double x, double y) {
        if (x == 0.0 || y == 0.0)
            return true;
        return x >= Math.pow(10, Math.ceil(Math.log10(y)));
    }

    public static List<String> addReduce(List<String> splitup) {
        double total = 0.0;
        List<String> phrase = new ArrayList<String>();
        for (String token : splitup) {
            String x = token;
            if (safe_double(token)) {

                if (addibleNeighbors(total, Double.parseDouble(token)))
                    total += Double.parseDouble(token);
                else
                    token += "sd "; // Forcefully making it non-number
            }
            if (safe_double(token) == false) {
                if (total != 0.0) {
                    phrase.add("" + total);
                    total = 0.0;
                }
                phrase.add(x);
            }
        }
        if (total != 0.0)
            phrase.add("" + total);
        // System.err.println("After add_reduce :"+Arrays.toString(phrase.toArray()));
        return phrase;
    }


    public static List<String> andReduce(List<String> splitup) {
        // XX and YY => XX+YY
        // System.err.println("Before and_reduce :"+Arrays.toString(splitup.toArray()));
        int N = splitup.size();
        int i = 0;
        double x = 0.0, y = 0.0;
        while (i < N - 2) {
            // only collapse "and" between the two numbers
            i++;
            if (splitup.get(i).toLowerCase().equals("and")) {
                if (safe_double(splitup.get(i - 1)))
                    x = Double.parseDouble(splitup.get(i - 1));
                else
                    continue;
                if (safe_double(splitup.get(i + 1)))
                    y = Double.parseDouble(splitup.get(i + 1));
                else
                    continue;
                splitup.remove(i - 1);
                splitup.remove(i - 1);
                splitup.set(i - 1, "" + (x + y));
                N -= 2;
            }
        }
        return splitup;
    }

    public static List<String> Reduce(List<String> splitup) {
        splitup = addReduce(splitup);
        splitup = andReduce(splitup);
        return splitup;
    }


    public static List<String> mulReduce(String order, List<String> splitup) {
        // System.err.println("Before mul_reduce :"+Arrays.toString(splitup.toArray())+" "
        // + "Order:"+order);
        double x;
        String str;

        for (int i = 0; i < splitup.size(); i++) {
            str = splitup.get(i);
            if (str.toLowerCase().equals(order)) {
                if (i == 0)
                    continue;
                splitup.remove(str);
                if (safe_double(splitup.get(i - 1)))
                    x = Double.parseDouble(splitup.get(i - 1));
                else
                    x = 1.0;

                splitup.set(i - 1, "" + (x * Numbers.str2num.get(order)));
                i--;
            }
        }
        // System.err.println("After mul_reduce :"+Arrays.toString(splitup.toArray()));
        return splitup;
    }

    public static String collapseConsecutiveNumbers(String phrase) {
        // perform the conversion of long written err numbers
        List<String> splitup = new ArrayList<String>(Arrays.asList(phrase.split("[\\s\\-]")));
        // System.err.println("Before trimming: "+phrase);
        // Remove empty strings
        for (int i = 0; i < splitup.size(); i++) {
            if (splitup.get(i).trim().equals("")) {
                splitup.remove(splitup.get(i));
                i--;
            }
        }
        // System.err.println("Before repl: "+Arrays.toString(splitup.toArray()) );
        for (int i = 0; i < splitup.size(); i++)
            splitup.set(i, repl(splitup.get(i)));
        // System.err.println("After repl: "+Arrays.toString(splitup.toArray()) );
        splitup = Reduce(splitup);
        for (String order : Numbers.Orders) {
            splitup = mulReduce(order, splitup);
            splitup = Reduce(splitup);
        }
        phrase = " ";
        for (String str : splitup)
            phrase += str + " ";
        phrase = Danglers(phrase);
        return phrase;
    }

    public static String repl(String x) {
        String w = x.toLowerCase().trim();
        if (Numbers.fractions.containsKey(w)) {
            if (Numbers.fractions.get(w) < 10)
                return "" + (1.0 / Numbers.fractions.get(w));
        }
        if (Numbers.Orders.contains(w) == false && Numbers.str2num.containsKey(w) == true) {
            return "" + Numbers.str2num.get(w);
        } else {
            return x;
        }
    }

    public static String std_currency(String phrase) {
        // Standardize units of currency
        Matcher matcher = usdPat.matcher(phrase);
        if (matcher.find()) {
            phrase = phrase.replace(matcher.group(), " US$ ");
        }

        // pattern = Pattern.compile("(\\W|^)(€|%u20AC|Euros?|EUR|Eu|E)([\\s\\d])",
        // Pattern.CASE_INSENSITIVE );
        // matcher = pattern.matcher( phrase );
        // if(matcher.find())
        // phrase = phrase.replace( matcher.group(), matcher.group(1)+" Euros "+matcher.group(3) );
        //
        //
        // pattern = Pattern.compile("(\\W|^)(Â£|£|GBP)([\\s\\d])", Pattern.CASE_INSENSITIVE );
        // matcher = pattern.matcher( phrase );
        // if(matcher.find())
        // phrase = phrase.replace( matcher.group(), matcher.group(1)+" GBP "+matcher.group(3) );

        return phrase;
    }

    public static String preprocessing(String phrase) {
        // Standardize foreign, decimal points
        Matcher matcher = standardizeDecimalPat.matcher(phrase);
        if (matcher.find())
            phrase =
                    phrase.replace(matcher.group(), matcher.group(1) + "." + matcher.group(2) + " "
                            + matcher.group(3));
        phrase = RemoveScientificNotation(phrase);
        // pry numbers from letters
        matcher = pryNumbersFromLettersPat.matcher(phrase);
        if (matcher.find())
            phrase =
                    phrase.replace(matcher.group(), matcher.group(1) + " " + matcher.group(2) + " ");
        // pry numbers from some symbols
        matcher = pryNumbersFromSymbolsPat.matcher(phrase);
        if (matcher.find())
            phrase =
                    phrase.replace(matcher.group(), " " + matcher.group(1) + " " + matcher.group(2));
        // remove commas and pad the string
        matcher = commaPat.matcher(phrase);
        if (matcher.find())
            phrase = (" " + phrase + " ").replace(matcher.group(), "");

        return phrase;

    }


    public static String postprocess(String phrase) {
        // don't float unless its float
        Matcher matcher = dontFloatUnlessFloatPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1) + " " + matcher.group(2));
        // tighten up dangling s
        matcher = tightenDanglingSPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1) + "s " + matcher.group(2));
        // tighten up spaces
        matcher = tightenSpacePat.matcher(phrase);
        while (matcher.find())
            phrase = phrase.replace(matcher.group(), " ");
        return phrase;
    }

    public static String feet_and_inches_shorthand(String phrase) {
        Matcher matcher = feetPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1) + " feet");
        matcher = inchPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1) + " inches");
        return phrase;
    }

    public static String separate_s_suffix_from_number_words(String phrase) {
        Matcher matcher = separateSuffixPat.matcher(phrase);
        if (matcher.find()) {
            phrase = phrase.replace(matcher.group(), matcher.group(1) + " s");
        }
        return phrase;
    }

    public static String RemoveScientificNotation(String str) {
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

    // Converts words to number values
    public static String convert2numbers(String phrase) {
        phrase = feet_and_inches_shorthand(phrase);
        System.out.println(phrase);

        phrase = preprocessing(phrase);
        // System.err.println("After preprocessing: "+phrase );

        // Standardize units of currency
        phrase = std_currency(phrase);
        System.out.println(phrase);
        // resolve a special case of a/an
        phrase = AorAnThenNumber(phrase);
        // System.err.println("After AorAn: "+phrase );

        // convert fractions
        phrase = Frac1(phrase);
        // System.err.println("After frac1: "+phrase );
        phrase = Frac2double(phrase);
        // System.err.println("After frac2double: "+phrase );

        // separate the 's' from number words
        phrase = separate_s_suffix_from_number_words(phrase);
        // System.err.println("After Separate s: "+phrase );
        phrase = NumberWordHyphenation(phrase);
        // System.err.println("After Hyphenation:: "+phrase );

        // perform the conversion of long written err numbers
        phrase = collapseConsecutiveNumbers(phrase);
        // System.err.println("After collapse consective: "+phrase );

        // scientific notation
        phrase = RemoveScientificNotation(phrase);
        // System.err.println("After RemoveScientificNotation: "+phrase );

        // convert special case of a/an
        Matcher matcher = aAnPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1) + " 1.0 " + matcher.group(3));

        matcher = digitPat.matcher(phrase);
        if (matcher.find())
            matcher = anAPerPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1) + " / " + matcher.group(3));

        // Rearrange prefixed units of currency
        matcher = prefixUsdPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(2) + " US$");
        System.out.println(phrase);
        matcher = prefixEuroPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1) + " Euros");

        matcher = prefixGbpPat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1) + " GBP");

        // The little 'm' must mean 'millions' in this case..
        // System.err.println("Before little m: "+phrase);
        matcher = mForMillionPat.matcher(phrase);
        if (matcher.find())
            phrase =
                    phrase.replace(matcher.group(),
                            "million " + matcher.group(1) + " " + matcher.group(2));
        // System.err.println("After little m: "+phrase);

        phrase = postprocess(phrase);

        // Convert some special cases of lonely numbers
        phrase = Danglers(phrase);
        // System.err.println("After danglers: "+phrase);
        phrase = OrdinalDanglers(phrase);
        // System.err.println("After Ordinal danglers: "+phrase);
        phrase = DanglingAddition(phrase);
        // System.err.println("After dangling addition: "+phrase);
        phrase = DanglingMultiplication(phrase);
        // System.err.println("After dangling: "+phrase);

        matcher = notSurePat.matcher(phrase);
        if (matcher.find())
            phrase = phrase.replace(matcher.group(), matcher.group(1));

        phrase = postprocess(phrase);
        // System.err.println("After second preprocessing: "+phrase);

        return phrase;


    }


    public static String wordsplitSentence(String sentence) {
        Matcher matcher;
        String before = "";
        // Separate single quotes that don't look like apostrophes
        matcher = wordSplitPat[4].matcher(sentence);
        while (matcher.find()) {
            sentence =
                    sentence.replace(matcher.group(), matcher.group(1) + matcher.group(2) + " "
                            + matcher.group(3));
        }
        matcher = wordSplitPat[5].matcher(sentence);
        while (matcher.find()) {
            sentence =
                    sentence.replace(matcher.group(), matcher.group(1) + " " + matcher.group(2)
                            + matcher.group(3));
        }

        // We want 's not ' s
        matcher = wordSplitPat[6].matcher(sentence);
        while (matcher.find()) {
            sentence =
                    sentence.replace(matcher.group(), matcher.group(1) + " 's " + matcher.group(2));
        }

        // Separate contractions
        matcher = wordSplitPat[7].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), matcher.group(1) + " " + matcher.group(2));
        }
        matcher = wordSplitPat[8].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), matcher.group(1) + " " + matcher.group(2));
        }

        // Separate opening single quotes from everything else
        matcher = wordSplitPat[9].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), matcher.group(1) + " " + matcher.group(2));
        }
        matcher = wordSplitPat[10].matcher(sentence);
        while (matcher.find()) {
            sentence = sentence.replace(matcher.group(), matcher.group(1) + " " + matcher.group(2));
        }
        before = "";
        while (before.equals(sentence) == false) {
            before = sentence;
            matcher = wordSplitPat[11].matcher(sentence);
            while (matcher.find()) {
                sentence = sentence.replace(matcher.group(), matcher.group(1) + "\\`\\` \\`");
            }
        }

        // Separate stray dashes
        matcher = wordSplitPat[12].matcher(sentence);
        while (matcher.find()) {
            sentence =
                    sentence.replace(matcher.group(), matcher.group(1) + " " + matcher.group(2)
                            + matcher.group(3));
        }
        matcher = wordSplitPat[13].matcher(sentence);
        while (matcher.find()) {
            sentence =
                    sentence.replace(matcher.group(), matcher.group(1) + matcher.group(2) + " "
                            + matcher.group(3));
        }
        return sentence;
    }

    public static void test(String phrase, String target) {
        System.out.println("Phrase: " + phrase);
        System.out.println("Target: " + target);
        String converted = convert2numbers(phrase);
        System.out.println("Converted: " + converted);
    }


    public static void main(String args[]) {

        // System.out.println("NUMBER: "+NUMBER);
        // System.out.println("FRACTION: "+FRACTION );
        // test("Five", "5");
        // test("half", "0.5");
        // test("2/5","str(2/5)");
        // test("2.0/5","str(2/5)");
        // test("one half", "0.5");
        // test("two halves", "1.0");
        // test("two-halves", "1.0");
        // test("three halves", "1.5");
        // test("six fourths", "1.5");
        // test("thirty hundredths", "30/100");
        // test("sixteen - sevenths", "16/7");
        // test("an eighth", "1/8");
        // test("AN eiGHth", "1/8");
        // test("she only meets half of the requirements.",
        // "she only meets 0.5 of the requirements.");
        // test("one hundred", "100");
        // test("thirty-three", "33");
        // test("2 1/2", "2.5");
        // test("two and a half", "2.5");
        // test("26 189/234 barrels", "str(26 + 189/234) +  barrels");
        // test("1 and 3/18", "1 + 3/18");
        // test("six and a half", "6.5");
        // test("five and a quarter", "5.25");
        // test("one and twenty-sevenths", "str(1 + 20.0/7)");
        // test("one-fifth","1/5");
        // test("180.2 billion", "180.2e9");
        // test("four million twenty three and three halves", "4e6 + 23 + 3/2");
        // test("1 million and 2 thousand and 3 hundred and 4", "1e6 + 2e3 + 304");
        // test("1.2 million dollars", "str(1.2 * 1e6) +  US$");
        // test("1.2 million dollars and 50 cents", "str(1.2e6) +  US$ and 50 cents");
        // test("Two hours were spent at the seven mosques", "2.0 hours were spent at the "
        // + "7.0 mosques");
        // test("a quarter million dollars", "250000.0 US$");
        // test("one and a quarter", "1.25");
        // test("one and a quarter million dollars", "1.25e6 US$");
        // test("vacationers", "vacationers");
        // test("one dollars a share", "1.0 US$ / share");
        // test("more than 400 US$ a share", "more than 400 US$ / share");
        // test("$1.2 million","1200000.0 US$");
        // test("one hundred million", "100e6");
        // test("a dozen roses", "12 roses");
        // test("thirty-three thousand", "33000");
        // test("thirty-three thousand two-hundred", "33200");
        // test("1.2 million", "1.2 * 1e6");
        // test("1.2 million and 33", "1.2e6 + 33");
        // test("1.2 million and 33.0", "1.2e6 + 33");
        // test("fourty-four million twenty three", "44e6 + 23");
        // test("fourty-three million twenty three", "43e6 + 23");
        // test("four million twenty-six thousand eight hundred and three", "4e6 + 26e3 + 8e2 + 3");
        // test("1.2 million dollars and fifty cents", "str(1.2e6) + US$ and 50.0 cents");
        // test("one million two-hundred thousand", "1e6 + 200e3");
        // test("two hundred one million twenty-two hundred", "201e6 + 2200");
        // test("2.0 hundred 1.0", "201");
        // test("15 US$ and 16 cents", "15 US$ and 16 cents");
        // test("five hundred and 26 thousand twenty-two dollars and 16 cents", "526022 US$ and "
        // + "16 cents");
        // test("One billion Two Hundred Million Ninety-Nine Thousand Three Hundred and Five",
        // "1e9 + 200e6 + 99e3 + 305");
        // test("One billion Two Hundred Million Ninety-Nine Thousand Three Hundred and Five 66/100",
        // "1e9 + 200e6 + 99e3 + 305 + 0.66");
        // test("twenty five million two hundred and twenty-seven thousand and 66/100 meters per sec",
        // "str(25e6 + 227e3 + 66/100) + meters / sec");
        // test("9999999999999999999999999999999999999999999999999999999 dollars",
        // "9999999999999999999999999999999999999999999999999999999 US$");
        // test("0.000000000000000000000000000000000000000000000000000000000000000000000000"
        // + "0000000000003423 dollars",
        // "0.0000000000000000000000000000000000000000000000000000000000000000000"
        // + "000000000000000003423 US$");
        // test("$1,002,100,555,462.55", "1002100555462.55 US$");
        // test("1e+021","1" );
        // test(" Two billion one - million five - hundred and fifty five thousand 4 hundred sixty "
        // + "two dollars","1002001555460 US$");
        // test("$500000","500000 US$");
        // test("a man and a baby","1 man and 1 baby");
        // test("Two men and a baby", "2 men and 1 baby");
        // test("Two Men & a BaBy", "2 Men & 1 BaBy");
        // test("6 seconds", "6 seconds");
        // test("1 second", "1 second");
        // test("the twenty-first century", "the 21 century");
        // test("the twenty-second century", "the 22 century");
        // test("the sixteen hundreds", "the 1600s");
        // test("twenty first century", "21 century");
        // test("twenty-first century", "21 century");
        // test("the first round", "the 1.0 round");
        // test("$16 a barrel", "16 US$ / barrel");
        // test("four-hundred twenty-Two billion 3-million five - hundred and fifty five thousand
        // 4 hundred sixty two dollars","422003555462 US$");
        //
        // test("3/18/1986", "3/18/1986");
        // test("03/18/1986", "03/18/1986");
        // test("1986/03/18", "1986/03/18");
        //
        // test("thirty-fifth century", "35 century");
        // test("thirty fifth century", "35 century");
        // test("twenty-second century", "22 century");
        // test("twenty second century", "22 century");
        //
        // test("non-stop flight", "non stop flight");
        // test("the first non-stop flight","the 1 non stop flight");
        // test("6423,23","6423.23");
        //
        // test("5.7bn", "5700000000");
        // test("5.7 bn", "5700000000");
        // test("5.7 mn", "5700000");
        // test("5.7mn", "5700000");
        // test("5.7 million", "5700000");
        // test("5.7million", "5700000");
        //
        // test("thirty hundredth", "30/100");
        // test("sixteen seventh", "16/7");
        //
        // test("nearly half million dollar","nearly 500000 US$");
        // test("nearly one half million dollar","nearly 500000 US$");
        // test("nearly two halves million dollar","nearly 1000000 US$");
        // test("nearly quarter million dollar","nearly 250000 US$");
        // test("nearly half a million dollar","nearly 500000 US$");
        //
        // test("1.2e6 US$","1200000 US$");
        // test("500 Euros","500 Euros");
        // test("E500","500 Euros");
        // test("E500,33","500.33 Euros");
        // test("$66USD","66 US$");
        // test("£50", "50 GBP");
        // test("50GBP", "50 GBP");
        // test("GBP50", "50 GBP");
        // test("GBP 50", "50 GBP");
        //
        // test("£1m", "1000000 GBP");
        // test("£1men", "1 GBP men");
        //
        // test("20 additional six-car metro trains", "20 additional 6 car metro trains");
        // test("29\' 4 1/2\"", "29 feet 4.5 inches");
        // test("20s", " 20s");
        // test("third quarter", "0.750000000001");
        //
        // test("1.1.", "1.1");
        // test("Five hundred and 26 Thousand twenty-two dollars and 16 cents", "526022 US$ "
        // + "and 16 cents");
        //
        // test("two thirds million", "(2/3) * 1e6");
        // test("two thirds of a million", "0.666666666667 of 1000000");
        // test("twenty two thirds", "22/3");
        // test("a single unauthorized copy","1 unauthorized copy");
        // test("hundreds of thousands of dollars", "{{Range:100000:999999}}");
        //
        // test("fifteenth", "???");
        // test("fifteenths", "???");
        // test("sixth", "???");
    }
}
