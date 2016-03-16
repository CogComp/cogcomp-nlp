package edu.illinois.cs.cogcomp.nlp.lemmatizer.MASCevaluation;

public interface LemmatizerInterface {
    void initLemmatizer();

    String getLemma(String word, String pos);
}
