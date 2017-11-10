/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.edison.features.ListConstituentFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordLists;

public abstract class ListFeatureFactory {
    public static final ListConstituentFeatureExtractor daysOfTheWeek = new ListConstituentFeatureExtractor("days",
            WordLists.DAYS_OF_WEEK, false);

    public static final ListConstituentFeatureExtractor months = new ListConstituentFeatureExtractor("mnths",
            WordLists.MONTHS, false);

    public static final ListConstituentFeatureExtractor possessivePronouns = new ListConstituentFeatureExtractor(
            "poss-pron", WordLists.POSSESSIVE_PRONOUNS, false);
}
