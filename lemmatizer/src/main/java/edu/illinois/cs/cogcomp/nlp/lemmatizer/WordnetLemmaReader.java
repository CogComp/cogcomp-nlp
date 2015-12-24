package edu.illinois.cs.cogcomp.nlp.lemmatizer;

import edu.illinois.cs.cogcomp.core.io.IOUtils;

import java.io.IOException;
import java.net.URISyntaxException;
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
        try {
            if (IOUtils.lsResources(WordnetLemmaReader.class, wordnetPath).size() == 0) {
                System.err.println("Wordnet path does not point to a directory.");
                System.exit(-1);
            }
        } catch (URISyntaxException e) {
            System.err.println("Error while trying to access Wordnet.");
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("Error while trying to access Wordnet.");
            System.exit(-1);
        }

        nounLemmas = new HashMap<>();
        verbLemmas = new HashMap<>();
        adjectiveLemmas = new HashMap<>();
        adverbLemmas = new HashMap<>();

        // Read each file directly
        List<String> list = IllinoisLemmatizer.readFromClasspath(wordnetPath + "/noun.exc");
        for (String wordLemmas : list) {
            String[] wordLemma = wordLemmas.split("\\s+");
            nounLemmas.put(wordLemma[0], wordLemma[1]);
        }

        list = IllinoisLemmatizer.readFromClasspath(wordnetPath + "/verb.exc");
        for (String wordLemmas : list) {
            String[] wordLemma = wordLemmas.split("\\s+");
            verbLemmas.put(wordLemma[0], wordLemma[1]);
        }

        list = IllinoisLemmatizer.readFromClasspath(wordnetPath + "/adj.exc");
        for (String wordLemmas : list) {
            String[] wordLemma = wordLemmas.split("\\s+");
            adjectiveLemmas.put(wordLemma[0], wordLemma[1]);
        }

        list = IllinoisLemmatizer.readFromClasspath(wordnetPath + "/adv.exc");
        for (String wordLemmas : list) {
            String[] wordLemma = wordLemmas.split("\\s+");
            adverbLemmas.put(wordLemma[0], wordLemma[1]);
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
