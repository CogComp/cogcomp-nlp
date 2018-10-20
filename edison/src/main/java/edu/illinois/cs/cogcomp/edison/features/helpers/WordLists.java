/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import java.util.*;

/**
 * @author Vivek Srikumar
 */
public class WordLists {
    public static final Set<String> toBeVerbs = Collections.unmodifiableSet(new LinkedHashSet<>(
            Arrays.asList("am", "are", "be", "been", "being", "is", "was", "were", "'s", "'re",
                    "'m")));

    // These lists come from CYC data.
    public static final List<String> PREFIXES = Collections.unmodifiableList(Arrays.asList("poly",
            "ultra", "post", "multi", "pre", "fore", "ante", "pro", "meta", "out"));

    public static final List<String> DE_VERB_SUFFIXES = Collections.unmodifiableList(Arrays.asList(
            "ate", "en", "fy", "ise", "ize"));

    public static final List<String> DENOM_SUFFIXES = Collections.unmodifiableList(Arrays.asList(
            "ade", "age", "ana", "arian", "ary", "dom", "eer", "er", "ery", "ese", "ess", "ette",
            "ette", "ful", "hood", "ian", "ing", "ing", "ism", "ism", "ist", "ist", "ite", "itis",
            "let", "ling", "nik", "oid", "scape", "ship", "ster"));

    public static final List<String> DE_ADJ_SUFFIXES = Collections.unmodifiableList(Arrays.asList(
            "ability", "ancy", "hood", "ity", "ness", "ship"));

    // These lists were hand-made
    public static final Set<String> NUMBERS = Collections.unmodifiableSet(new LinkedHashSet<>(
            Arrays.asList("billion", "billions", "eight", "eighteen", "eight-teen", "eighty",
                    "eleven", "fifty", "five", "forteen", "four", "four-teen", "fourty", "hundred",
                    "hundreds", "million", "millions", "nine", "nineteen", "nine-teen", "ninety",
                    "one", "seven", "seventeen", "seven-teen", "seventy", "six", "sixteen",
                    "six-teen", "sixty", "ten", "tens", "thirteen", "thirty", "thousand",
                    "thousands", "three", "trillion", "trillions", "twelve", "twenty",
                    "twenty-eight", "twenty-five", "twenty-four", "twenty-nine", "twenty-one",
                    "twenty-seven", "twenty-six", "twenty-three", "twenty-two", "two", "zero")));

    public static final List<String> DAYS_OF_WEEK = Collections.unmodifiableList(Arrays.asList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));

    public static final Collection<String> MONTHS = Collections.unmodifiableList(Arrays.asList(
            "January", "February", "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December"));

    public static final List<String> POSSESSIVE_PRONOUNS = Collections.unmodifiableList(Arrays
            .asList("mine", "yours", "his", "hers", "its", "ours", "theirs"));

}
