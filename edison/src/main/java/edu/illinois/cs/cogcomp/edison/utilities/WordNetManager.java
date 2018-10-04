/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.utilities;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class WordNetManager {

    private final static POS[] poses = new POS[] {POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB};
    private static boolean loadAsResource = false;
    // Dictionary object
    private static Dictionary wordnet = null;
    private static WordNetManager INSTANCE;
    Logger log = LoggerFactory.getLogger(WordNetManager.class);

    // Initialize the database
    protected WordNetManager() throws JWNLException {
        if (wordnet == null) {

            synchronized (WordNetManager.class) {
                if (wordnet == null) {

                    JWNL.initialize();

                    // Create dictionary object
                    wordnet = Dictionary.getInstance();
                }
            }
        }
    }

    /**
     * Where should the WordNetManager load its configuration file from. If this function is given a
     * paramter <code>true</code>, then it looks for the file in the classpath. Otherwise, it looks
     * for it in the file system. The default option is to look for it in the classpath.
     */
    public synchronized static void loadConfigAsClasspathResource(boolean loadAsResource) {
        WordNetManager.loadAsResource = loadAsResource;
    }

    public static WordNetManager getInstance() throws FileNotFoundException, JWNLException {
        if (INSTANCE == null) {
            synchronized (WordNetManager.class) {
                if (INSTANCE == null)
                    INSTANCE = new WordNetManager();
            }
        }
        return INSTANCE;
    }

    public synchronized boolean existsEntry(String word, POS pos) throws JWNLException {
        return getIndexWord(pos, word) != null;
    }

    public synchronized Synset[] getAllSenses(String word, POS pos) throws JWNLException {
        IndexWord iw = getIndexWord(pos, word);
        if (iw == null)
            return new Synset[0];
        else
            return iw.getSenses();
    }

    public synchronized List<String> getGlosses(String word, boolean firstOnly)
            throws JWNLException {
        List<String> glosses = new ArrayList<>();

        for (POS pos : poses) {
            glosses.addAll(getGlosses(word, pos, firstOnly));
        }

        return glosses;
    }

    public synchronized List<String> getGlosses(String word, POS pos, boolean firstOnly)
            throws JWNLException {

        List<String> glosses = new ArrayList<>();
        IndexWord indexWord = getIndexWord(pos, word);
        if (indexWord == null)
            return glosses;

        for (Synset synset : indexWord.getSenses()) {

            glosses.add(synset.getGloss());
            if (firstOnly)
                break;
        }
        return glosses;
    }

    /**
     * Get the IndexWord object for a String and POS
     */
    public synchronized IndexWord getIndexWord(POS pos, String s) throws JWNLException {

        // This function tries the stemmed form of the lemma
        return wordnet.lookupIndexWord(pos, s);
    }

    public synchronized Synset getSynset(POS pos, long offset) throws JWNLException {
        return wordnet.getSynsetAt(pos, offset);
    }

    public synchronized String getLemma(String word, POS pos) throws JWNLException {
        IndexWord iw = getIndexWord(pos, word);
        if (iw == null)
            return word;
        else
            return iw.getLemma();
    }

    public synchronized Set<String> getLemmaAllPOS(String word) throws JWNLException {
        Set<String> s = new LinkedHashSet<>();

        for (POS pos : poses) {
            String lemma = getLemma(word, pos);
            s.add(lemma);
        }
        return s;
    }

    public synchronized List<String> getLexicographerFileNames(String word, boolean firstOnly)
            throws JWNLException {
        List<String> names = new ArrayList<>();

        for (POS pos : poses) {
            names.addAll(getLexicographerFileNames(word, pos, firstOnly));
        }

        return names;
    }

    public synchronized List<String> getLexicographerFileNames(String word, POS pos,
            boolean firstOnly) throws JWNLException {

        List<String> names = new ArrayList<>();
        IndexWord indexWord = getIndexWord(pos, word);
        if (indexWord == null)
            return names;

        for (Synset synset : indexWord.getSenses()) {

            names.add(synset.getLexFileName());
            if (firstOnly)
                break;
        }
        return names;
    }

    /**
     * List of all link types (eg. meronyms, etc) associated with the word
     */
    public synchronized Set<String> getPointers(String word, boolean firstOnly)
            throws JWNLException {
        Set<String> pointers = new LinkedHashSet<>();

        for (POS pos : poses) {
            pointers.addAll(getPointers(word, pos, firstOnly));
        }

        return pointers;
    }

    public synchronized Set<String> getPointers(String word, POS pos, boolean firstOnly)
            throws JWNLException {
        Set<String> pointers = new LinkedHashSet<>();

        IndexWord indexWord = getIndexWord(pos, word);
        if (indexWord == null)
            return pointers;

        for (Synset synset : indexWord.getSenses()) {

            for (Pointer p : synset.getPointers()) {
                pointers.add(p.getType().getLabel());
            }

            if (firstOnly)
                break;
        }
        return pointers;
    }

    public synchronized ArrayList<String> getMorphs(POS pos, String lexicalForm)
            throws JWNLException {
        HashSet<String> forms = new LinkedHashSet<>();
        List<?> baseForms =
                wordnet.getMorphologicalProcessor().lookupAllBaseForms(pos, lexicalForm);
        for (Object baseForm : baseForms) {
            forms.add(baseForm.toString());
        }
        return new ArrayList<>(forms);
    }

    public synchronized Set<String> getMorphsAllPOS(String word) throws JWNLException {

        Set<String> s = new LinkedHashSet<>();

        for (POS pos : poses) {
            ArrayList<String> morphs = getMorphs(pos, word);
            for (String m : morphs)
                s.add(m);
        }
        return s;
    }

    // Return array of POS objects for a given String
    public synchronized POS[] getPOS(String s) throws JWNLException {
        // Look up all IndexWords (an IndexWord can only be one POS)
        IndexWordSet set = wordnet.lookupAllIndexWords(s);
        // Turn it into an array of IndexWords
        IndexWord[] words = set.getIndexWordArray();
        // Make the array of POS
        POS[] pos = new POS[words.length];
        for (int i = 0; i < words.length; i++) {
            pos[i] = words[i].getPOS();
        }
        return pos;
    }

    public synchronized List<Synset> getRelated(String word, PointerType type, boolean firstOnly)
            throws JWNLException {

        List<Synset> output = new ArrayList<>();

        for (POS pos : poses) {
            output.addAll(getRelated(word, pos, type, firstOnly));
        }

        return output;
    }

    public synchronized List<Synset> getRelated(String word, POS pos, PointerType type,
            boolean firstOnly) throws JWNLException {

        List<Synset> output = new ArrayList<>();

        IndexWord indexWord = getIndexWord(pos, word);

        if (indexWord == null)
            return output;

        for (Synset synset : indexWord.getSenses()) {
            Pointer[] pointers = synset.getPointers(type);

            for (Pointer p : pointers) {
                output.add(p.getTargetSynset());
            }

            if (firstOnly)
                break;
        }
        return output;
    }

    public synchronized List<String> getRelatedWords(String word, PointerType type,
            boolean firstOnly) throws JWNLException {
        List<String> hyps = new ArrayList<>();

        for (POS pos : poses) {
            hyps.addAll(getRelatedWords(word, pos, type, firstOnly));
        }

        return hyps;
    }

    public synchronized List<String> getRelatedWords(String word, POS pos, PointerType type,
            boolean firstOnly) throws JWNLException {
        List<String> syns = new ArrayList<>();
        IndexWord indexWord = getIndexWord(pos, word);

        if (indexWord == null)
            return syns;

        for (Synset synset : indexWord.getSenses()) {

            Pointer[] pointers = synset.getPointers(type);

            for (Pointer p : pointers) {
                Synset target = p.getTargetSynset();

                for (Word w : target.getWords()) {
                    syns.add(w.getLemma());
                }

            }
            if (firstOnly)
                break;
        }
        return syns;
    }

    public synchronized List<String> getHyponyms(String word, boolean firstOnly)
            throws JWNLException {
        return getRelatedWords(word, PointerType.HYPONYM, firstOnly);
    }

    public synchronized List<String> getHyponyms(String word, POS pos, boolean firstOnly)
            throws JWNLException {
        return getRelatedWords(word, pos, PointerType.HYPONYM, firstOnly);
    }

    public synchronized List<String> getHypernyms(String word, boolean firstOnly)
            throws JWNLException {
        return getRelatedWords(word, PointerType.HYPERNYM, firstOnly);
    }

    public synchronized List<String> getHypernyms(String word, POS pos, boolean firstOnly)
            throws JWNLException {
        return getRelatedWords(word, pos, PointerType.HYPERNYM, firstOnly);
    }

    public synchronized List<String> getSynsets(String word, boolean firstOnly)
            throws JWNLException {
        List<String> syns = new ArrayList<>();

        for (POS pos : poses) {
            syns.addAll(getSynsets(word, pos, firstOnly));
        }
        return syns;
    }

    public synchronized List<String> getSynsets(String word, POS pos, boolean firstOnly)
            throws JWNLException {
        List<String> syns = new ArrayList<>();
        IndexWord indexWord = getIndexWord(pos, word);

        if (indexWord == null)
            return syns;

        for (Synset synset : indexWord.getSenses()) {

            syns.add(synset.getKey().toString());

            if (firstOnly)
                break;
        }
        return syns;
    }

    public synchronized List<String> getSynonyms(String word, boolean firstOnly)
            throws JWNLException {
        List<String> syns = new ArrayList<>();

        for (POS pos : poses) {
            syns.addAll(getSynonyms(word, pos, firstOnly));
        }

        return syns;
    }

    public synchronized List<String> getSynonyms(String word, POS pos, boolean firstOnly)
            throws JWNLException {
        List<String> syns = new ArrayList<>();
        IndexWord indexWord = getIndexWord(pos, word);

        if (indexWord == null)
            return syns;

        for (Synset synset : indexWord.getSenses()) {

            for (Word w : synset.getWords()) {
                syns.add(w.getLemma());
            }
            if (firstOnly)
                break;
        }
        return syns;
    }

    /**
     * List of all verb frames associated with the word
     */
    public synchronized Set<String> getVerbFrames(String word, boolean firstOnly)
            throws JWNLException {
        Set<String> verbFrames = new LinkedHashSet<>();

        IndexWord indexWord = getIndexWord(POS.VERB, word);
        if (indexWord == null)
            return verbFrames;

        for (Synset synset : indexWord.getSenses()) {

            verbFrames.addAll(Arrays.asList(synset.getVerbFrames()));

            if (firstOnly)
                break;
        }
        return verbFrames;
    }

}
