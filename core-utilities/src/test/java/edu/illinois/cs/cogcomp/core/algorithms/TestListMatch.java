/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.algorithms;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestListMatch {

    @Test
    public void testRegexMatchesList() {
        String textString = "gcatcgcagagagtatacagtacg";
        List<String> pattern = Arrays.asList("g", "a", "g");


        List<String> textSplit = new LinkedList<>();

        for (int i = 0; i < textString.length(); ++i)
            textSplit.add(textString.substring(i, i + 1));


        ListMatch<String> matcher = new RegexBasedMatch<>(pattern);
        // ListMatch<String> matcher = new NaiveListMatcher<String>(pattern);

        for (int i : matcher.matches(textSplit)) {
            assertTrue(textString.substring(i).startsWith("gag"));
        }
    }

    @Test
    public void testRegexMatchesSeuss() {

        List<String> pattern = Arrays.asList("do", "not", "like");

        List<String> text =
                new ArrayList<>(Arrays.asList("I", "do", "not", "like", "them", "in", "a", "box",
                        ".", "I", "do", "not", "like", "them", "with", "a", "fox", ".", "I", "do",
                        "not", "like", "them", "in", "a", "house", ".", "I", "do", "not", "like",
                        "them", "with", "a", "mouse", ".", "I", "do", "not", "lik", "them", "here",
                        "or", "there", ".", "I", "do", "not", "like", "them", "anywhere", ".", "I",
                        "do", "not", "like", "green", "eggs", "and", "ham", ".", "I", "do", "not",
                        "like", "them,", "Sam-I-am", "."));

        ListMatch<String> matcher = new RegexBasedMatch<>(pattern);

        for (int i : matcher.matches(text)) {

            for (int j = 0; j < pattern.size(); j++) {
                assertEquals(pattern.get(j), text.get(i + j));
            }

        }
    }

    @Test
    public void testRegexMatchesSeussSpecial() {

        List<String> pattern = Arrays.asList("****", "*not", "li+e");

        List<String> text =
                new ArrayList<>(Arrays.asList("I", "****", "*not", "li+e", "them", "in", "a",
                        "box", ".", "I", "****", "*not", "li+e", "them", "with", "a", "fox", ".",
                        "I", "****", "*not", "li+e", "them", "in", "a", "house", ".", "I", "****",
                        "*not", "li+e", "them", "with", "a", "mouse", ".", "I", "****", "*not",
                        "lik", "them", "here", "or", "there", ".", "I", "****", "*not", "li+e",
                        "them", "anywhere", ".", "I", "****", "*not", "li+e", "green", "eggs",
                        "and", "ham", ".", "I", "****", "*not", "li+e", "them,", "Sam-I-am", "."));

        ListMatch<String> matcher = new RegexBasedMatch<>(pattern);

        for (int i : matcher.matches(text)) {

            for (int j = 0; j < pattern.size(); j++) {
                assertEquals(pattern.get(j), text.get(i + j));
            }

        }
    }

    @Test
    public void testBMatchesList() {
        String textString = "gcatcgcagagagtatacagtacg";
        List<String> pattern = Arrays.asList("g", "a", "g");

        List<String> text = Arrays.asList(textString.split("(?!^)"));

        ListMatch<String> matcher = new BoyerMooreHorspoolMatch<>(pattern);
        // ListMatch<String> matcher = new NaiveListMatcher<String>(pattern);

        for (int i : matcher.matches(text)) {
            assertTrue(textString.substring(i).startsWith("gag"));
        }
    }

    @Test
    public void testBMatchesSeuss() {

        List<String> pattern = Arrays.asList("do", "not", "like");

        List<String> text =
                new ArrayList<>(Arrays.asList("I", "do", "not", "like", "them", "in", "a", "box",
                        ".", "I", "do", "not", "like", "them", "with", "a", "fox", ".", "I", "do",
                        "not", "like", "them", "in", "a", "house", ".", "I", "do", "not", "like",
                        "them", "with", "a", "mouse", ".", "I", "do", "not", "lik", "them", "here",
                        "or", "there", ".", "I", "do", "not", "like", "them", "anywhere", ".", "I",
                        "do", "not", "like", "green", "eggs", "and", "ham", ".", "I", "do", "not",
                        "like", "them,", "Sam-I-am", "."));

        ListMatch<String> matcher = new BoyerMooreHorspoolMatch<>(pattern);

        for (int i : matcher.matches(text)) {

            for (int j = 0; j < pattern.size(); j++) {
                assertEquals(pattern.get(j), text.get(i + j));
            }

        }
    }

    @Test
    public void testBMatchesSeussSpecial() {

        List<String> pattern = Arrays.asList("****", "*not", "li+e");

        List<String> text =
                new ArrayList<>(Arrays.asList("I", "****", "*not", "li+e", "them", "in", "a",
                        "box", ".", "I", "****", "*not", "li+e", "them", "with", "a", "fox", ".",
                        "I", "****", "*not", "li+e", "them", "in", "a", "house", ".", "I", "****",
                        "*not", "li+e", "them", "with", "a", "mouse", ".", "I", "****", "*not",
                        "lik", "them", "here", "or", "there", ".", "I", "****", "*not", "li+e",
                        "them", "anywhere", ".", "I", "****", "*not", "li+e", "green", "eggs",
                        "and", "ham", ".", "I", "****", "*not", "li+e", "them,", "Sam-I-am", "."));

        ListMatch<String> matcher = new BoyerMooreHorspoolMatch<>(pattern);

        for (int i : matcher.matches(text)) {

            for (int j = 0; j < pattern.size(); j++) {
                assertEquals(pattern.get(j), text.get(i + j));
            }

        }
    }
}
