package edu.illinois.cs.cogcomp.nlp.lemmatizer.MASCevaluation;

import edu.illinois.cs.cogcomp.nlp.lemmatizer.MorphaStemmer;

public class MorphaLemmatizerInterface implements LemmatizerInterface {

    private MorphaStemmer morphaStemmer;

    @Override
    public void initLemmatizer() {
        morphaStemmer = new MorphaStemmer();
    }

    @Override
    public String getLemma(String word, String pos) {

        return morphaStemmer.stem(word);
    }

}
