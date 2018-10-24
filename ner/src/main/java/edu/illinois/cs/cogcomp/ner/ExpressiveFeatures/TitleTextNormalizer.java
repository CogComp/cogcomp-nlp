/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ExpressiveFeatures;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.CharacteristicWords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;


/*
 * NOTE- IMPORTANT!!!! FOR THIS CLASS, WE HAVE TO RESPECT THE DOCUMENT BOUNDARIES. PLEASE DO NOT
 * READ ALL THE DOCUMENTS AS A SINGLE DATA COLLECTION!!!
 * 
 * This class will "fix" the all-cap sentneces in the titles. If the word is allCAP in a sentence,
 * I'll see if it appears as capitilized in non-sentence start position within the text. If it does,
 * I use that instance from within the document. Otherwise, I'll lowercase the fucker
 */
public class TitleTextNormalizer {
    public static final int WindowSize = 500;// the size of the window to be used for text
                                             // normalization. if we see a lowercased word in 500
                                             // word proximity from an all-caps title word, we
                                             // lowercase it.
    public static String pathToBrownClusterForWordFrequencies = null;
    public static HashMap<String, String> lowercasedToNormalizedTokensMap = null;

    public static void init() {
        InFile in = new InFile(pathToBrownClusterForWordFrequencies);
        String line = in.readLine();
        lowercasedToNormalizedTokensMap = new HashMap<>();
        HashMap<String, Integer> normalizedTokenCounts = new HashMap<>();
        while (line != null) {
            StringTokenizer st = new StringTokenizer(line);
            String path = st.nextToken();
            String word = st.nextToken();
            int occ = Integer.parseInt(st.nextToken());
            if (lowercasedToNormalizedTokensMap.containsKey(word.toLowerCase())) {
                String normalizedWord = lowercasedToNormalizedTokensMap.get(word.toLowerCase());
                int prevCount = normalizedTokenCounts.get(normalizedWord);
                if (prevCount < occ) {
                    lowercasedToNormalizedTokensMap.put(word.toLowerCase(), word);
                    normalizedTokenCounts.put(word, occ);
                }
            } else {
                lowercasedToNormalizedTokensMap.put(word.toLowerCase(), word);
                normalizedTokenCounts.put(word, occ);
            }
            line = in.readLine();
        }

    }

    public static void normalizeCase(Data data) {
        if (lowercasedToNormalizedTokensMap == null)
            init();
        // Below are the words that we'll want to normalize. We'll fill in the hashtable below with
        // the
        // words that appear in non-mixed case sentences. For CoNLL data, we basically fill the
        // hashmap
        // below with words from the titles
        HashMap<NEWord, Boolean> wordsToNormalize = new HashMap<>();
        HashMap<NEWord, Boolean> wordsInMixedCaseSentences = new HashMap<>();

        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
            for (LinkedVector sentence : sentences) {
                if (mixedCase(sentence)) {
                    // note that I exclude here the first word of a sentence on purpose!!!
                    for (int j = 1; j < sentence.size(); j++)
                        wordsInMixedCaseSentences.put((NEWord) sentence.get(j), true);
                } else {
                    // these words - in all caps or all-lowercase sentences are subject for
                    // normalization
                    for (int j = 0; j < sentence.size(); j++)
                        wordsToNormalize.put(((NEWord) sentence.get(j)), true);
                }
            }
        }

        for (NEWord w : wordsToNormalize.keySet()) {
            w.isCaseNormalized = true;
            if (w.form.equals("A")) {
                w.normalizedForm = "a";
                w.form = w.normalizedForm;
            } else {
                // the hashmap below remembers the words that appeared lowercased in the document
                HashMap<String, Boolean> lowecasedForms = new HashMap<>();
                // note that this MUST EXCLUDE the words that start a sentence!!!!
                // for each mixed-case string in mixed-case sentences, such as "McLaren"
                // we're keeping all the ways to write them out. E.g. McLaren MCLAREN etc.
                // Eventually, we'll normalize to the most common spelling in the document
                HashMap<String, CharacteristicWords> uppercasedFormsInMixedCaseNonSentenceStart =
                        new HashMap<>();
                getNeighborhoodWordStatistics(w, wordsInMixedCaseSentences,
                        uppercasedFormsInMixedCaseNonSentenceStart, lowecasedForms);
                // w.originalForm=w.form; // this can cauze all sorts of problems!!!
                String key = w.form.toLowerCase();
                if (w.normalizedMostLinkableExpression == null) {
                    if (lowecasedForms.containsKey(key)) {
                        w.normalizedForm = key;
                    } else {
                        if (uppercasedFormsInMixedCaseNonSentenceStart.containsKey(key))
                            w.normalizedForm =
                                    uppercasedFormsInMixedCaseNonSentenceStart.get(key).topWords
                                            .elementAt(0);
                        else {
                            if (lowercasedToNormalizedTokensMap.containsKey(w.form.toLowerCase()))
                                w.normalizedForm =
                                        lowercasedToNormalizedTokensMap.get(w.form.toLowerCase());
                            else
                                w.normalizedForm = w.form;// .toLowerCase();
                        }
                    }
                } else {
                    int start =
                            w.normalizedMostLinkableExpression.toLowerCase().indexOf(
                                    w.form.toLowerCase());
                    String normalizedForm =
                            w.normalizedMostLinkableExpression.substring(start,
                                    start + w.form.length());
                    if (Character.isLowerCase(normalizedForm.charAt(0))
                            && uppercasedFormsInMixedCaseNonSentenceStart
                                    .containsKey(normalizedForm.toLowerCase()))
                        w.normalizedForm =
                                uppercasedFormsInMixedCaseNonSentenceStart.get(normalizedForm
                                        .toLowerCase()).topWords.elementAt(0);
                    else
                        w.normalizedForm = normalizedForm;
                }
                if (w.previous == null && Character.isLowerCase(w.normalizedForm.charAt(0)))
                    w.normalizedForm =
                            Character.toUpperCase(w.normalizedForm.charAt(0))
                                    + w.normalizedForm.substring(1);
                w.form = w.normalizedForm;
            }
        }
    }

    /*
     * the first 2 parameters must be passed. the last 2 places is where I'm keeping the answers
     */
    public static void getNeighborhoodWordStatistics(NEWord word,
            HashMap<NEWord, Boolean> wordsInMixedCasedSentences,
            HashMap<String, CharacteristicWords> uppercasedFormsInMixedCaseNonSentenceStart,
            HashMap<String, Boolean> lowecasedForms) {
        NEWord temp = word.previousIgnoreSentenceBoundary;
        int count = 0;
        while (temp != null && count < WindowSize) {
            // we dont want to take into statistics words that begin sentences
            if (wordsInMixedCasedSentences.containsKey(temp) && temp.previous != null) {
                String w = temp.form;
                String key = w.toLowerCase();
                if (Character.isUpperCase(w.charAt(0))) {
                    CharacteristicWords topSpellings = new CharacteristicWords(5);
                    if (uppercasedFormsInMixedCaseNonSentenceStart.containsKey(key))
                        topSpellings = uppercasedFormsInMixedCaseNonSentenceStart.get(key);
                    topSpellings.addElement(w, 1);
                    uppercasedFormsInMixedCaseNonSentenceStart.put(key, topSpellings);
                }
                if (Character.isLowerCase(w.charAt(0)))
                    lowecasedForms.put(key, true);
            }
            count++;
            temp = temp.previousIgnoreSentenceBoundary;
        }
        temp = word.nextIgnoreSentenceBoundary;
        count = 0;
        while (temp != null && count < WindowSize) {
            // we dont want to take into statistics words that begin sentences
            if (wordsInMixedCasedSentences.containsKey(temp) && temp.previous != null) {
                String w = temp.form;
                String key = w.toLowerCase();
                if (Character.isUpperCase(w.charAt(0))) {
                    CharacteristicWords topSpellings = new CharacteristicWords(5);
                    if (uppercasedFormsInMixedCaseNonSentenceStart.containsKey(key))
                        topSpellings = uppercasedFormsInMixedCaseNonSentenceStart.get(key);
                    topSpellings.addElement(w, 1);
                    uppercasedFormsInMixedCaseNonSentenceStart.put(key, topSpellings);
                }
                if (Character.isLowerCase(w.charAt(0)))
                    lowecasedForms.put(key, true);
            }
            count++;
            temp = temp.nextIgnoreSentenceBoundary;
        }
    }

    public static boolean mixedCase(LinkedVector sentence) {
        if (lowercasedToNormalizedTokensMap == null)
            init();
        boolean hasLowecaseLetters = false;
        boolean hasUppercaseLetters = false;
        for (int i = 0; i < sentence.size(); i++) {
            String s = ((NEWord) sentence.get(i)).originalForm;
            for (int j = 0; j < s.length(); j++) {
                if (Character.isLowerCase(s.charAt(j)))
                    hasLowecaseLetters = true;
                if (Character.isUpperCase(s.charAt(j)))
                    hasUppercaseLetters = true;
            }
        }
        return hasLowecaseLetters && hasUppercaseLetters;
    }
}
