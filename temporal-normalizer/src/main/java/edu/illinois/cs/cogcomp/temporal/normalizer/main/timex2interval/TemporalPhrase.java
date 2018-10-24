/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

/**
 * Created by zhilifeng on 2/16/17.
 * This class contains a string that is the temporal phrase, and a tense
 * that could be present, past, or future
 */

public class TemporalPhrase {
    private String phrase;
    private String tense;

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public String getTense() {
        return tense;
    }

    public void setTense(String tense) {
        this.tense = tense;
    }

    public TemporalPhrase() {}

    public TemporalPhrase(String phrase) {
        this.phrase = phrase;
    }

    public TemporalPhrase(String phrase, String tense) {
        this.phrase = phrase;
        this.tense = tense;
    }
}
