/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.standardize;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Subhro Roy Separate bound from rest of phrase
 */



class Bounds {


    // *********************************************************************************************
    // BOUNDS
    // *********************************************************************************************
    public static Pattern eqPat, incrPat, decrPat, apPat, ltPat, ltNegatedPat, gtPat, gtNegatedPat,
            removeThePat;

    public static void initialize() {

        // ***************************************************************************************
        // BOUNDS
        // ***************************************************************************************
        String[] eqList =
                {
                        "exact(?:ly)?",
                        "equals?(?:ing)?(?:\\s*to)?",
                        "(?:or|and)\\s*(?:more|less|under|below|fewer|higher|older|up|over|above|"
                                + "younger|before|after|prior|under|beneath)", "as.*?as"};

        String[] incrList =
                {"(?:increased?|risen|grew|rose)(?:\\s*by)?", "gaine?d?", "added",
                        "(?:in\\s*)?additiona?l?", "(?:later|after)\\s*$"};

        String[] decrList =
                {"(?:decreased?|dropped|drop)(?:\\s*by)?", "loss", "(?:before)\\s*$",
                        "(?:fell|shr[ia]nk)\\sby"};

        String[] apList =
                {"(?:just\\s*)?about", "around", "approx(?:imate(?:ly)?d?)?\\.?", "rough(?:ly)?",
                        "estimated?", "some", "close(?:\\s*to|ly)?", "near(?:ly)?", "almost",
                        "or\\s*so", "semi", "like", "quasi(?:\\s*-\\s*)?"};

        String[] ltList =
                {
                        "(?:at\\s*)?(?:most|(?:the)?\\s*highest|(?:a\\s*)?maximum(?:\\s*of)?)",
                        "below",
                        "(?:or|and)?\\s*(?:less|fewer|lower)(?:\\s*than)?",
                        "fewer",
                        "before",
                        "prior(?:\\s*to)?",
                        "under",
                        "beneath",
                        "as\\s*(?:many|much|long|late|far|heavy|dense|high|big|huge|"
                                + "large|old)\\s*as", "(?:or\\s*)?younger(?:\\s*than)?", "up\\s*to"};

        String[] gtList =
                {"(?:at\\s*)?(?:least|(?:the)?\\s*(?:lowest|least)|(?:a\\s*)?minimum(?:\\s*of)?)",
                        "above", "over", "(?:or|and)?\\s*(?:greater|more|higher)\\s*(?:than)?",
                        "as\\s*(?:few|young|little|near|light|small|close|low|tiny)\\s*as",
                        "following", "upwards(?:\\s*of)?", "(?:a\\s*)?minimum(?:\\s*of)?", "after",
                        "following", "since"};

        String[] ltNegatedList = new String[ltList.length];
        for (int i = 0; i < ltNegatedList.length; i++) {
            ltNegatedList[i] = "(?:not?)\\s*" + ltList[i];
        }

        String[] gtNegatedList = new String[gtList.length];
        for (int i = 0; i < gtNegatedList.length; i++) {
            gtNegatedList[i] = "(?:not?)\\s*" + gtList[i];
        }

        eqPat = Pattern.compile(keyword_expr(eqList), Pattern.CASE_INSENSITIVE);
        apPat = Pattern.compile(keyword_expr(apList), Pattern.CASE_INSENSITIVE);
        ltPat = Pattern.compile(keyword_expr(ltList), Pattern.CASE_INSENSITIVE);
        ltNegatedPat = Pattern.compile(keyword_expr(ltNegatedList), Pattern.CASE_INSENSITIVE);
        gtPat = Pattern.compile(keyword_expr(gtList), Pattern.CASE_INSENSITIVE);
        gtNegatedPat = Pattern.compile(keyword_expr(gtNegatedList), Pattern.CASE_INSENSITIVE);
        incrPat = Pattern.compile(keyword_expr(incrList), Pattern.CASE_INSENSITIVE);
        decrPat = Pattern.compile(keyword_expr(decrList), Pattern.CASE_INSENSITIVE);
        removeThePat = Pattern.compile("\\W[tT][hH][eE]\\W", Pattern.CASE_INSENSITIVE);
    }

    // Take a phrase and separate the bound from the rest of the phrase
    // returns a tuple (bound, remainder)

    public static String[] extractBound(String phrase) {
        // Padding added to simplify patterns
        phrase = " " + phrase + " ";

        Matcher matcher = removeThePat.matcher(phrase);
        while (matcher.find()) {
            phrase = phrase.replace(matcher.group(), " ");
        }

        String removed = phrase;
        // Negations need to come first because there is dependency
        matcher = gtNegatedPat.matcher(removed);
        while (matcher.find()) {
            removed = removed.replace(matcher.group(), " ");
        }
        matcher = ltNegatedPat.matcher(removed);
        while (matcher.find()) {
            removed = removed.replace(matcher.group(), " ");
        }
        matcher = eqPat.matcher(removed);
        while (matcher.find()) {
            removed = removed.replace(matcher.group(), " ");
        }
        matcher = ltPat.matcher(removed);
        while (matcher.find()) {
            removed = removed.replace(matcher.group(), " ");
        }
        matcher = gtPat.matcher(removed);
        while (matcher.find()) {
            removed = removed.replace(matcher.group(), " ");
        }
        matcher = apPat.matcher(removed);
        while (matcher.find()) {
            removed = removed.replace(matcher.group(), " ");
        }

        // create string arrays for all matches
        // negations need to come first for dependency issues

        List<String> gt_negated_match = new ArrayList<String>();
        matcher = gtNegatedPat.matcher(phrase);
        while (matcher.find()) {
            gt_negated_match.add(matcher.group());
        }

        List<String> lt_negated_match = new ArrayList<String>();
        matcher = ltNegatedPat.matcher(phrase);
        while (matcher.find()) {
            lt_negated_match.add(matcher.group());
        }

        List<String> lt_match = new ArrayList<String>();
        matcher = ltPat.matcher(phrase);
        while (matcher.find()) {
            lt_match.add(matcher.group());
        }

        List<String> gt_match = new ArrayList<String>();
        matcher = gtPat.matcher(phrase);
        while (matcher.find()) {
            gt_match.add(matcher.group());
        }

        List<String> eq_match = new ArrayList<String>();
        matcher = eqPat.matcher(phrase);
        while (matcher.find()) {
            eq_match.add(matcher.group());
        }

        List<String> ap_match = new ArrayList<String>();
        matcher = apPat.matcher(phrase);
        while (matcher.find()) {
            ap_match.add(matcher.group());
        }

        List<String> incr_match = new ArrayList<String>();
        matcher = incrPat.matcher(phrase);
        while (matcher.find()) {
            incr_match.add(matcher.group());
        }

        List<String> decr_match = new ArrayList<String>();
        matcher = decrPat.matcher(phrase);
        while (matcher.find()) {
            decr_match.add(matcher.group());
        }

        // Apply negation handling logic
        if (gt_negated_match.size() > 0) {
            gt_match = gt_negated_match;
            eq_match = gt_negated_match;
            lt_match.clear();
        }

        if (lt_negated_match.size() > 0) {
            lt_match = lt_negated_match;
            eq_match = lt_negated_match;
            gt_match.clear();
        }

        String bound = "";
        if (lt_match.size() > 0)
            bound += "<";
        if (gt_match.size() > 0)
            bound += ">";
        if (eq_match.size() > 0)
            bound += "=";
        if (ap_match.size() > 0)
            bound += "~";
        if (incr_match.size() > 0)
            bound += "+";
        if (decr_match.size() > 0)
            bound += "-";
        if (bound.equals("")) {
            bound = "=";
        }

        matcher = incrPat.matcher(removed);
        while (matcher.find()) {
            removed = removed.replace(matcher.group(), " ");
        }

        matcher = decrPat.matcher(removed);
        while (matcher.find()) {
            removed = removed.replace(matcher.group(), " ");
        }

        String[] ans = new String[2];
        ans[0] = bound;
        ans[1] = removed;
        return ans;
    }


    public static int compare_patterns(String x, String y) {
        if (x.length() < y.length()) {
            return -1;
        } else {
            return 1;
        }
    }


    // Takes a list of keywords and generates a regular expression disjunction
    // such that any complete pattern in the disjunction is not a prefix of any another.

    public static String keyword_expr(String[] keywords) {
        String temp;
        for (int i = 0; i < keywords.length - 1; i++) {
            for (int j = 0; j < keywords.length - i - 1; j++) {
                if (compare_patterns(keywords[j], keywords[j + 1]) < 0) {
                    temp = keywords[j];
                    keywords[j] = keywords[j + 1];
                    keywords[j + 1] = temp;
                }
            }
        }
        String str = "(?:\\W|^)(?:" + keywords[0];
        for (int i = 1; i < keywords.length; i++) {
            str += "|" + keywords[i];
        }
        str += ")(?:\\W|$)";
        return str;
    }

    // Implementation of join method of python
    public static String join(String delimiter, Set<String> setstr) {
        if (setstr.size() == 0)
            return "";

        String[] set = new String[setstr.size()];
        int count = 0;
        for (String str : setstr) {
            set[count++] = str;
        }
        String result = set[0];
        for (int i = 1; i < count; i++) {
            result += delimiter + set[i];
        }
        return result;
    }
}
