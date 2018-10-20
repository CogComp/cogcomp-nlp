/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor.WordNetFeatureClass;

import java.util.*;

import static edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor.WordNetFeatureClass.*;

class WordNetClasses {

    final static Map<String, WordNetFeatureClass> wnClasses = new HashMap<>();

    static {
        wnClasses.put("wn:exists-entry", existsEntry);
        wnClasses.put("wn:lemma", WordNetFeatureClass.lemma);

        wnClasses.put("wn:synonyms-first-sense", synonymsFirstSense);
        wnClasses.put("wn:synonyms-all", synonymsAllSenses);

        wnClasses.put("wn:synsets-first-sense", synsetsFirstSense);
        wnClasses.put("wn:synsets-all", synsetsAllSenses);

        wnClasses.put("wn:hypernyms-first-sense", hypernymsFirstSense);
        wnClasses.put("wn:hypernyms-all", hypernymsAllSenses);

        wnClasses.put("wn:part-holonyms-first-sense", partHolonymsFirstSense);
        wnClasses.put("wn:part-holonyms-all", partHolonymsAllSenses);

        wnClasses.put("wn:substance-holonyms-first-sense", substanceHolonymsFirstSense);
        wnClasses.put("wn:substance-holonyms-all", substanceHolonymsAllSenses);

        wnClasses.put("wn:member-holonyms-first-sense", memberHolonymsFirstSense);
        wnClasses.put("wn:member-holonyms-all", memberHolonymsAllSenses);

        wnClasses.put("wn:pointers-first-sense", pointersFirstSense);
        wnClasses.put("wn:pointers-all", pointersAllSenses);

        wnClasses.put("wn:verb-frames-first-sense", verbFramesFirstSense);
        wnClasses.put("wn:verb-frames-all", verbFramesAllSenses);

        wnClasses.put("wn:lexfiles-first-sense", lexicographerFileNamesFirstSense);
        wnClasses.put("wn:lexfiles-all", lexicographerFileNamesAllSenses);

        wnClasses
                .put("wn:hypernyms-lexfiles-first-sense", hypernymFirstSenseLexicographerFileNames);
        wnClasses.put("wn:hypernyms-lexfiles-all", hypernymAllSensesLexicographerFileNames);

        wnClasses.put("wn:part-holonyms-lexfiles-first-sense",
                partHolonymsFirstSenseLexicographerFileNames);
        wnClasses.put("wn:part-holonyms-lexfiles-all", partHolonymsAllSensesLexicographerFileNames);

        wnClasses.put("wn:substance-holonyms-lexfiles-first-sense",
                substanceHolonymsFirstSenseLexicographerFileNames);
        wnClasses.put("wn:substance-lexfiles-holonyms-all",
                substanceHolonymsAllSensesLexicographerFileNames);

        wnClasses.put("wn:member-holonyms-lexfiles-first-sense",
                memberHolonymsFirstSenseLexicographerFileNames);
        wnClasses.put("wn:member-lexfiles-holonyms-all",
                memberHolonymsAllSensesLexicographerFileNames);

        assert wnClasses.size() == WordNetFeatureClass.values().length;
    }

    static List<String> getKnownFeatureExtractors() {

        List<String> f = new ArrayList<>();

        for (String name : new TreeSet<>(wnClasses.keySet())) {
            f.add(name);
        }

        return f;
    }
}
