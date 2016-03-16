package edu.illinois.cs.cogcomp.nlp.lemmatizer;

import edu.illinois.cs.cogcomp.core.io.LineIO;

import java.io.IOException;
import java.util.*;

/**
 * A reader for the WordNet lemma files categorized by PoS.
 *
 * @author Christos Christodoulopoulos
 */
public class WordnetLemmaReader {

    /** Surface-form -> lemma maps to be accessed directly */
    private static Map<String, String> nounLemmas, verbLemmas, adjectiveLemmas, adverbLemmas;

    public WordnetLemmaReader(String wordnetPath) {
        nounLemmas = new HashMap<>();
        verbLemmas = new HashMap<>();
        adjectiveLemmas = new HashMap<>();
        adverbLemmas = new HashMap<>();

        // Read each file directly
        try {
            List<String> list = LineIO.readFromClasspath(wordnetPath + "/noun.exc");
            for (String wordLemmas : list) {
                String[] wordLemma = wordLemmas.split("\\s+");
                nounLemmas.put(wordLemma[0], wordLemma[1]);
            }

            list = LineIO.readFromClasspath(wordnetPath + "/verb.exc");
            for (String wordLemmas : list) {
                String[] wordLemma = wordLemmas.split("\\s+");
                verbLemmas.put(wordLemma[0], wordLemma[1]);
            }

            list = LineIO.readFromClasspath(wordnetPath + "/adj.exc");
            for (String wordLemmas : list) {
                String[] wordLemma = wordLemmas.split("\\s+");
                adjectiveLemmas.put(wordLemma[0], wordLemma[1]);
            }

            list = LineIO.readFromClasspath(wordnetPath + "/adv.exc");
            for (String wordLemmas : list) {
                String[] wordLemma = wordLemmas.split("\\s+");
                adverbLemmas.put(wordLemma[0], wordLemma[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLemma(String word, String pos) {
        if (pos.startsWith("N"))
            return nounLemmas.get(word);
        if (pos.startsWith("J"))
            return adjectiveLemmas.get(word);
        if (pos.startsWith("V"))
            return verbLemmas.get(word);
        if (pos.startsWith("R"))
            return adverbLemmas.get(word);

        return null;
    }

    public Set<String> getLemmaAllPOS(String word) {
        Set<String> lemmas = new HashSet<>();
        if (nounLemmas.containsKey(word))
            lemmas.add(nounLemmas.get(word));
        if (verbLemmas.containsKey(word))
            lemmas.add(verbLemmas.get(word));
        if (adjectiveLemmas.containsKey(word))
            lemmas.add(adjectiveLemmas.get(word));
        if (adverbLemmas.containsKey(word))
            lemmas.add(adverbLemmas.get(word));
        return lemmas;
    }
}
