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
