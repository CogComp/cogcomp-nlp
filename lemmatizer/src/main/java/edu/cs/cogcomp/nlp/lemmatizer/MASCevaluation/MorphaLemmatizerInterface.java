/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.nlp.lemmatizer.MASCevaluation;

import edu.cs.cogcomp.nlp.lemmatizer.MorphaStemmer;

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
