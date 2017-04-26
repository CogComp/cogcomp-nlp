package edu.illinois.cs.cogcomp.temporal.normalizer.main.timex2interval;

/**
 * Created by zhilifeng on 2/16/17.
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
