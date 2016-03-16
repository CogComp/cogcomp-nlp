package edu.illinois.cs.cogcomp.core.algorithms;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TestListMatch extends TestCase {

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
