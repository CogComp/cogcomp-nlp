/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

/**
 * Created by zhilifeng on 3/19/17.
 */
public class TimexNames {

    //attributes names:
    public static final String type = "type";
    public static final String value = "value";
    public static final String mod = "mod";
    public static final String quant = "quant";
    public static final String freq = "freq";

    //types:
    public static final String TIME = "TIME";
    public static final String DURATION = "DURATION";
    public static final String DATE = "DATE";
    public static final String SET = "SET";

    //mods:
    //points:
    public static final String START = "START";
    public static final String END = "END";
    public static final String MID = "MID";
    public static final String BEFORE = "BEFORE";
    public static final String AFTER = "AFTER";
    public static final String ON_OR_BEFORE = "ON_OR_BEFORE";
    public static final String ON_OR_AFTER = "ON_OR_AFTER";
    //durations:
    public static final String LESS_THAN = "LESS_THAN";
    public static final String MORE_THAN = "MORE_THAN";
    public static final String EQUAL_OR_LESS = "EQUAL_OR_LESS";
    public static final String EQUAL_OR_MORE = "EQUAL_OR_MORE";

    //other
    public static final String MORNING = "TMO";
    public static final String NOON = "T12:00";
    public static final String AFTERNOON = "TAF";
    public static final String EVENING = "TEV";
    public static final String NIGHT = "TNI";
    public static final String SPRING = "SP";
    public static final String SUMMER = "SU";
    public static final String FALL = "FA";
    public static final String WINTER = "WI";
    public static final String PRESENT_REF = "PRESENT_REF";
    public static final String PAST_REF = "PAST_REF";
    public static final String FUTURE_REF = "FUTURE_REF";



}
