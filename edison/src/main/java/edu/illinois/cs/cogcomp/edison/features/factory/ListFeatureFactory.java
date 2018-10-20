/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.edison.features.ListFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordLists;

public abstract class ListFeatureFactory {
    public static final ListFeatureExtractor daysOfTheWeek = new ListFeatureExtractor("days",
            WordLists.DAYS_OF_WEEK, false);

    public static final ListFeatureExtractor months = new ListFeatureExtractor("mnths",
            WordLists.MONTHS, false);

    public static final ListFeatureExtractor possessivePronouns = new ListFeatureExtractor(
            "poss-pron", WordLists.POSSESSIVE_PRONOUNS, false);
}
